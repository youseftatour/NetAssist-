package com.yousef.netassist;

import java.time.LocalDateTime;

public record MonitorIncident(
        LocalDateTime timestamp,
        Type type,
        String message,
        long durationSeconds
) {

    public enum Type {
        OUTAGE,
        RECOVERY
    }
}
