package com.yousef.netassist;

import java.io.IOException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MonitoringManager
        implements AutoCloseable {

    public interface Listener {

        void onTargetsChanged();

        default void onPersistenceError(
                String message
        ) {
        }
    }

    private static final int MAX_INCIDENTS_PER_TARGET = 250;
    private static final int MAX_RECENT_RESULTS = 60;

    private final NetworkDiagnostics diagnostics;
    private final MonitoringTargetStore targetStore;
    private final MonitoringHistoryStore historyStore;
    private final NotificationService notificationService;

    private final Map<String, TargetRuntime> runtimes =
            new LinkedHashMap<>();

    private final List<Listener> listeners =
            new CopyOnWriteArrayList<>();

    private String persistenceWarning;

    public MonitoringManager(
            NetworkDiagnostics diagnostics
    ) {
        this(
                diagnostics,
                new MonitoringTargetStore(),
                new MonitoringHistoryStore(),
                new NotificationService()
        );
    }

    public MonitoringManager(
            NetworkDiagnostics diagnostics,
            MonitoringTargetStore targetStore,
            MonitoringHistoryStore historyStore,
            NotificationService notificationService
    ) {
        this.diagnostics =
                Objects.requireNonNull(
                        diagnostics,
                        "diagnostics"
                );

        this.targetStore =
                Objects.requireNonNull(
                        targetStore,
                        "targetStore"
                );

        this.historyStore =
                Objects.requireNonNull(
                        historyStore,
                        "historyStore"
                );

        this.notificationService =
                Objects.requireNonNull(
                        notificationService,
                        "notificationService"
                );

        loadSavedTargets();
    }

    private void loadSavedTargets() {
        Map<String, List<MonitorIncident>> savedIncidents =
                loadHistoricalIncidents();

        try {
            for (MonitoringTarget target
                    : targetStore.load()) {

                TargetRuntime runtime =
                        new TargetRuntime(
                                target
                        );

                List<MonitorIncident> targetIncidents =
                        savedIncidents.getOrDefault(
                                target.id(),
                                List.of()
                        );

                int start =
                        Math.max(
                                0,
                                targetIncidents.size()
                                        - MAX_INCIDENTS_PER_TARGET
                        );

                for (
                        int index = start;
                        index < targetIncidents.size();
                        index++
                ) {
                    runtime.incidents.addLast(
                            targetIncidents.get(
                                    index
                            )
                    );
                }

                runtimes.put(
                        target.id(),
                        runtime
                );
            }

        } catch (IOException exception) {
            persistenceWarning =
                    "Saved monitoring targets could not be loaded from "
                            + targetStore.getFile()
                            + ". "
                            + exception.getMessage();
        }
    }

    private Map<String, List<MonitorIncident>> loadHistoricalIncidents() {
        Map<String, List<MonitorIncident>> incidentsByTarget =
                new HashMap<>();

        try {
            for (HistoricalIncident historical
                    : historyStore.loadIncidents()) {

                incidentsByTarget
                        .computeIfAbsent(
                                historical.targetId(),
                                ignored ->
                                        new ArrayList<>()
                        )
                        .add(
                                new MonitorIncident(
                                        historical.timestamp(),
                                        historical.type(),
                                        historical.message(),
                                        historical.durationSeconds()
                                )
                        );
            }

        } catch (IOException exception) {
            persistenceWarning =
                    "Monitoring history could not be loaded from "
                            + historyStore.getIncidentFile()
                            + ". "
                            + exception.getMessage();
        }

        return incidentsByTarget;
    }

    public void addListener(
            Listener listener
    ) {
        listeners.add(
                Objects.requireNonNull(
                        listener,
                        "listener"
                )
        );

        if (persistenceWarning != null) {
            listener.onPersistenceError(
                    persistenceWarning
            );

            persistenceWarning = null;
        }
    }

    public void removeListener(
            Listener listener
    ) {
        listeners.remove(
                listener
        );
    }

    public synchronized List<MonitoringTargetSnapshot> snapshots() {
        List<MonitoringTargetSnapshot> snapshots =
                new ArrayList<>();

        for (TargetRuntime runtime
                : runtimes.values()) {

            snapshots.add(
                    runtime.snapshot()
            );
        }

        snapshots.sort(
                Comparator.comparing(
                        snapshot ->
                                snapshot
                                        .target()
                                        .name(),
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return snapshots;
    }

    public synchronized MonitoringTargetSnapshot snapshot(
            String targetId
    ) {
        TargetRuntime runtime =
                runtimes.get(
                        targetId
                );

        return runtime == null
                ? null
                : runtime.snapshot();
    }

    public synchronized void addTarget(
            MonitoringTarget target
    ) {
        Objects.requireNonNull(
                target,
                "target"
        );

        if (runtimes.containsKey(
                target.id()
        )) {
            throw new IllegalArgumentException(
                    "A target with this ID already exists."
            );
        }

        runtimes.put(
                target.id(),
                new TargetRuntime(
                        target
                )
        );

        persistTargets();
        fireChanged();
    }

    public synchronized void updateTarget(
            MonitoringTarget target
    ) {
        Objects.requireNonNull(
                target,
                "target"
        );

        TargetRuntime existing =
                runtimes.get(
                        target.id()
                );

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Monitoring target no longer exists."
            );
        }

        boolean restart =
                existing.isRunning();

        existing.stop();

        TargetRuntime replacement =
                new TargetRuntime(
                        target
                );

        /*
         * Preserve the in-memory incident view when editing a profile.
         */
        replacement.incidents.addAll(
                existing.incidents
        );

        runtimes.put(
                target.id(),
                replacement
        );

        persistTargets();

        if (restart) {
            replacement.start();
        }

        fireChanged();
    }

    public synchronized void removeTarget(
            String targetId
    ) {
        TargetRuntime runtime =
                runtimes.remove(
                        targetId
                );

        if (runtime == null) {
            return;
        }

        runtime.stop();

        persistTargets();
        fireChanged();
    }

    public synchronized void startTarget(
            String targetId
    ) {
        requireRuntime(
                targetId
        ).start();

        fireChanged();
    }

    public synchronized void stopTarget(
            String targetId
    ) {
        requireRuntime(
                targetId
        ).stop();

        fireChanged();
    }

    public synchronized void startAll() {
        for (TargetRuntime runtime
                : runtimes.values()) {

            runtime.start();
        }

        fireChanged();
    }

    public synchronized void stopAll() {
        for (TargetRuntime runtime
                : runtimes.values()) {

            runtime.stop();
        }

        fireChanged();
    }

    public synchronized int targetCount() {
        return runtimes.size();
    }

    public synchronized int runningCount() {
        int count = 0;

        for (TargetRuntime runtime
                : runtimes.values()) {

            if (runtime.isRunning()) {
                count++;
            }
        }

        return count;
    }

    public boolean notificationsAvailable() {
        return notificationService.isAvailable();
    }

    public boolean notificationsEnabled() {
        return notificationService.isEnabled();
    }

    public void setNotificationsEnabled(
            boolean enabled
    ) {
        notificationService.setEnabled(
                enabled
        );
    }

    private TargetRuntime requireRuntime(
            String targetId
    ) {
        TargetRuntime runtime =
                runtimes.get(
                        targetId
                );

        if (runtime == null) {
            throw new IllegalArgumentException(
                    "Monitoring target no longer exists."
            );
        }

        return runtime;
    }

    private void persistTargets() {
        List<MonitoringTarget> targets =
                new ArrayList<>();

        for (TargetRuntime runtime
                : runtimes.values()) {

            targets.add(
                    runtime.target
            );
        }

        try {
            targetStore.save(
                    targets
            );

        } catch (IOException exception) {
            firePersistenceError(
                    "Monitoring targets are active, but NetAssist could not save them to "
                            + targetStore.getFile()
                            + ". "
                            + exception.getMessage()
            );
        }
    }

    private void persistIncident(
            MonitoringTarget target,
            MonitorIncident incident
    ) {
        try {
            historyStore.appendIncident(
                    target,
                    incident
            );

        } catch (IOException exception) {
            firePersistenceError(
                    "NetAssist detected an incident but could not save it to "
                            + historyStore.getIncidentFile()
                            + ". "
                            + exception.getMessage()
            );
        }
    }

    private void persistSession(
            MonitoringTarget target,
            MonitoringStats stats
    ) {
        try {
            historyStore.appendSession(
                    target,
                    stats
            );

        } catch (IOException exception) {
            firePersistenceError(
                    "NetAssist could not save the completed monitoring session to "
                            + historyStore.getSessionFile()
                            + ". "
                            + exception.getMessage()
            );
        }
    }

    private void fireChanged() {
        for (Listener listener
                : listeners) {

            listener.onTargetsChanged();
        }
    }

    private void firePersistenceError(
            String message
    ) {
        for (Listener listener
                : listeners) {

            listener.onPersistenceError(
                    message
            );
        }
    }

    @Override
    public synchronized void close() {
        stopAll();

        notificationService.close();
    }

    private final class TargetRuntime {

        private final MonitoringTarget target;

        private MonitoringSession session;
        private MonitoringStats stats;
        private MonitorResult lastResult;

        private final Deque<MonitorIncident> incidents =
                new ArrayDeque<>();

        private final Deque<MonitorResult> recentResults =
                new ArrayDeque<>();

        private TargetRuntime(
                MonitoringTarget target
        ) {
            this.target = target;
        }

        private boolean isRunning() {
            return session != null
                    && session.isRunning();
        }

        private void start() {
            if (isRunning()) {
                return;
            }

            /*
             * Each run gets new performance statistics while the incident
             * timeline remains visible across runs.
             */
            stats = null;
            lastResult = null;
            recentResults.clear();

            MonitoringTarget current =
                    target;

            session =
                    new MonitoringSession(
                            diagnostics,
                            current.host(),
                            current.port(),
                            current.intervalSeconds(),
                            current.failureThreshold(),
                            current.recoveryThreshold(),
                            new MonitoringSession.Listener() {

                                @Override
                                public void onCheck(
                                        MonitorResult result,
                                        MonitoringStats newStats
                                ) {
                                    synchronized (
                                            MonitoringManager.this
                                    ) {
                                        lastResult = result;
                                        stats = newStats;

                                        recentResults.addLast(
                                                result
                                        );

                                        while (recentResults.size()
                                                > MAX_RECENT_RESULTS) {

                                            recentResults.removeFirst();
                                        }
                                    }

                                    fireChanged();
                                }

                                @Override
                                public void onIncident(
                                        MonitorIncident incident
                                ) {
                                    synchronized (
                                            MonitoringManager.this
                                    ) {
                                        incidents.addLast(
                                                incident
                                        );

                                        while (incidents.size()
                                                > MAX_INCIDENTS_PER_TARGET) {

                                            incidents.removeFirst();
                                        }
                                    }

                                    persistIncident(
                                            current,
                                            incident
                                    );

                                    notificationService.notifyIncident(
                                            current,
                                            incident
                                    );

                                    fireChanged();
                                }

                                @Override
                                public void onStopped(
                                        MonitoringStats finalStats
                                ) {
                                    synchronized (
                                            MonitoringManager.this
                                    ) {
                                        stats = finalStats;
                                    }

                                    persistSession(
                                            current,
                                            finalStats
                                    );

                                    fireChanged();
                                }
                            }
                    );

            session.start();
        }

        private void stop() {
            MonitoringSession currentSession =
                    session;

            if (currentSession != null) {
                currentSession.stop();
                session = null;
            }
        }

        private MonitoringTargetSnapshot snapshot() {
            return new MonitoringTargetSnapshot(
                    target,
                    isRunning(),
                    lastResult,
                    stats,
                    List.copyOf(
                            incidents
                    ),
                    List.copyOf(
                            recentResults
                    )
            );
        }
    }
}
