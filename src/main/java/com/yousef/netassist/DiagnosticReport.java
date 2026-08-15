package com.yousef.netassist;

public record DiagnosticReport(
        CheckResult dns,
        CheckResult reachability,
        TcpCheckResult tcp,
        String fullReport
) {
}
