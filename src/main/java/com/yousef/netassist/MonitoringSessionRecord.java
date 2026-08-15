package com.yousef.netassist;

import java.time.LocalDateTime;

public record MonitoringSessionRecord(
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String targetId,
        String targetName,
        String host,
        int port,
        String service,
        int totalChecks,
        int successfulChecks,
        int failedChecks,
        double uptimePercent,
        double averageLatencyMs,
        long minimumLatencyMs,
        long maximumLatencyMs
) {
}
