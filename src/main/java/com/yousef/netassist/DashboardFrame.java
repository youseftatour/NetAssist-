package com.yousef.netassist;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter; 
import javax.swing.JComboBox;
public final class DashboardFrame extends JFrame {

	private final JTextField hostField = new JTextField("example.com", 22);

	private final JComboBox<ServicePreset> serviceBox =
	        new JComboBox<>(ServicePreset.values());

	private final JTextField portField = new JTextField("443", 6);
    private final JButton runButton = new JButton("Run diagnostics");
    private final JButton commonPortsButton = new JButton("Test common ports");
    private final JButton clearButton = new JButton("Clear");
    private final JTextArea outputArea = new JTextArea();
    private final NetworkDiagnostics diagnostics = new NetworkDiagnostics();

    public DashboardFrame() {
        super("NetAssist - Network Troubleshooting Dashboard");
        configureWindow();
        createLayout();
        registerListeners();
        updatePortFromService();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 540);
        setLocationRelativeTo(null);
    }

    private void createLayout() {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        inputPanel.add(new JLabel("Host:"));
        inputPanel.add(hostField);

        inputPanel.add(new JLabel("Service:"));
        inputPanel.add(serviceBox);

        inputPanel.add(new JLabel("TCP port:"));
        inputPanel.add(portField);
        inputPanel.add(portField);
        inputPanel.add(runButton);
        inputPanel.add(commonPortsButton);
        inputPanel.add(clearButton);

        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        outputArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
    }

    private void registerListeners() {
        runButton.addActionListener(event -> runDiagnostics());
        commonPortsButton.addActionListener(event -> testCommonPorts());
        clearButton.addActionListener(event -> outputArea.setText(""));
        hostField.addActionListener(event -> runDiagnostics());
        portField.addActionListener(event -> runDiagnostics());
        serviceBox.addActionListener(
                event -> updatePortFromService()
        );
    }
    
    private void updatePortFromService() {

        ServicePreset selected =
                (ServicePreset) serviceBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        if (selected.isCustom()) {

            portField.setEditable(true);
            portField.requestFocusInWindow();

        } else {

            portField.setText(
                    String.valueOf(selected.getPort())
            );

            portField.setEditable(false);
        }
    }

    private void runDiagnostics() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDate = myDateObj.format(myFormatObj);

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

        runButton.setEnabled(false);
        outputArea.setText("Running diagnostics for " + host + "...\n");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                StringBuilder report = new StringBuilder();
                report.append(formattedDate + "\n\n");
                report.append("NETASSIST DIAGNOSTIC REPORT\n");
                report.append("Target: ").append(host).append(':').append(port).append("\n");
                report.append("Service: ")
                .append(getSelectedServiceName())
                .append("\n");
                report.append("==================================================\n\n");

                report.append("LOCAL NETWORK\n");
                report.append(diagnostics.getLocalNetworkInformation()).append("\n\n");

                List<CheckResult> results = List.of(
                        diagnostics.resolveHost(host),
                        diagnostics.checkReachability(host, 2_000),
                        diagnostics.testTcpPort(host, port, 2_000)
                );

                results.forEach(result -> report.append(result).append('\n'));
                report.append(buildSummary(results));
                return report.toString();
            }

            @Override
            protected void done() {
                try {
                    outputArea.setText(get());
                    outputArea.setCaretPosition(0);
                } catch (Exception exception) {
                    outputArea.setText("Unexpected error: " + exception.getMessage());
                } finally {
                    runButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }
    
    private void testCommonPorts() {

        String host = hostField.getText().trim();

        if (host.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a hostname or IP address."
            );

            return;
        }

        runButton.setEnabled(false);
        commonPortsButton.setEnabled(false);

        outputArea.setText(
                "Testing common services on "
                        + host
                        + "...\n"
        );

        SwingWorker<String, Void> worker =
                new SwingWorker<>() {

            @Override
            protected String doInBackground() {

                StringBuilder report =
                        new StringBuilder();

                report.append(
                        "COMMON SERVICE CHECK\n"
                );

                report.append("Target: ")
                        .append(host)
                        .append("\n");

                report.append(
                        "========================================\n\n"
                );

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

                    CheckResult result =
                            diagnostics.testTcpPort(
                                    host,
                                    preset.getPort(),
                                    1000
                            );

                    report.append(
                            String.format(
                                    "%-15s port %-5d -> %s%n",
                                    preset,
                                    preset.getPort(),
                                    result.successful()
                                            ? "OPEN"
                                            : "NO CONNECTION"
                            )
                    );
                }

                return report.toString();
            }

            @Override
            protected void done() {

                try {

                    outputArea.setText(get());
                    outputArea.setCaretPosition(0);

                } catch (Exception exception) {

                    outputArea.setText(
                            "Unexpected error: "
                                    + exception.getMessage()
                    );

                } finally {

                    runButton.setEnabled(true);
                    commonPortsButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }
    
    private String getSelectedServiceName() {

        ServicePreset selected =
                (ServicePreset) serviceBox.getSelectedItem();

        return selected == null
                ? "Unknown"
                : selected.toString();
    }
    private String buildSummary(List<CheckResult> results) {
        boolean dnsWorked = results.get(0).successful();
        boolean hostResponded = results.get(1).successful();
        boolean portWorked = results.get(2).successful();

        String conclusion;
        if (!dnsWorked) {
            conclusion = "The hostname could not be resolved. Check the name and DNS configuration.";
        } else if (portWorked) {
            conclusion = "DNS and the requested TCP service are reachable.";
        } else if (hostResponded) {
            conclusion = "The host responded, but the requested TCP service was unavailable.";
        } else {
            conclusion = "DNS worked, but no reachability or TCP response was received. "
                    + "A firewall may be blocking the checks.";
        }

        return "SUMMARY\n" + conclusion + "\n";
    }
    
    
}
