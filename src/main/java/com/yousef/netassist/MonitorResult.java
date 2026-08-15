package com.yousef.netassist;

import java.time.LocalDateTime;

public record MonitorResult(
        LocalDateTime timestamp,
        boolean online,
        TcpStatus status,
        long responseTimeMs,
        String details
) {
}
