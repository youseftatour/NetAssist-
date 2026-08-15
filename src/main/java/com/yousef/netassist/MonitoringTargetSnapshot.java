package com.yousef.netassist;

import java.util.List;

public record MonitoringTargetSnapshot(
        MonitoringTarget target,
        boolean running,
        MonitorResult lastResult,
        MonitoringStats stats,
        List<MonitorIncident> incidents,
        List<MonitorResult> recentResults
) {

    public MonitoringSession.State state() {
        if (stats == null) {
            return MonitoringSession.State.STOPPED;
        }

        return stats.state();
    }

    public int incidentCount() {
        return incidents.size();
    }

    public boolean activeIncident() {
        return state()
                == MonitoringSession.State.OFFLINE;
    }
}
