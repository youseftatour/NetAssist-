# NetAssist

NetAssist is a Java 17 desktop network troubleshooting and monitoring application built with Swing.

It combines one-off diagnostic tools with continuous multi-target service monitoring in a dark desktop dashboard.

## Features

### Quick Diagnostics

- DNS resolution
- Host reachability check
- TCP service connectivity
- Service presets
- Visual PASS / warning / failure cards
- Troubleshooting summary
- Detailed text report
- Copy report to clipboard
- Export report to TXT

### Multi-Target Monitoring

- Save multiple hosts/services
- Add, edit, and delete monitoring profiles
- Start/stop one target
- Start/stop all targets
- Independent background monitoring sessions
- Configurable check interval
- Configurable failure threshold
- Configurable recovery threshold
- Current status
- Live TCP latency
- Uptime percentage
- Total checks
- Average/minimum/maximum latency
- Live latency graph
- Active incident count
- Selected-target incident timeline

### Incident Detection

NetAssist does not mark a service down after one failed connection.

Example with failure threshold = 3:

1. FAIL
2. FAIL
3. FAIL
4. OUTAGE created

A recovery is also threshold based, reducing false-positive status changes.

### Persistent History

Monitoring profiles, outage/recovery incidents, and completed monitoring sessions survive application restarts.

The History tab provides:

- Incident history
- Monitoring-session history
- Search/filter
- Outage/recovery totals
- Uptime/session metrics
- CSV export

### Desktop Alerts

When the operating system supports `SystemTray`, NetAssist displays notifications for confirmed:

- Service outages
- Service recoveries

### Common Ports

Tests common TCP services such as:

- HTTP
- HTTPS
- DNS
- SSH
- Remote Desktop
- SQL Server
- MySQL

### Advanced Tools

- NSLookup
- Traceroute / Tracert

### Local Network

- Active local IPv4 interfaces
- Primary adapter/IP summary
- Full operating-system IP configuration output

### Desktop UI

- Dark Swing interface
- Borderless main window
- Custom vector minimize/maximize/restore/close controls
- True fullscreen
- `Esc` exits fullscreen
- Double-click header to toggle fullscreen
- Draggable window while windowed

## Requirements

- Java 17 or newer
- Windows is the primary target platform

The command runner also contains basic Unix fallbacks for traceroute and local IP information.

## Run in Eclipse

1. Import the project as an existing Maven project, or create/import it as a Java project.
2. Make sure the JRE/JDK is Java 17+.
3. Run:

`com.yousef.netassist.Main`

## Build on Windows without Maven

Open Command Prompt in the project directory and run:

```bat
build.bat
```

Then run:

```bat
run.bat
```

The runnable JAR is created at:

```text
build\NetAssist.jar
```

## Maven

```bash
mvn clean package
java -jar target/NetAssist.jar
```

## Runtime Data

NetAssist stores user-created monitoring data under:

```text
%USERPROFILE%\.netassist\
```

On Unix-like systems this corresponds to:

```text
~/.netassist/
```

Files include:

- `monitor-targets.properties`
- `monitor-incidents.tsv`
- `monitor-sessions.tsv`

No monitoring data is committed to the Git repository.

## Project Structure

```text
src/main/java/com/yousef/netassist/
├── Main.java
├── DashboardFrame.java
├── NetworkDiagnostics.java
├── WindowsDiagnostics.java
├── MonitoringDashboardPanel.java
├── MonitoringManager.java
├── MonitoringSession.java
├── MonitoringTarget.java
├── MonitoringTargetDialog.java
├── MonitoringTargetStore.java
├── MonitoringHistoryStore.java
├── HistoryPanel.java
├── NotificationService.java
├── ExportUtils.java
└── result/model records...
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the architecture overview.

## Responsible Use

Use network diagnostics and port checks only on systems and networks you own or are authorized to troubleshoot.
