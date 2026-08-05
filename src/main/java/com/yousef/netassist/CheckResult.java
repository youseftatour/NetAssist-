package com.yousef.netassist;

public record CheckResult(
        String checkName,
        boolean successful,
        String details,
        long durationMs
) {
    @Override
    public String toString() {
        String status = successful ? "PASS" : "FAIL";
        return "[%s] %s (%d ms)%n%s%n"
                .formatted(status, checkName, durationMs, details);
    }
}
