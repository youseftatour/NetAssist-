package com.yousef.netassist;

public record TcpCheckResult(
        TcpStatus status,
        String details,
        long durationMs
) {
    public boolean successful() {
        return status == TcpStatus.OPEN;
    }

    @Override
    public String toString() {
        String passFail = successful() ? "PASS" : "FAIL";
        return "[%s] TCP port check - %s (%d ms)%n%s%n"
                .formatted(passFail, status, durationMs, details);
    }
}
