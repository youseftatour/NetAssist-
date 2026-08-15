package com.yousef.netassist;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DashboardFrame extends JFrame {

    private final JTextField hostField = new JTextField("example.com", 28);

    private final JComboBox<ServicePreset> serviceBox =
            new JComboBox<>(ServicePreset.values());

    private final JTextField portField = new JTextField("443", 6);

    private final JButton runButton = new JButton("Run Diagnostics");
    private final JButton commonPortsButton = new JButton("Test Common Ports");
    private final JButton nslookupButton = new JButton("NSLookup");
    private final JButton tracertButton = new JButton("Traceroute");
    private final JButton ipconfigButton = new JButton("IP Configuration");

    private final JButton clearQuickButton = new JButton("Clear");
    private final JButton clearCommonButton = new JButton("Clear");
    private final JButton clearAdvancedButton = new JButton("Clear");
    private final JButton clearLocalButton = new JButton("Clear");

    private final JTextArea quickOutputArea = new JTextArea();
    private final JTextArea commonPortsOutputArea = new JTextArea();
    private final JTextArea advancedOutputArea = new JTextArea();
    private final JTextArea localOutputArea = new JTextArea();

    private final NetworkDiagnostics diagnostics = new NetworkDiagnostics();
    private final WindowsDiagnostics windowsDiagnostics = new WindowsDiagnostics();

    public DashboardFrame() {
        super("NetAssist - Network Troubleshooting Dashboard");

        configureWindow();
        configureTextAreas();
        createLayout();
        registerListeners();
        updatePortFromService();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
    }

    private void configureTextAreas() {
        configureTextArea(quickOutputArea);
        configureTextArea(commonPortsOutputArea);
        configureTextArea(advancedOutputArea);
        configureTextArea(localOutputArea);
    }

    private void configureTextArea(JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void createLayout() {
        setLayout(new BorderLayout());

        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        targetPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        targetPanel.add(new JLabel("Target:"));
        targetPanel.add(hostField);

        add(targetPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Quick Diagnostics", createQuickDiagnosticsPanel());
        tabs.addTab("Common Ports", createCommonPortsPanel());
        tabs.addTab("Advanced Tools", createAdvancedToolsPanel());
        tabs.addTab("Local Network", createLocalNetworkPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createQuickDiagnosticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        controls.add(new JLabel("Service:"));
        controls.add(serviceBox);
        controls.add(new JLabel("TCP Port:"));
        controls.add(portField);
        controls.add(runButton);
        controls.add(clearQuickButton);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(quickOutputArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCommonPortsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        controls.add(commonPortsButton);
        controls.add(clearCommonButton);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(commonPortsOutputArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAdvancedToolsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        controls.add(nslookupButton);
        controls.add(tracertButton);
        controls.add(clearAdvancedButton);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(advancedOutputArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLocalNetworkPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        controls.add(ipconfigButton);
        controls.add(clearLocalButton);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(localOutputArea), BorderLayout.CENTER);

        return panel;
    }

    private void registerListeners() {
        runButton.addActionListener(event -> runDiagnostics());
        commonPortsButton.addActionListener(event -> testCommonPorts());
        nslookupButton.addActionListener(event -> runNslookup());
        tracertButton.addActionListener(event -> runTraceroute());
        ipconfigButton.addActionListener(event -> runIpConfig());
        serviceBox.addActionListener(event -> updatePortFromService());
        hostField.addActionListener(event -> runDiagnostics());
        portField.addActionListener(event -> runDiagnostics());

        clearQuickButton.addActionListener(event -> quickOutputArea.setText(""));
        clearCommonButton.addActionListener(event -> commonPortsOutputArea.setText(""));
        clearAdvancedButton.addActionListener(event -> advancedOutputArea.setText(""));
        clearLocalButton.addActionListener(event -> localOutputArea.setText(""));
    }

    private void updatePortFromService() {
        ServicePreset selected = (ServicePreset) serviceBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        if (selected.isCustom()) {
            portField.setEditable(true);
            portField.requestFocusInWindow();
        } else {
            portField.setText(String.valueOf(selected.getPort()));
            portField.setEditable(false);
        }
    }

    private void runDiagnostics() {
        String host = hostField.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a hostname or IP address.");
            return;
        }

        final int port;

        try {
            port = Integer.parseInt(portField.getText().trim());

            if (port < 1 || port > 65_535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Enter a port between 1 and 65535.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDate = now.format(formatter);

        setDiagnosticButtonsEnabled(false);
        quickOutputArea.setText("Running diagnostics for " + host + "...\n");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                StringBuilder report = new StringBuilder();

                report.append(formattedDate).append("\n\n");
                report.append("NETASSIST DIAGNOSTIC REPORT\n");
                report.append("Target: ").append(host).append(':').append(port).append("\n");
                report.append("Service: ").append(getSelectedServiceName()).append("\n");
                report.append("==================================================\n\n");

                report.append("LOCAL NETWORK\n");
                report.append(diagnostics.getLocalNetworkInformation()).append("\n\n");

                CheckResult dnsResult = diagnostics.resolveHost(host);
                CheckResult reachabilityResult = diagnostics.checkReachability(host, 2_000);
                TcpCheckResult tcpResult = diagnostics.testTcpPort(host, port, 2_000);

                report.append(dnsResult).append('\n');
                report.append(reachabilityResult).append('\n');
                report.append(tcpResult).append('\n');
                report.append(buildSummary(dnsResult, reachabilityResult, tcpResult));

                return report.toString();
            }

            @Override
            protected void done() {
                try {
                    quickOutputArea.setText(get());
                    quickOutputArea.setCaretPosition(0);
                } catch (Exception exception) {
                    quickOutputArea.setText("Unexpected error: " + exception.getMessage());
                } finally {
                    setDiagnosticButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void testCommonPorts() {
        String host = hostField.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a hostname or IP address.");
            return;
        }

        setDiagnosticButtonsEnabled(false);
        commonPortsOutputArea.setText("Testing common services on " + host + "...\n");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                StringBuilder report = new StringBuilder();

                report.append("COMMON SERVICE CHECK\n");
                report.append("Target: ").append(host).append("\n");
                report.append("========================================\n\n");

                ServicePreset[] presets = {
                        ServicePreset.HTTP,
                        ServicePreset.HTTPS,
                        ServicePreset.DNS,
                        ServicePreset.SSH,
                        ServicePreset.REMOTE_DESKTOP,
                        ServicePreset.SQL_SERVER,
                        ServicePreset.MYSQL
                };

                for (ServicePreset preset : presets) {
                    TcpCheckResult result = diagnostics.testTcpPort(
                            host,
                            preset.getPort(),
                            1_000
                    );

                    report.append(String.format(
                            "%-15s port %-5d -> %-28s (%d ms)%n",
                            preset,
                            preset.getPort(),
                            result.status(),
                            result.durationMs()
                    ));
                }

                return report.toString();
            }

            @Override
            protected void done() {
                try {
                    commonPortsOutputArea.setText(get());
                    commonPortsOutputArea.setCaretPosition(0);
                } catch (Exception exception) {
                    commonPortsOutputArea.setText(
                            "Unexpected error: " + exception.getMessage()
                    );
                } finally {
                    setDiagnosticButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void runNslookup() {
        String host = hostField.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a hostname.");
            return;
        }

        setDiagnosticButtonsEnabled(false);
        advancedOutputArea.setText("Running NSLookup for " + host + "...\n");

        SwingWorker<CommandResult, Void> worker = new SwingWorker<>() {
            @Override
            protected CommandResult doInBackground() {
                return windowsDiagnostics.runNslookup(host);
            }

            @Override
            protected void done() {
                try {
                    CommandResult result = get();
                    advancedOutputArea.setText(formatCommandResult(result));
                    advancedOutputArea.setCaretPosition(0);
                } catch (Exception exception) {
                    advancedOutputArea.setText(
                            "Unexpected error: " + exception.getMessage()
                    );
                } finally {
                    setDiagnosticButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void runTraceroute() {
        String host = hostField.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a hostname or IP address.");
            return;
        }

        setDiagnosticButtonsEnabled(false);
        advancedOutputArea.setText("Tracing route to " + host + "...\n");

        SwingWorker<CommandResult, Void> worker = new SwingWorker<>() {
            @Override
            protected CommandResult doInBackground() {
                return windowsDiagnostics.runTraceroute(host);
            }

            @Override
            protected void done() {
                try {
                    CommandResult result = get();
                    advancedOutputArea.setText(formatCommandResult(result));
                    advancedOutputArea.setCaretPosition(0);
                } catch (Exception exception) {
                    advancedOutputArea.setText(
                            "Unexpected error: " + exception.getMessage()
                    );
                } finally {
                    setDiagnosticButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void runIpConfig() {
        setDiagnosticButtonsEnabled(false);
        localOutputArea.setText("Reading Windows network configuration...\n");

        SwingWorker<CommandResult, Void> worker = new SwingWorker<>() {
            @Override
            protected CommandResult doInBackground() {
                return windowsDiagnostics.runIpConfig();
            }

            @Override
            protected void done() {
                try {
                    CommandResult result = get();
                    localOutputArea.setText(formatCommandResult(result));
                    localOutputArea.setCaretPosition(0);
                } catch (Exception exception) {
                    localOutputArea.setText(
                            "Unexpected error: " + exception.getMessage()
                    );
                } finally {
                    setDiagnosticButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void setDiagnosticButtonsEnabled(boolean enabled) {
        runButton.setEnabled(enabled);
        commonPortsButton.setEnabled(enabled);
        nslookupButton.setEnabled(enabled);
        tracertButton.setEnabled(enabled);
        ipconfigButton.setEnabled(enabled);
    }

    private String formatCommandResult(CommandResult result) {
        String status = result.successful() ? "SUCCESS" : "FAILED";

        return result.commandName()
                + "\nStatus: " + status
                + "\nDuration: " + result.durationMs() + " ms"
                + "\n========================================\n\n"
                + result.output();
    }

    private String getSelectedServiceName() {
        ServicePreset selected = (ServicePreset) serviceBox.getSelectedItem();
        return selected == null ? "Unknown" : selected.toString();
    }

    private String buildSummary(
            CheckResult dnsResult,
            CheckResult reachabilityResult,
            TcpCheckResult tcpResult
    ) {
        String conclusion;

        if (!dnsResult.successful()) {
            conclusion = "DNS failed. Verify the hostname and your DNS configuration.";
        } else {
            conclusion = switch (tcpResult.status()) {
                case OPEN ->
                        "The requested TCP service is reachable.";

                case CONNECTION_REFUSED ->
                        "The host is reachable, but the target port refused the connection. "
                                + "The service may be stopped or not listening on that port.";

                case TIMEOUT ->
                        "The TCP connection timed out. A firewall may be filtering the port, "
                                + "or the target may not be responding.";

                case UNREACHABLE ->
                        "The target network or host could not be reached.";

                case DNS_FAILURE ->
                        "The hostname could not be resolved.";

                case INVALID_PORT ->
                        "The selected port number is invalid.";

                case ERROR ->
                        "The TCP test encountered an unexpected error.";
            };
        }

        return "SUMMARY\n" + conclusion + "\n";
    }
}
