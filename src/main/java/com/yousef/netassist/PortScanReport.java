package com.yousef.netassist;

import java.util.Map;

public record PortScanReport(
        Map<ServicePreset, TcpCheckResult> results,
        String fullReport,
        long durationMs
) {
}
