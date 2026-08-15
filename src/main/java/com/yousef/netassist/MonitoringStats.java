package com.yousef.netassist;

import java.time.LocalDateTime;

public record MonitoringStats(
        LocalDateTime startedAt,
        LocalDateTime lastCheckAt,
        int totalChecks,
        int successfulChecks,
        int failedChecks,
        double uptimePercent,
        long currentLatencyMs,
        double averageLatencyMs,
        long minimumLatencyMs,
        long maximumLatencyMs,
        MonitoringSession.State state,
        int consecutiveFailures,
        int consecutiveSuccesses
) {
}
