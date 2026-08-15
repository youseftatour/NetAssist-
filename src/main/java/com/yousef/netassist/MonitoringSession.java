package com.yousef.netassist;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MonitoringSession implements AutoCloseable {

    public enum State {
        STARTING,
        ONLINE,
        DEGRADED,
        OFFLINE,
        STOPPED
    }

    public interface Listener {

        void onCheck(
                MonitorResult result,
                MonitoringStats stats
        );

        void onIncident(
                MonitorIncident incident
        );

        void onStopped(
                MonitoringStats finalStats
        );
    }

    private final NetworkDiagnostics diagnostics;
    private final String host;
    private final int port;
    private final int intervalSeconds;
    private final int failureThreshold;
    private final int recoveryThreshold;
    private final int timeoutMs;
    private final Listener listener;

    private final AtomicBoolean running =
            new AtomicBoolean(false);

    private ScheduledExecutorService executor;

    private LocalDateTime startedAt;
    private LocalDateTime lastCheckAt;

    private int totalChecks;
    private int successfulChecks;
    private int failedChecks;

    private long successfulLatencyTotal;
    private long currentLatencyMs;
    private long minimumLatencyMs = Long.MAX_VALUE;
    private long maximumLatencyMs;

    private int consecutiveFailures;
    private int consecutiveSuccesses;

    private State state = State.STARTING;

    private LocalDateTime firstFailureAt;
    private LocalDateTime outageStartedAt;

    private volatile MonitoringStats lastStats;

    public MonitoringSession(
            NetworkDiagnostics diagnostics,
            String host,
            int port,
            int intervalSeconds,
            int failureThreshold,
            int recoveryThreshold,
            Listener listener
    ) {
        this.diagnostics =
                Objects.requireNonNull(
                        diagnostics,
                        "diagnostics"
                );

        this.host =
                Objects.requireNonNull(
                        host,
                        "host"
                ).trim();

        this.port = port;
        this.intervalSeconds =
                Math.max(
                        1,
                        intervalSeconds
                );

        this.failureThreshold =
                Math.max(
                        1,
                        failureThreshold
                );

        this.recoveryThreshold =
                Math.max(
                        1,
                        recoveryThreshold
                );

        /*
         * Keep individual checks from hanging for longer than
         * the configured monitoring interval.
         */
        this.timeoutMs =
                Math.max(
                        500,
                        Math.min(
                                5_000,
                                this.intervalSeconds * 1_000
                        )
                );

        this.listener =
                Objects.requireNonNull(
                        listener,
                        "listener"
                );

        validate();
    }

    private void validate() {
        if (host.isBlank()) {
            throw new IllegalArgumentException(
                    "Host cannot be empty."
            );
        }

        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException(
                    "Port must be between 1 and 65535."
            );
        }
    }

    public synchronized void start() {
        if (!running.compareAndSet(
                false,
                true
        )) {
            return;
        }

        startedAt =
                LocalDateTime.now();

        state =
                State.STARTING;

        lastStats =
                buildStats();

        ThreadFactory factory =
                runnable -> {
                    Thread thread =
                            new Thread(
                                    runnable,
                                    "NetAssist-Monitor"
                            );

                    /*
                     * A daemon worker cannot keep the application alive
                     * after the Swing window is closed.
                     */
                    thread.setDaemon(true);

                    return thread;
                };

        executor =
                Executors
                        .newSingleThreadScheduledExecutor(
                                factory
                        );

        /*
         * Fixed delay avoids overlapping checks when a slow network
         * operation takes longer than expected.
         */
        executor.scheduleWithFixedDelay(
                this::runCheckSafely,
                0,
                intervalSeconds,
                TimeUnit.SECONDS
        );
    }

    private void runCheckSafely() {
        if (!running.get()) {
            return;
        }

        try {
            runCheck();

        } catch (Exception exception) {
            /*
             * A monitoring worker should survive an unexpected check
             * failure instead of silently terminating its scheduler.
             */
            LocalDateTime now =
                    LocalDateTime.now();

            MonitorResult result =
                    new MonitorResult(
                            now,
                            false,
                            TcpStatus.ERROR,
                            0,
                            exception.getClass()
                                    .getSimpleName()
                                    + ": "
                                    + exception.getMessage()
                    );

            synchronized (this) {
                processResult(
                        result
                );
            }
        }
    }

    private void runCheck() {
        TcpCheckResult tcpResult =
                diagnostics.testTcpPort(
                        host,
                        port,
                        timeoutMs
                );

        LocalDateTime timestamp =
                LocalDateTime.now();

        MonitorResult result =
                new MonitorResult(
                        timestamp,
                        tcpResult.successful(),
                        tcpResult.status(),
                        tcpResult.durationMs(),
                        tcpResult.details()
                );

        synchronized (this) {
            processResult(
                    result
            );
        }
    }

    private void processResult(
            MonitorResult result
    ) {
        lastCheckAt =
                result.timestamp();

        totalChecks++;

        if (result.online()) {
            successfulChecks++;
            consecutiveSuccesses++;
            consecutiveFailures = 0;

            currentLatencyMs =
                    result.responseTimeMs();

            successfulLatencyTotal +=
                    result.responseTimeMs();

            minimumLatencyMs =
                    Math.min(
                            minimumLatencyMs,
                            result.responseTimeMs()
                    );

            maximumLatencyMs =
                    Math.max(
                            maximumLatencyMs,
                            result.responseTimeMs()
                    );

            firstFailureAt = null;

            handleSuccessfulCheck(
                    result
            );

        } else {
            failedChecks++;
            consecutiveFailures++;
            consecutiveSuccesses = 0;

            currentLatencyMs = 0;

            if (firstFailureAt == null) {
                firstFailureAt =
                        result.timestamp();
            }

            handleFailedCheck(
                    result
            );
        }

        MonitoringStats stats =
                buildStats();

        lastStats = stats;

        listener.onCheck(
                result,
                stats
        );
    }

    private void handleSuccessfulCheck(
            MonitorResult result
    ) {
        if (state == State.STARTING) {
            state =
                    State.ONLINE;

            return;
        }

        if (state == State.OFFLINE) {
            if (consecutiveSuccesses
                    >= recoveryThreshold) {

                state =
                        State.ONLINE;

                LocalDateTime recoveredAt =
                        result.timestamp();

                long downtimeSeconds =
                        outageStartedAt == null
                                ? 0
                                : Math.max(
                                        0,
                                        Duration.between(
                                                outageStartedAt,
                                                recoveredAt
                                        ).getSeconds()
                                );

                listener.onIncident(
                        new MonitorIncident(
                                recoveredAt,
                                MonitorIncident.Type.RECOVERY,
                                host
                                        + ":"
                                        + port
                                        + " recovered after "
                                        + downtimeSeconds
                                        + " seconds.",
                                downtimeSeconds
                        )
                );

                outageStartedAt = null;
            }

            return;
        }

        state =
                State.ONLINE;
    }

    private void handleFailedCheck(
            MonitorResult result
    ) {
        if (state == State.OFFLINE) {
            return;
        }

        if (consecutiveFailures
                >= failureThreshold) {

            state =
                    State.OFFLINE;

            outageStartedAt =
                    firstFailureAt != null
                            ? firstFailureAt
                            : result.timestamp();

            listener.onIncident(
                    new MonitorIncident(
                            outageStartedAt,
                            MonitorIncident.Type.OUTAGE,
                            host
                                    + ":"
                                    + port
                                    + " declared DOWN after "
                                    + consecutiveFailures
                                    + " consecutive failed checks. Last status: "
                                    + result.status(),
                            0
                    )
            );

        } else {
            state =
                    State.DEGRADED;
        }
    }

    private MonitoringStats buildStats() {
        double uptimePercent =
                totalChecks == 0
                        ? 0.0
                        : successfulChecks
                                * 100.0
                                / totalChecks;

        double averageLatencyMs =
                successfulChecks == 0
                        ? 0.0
                        : successfulLatencyTotal
                                * 1.0
                                / successfulChecks;

        long minLatency =
                successfulChecks == 0
                        ? 0
                        : minimumLatencyMs;

        return new MonitoringStats(
                startedAt,
                lastCheckAt,
                totalChecks,
                successfulChecks,
                failedChecks,
                uptimePercent,
                currentLatencyMs,
                averageLatencyMs,
                minLatency,
                maximumLatencyMs,
                state,
                consecutiveFailures,
                consecutiveSuccesses
        );
    }

    public synchronized boolean isRunning() {
        return running.get();
    }

    public synchronized MonitoringStats getLastStats() {
        return lastStats;
    }

    public synchronized void stop() {
        if (!running.compareAndSet(
                true,
                false
        )) {
            return;
        }

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        state =
                State.STOPPED;

        MonitoringStats finalStats =
                buildStats();

        lastStats =
                finalStats;

        listener.onStopped(
                finalStats
        );
    }

    @Override
    public void close() {
        stop();
    }
}
