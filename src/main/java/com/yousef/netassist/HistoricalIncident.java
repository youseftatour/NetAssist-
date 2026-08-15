package com.yousef.netassist;

import java.time.LocalDateTime;

public record HistoricalIncident(
        LocalDateTime timestamp,
        String targetId,
        String targetName,
        String host,
        int port,
        String service,
        MonitorIncident.Type type,
        String message,
        long durationSeconds
) {
}
