# Architecture

NetAssist is a single-process Java Swing desktop application. The user
interface runs on the Swing event-dispatch thread, while diagnostics and
monitoring work execute in background workers or scheduled executors.

## Main Components

| Area | Primary classes | Responsibility |
| --- | --- | --- |
| Application shell | `Main`, `DashboardFrame` | Starts Swing and assembles the dashboard tabs. |
| Diagnostics | `NetworkDiagnostics`, `WindowsDiagnostics` | Runs Java networking checks and operating-system commands. |
| Monitoring UI | `MonitoringDashboardPanel`, `MonitoringPanel`, `MonitoringTargetDialog` | Displays targets, sessions, charts, and target configuration. |
| Monitoring engine | `MonitoringManager`, `MonitoringSession` | Schedules checks and applies outage/recovery thresholds. |
| Persistence | `MonitoringTargetStore`, `MonitoringHistoryStore` | Stores profiles, incidents, and session history under the user's home directory. |
| History and export | `HistoryPanel`, `ExportUtils` | Filters historical records and exports TXT or CSV files. |
| Notifications | `NotificationService` | Publishes supported system-tray alerts. |

## Execution Flow

1. `Main` starts the Swing event-dispatch thread.
2. `DashboardFrame` creates the diagnostic, monitoring, and history views.
3. One-off diagnostics execute away from the UI thread and return immutable
   result records for display.
4. `MonitoringManager` owns active `MonitoringSession` instances.
5. Each session schedules TCP checks and updates statistics and incident state.
6. Persistence services write monitoring data to `~/.netassist/`.

## Build Boundary

Maven compiles `src/main/java` for Java 17 and packages
`com.yousef.netassist.Main` as the executable JAR entry point. NetAssist has no
external runtime libraries; all application code uses Java SE APIs.
