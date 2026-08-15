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
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxUI;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DashboardFrame extends JFrame {

    /*
     * ------------------------------
     * Color palette
     * ------------------------------
     */
    private static final Color APP_BACKGROUND = new Color(15, 23, 42);
    private static final Color HEADER_BACKGROUND = new Color(9, 15, 28);
    private static final Color SURFACE = new Color(24, 34, 53);
    private static final Color SURFACE_HOVER = new Color(32, 45, 68);
    private static final Color INPUT_BACKGROUND = new Color(17, 27, 45);
    private static final Color INPUT_DISABLED = new Color(30, 41, 59);
    private static final Color OUTPUT_BACKGROUND = new Color(9, 16, 30);

    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_HOVER = new Color(59, 130, 246);
    private static final Color ACCENT = new Color(20, 184, 166);
    private static final Color ACCENT_DARK = new Color(15, 118, 110);

    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);
    private static final Color SECONDARY_BUTTON = new Color(51, 65, 85);
    private static final Color SECONDARY_BUTTON_HOVER = new Color(71, 85, 105);

    /*
     * ------------------------------
     * Main controls
     * ------------------------------
     */
    private final JTextField hostField =
            new JTextField("example.com", 28);

    private final JComboBox<ServicePreset> serviceBox =
            new JComboBox<>(ServicePreset.values());

    private final JTextField portField =
            new JTextField("443", 6);

    private final JButton runButton =
            createPrimaryButton("Run Diagnostics");

    private final JButton commonPortsButton =
            createPrimaryButton("Test Common Ports");

    private final JButton nslookupButton =
            createPrimaryButton("NSLookup");

    private final JButton tracertButton =
            createPrimaryButton("Traceroute");

    private final JButton ipconfigButton =
            createPrimaryButton("IP Configuration");

    private final JButton clearQuickButton =
            createSecondaryButton("Clear");

    private final JButton clearCommonButton =
            createSecondaryButton("Clear");

    private final JButton clearAdvancedButton =
            createSecondaryButton("Clear");

    private final JButton clearLocalButton =
            createSecondaryButton("Clear");

    /*
     * ------------------------------
     * Output areas
     * ------------------------------
     */
    private final JTextArea quickOutputArea =
            new JTextArea();

    private final JTextArea commonPortsOutputArea =
            new JTextArea();

    private final JTextArea advancedOutputArea =
            new JTextArea();

    private final JTextArea localOutputArea =
            new JTextArea();

    private final JLabel statusLabel =
            new JLabel("Ready");

    /*
     * ------------------------------
     * Logic classes
     * ------------------------------
     */
    private final NetworkDiagnostics diagnostics =
            new NetworkDiagnostics();

    private final WindowsDiagnostics windowsDiagnostics =
            new WindowsDiagnostics();

    public DashboardFrame() {
        super("NetAssist - Network Troubleshooting Dashboard");

        configureWindow();
        configureInputs();
        configureTextAreas();
        createLayout();
        registerListeners();
        updatePortFromService();
    }

    /*
     * =========================================================
     * Window and component styling
     * =========================================================
     */

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        getContentPane().setBackground(APP_BACKGROUND);
    }

   private void configureInputs() {

    // -------------------------
    // Text fields
    // -------------------------

    styleTextField(hostField);
    styleTextField(portField);


    // -------------------------
    // Service ComboBox
    // -------------------------

    serviceBox.setFont(
            new Font(
                    "Segoe UI",
                    Font.PLAIN,
                    13
            )
    );

    serviceBox.setForeground(TEXT_PRIMARY);
    serviceBox.setBackground(INPUT_BACKGROUND);
    serviceBox.setFocusable(false);
    serviceBox.setOpaque(false);


    // -------------------------
    // Dropdown items
    // -------------------------

    serviceBox.setRenderer(
            new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                        JList<?> list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus
                ) {

                    JLabel label =
                            (JLabel) super.getListCellRendererComponent(
                                    list,
                                    value,
                                    index,
                                    isSelected,
                                    cellHasFocus
                            );

                    label.setFont(
                            new Font(
                                    "Segoe UI",
                                    Font.PLAIN,
                                    13
                            )
                    );

                    label.setOpaque(true);

                    /*
                     * index == -1 means this is the
                     * currently displayed value,
                     * not an item in the opened menu.
                     */
                    if (index == -1) {

                        label.setBackground(INPUT_BACKGROUND);
                        label.setForeground(TEXT_PRIMARY);

                    } else if (isSelected) {

                        label.setBackground(PRIMARY);
                        label.setForeground(Color.WHITE);

                    } else {

                        label.setBackground(INPUT_BACKGROUND);
                        label.setForeground(TEXT_PRIMARY);
                    }

                    label.setBorder(
                            BorderFactory.createEmptyBorder(
                                    7,
                                    10,
                                    7,
                                    10
                            )
                    );

                    return label;
                }
            }
    );


    // -------------------------
    // Custom ComboBox UI
    // -------------------------

    serviceBox.setUI(
            new BasicComboBoxUI() {

                /*
                 * THIS is the important part.
                 *
                 * Windows normally paints the
                 * selected-value area white.
                 *
                 * We override that painting.
                 */
                @Override
                public void paintCurrentValueBackground(
                        Graphics graphics,
                        Rectangle bounds,
                        boolean hasFocus
                ) {

                    Graphics2D g2 =
                            (Graphics2D) graphics.create();

                    g2.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON
                    );

                    g2.setColor(INPUT_BACKGROUND);

                    g2.fillRoundRect(
                            bounds.x,
                            bounds.y,
                            bounds.width,
                            bounds.height,
                            8,
                            8
                    );

                    g2.dispose();
                }


                /*
                 * Custom arrow button
                 */
                @Override
                protected JButton createArrowButton() {

                    JButton button =
                            new JButton("▼");

                    button.setFont(
                            new Font(
                                    "Segoe UI",
                                    Font.BOLD,
                                    9
                            )
                    );

                    button.setForeground(TEXT_SECONDARY);
                    button.setBackground(INPUT_BACKGROUND);

                    button.setFocusPainted(false);
                    button.setBorderPainted(false);
                    button.setContentAreaFilled(false);
                    button.setOpaque(false);

                    button.setCursor(
                            Cursor.getPredefinedCursor(
                                    Cursor.HAND_CURSOR
                            )
                    );

                    return button;
                }
            }
    );


    // -------------------------
    // ComboBox border
    // -------------------------

    serviceBox.setBorder(
            BorderFactory.createCompoundBorder(

                    BorderFactory.createLineBorder(
                            BORDER_COLOR,
                            1,
                            true
                    ),

                    BorderFactory.createEmptyBorder(
                            1,
                            1,
                            1,
                            1
                    )
            )
    );

    serviceBox.setBackground(INPUT_BACKGROUND);
    serviceBox.setForeground(TEXT_PRIMARY);
}

    private void styleTextField(JTextField field) {
        field.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBackground(INPUT_BACKGROUND);
        field.setSelectionColor(PRIMARY);
        field.setSelectedTextColor(Color.WHITE);

        Border lineBorder =
                BorderFactory.createLineBorder(
                        BORDER_COLOR,
                        1,
                        true
                );

        Border padding =
                BorderFactory.createEmptyBorder(
                        7,
                        10,
                        7,
                        10
                );

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        lineBorder,
                        padding
                )
        );
    }

    private void configureTextAreas() {
        configureTextArea(quickOutputArea);
        configureTextArea(commonPortsOutputArea);
        configureTextArea(advancedOutputArea);
        configureTextArea(localOutputArea);
    }

    private void configureTextArea(JTextArea textArea) {
        textArea.setEditable(false);

        textArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        13
                )
        );

        textArea.setBackground(OUTPUT_BACKGROUND);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setCaretColor(TEXT_PRIMARY);
        textArea.setSelectionColor(PRIMARY);
        textArea.setSelectedTextColor(Color.WHITE);

        textArea.setBorder(
                BorderFactory.createEmptyBorder(
                        16,
                        18,
                        16,
                        18
                )
        );
    }

    /*
     * =========================================================
     * Layout
     * =========================================================
     */

    private void createLayout() {
        setLayout(new BorderLayout());

        add(
                createHeaderPanel(),
                BorderLayout.NORTH
        );

        JPanel body =
                new JPanel(
                        new BorderLayout()
                );

        body.setBackground(APP_BACKGROUND);

        body.setBorder(
                BorderFactory.createEmptyBorder(
                        16,
                        18,
                        10,
                        18
                )
        );

        JTabbedPane tabs =
                createTabbedPane();

        tabs.addTab(
                "Quick Diagnostics",
                createQuickDiagnosticsPanel()
        );

        tabs.addTab(
                "Common Ports",
                createCommonPortsPanel()
        );

        tabs.addTab(
                "Advanced Tools",
                createAdvancedToolsPanel()
        );

        tabs.addTab(
                "Local Network",
                createLocalNetworkPanel()
        );

        body.add(
                tabs,
                BorderLayout.CENTER
        );

        add(
                body,
                BorderLayout.CENTER
        );

        add(
                createStatusBar(),
                BorderLayout.SOUTH
        );
    }

    private JPanel createHeaderPanel() {

    JPanel header =
            new JPanel(
                    new BorderLayout()
            );

    header.setBackground(
            HEADER_BACKGROUND
    );

    header.setBorder(
            BorderFactory.createEmptyBorder(
                    18,
                    24,
                    18,
                    24
            )
    );


    // --------------------------------
    // Left side
    // --------------------------------

    JPanel leftPanel =
            new JPanel();

    leftPanel.setOpaque(false);

    leftPanel.setLayout(
            new javax.swing.BoxLayout(
                    leftPanel,
                    javax.swing.BoxLayout.Y_AXIS
            )
    );


    // Title
    JLabel title =
            new JLabel("NetAssist");

    title.setFont(
            new Font(
                    "Segoe UI",
                    Font.BOLD,
                    24
            )
    );

    title.setForeground(
            TEXT_PRIMARY
    );

    title.setAlignmentX(
            LEFT_ALIGNMENT
    );


    // Subtitle
    JLabel subtitle =
            new JLabel(
                    "Network Troubleshooting Dashboard"
            );

    subtitle.setFont(
            new Font(
                    "Segoe UI",
                    Font.PLAIN,
                    12
            )
    );

    subtitle.setForeground(
            TEXT_SECONDARY
    );

    subtitle.setAlignmentX(
            LEFT_ALIGNMENT
    );


    // --------------------------------
    // Target field card
    // --------------------------------

    RoundedPanel targetCard =
            new RoundedPanel(
                    SURFACE,
                    14
            );

    targetCard.setLayout(
            new FlowLayout(
                    FlowLayout.LEFT,
                    10,
                    8
            )
    );

    targetCard.setBorder(
            BorderFactory.createEmptyBorder(
                    2,
                    6,
                    2,
                    6
            )
    );

    targetCard.setAlignmentX(
            LEFT_ALIGNMENT
    );


    JLabel targetLabel =
            createFieldLabel("Target");

    targetCard.add(
            targetLabel
    );

    targetCard.add(
            hostField
    );


    // --------------------------------
    // Assemble left side
    // --------------------------------

    leftPanel.add(title);

    leftPanel.add(
            javax.swing.Box.createVerticalStrut(3)
    );

    leftPanel.add(subtitle);

    leftPanel.add(
            javax.swing.Box.createVerticalStrut(14)
    );

    leftPanel.add(targetCard);


    header.add(
            leftPanel,
            BorderLayout.WEST
    );

    return header;
}

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabs =
                new JTabbedPane();

        tabs.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        tabs.setBackground(
                APP_BACKGROUND
        );

        tabs.setForeground(
                TEXT_PRIMARY
        );

        tabs.setFocusable(false);

        tabs.setUI(
                new ModernTabbedPaneUI()
        );

        return tabs;
    }

    private JPanel createQuickDiagnosticsPanel() {
        JPanel panel =
                createTabPanel();

        RoundedPanel controls =
                createControlCard();

        controls.add(
                createFieldLabel("Service")
        );

        controls.add(serviceBox);

        controls.add(
                createFieldLabel("TCP Port")
        );

        controls.add(portField);

        controls.add(runButton);
        controls.add(clearQuickButton);

        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                createOutputScrollPane(
                        quickOutputArea
                ),
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createCommonPortsPanel() {
        JPanel panel =
                createTabPanel();

        RoundedPanel controls =
                createControlCard();

        controls.add(
                commonPortsButton
        );

        controls.add(
                clearCommonButton
        );

        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                createOutputScrollPane(
                        commonPortsOutputArea
                ),
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createAdvancedToolsPanel() {
        JPanel panel =
                createTabPanel();

        RoundedPanel controls =
                createControlCard();

        controls.add(
                nslookupButton
        );

        controls.add(
                tracertButton
        );

        controls.add(
                clearAdvancedButton
        );

        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                createOutputScrollPane(
                        advancedOutputArea
                ),
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createLocalNetworkPanel() {
        JPanel panel =
                createTabPanel();

        RoundedPanel controls =
                createControlCard();

        controls.add(
                ipconfigButton
        );

        controls.add(
                clearLocalButton
        );

        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                createOutputScrollPane(
                        localOutputArea
                ),
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createTabPanel() {
        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                0,
                                12
                        )
                );

        panel.setBackground(
                APP_BACKGROUND
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        14,
                        2,
                        2,
                        2
                )
        );

        return panel;
    }

    private RoundedPanel createControlCard() {
        RoundedPanel card =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        card.setLayout(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        10
                )
        );

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        3,
                        6,
                        3,
                        6
                )
        );

        return card;
    }

    private JScrollPane createOutputScrollPane(
            JTextArea area
    ) {
        JScrollPane scrollPane =
                new JScrollPane(area);

        scrollPane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                0,
                                0,
                                0,
                                0
                        )
                )
        );

        scrollPane.getViewport()
                .setBackground(
                        OUTPUT_BACKGROUND
                );

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);

        return scrollPane;
    }

    private JPanel createStatusBar() {
        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBackground(
                HEADER_BACKGROUND
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                1,
                                0,
                                0,
                                0,
                                BORDER_COLOR
                        ),
                        BorderFactory.createEmptyBorder(
                                8,
                                22,
                                8,
                                22
                        )
                )
        );

        statusLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        statusLabel.setForeground(
                TEXT_SECONDARY
        );

        panel.add(
                statusLabel,
                BorderLayout.WEST
        );

        JLabel accentLabel =
                new JLabel(
                        "NETASSIST"
                );

        accentLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        accentLabel.setForeground(
                ACCENT
        );

        panel.add(
                accentLabel,
                BorderLayout.EAST
        );

        return panel;
    }

    private JLabel createFieldLabel(
            String text
    ) {
        JLabel label =
                new JLabel(text + ":");

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        label.setForeground(
                TEXT_SECONDARY
        );

        return label;
    }

    /*
     * =========================================================
     * Buttons
     * =========================================================
     */

    private static JButton createPrimaryButton(
            String text
    ) {
        return new RoundedButton(
                text,
                PRIMARY,
                PRIMARY_HOVER,
                Color.WHITE
        );
    }

    private static JButton createSecondaryButton(
            String text
    ) {
        return new RoundedButton(
                text,
                SECONDARY_BUTTON,
                SECONDARY_BUTTON_HOVER,
                TEXT_PRIMARY
        );
    }

    /*
     * =========================================================
     * Event listeners
     * =========================================================
     */

    private void registerListeners() {
        runButton.addActionListener(
                event -> runDiagnostics()
        );

        commonPortsButton.addActionListener(
                event -> testCommonPorts()
        );

        nslookupButton.addActionListener(
                event -> runNslookup()
        );

        tracertButton.addActionListener(
                event -> runTraceroute()
        );

        ipconfigButton.addActionListener(
                event -> runIpConfig()
        );

        serviceBox.addActionListener(
                event -> updatePortFromService()
        );

        hostField.addActionListener(
                event -> runDiagnostics()
        );

        portField.addActionListener(
                event -> runDiagnostics()
        );

        clearQuickButton.addActionListener(
                event -> quickOutputArea
                        .setText("")
        );

        clearCommonButton.addActionListener(
                event -> commonPortsOutputArea
                        .setText("")
        );

        clearAdvancedButton.addActionListener(
                event -> advancedOutputArea
                        .setText("")
        );

        clearLocalButton.addActionListener(
                event -> localOutputArea
                        .setText("")
        );
    }

    /*
     * =========================================================
     * Service / port handling
     * =========================================================
     */

    private void updatePortFromService() {
        ServicePreset selected =
                (ServicePreset)
                        serviceBox
                                .getSelectedItem();

        if (selected == null) {
            return;
        }

        if (selected.isCustom()) {
            portField.setEditable(true);
            portField.setBackground(
                    INPUT_BACKGROUND
            );

            portField.requestFocusInWindow();

        } else {
            portField.setText(
                    String.valueOf(
                            selected.getPort()
                    )
            );

            portField.setEditable(false);
            portField.setBackground(
                    INPUT_DISABLED
            );
        }
    }

    /*
     * =========================================================
     * Quick diagnostics
     * =========================================================
     */

    private void runDiagnostics() {
        String host =
                hostField
                        .getText()
                        .trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a hostname or IP address."
            );

            return;
        }

        final int port;

        try {
            port =
                    Integer.parseInt(
                            portField
                                    .getText()
                                    .trim()
                    );

            if (
                    port < 1
                    || port > 65_535
            ) {
                throw new NumberFormatException();
            }

        } catch (
                NumberFormatException exception
        ) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a port between 1 and 65535."
            );

            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy HH:mm:ss"
                );

        String formattedDate =
                now.format(formatter);

        setDiagnosticButtonsEnabled(false);

        statusLabel.setText(
                "Running diagnostics for "
                        + host
                        + "..."
        );

        quickOutputArea.setText(
                "Running diagnostics for "
                        + host
                        + "...\n"
        );

        SwingWorker<String, Void> worker =
                new SwingWorker<>() {

            @Override
            protected String doInBackground() {
                StringBuilder report =
                        new StringBuilder();

                report.append(formattedDate)
                        .append("\n\n");

                report.append(
                        "NETASSIST DIAGNOSTIC REPORT\n"
                );

                report.append("Target: ")
                        .append(host)
                        .append(':')
                        .append(port)
                        .append("\n");

                report.append("Service: ")
                        .append(
                                getSelectedServiceName()
                        )
                        .append("\n");

                report.append(
                        "==================================================\n\n"
                );

                report.append(
                        "LOCAL NETWORK\n"
                );

                report.append(
                        diagnostics
                                .getLocalNetworkInformation()
                ).append("\n\n");

                CheckResult dnsResult =
                        diagnostics
                                .resolveHost(host);

                CheckResult reachabilityResult =
                        diagnostics
                                .checkReachability(
                                        host,
                                        2_000
                                );

                TcpCheckResult tcpResult =
                        diagnostics
                                .testTcpPort(
                                        host,
                                        port,
                                        2_000
                                );

                report.append(dnsResult)
                        .append('\n');

                report.append(
                        reachabilityResult
                ).append('\n');

                report.append(tcpResult)
                        .append('\n');

                report.append(
                        buildSummary(
                                dnsResult,
                                reachabilityResult,
                                tcpResult
                        )
                );

                return report.toString();
            }

            @Override
            protected void done() {
                try {
                    quickOutputArea.setText(
                            get()
                    );

                    quickOutputArea
                            .setCaretPosition(0);

                    statusLabel.setText(
                            "Diagnostics complete"
                    );

                } catch (Exception exception) {
                    quickOutputArea.setText(
                            "Unexpected error: "
                                    + exception
                                            .getMessage()
                    );

                    statusLabel.setText(
                            "Diagnostics failed"
                    );

                } finally {
                    setDiagnosticButtonsEnabled(
                            true
                    );
                }
            }
        };

        worker.execute();
    }

    /*
     * =========================================================
     * Common ports
     * =========================================================
     */

    private void testCommonPorts() {
        String host =
                hostField
                        .getText()
                        .trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a hostname or IP address."
            );

            return;
        }

        setDiagnosticButtonsEnabled(false);

        statusLabel.setText(
                "Testing common ports on "
                        + host
                        + "..."
        );

        commonPortsOutputArea.setText(
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

                for (
                        ServicePreset preset
                        : presets
                ) {
                    TcpCheckResult result =
                            diagnostics
                                    .testTcpPort(
                                            host,
                                            preset
                                                    .getPort(),
                                            1_000
                                    );

                    report.append(
                            String.format(
                                    "%-15s port %-5d -> %-28s (%d ms)%n",
                                    preset,
                                    preset.getPort(),
                                    result.status(),
                                    result.durationMs()
                            )
                    );
                }

                return report.toString();
            }

            @Override
            protected void done() {
                try {
                    commonPortsOutputArea
                            .setText(get());

                    commonPortsOutputArea
                            .setCaretPosition(0);

                    statusLabel.setText(
                            "Common port test complete"
                    );

                } catch (Exception exception) {
                    commonPortsOutputArea
                            .setText(
                                    "Unexpected error: "
                                            + exception
                                                    .getMessage()
                            );

                    statusLabel.setText(
                            "Common port test failed"
                    );

                } finally {
                    setDiagnosticButtonsEnabled(
                            true
                    );
                }
            }
        };

        worker.execute();
    }

    /*
     * =========================================================
     * Advanced tools
     * =========================================================
     */

    private void runNslookup() {
        String host =
                hostField
                        .getText()
                        .trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a hostname."
            );

            return;
        }

        setDiagnosticButtonsEnabled(false);

        statusLabel.setText(
                "Running NSLookup for "
                        + host
                        + "..."
        );

        advancedOutputArea.setText(
                "Running NSLookup for "
                        + host
                        + "...\n"
        );

        SwingWorker<CommandResult, Void> worker =
                new SwingWorker<>() {

            @Override
            protected CommandResult doInBackground() {
                return windowsDiagnostics
                        .runNslookup(host);
            }

            @Override
            protected void done() {
                try {
                    CommandResult result =
                            get();

                    advancedOutputArea
                            .setText(
                                    formatCommandResult(
                                            result
                                    )
                            );

                    advancedOutputArea
                            .setCaretPosition(0);

                    statusLabel.setText(
                            "NSLookup complete"
                    );

                } catch (Exception exception) {
                    advancedOutputArea
                            .setText(
                                    "Unexpected error: "
                                            + exception
                                                    .getMessage()
                            );

                    statusLabel.setText(
                            "NSLookup failed"
                    );

                } finally {
                    setDiagnosticButtonsEnabled(
                            true
                    );
                }
            }
        };

        worker.execute();
    }

    private void runTraceroute() {
        String host =
                hostField
                        .getText()
                        .trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a hostname or IP address."
            );

            return;
        }

        setDiagnosticButtonsEnabled(false);

        statusLabel.setText(
                "Tracing route to "
                        + host
                        + "..."
        );

        advancedOutputArea.setText(
                "Tracing route to "
                        + host
                        + "...\n"
        );

        SwingWorker<CommandResult, Void> worker =
                new SwingWorker<>() {

            @Override
            protected CommandResult doInBackground() {
                return windowsDiagnostics
                        .runTraceroute(host);
            }

            @Override
            protected void done() {
                try {
                    CommandResult result =
                            get();

                    advancedOutputArea
                            .setText(
                                    formatCommandResult(
                                            result
                                    )
                            );

                    advancedOutputArea
                            .setCaretPosition(0);

                    statusLabel.setText(
                            "Traceroute complete"
                    );

                } catch (Exception exception) {
                    advancedOutputArea
                            .setText(
                                    "Unexpected error: "
                                            + exception
                                                    .getMessage()
                            );

                    statusLabel.setText(
                            "Traceroute failed"
                    );

                } finally {
                    setDiagnosticButtonsEnabled(
                            true
                    );
                }
            }
        };

        worker.execute();
    }

    /*
     * =========================================================
     * Local network
     * =========================================================
     */

    private void runIpConfig() {
        setDiagnosticButtonsEnabled(false);

        statusLabel.setText(
                "Reading local network configuration..."
        );

        localOutputArea.setText(
                "Reading Windows network configuration...\n"
        );

        SwingWorker<CommandResult, Void> worker =
                new SwingWorker<>() {

            @Override
            protected CommandResult doInBackground() {
                return windowsDiagnostics
                        .runIpConfig();
            }

            @Override
            protected void done() {
                try {
                    CommandResult result =
                            get();

                    localOutputArea
                            .setText(
                                    formatCommandResult(
                                            result
                                    )
                            );

                    localOutputArea
                            .setCaretPosition(0);

                    statusLabel.setText(
                            "Local network information loaded"
                    );

                } catch (Exception exception) {
                    localOutputArea
                            .setText(
                                    "Unexpected error: "
                                            + exception
                                                    .getMessage()
                            );

                    statusLabel.setText(
                            "IP configuration failed"
                    );

                } finally {
                    setDiagnosticButtonsEnabled(
                            true
                    );
                }
            }
        };

        worker.execute();
    }

    /*
     * =========================================================
     * Helpers
     * =========================================================
     */

    private void setDiagnosticButtonsEnabled(
            boolean enabled
    ) {
        runButton.setEnabled(enabled);
        commonPortsButton.setEnabled(enabled);
        nslookupButton.setEnabled(enabled);
        tracertButton.setEnabled(enabled);
        ipconfigButton.setEnabled(enabled);
    }

    private String formatCommandResult(
            CommandResult result
    ) {
        String status =
                result.successful()
                        ? "SUCCESS"
                        : "FAILED";

        return result.commandName()
                + "\nStatus: "
                + status
                + "\nDuration: "
                + result.durationMs()
                + " ms"
                + "\n========================================\n\n"
                + result.output();
    }

    private String getSelectedServiceName() {
        ServicePreset selected =
                (ServicePreset)
                        serviceBox
                                .getSelectedItem();

        return selected == null
                ? "Unknown"
                : selected.toString();
    }

    private String buildSummary(
            CheckResult dnsResult,
            CheckResult reachabilityResult,
            TcpCheckResult tcpResult
    ) {
        String conclusion;

        if (!dnsResult.successful()) {
            conclusion =
                    "DNS failed. Verify the hostname "
                            + "and your DNS configuration.";

        } else {
            conclusion =
                    switch (
                            tcpResult.status()
                    ) {
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

        return "SUMMARY\n"
                + conclusion
                + "\n";
    }

    /*
     * =========================================================
     * Custom UI components
     * =========================================================
     */

    private static final class RoundedPanel
            extends JPanel {

        private final Color backgroundColor;
        private final int arc;

        private RoundedPanel(
                Color backgroundColor,
                int arc
        ) {
            this.backgroundColor =
                    backgroundColor;

            this.arc = arc;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {
            Graphics2D g2 =
                    (Graphics2D)
                            graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    backgroundColor
            );

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    arc,
                    arc
            );

            g2.dispose();

            super.paintComponent(
                    graphics
            );
        }
    }

    private static final class RoundedButton
            extends JButton {

        private final Color normalColor;
        private final Color hoverColor;
        private boolean hovered;

        private RoundedButton(
                String text,
                Color normalColor,
                Color hoverColor,
                Color foregroundColor
        ) {
            super(text);

            this.normalColor =
                    normalColor;

            this.hoverColor =
                    hoverColor;

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            12
                    )
            );

            setForeground(
                    foregroundColor
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setMargin(
                    new Insets(
                            8,
                            14,
                            8,
                            14
                    )
            );

            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseEntered(
                                MouseEvent event
                        ) {
                            hovered = true;
                            repaint();
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent event
                        ) {
                            hovered = false;
                            repaint();
                        }
                    }
            );
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {
            Graphics2D g2 =
                    (Graphics2D)
                            graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color fillColor;

            if (!isEnabled()) {
                fillColor =
                        new Color(
                                71,
                                85,
                                105
                        );

            } else if (hovered) {
                fillColor =
                        hoverColor;

            } else {
                fillColor =
                        normalColor;
            }

            g2.setColor(fillColor);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    10,
                    10
            );

            if (isFocusOwner()) {
                g2.setColor(ACCENT);

                g2.setStroke(
                        new BasicStroke(1.5f)
                );

                g2.drawRoundRect(
                        1,
                        1,
                        getWidth() - 3,
                        getHeight() - 3,
                        10,
                        10
                );
            }

            g2.dispose();

            super.paintComponent(
                    graphics
            );
        }
    }

    private static final class ModernTabbedPaneUI
            extends BasicTabbedPaneUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();

            tabInsets =
                    new Insets(
                            10,
                            16,
                            10,
                            16
                    );

            selectedTabPadInsets =
                    new Insets(
                            0,
                            0,
                            0,
                            0
                    );

            contentBorderInsets =
                    new Insets(
                            8,
                            0,
                            0,
                            0
                    );
        }

        @Override
        protected void paintTabBackground(
                Graphics graphics,
                int tabPlacement,
                int tabIndex,
                int x,
                int y,
                int width,
                int height,
                boolean isSelected
        ) {
            Graphics2D g2 =
                    (Graphics2D)
                            graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    isSelected
                            ? PRIMARY
                            : SURFACE
            );

            g2.fillRoundRect(
                    x + 3,
                    y + 3,
                    width - 6,
                    height - 6,
                    12,
                    12
            );

            g2.dispose();
        }

        @Override
        protected void paintTabBorder(
                Graphics graphics,
                int tabPlacement,
                int tabIndex,
                int x,
                int y,
                int width,
                int height,
                boolean isSelected
        ) {
            // Intentionally empty for a flat, modern appearance.
        }

        @Override
        protected void paintFocusIndicator(
                Graphics graphics,
                int tabPlacement,
                java.awt.Rectangle[] rects,
                int tabIndex,
                java.awt.Rectangle iconRect,
                java.awt.Rectangle textRect,
                boolean isSelected
        ) {
            // Intentionally empty.
        }

        @Override
        protected void paintContentBorder(
                Graphics graphics,
                int tabPlacement,
                int selectedIndex
        ) {
            // Intentionally empty.
        }

        @Override
        protected void paintText(
                Graphics graphics,
                int tabPlacement,
                Font font,
                FontMetrics metrics,
                int tabIndex,
                String title,
                java.awt.Rectangle textRect,
                boolean isSelected
        ) {
            graphics.setFont(font);

            graphics.setColor(
                    isSelected
                            ? Color.WHITE
                            : TEXT_SECONDARY
            );

            graphics.drawString(
                    title,
                    textRect.x,
                    textRect.y
                            + metrics.getAscent()
            );
        }
    }
}
