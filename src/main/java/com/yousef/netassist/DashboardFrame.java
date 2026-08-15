package com.yousef.netassist;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

public final class DashboardFrame extends JFrame {

    /*
     * =========================================================
     * Color palette
     * =========================================================
     */
    private static final Color APP_BACKGROUND = new Color(15, 23, 42);
    private static final Color HEADER_BACKGROUND = new Color(9, 15, 28);
    private static final Color SURFACE = new Color(24, 34, 53);
    private static final Color INPUT_BACKGROUND = new Color(17, 27, 45);
    private static final Color INPUT_DISABLED = new Color(30, 41, 59);
    private static final Color OUTPUT_BACKGROUND = new Color(9, 16, 30);

    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_HOVER = new Color(59, 130, 246);

    private static final Color ACCENT = new Color(20, 184, 166);

    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color ERROR = new Color(239, 68, 68);

    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);

    private static final Color SECONDARY_BUTTON = new Color(51, 65, 85);
    private static final Color SECONDARY_BUTTON_HOVER = new Color(71, 85, 105);

    /*
     * =========================================================
     * Main controls
     * =========================================================
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
     * =========================================================
     * Quick diagnostic status cards
     * =========================================================
     */
    private final JLabel dnsStatusLabel =
            new JLabel("NOT TESTED");

    private final JLabel dnsDurationLabel =
            new JLabel("-- ms");

    private final JLabel dnsDetailsLabel =
            new JLabel("Waiting for a diagnostic run");

    private final JLabel reachabilityStatusLabel =
            new JLabel("NOT TESTED");

    private final JLabel reachabilityDurationLabel =
            new JLabel("-- ms");

    private final JLabel reachabilityDetailsLabel =
            new JLabel("Waiting for a diagnostic run");

    private final JLabel tcpStatusLabel =
            new JLabel("NOT TESTED");

    private final JLabel tcpDurationLabel =
            new JLabel("-- ms");

    private final JLabel tcpDetailsLabel =
            new JLabel("Waiting for a diagnostic run");

    private final JLabel summaryLabel =
            new JLabel(
                    "<html>Run diagnostics to generate a troubleshooting summary.</html>"
            );

    private final JLabel targetDetailsLabel =
            new JLabel(
                    "<html>Target details will appear here after a diagnostic run.</html>"
            );


    /*
     * =========================================================
     * Common Ports visual state
     * =========================================================
     */
    private static final ServicePreset[] COMMON_SERVICES = {
            ServicePreset.HTTP,
            ServicePreset.HTTPS,
            ServicePreset.DNS,
            ServicePreset.SSH,
            ServicePreset.REMOTE_DESKTOP,
            ServicePreset.SQL_SERVER,
            ServicePreset.MYSQL
    };

    private final Map<ServicePreset, JLabel> commonPortStatusLabels =
            new EnumMap<>(ServicePreset.class);

    private final Map<ServicePreset, JLabel> commonPortDurationLabels =
            new EnumMap<>(ServicePreset.class);

    private final JLabel commonOpenCountLabel =
            new JLabel("0");

    private final JLabel commonRefusedCountLabel =
            new JLabel("0");

    private final JLabel commonTimeoutCountLabel =
            new JLabel("0");

    private final JLabel commonScanDurationLabel =
            new JLabel("-- ms");

    /*
     * =========================================================
     * Advanced Tools visual state
     * =========================================================
     */
    private final JLabel advancedToolLabel =
            new JLabel("NONE");

    private final JLabel advancedStatusLabel =
            new JLabel("NOT RUN");

    private final JLabel advancedDurationLabel =
            new JLabel("-- ms");

    private final JLabel advancedTargetLabel =
            new JLabel("--");

    /*
     * =========================================================
     * Local Network visual state
     * =========================================================
     */
    private final JLabel localStatusLabel =
            new JLabel("NOT LOADED");

    private final JLabel localDurationLabel =
            new JLabel("-- ms");

    private final JLabel localIpv4Label =
            new JLabel("--");

    private final JLabel localAdapterLabel =
            new JLabel("--");

    private final JLabel localSnapshotLabel =
            new JLabel(
                    "<html>Run IP Configuration to load a local network snapshot.</html>"
            );

    /*
     * =========================================================
     * Output areas
     * =========================================================
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
     * =========================================================
     * Custom window controls / fullscreen state
     * =========================================================
     */
    private final GraphicsDevice screenDevice =
            GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();

    private boolean fullScreen = false;

    private Rectangle windowedBounds =
            new Rectangle(
                    100,
                    100,
                    1120,
                    760
            );

    private Point dragOffset;

    private final WindowControlButton minimizeButton =
            new WindowControlButton(
                    WindowControlType.MINIMIZE
            );

    private final WindowControlButton fullscreenButton =
            new WindowControlButton(
                    WindowControlType.MAXIMIZE
            );

    private final WindowControlButton closeButton =
            new WindowControlButton(
                    WindowControlType.CLOSE
            );

    /*
     * =========================================================
     * Logic classes
     * =========================================================
     */
    private final NetworkDiagnostics diagnostics =
            new NetworkDiagnostics();

    private final WindowsDiagnostics windowsDiagnostics =
            new WindowsDiagnostics();


    private final MonitoringDashboardPanel monitoringDashboardPanel =
            new MonitoringDashboardPanel(
                    diagnostics
            );

    public DashboardFrame() {
        super("NetAssist - Network Troubleshooting Dashboard");

        configureWindow();
        configureWindowControls();
        configureInputs();
        configureTextAreas();
        configureStatusLabels();
        createLayout();
        registerListeners();
        updatePortFromService();
        configureFullscreenShortcut();
    }

    /*
     * =========================================================
     * Window / base styling
     * =========================================================
     */

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
         * Remove the native Windows title bar.
         * This must happen before the frame becomes visible.
         */
        setUndecorated(true);

        setSize(1120, 760);

        setMinimumSize(
                new Dimension(
                        960,
                        650
                )
        );

        setLocationRelativeTo(null);

        getContentPane().setBackground(
                APP_BACKGROUND
        );
    }

    private void configureWindowControls() {
        minimizeButton.setToolTipText(
                "Minimize"
        );

        fullscreenButton.setToolTipText(
                "Enter Fullscreen"
        );

        closeButton.setToolTipText(
                "Close"
        );

        /*
         * Keep the controls compact like native window buttons.
         */
        minimizeButton.setPreferredSize(
                new Dimension(
                        42,
                        32
                )
        );

        fullscreenButton.setPreferredSize(
                new Dimension(
                        42,
                        32
                )
        );

        closeButton.setPreferredSize(
                new Dimension(
                        42,
                        32
                )
        );
    }


    private void configureInputs() {
        styleTextField(hostField);
        styleTextField(portField);

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

        serviceBox.setUI(
                new BasicComboBoxUI() {

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

    private void configureStatusLabels() {
        configureCardStatusLabel(dnsStatusLabel);
        configureCardStatusLabel(reachabilityStatusLabel);
        configureCardStatusLabel(tcpStatusLabel);

        configureCardDurationLabel(dnsDurationLabel);
        configureCardDurationLabel(reachabilityDurationLabel);
        configureCardDurationLabel(tcpDurationLabel);

        configureCardDetailsLabel(dnsDetailsLabel);
        configureCardDetailsLabel(reachabilityDetailsLabel);
        configureCardDetailsLabel(tcpDetailsLabel);

        summaryLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        summaryLabel.setForeground(TEXT_PRIMARY);
        summaryLabel.setVerticalAlignment(SwingConstants.TOP);

        targetDetailsLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        targetDetailsLabel.setForeground(TEXT_SECONDARY);
        targetDetailsLabel.setVerticalAlignment(SwingConstants.TOP);

        configureMetricValueLabel(commonOpenCountLabel);
        configureMetricValueLabel(commonRefusedCountLabel);
        configureMetricValueLabel(commonTimeoutCountLabel);
        configureMetricValueLabel(commonScanDurationLabel);

        configureMetricValueLabel(advancedToolLabel);
        configureMetricValueLabel(advancedStatusLabel);
        configureMetricValueLabel(advancedDurationLabel);
        configureMetricValueLabel(advancedTargetLabel);

        configureMetricValueLabel(localStatusLabel);
        configureMetricValueLabel(localDurationLabel);
        configureMetricValueLabel(localIpv4Label);
        configureMetricValueLabel(localAdapterLabel);

        localSnapshotLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        localSnapshotLabel.setForeground(TEXT_SECONDARY);
        localSnapshotLabel.setVerticalAlignment(SwingConstants.TOP);
    }

    private void configureCardStatusLabel(JLabel label) {
        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18
                )
        );

        label.setForeground(TEXT_SECONDARY);
    }

    private void configureCardDurationLabel(JLabel label) {
        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        label.setForeground(TEXT_SECONDARY);
    }

    private void configureCardDetailsLabel(JLabel label) {
        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        label.setForeground(TEXT_SECONDARY);
    }

    private void configureMetricValueLabel(
            JLabel label
    ) {
        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18
                )
        );

        label.setForeground(TEXT_PRIMARY);
    }


    /*
     * =========================================================
     * Main layout
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
                "Monitor",
                monitoringDashboardPanel
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
                        new BorderLayout(
                                18,
                                0
                        )
                );

        header.setBackground(HEADER_BACKGROUND);

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        14,
                        24,
                        14,
                        18
                )
        );

        JPanel leftPanel =
                new JPanel();

        leftPanel.setOpaque(false);

        leftPanel.setLayout(
                new BoxLayout(
                        leftPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title =
                new JLabel("NetAssist");

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

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

        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

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

        targetCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel targetLabel =
                createFieldLabel("Target");

        targetCard.add(targetLabel);
        targetCard.add(hostField);

        leftPanel.add(title);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(subtitle);
        leftPanel.add(Box.createVerticalStrut(14));
        leftPanel.add(targetCard);

        JPanel windowControls =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                6,
                                0
                        )
                );

        windowControls.setOpaque(false);

        windowControls.add(minimizeButton);
        windowControls.add(fullscreenButton);
        windowControls.add(closeButton);

        /*
         * Keep the controls pinned to the top-right of the header.
         */
        JPanel rightPanel =
                new JPanel(
                        new BorderLayout()
                );

        rightPanel.setOpaque(false);

        rightPanel.add(
                windowControls,
                BorderLayout.NORTH
        );

        header.add(
                leftPanel,
                BorderLayout.WEST
        );

        header.add(
                rightPanel,
                BorderLayout.EAST
        );

        configureWindowDragging(
                header
        );

        /*
         * Double-clicking the empty header area also toggles fullscreen.
         */
        header.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            MouseEvent event
                    ) {
                        if (event.getClickCount() == 2
                                && event.getButton()
                                == MouseEvent.BUTTON1) {
                            toggleFullscreen();
                        }
                    }
                }
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

        tabs.setBackground(APP_BACKGROUND);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFocusable(false);

        tabs.setUI(
                new ModernTabbedPaneUI()
        );

        return tabs;
    }

    /*
     * =========================================================
     * Quick diagnostics panel
     * =========================================================
     */

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

        JPanel content =
                new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel cardsPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                12,
                                0
                        )
                );

        cardsPanel.setOpaque(false);

        cardsPanel.add(
                createDiagnosticCard(
                        "DNS RESOLUTION",
                        dnsStatusLabel,
                        dnsDurationLabel,
                        dnsDetailsLabel
                )
        );

        cardsPanel.add(
                createDiagnosticCard(
                        "HOST REACHABILITY",
                        reachabilityStatusLabel,
                        reachabilityDurationLabel,
                        reachabilityDetailsLabel
                )
        );

        cardsPanel.add(
                createDiagnosticCard(
                        "TCP SERVICE",
                        tcpStatusLabel,
                        tcpDurationLabel,
                        tcpDetailsLabel
                )
        );

        cardsPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        145
                )
        );

        content.add(cardsPanel);
        content.add(Box.createVerticalStrut(12));

        JPanel analysisPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                12,
                                0
                        )
                );

        analysisPanel.setOpaque(false);

        analysisPanel.add(
                createInformationCard(
                        "Troubleshooting Summary",
                        summaryLabel
                )
        );

        analysisPanel.add(
                createInformationCard(
                        "Target Details",
                        targetDetailsLabel
                )
        );

        analysisPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        145
                )
        );

        content.add(analysisPanel);
        content.add(Box.createVerticalStrut(12));

        JPanel detailedHeader =
                new JPanel(
                        new BorderLayout()
                );

        detailedHeader.setOpaque(false);

        JLabel detailedTitle =
                new JLabel(
                        "Detailed Diagnostic Report"
                );

        detailedTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        detailedTitle.setForeground(TEXT_PRIMARY);

        detailedHeader.add(
                detailedTitle,
                BorderLayout.WEST
        );

        detailedHeader.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        28
                )
        );

        content.add(detailedHeader);
        content.add(Box.createVerticalStrut(7));

        JScrollPane reportScrollPane =
                createOutputScrollPane(
                        quickOutputArea
                );

        reportScrollPane.setAlignmentX(
                LEFT_ALIGNMENT
        );

        content.add(reportScrollPane);

        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                content,
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createDiagnosticCard(
            String title,
            JLabel statusLabel,
            JLabel durationLabel,
            JLabel detailsLabel
    ) {
        RoundedPanel card =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                14,
                                16,
                                14,
                                16
                        )
                )
        );

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        titleLabel.setForeground(TEXT_SECONDARY);

        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        durationLabel.setAlignmentX(LEFT_ALIGNMENT);
        detailsLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(durationLabel);
        card.add(Box.createVerticalStrut(7));
        card.add(detailsLabel);

        return card;
    }

    private JPanel createInformationCard(
            String title,
            JLabel contentLabel
    ) {
        RoundedPanel card =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        card.setLayout(
                new BorderLayout(
                        0,
                        8
                )
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                14,
                                16,
                                14,
                                16
                        )
                )
        );

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        titleLabel.setForeground(ACCENT);

        card.add(
                titleLabel,
                BorderLayout.NORTH
        );

        card.add(
                contentLabel,
                BorderLayout.CENTER
        );

        return card;
    }

    /*
     * =========================================================
     * Other tabs
     * =========================================================
     */

    private JPanel createCommonPortsPanel() {
        JPanel panel =
                createTabPanel();

        RoundedPanel controls =
                createControlCard();

        controls.add(commonPortsButton);
        controls.add(clearCommonButton);

        JPanel content =
                new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel metrics =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                12,
                                0
                        )
                );

        metrics.setOpaque(false);

        metrics.add(
                createMetricCard(
                        "OPEN",
                        commonOpenCountLabel,
                        "Services accepting TCP"
                )
        );

        metrics.add(
                createMetricCard(
                        "REFUSED",
                        commonRefusedCountLabel,
                        "Host actively rejected"
                )
        );

        metrics.add(
                createMetricCard(
                        "TIMED OUT",
                        commonTimeoutCountLabel,
                        "No response before timeout"
                )
        );

        metrics.add(
                createMetricCard(
                        "SCAN TIME",
                        commonScanDurationLabel,
                        "Total elapsed time"
                )
        );

        metrics.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        105
                )
        );

        content.add(metrics);
        content.add(Box.createVerticalStrut(12));

        RoundedPanel serviceMatrix =
                createCommonServiceMatrix();

        serviceMatrix.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        285
                )
        );

        content.add(serviceMatrix);
        content.add(Box.createVerticalStrut(12));

        content.add(
                createSectionTitle(
                        "Detailed Port Scan Report"
                )
        );

        content.add(Box.createVerticalStrut(7));

        JScrollPane reportScroll =
                createOutputScrollPane(
                        commonPortsOutputArea
                );

        reportScroll.setAlignmentX(
                LEFT_ALIGNMENT
        );

        content.add(reportScroll);

        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                content,
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createAdvancedToolsPanel() {
        JPanel panel =
                createTabPanel();

        RoundedPanel controls =
                createControlCard();

        controls.add(clearAdvancedButton);

        JPanel content =
                new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel tools =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                12,
                                0
                        )
                );

        tools.setOpaque(false);

        tools.add(
                createToolActionCard(
                        "NSLookup",
                        "Query DNS and inspect how the target hostname resolves.",
                        nslookupButton
                )
        );

        tools.add(
                createToolActionCard(
                        "Traceroute",
                        "Trace the network path to the target and inspect each hop.",
                        tracertButton
                )
        );

        tools.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        125
                )
        );

        content.add(tools);
        content.add(Box.createVerticalStrut(12));

        JPanel metrics =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                12,
                                0
                        )
                );

        metrics.setOpaque(false);

        metrics.add(
                createMetricCard(
                        "LAST TOOL",
                        advancedToolLabel,
                        "Most recently executed"
                )
        );

        metrics.add(
                createMetricCard(
                        "STATUS",
                        advancedStatusLabel,
                        "Command result"
                )
        );

        metrics.add(
                createMetricCard(
                        "DURATION",
                        advancedDurationLabel,
                        "Execution time"
                )
        );

        metrics.add(
                createMetricCard(
                        "TARGET",
                        advancedTargetLabel,
                        "Current remote target"
                )
        );

        metrics.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        105
                )
        );

        content.add(metrics);
        content.add(Box.createVerticalStrut(12));

        content.add(
                createSectionTitle(
                        "Command Output"
                )
        );

        content.add(Box.createVerticalStrut(7));

        JScrollPane outputScroll =
                createOutputScrollPane(
                        advancedOutputArea
                );

        outputScroll.setAlignmentX(
                LEFT_ALIGNMENT
        );

        content.add(outputScroll);

        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                content,
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createLocalNetworkPanel() {
        JPanel panel =
                createTabPanel();

        RoundedPanel controls =
                createControlCard();

        controls.add(ipconfigButton);
        controls.add(clearLocalButton);

        JPanel content =
                new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel metrics =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                12,
                                0
                        )
                );

        metrics.setOpaque(false);

        metrics.add(
                createMetricCard(
                        "STATUS",
                        localStatusLabel,
                        "IP configuration command"
                )
        );

        metrics.add(
                createMetricCard(
                        "DURATION",
                        localDurationLabel,
                        "Collection time"
                )
        );

        metrics.add(
                createMetricCard(
                        "LOCAL IPv4",
                        localIpv4Label,
                        "Primary active address"
                )
        );

        metrics.add(
                createMetricCard(
                        "ADAPTER",
                        localAdapterLabel,
                        "Primary active interface"
                )
        );

        metrics.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        105
                )
        );

        content.add(metrics);
        content.add(Box.createVerticalStrut(12));

        RoundedPanel snapshotCard =
                (RoundedPanel) createInformationCard(
                        "Local Network Snapshot",
                        localSnapshotLabel
                );

        snapshotCard.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        120
                )
        );

        content.add(snapshotCard);
        content.add(Box.createVerticalStrut(12));

        content.add(
                createSectionTitle(
                        "Full IP Configuration"
                )
        );

        content.add(Box.createVerticalStrut(7));

        JScrollPane outputScroll =
                createOutputScrollPane(
                        localOutputArea
                );

        outputScroll.setAlignmentX(
                LEFT_ALIGNMENT
        );

        content.add(outputScroll);

        panel.add(
                controls,
                BorderLayout.NORTH
        );

        panel.add(
                content,
                BorderLayout.CENTER
        );

        return panel;
    }

    private RoundedPanel createMetricCard(
            String title,
            JLabel valueLabel,
            String subtitle
    ) {
        RoundedPanel card =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                13,
                                15,
                                13,
                                15
                        )
                )
        );

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel subtitleLabel =
                new JLabel(subtitle);

        subtitleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        subtitleLabel.setForeground(TEXT_SECONDARY);

        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(7));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);

        return card;
    }

    private RoundedPanel createToolActionCard(
            String title,
            String description,
            JButton actionButton
    ) {
        RoundedPanel card =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        card.setLayout(
                new BorderLayout(
                        18,
                        0
                )
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                15,
                                17,
                                15,
                                17
                        )
                )
        );

        JPanel textPanel =
                new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
                new BoxLayout(
                        textPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        15
                )
        );

        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel descriptionLabel =
                new JLabel(
                        "<html>"
                                + escapeHtml(description)
                                + "</html>"
                );

        descriptionLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        descriptionLabel.setForeground(TEXT_SECONDARY);

        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(descriptionLabel);

        card.add(
                textPanel,
                BorderLayout.CENTER
        );

        card.add(
                actionButton,
                BorderLayout.EAST
        );

        return card;
    }

    private RoundedPanel createCommonServiceMatrix() {
        RoundedPanel card =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        card.setLayout(
                new GridLayout(
                        COMMON_SERVICES.length + 1,
                        4,
                        0,
                        0
                )
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                7,
                                10,
                                7,
                                10
                        )
                )
        );

        card.add(
                createMatrixCell(
                        "SERVICE",
                        true,
                        SwingConstants.LEFT
                )
        );

        card.add(
                createMatrixCell(
                        "PORT",
                        true,
                        SwingConstants.LEFT
                )
        );

        card.add(
                createMatrixCell(
                        "STATUS",
                        true,
                        SwingConstants.LEFT
                )
        );

        card.add(
                createMatrixCell(
                        "DURATION",
                        true,
                        SwingConstants.LEFT
                )
        );

        for (ServicePreset preset : COMMON_SERVICES) {
            JLabel status =
                    createMatrixCell(
                            "NOT TESTED",
                            false,
                            SwingConstants.LEFT
                    );

            JLabel duration =
                    createMatrixCell(
                            "-- ms",
                            false,
                            SwingConstants.LEFT
                    );

            commonPortStatusLabels.put(
                    preset,
                    status
            );

            commonPortDurationLabels.put(
                    preset,
                    duration
            );

            card.add(
                    createMatrixCell(
                            preset.toString(),
                            false,
                            SwingConstants.LEFT
                    )
            );

            card.add(
                    createMatrixCell(
                            String.valueOf(
                                    preset.getPort()
                            ),
                            false,
                            SwingConstants.LEFT
                    )
            );

            card.add(status);
            card.add(duration);
        }

        return card;
    }

    private JLabel createMatrixCell(
            String text,
            boolean header,
            int alignment
    ) {
        JLabel label =
                new JLabel(
                        text,
                        alignment
                );

        label.setOpaque(true);

        label.setBackground(
                header
                        ? INPUT_BACKGROUND
                        : SURFACE
        );

        label.setForeground(
                header
                        ? ACCENT
                        : TEXT_PRIMARY
        );

        label.setFont(
                new Font(
                        "Segoe UI",
                        header
                                ? Font.BOLD
                                : Font.PLAIN,
                        header
                                ? 10
                                : 12
                )
        );

        label.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                0,
                                1,
                                0,
                                BORDER_COLOR
                        ),
                        BorderFactory.createEmptyBorder(
                                7,
                                8,
                                7,
                                8
                        )
                )
        );

        return label;
    }

    private JPanel createSectionTitle(
            String title
    ) {
        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setOpaque(false);

        JLabel label =
                new JLabel(title);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        label.setForeground(TEXT_PRIMARY);

        panel.add(
                label,
                BorderLayout.WEST
        );

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        28
                )
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

        panel.setBackground(APP_BACKGROUND);

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
                BorderFactory.createLineBorder(
                        BORDER_COLOR,
                        1,
                        true
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

        panel.setBackground(HEADER_BACKGROUND);

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

        statusLabel.setForeground(TEXT_SECONDARY);

        panel.add(
                statusLabel,
                BorderLayout.WEST
        );

        JLabel accentLabel =
                new JLabel("NETASSIST");

        accentLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        accentLabel.setForeground(ACCENT);

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

        label.setForeground(TEXT_SECONDARY);

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
     * Listeners
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
                event -> clearQuickDiagnostics()
        );

        clearCommonButton.addActionListener(
                event -> clearCommonPorts()
        );

        clearAdvancedButton.addActionListener(
                event -> clearAdvancedTools()
        );

        clearLocalButton.addActionListener(
                event -> clearLocalNetwork()
        );


        minimizeButton.addActionListener(
                event -> minimizeWindow()
        );

        fullscreenButton.addActionListener(
                event -> toggleFullscreen()
        );

        closeButton.addActionListener(
                event -> {
                    monitoringDashboardPanel.shutdown();
                    dispose();
                }
        );
    }

    /*
     * =========================================================
     * Service / port handling
     * =========================================================
     */

    private void updatePortFromService() {
        ServicePreset selected =
                (ServicePreset) serviceBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        if (selected.isCustom()) {
            portField.setEditable(true);
            portField.setBackground(INPUT_BACKGROUND);
            portField.requestFocusInWindow();

        } else {
            portField.setText(
                    String.valueOf(
                            selected.getPort()
                    )
            );

            portField.setEditable(false);
            portField.setBackground(INPUT_DISABLED);
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

            if (port < 1 || port > 65_535) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a port between 1 and 65535."
            );

            return;
        }

        String serviceName =
                getSelectedServiceName();

        LocalDateTime now =
                LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy HH:mm:ss"
                );

        String formattedDate =
                now.format(formatter);

        setDiagnosticButtonsEnabled(false);
        setCardsRunning();

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

        SwingWorker<DiagnosticReport, Void> worker =
                new SwingWorker<>() {

            @Override
            protected DiagnosticReport doInBackground() {
                CheckResult dnsResult =
                        diagnostics.resolveHost(host);

                CheckResult reachabilityResult =
                        diagnostics.checkReachability(
                                host,
                                2_000
                        );

                TcpCheckResult tcpResult =
                        diagnostics.testTcpPort(
                                host,
                                port,
                                2_000
                        );

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
                        .append(serviceName)
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

                report.append(dnsResult)
                        .append('\n');

                report.append(reachabilityResult)
                        .append('\n');

                report.append(tcpResult)
                        .append('\n');

                report.append(
                        buildSummary(
                                dnsResult,
                                reachabilityResult,
                                tcpResult
                        )
                );

                return new DiagnosticReport(
                        dnsResult,
                        reachabilityResult,
                        tcpResult,
                        report.toString()
                );
            }

            @Override
            protected void done() {
                try {
                    DiagnosticReport result =
                            get();

                    quickOutputArea.setText(
                            result.fullReport()
                    );

                    quickOutputArea.setCaretPosition(0);

                    updateDiagnosticCards(
                            result
                    );

                    updateSummaryAndTargetDetails(
                            result,
                            host,
                            port,
                            serviceName
                    );

                    statusLabel.setText(
                            "Diagnostics complete"
                    );

                } catch (Exception exception) {
                    quickOutputArea.setText(
                            "Unexpected error: "
                                    + exception.getMessage()
                    );

                    setCardsError();

                    statusLabel.setText(
                            "Diagnostics failed"
                    );

                } finally {
                    setDiagnosticButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void updateDiagnosticCards(
            DiagnosticReport report
    ) {
        updateDnsCard(
                report.dns()
        );

        updateReachabilityCard(
                report.reachability()
        );

        updateTcpCard(
                report.tcp()
        );
    }

    private void updateDnsCard(
            CheckResult result
    ) {
        if (result.successful()) {
            dnsStatusLabel.setText("PASS");
            dnsStatusLabel.setForeground(SUCCESS);

            dnsDetailsLabel.setText(
                    "Hostname resolved successfully"
            );

        } else {
            dnsStatusLabel.setText("FAILED");
            dnsStatusLabel.setForeground(ERROR);

            dnsDetailsLabel.setText(
                    "Hostname could not be resolved"
            );
        }

        dnsDurationLabel.setText(
                result.durationMs()
                        + " ms"
        );
    }

    private void updateReachabilityCard(
            CheckResult result
    ) {
        if (result.successful()) {
            reachabilityStatusLabel.setText(
                    "REACHABLE"
            );

            reachabilityStatusLabel.setForeground(
                    SUCCESS
            );

            reachabilityDetailsLabel.setText(
                    "Host responded to reachability check"
            );

        } else {
            reachabilityStatusLabel.setText(
                    "NO RESPONSE"
            );

            reachabilityStatusLabel.setForeground(
                    WARNING
            );

            reachabilityDetailsLabel.setText(
                    "Host may block ping-like checks"
            );
        }

        reachabilityDurationLabel.setText(
                result.durationMs()
                        + " ms"
        );
    }

    private void updateTcpCard(
            TcpCheckResult result
    ) {
        tcpStatusLabel.setText(
                result.status().toString()
        );

        switch (result.status()) {
            case OPEN -> {
                tcpStatusLabel.setForeground(
                        SUCCESS
                );

                tcpDetailsLabel.setText(
                        "Requested TCP service is reachable"
                );
            }

            case CONNECTION_REFUSED -> {
                tcpStatusLabel.setForeground(
                        WARNING
                );

                tcpDetailsLabel.setText(
                        "Host responded, but service refused connection"
                );
            }

            case TIMEOUT -> {
                tcpStatusLabel.setForeground(
                        WARNING
                );

                tcpDetailsLabel.setText(
                        "Connection timed out or may be filtered"
                );
            }

            case UNREACHABLE -> {
                tcpStatusLabel.setForeground(
                        ERROR
                );

                tcpDetailsLabel.setText(
                        "Target host or network was unreachable"
                );
            }

            case DNS_FAILURE -> {
                tcpStatusLabel.setForeground(
                        ERROR
                );

                tcpDetailsLabel.setText(
                        "TCP test could not resolve hostname"
                );
            }

            case INVALID_PORT -> {
                tcpStatusLabel.setForeground(
                        ERROR
                );

                tcpDetailsLabel.setText(
                        "Invalid TCP port"
                );
            }

            case ERROR -> {
                tcpStatusLabel.setForeground(
                        ERROR
                );

                tcpDetailsLabel.setText(
                        "Unexpected TCP test error"
                );
            }
        }

        tcpDurationLabel.setText(
                result.durationMs()
                        + " ms"
        );
    }

    private void updateSummaryAndTargetDetails(
            DiagnosticReport report,
            String host,
            int port,
            String serviceName
    ) {
        String summary =
                getSummaryText(
                        report.dns(),
                        report.reachability(),
                        report.tcp()
                );

        summaryLabel.setText(
                "<html>"
                        + escapeHtml(summary)
                        + "</html>"
        );

        String resolvedAddress =
                getResolvedAddress(
                        report.dns()
                );

        targetDetailsLabel.setText(
                "<html>"
                        + "<b>Host:</b> "
                        + escapeHtml(host)
                        + "<br>"
                        + "<b>Resolved IP:</b> "
                        + escapeHtml(resolvedAddress)
                        + "<br>"
                        + "<b>Service:</b> "
                        + escapeHtml(serviceName)
                        + "<br>"
                        + "<b>Port:</b> "
                        + port
                        + "</html>"
        );
    }

    private String getResolvedAddress(
            CheckResult dnsResult
    ) {
        if (!dnsResult.successful()) {
            return "Unavailable";
        }

        String details =
                dnsResult.details();

        if (details == null
                || details.isBlank()) {
            return "Resolved";
        }

        String[] lines =
                details.split("\\R");

        for (String line : lines) {
            String trimmed =
                    line.trim();

            int colon =
                    trimmed.lastIndexOf(':');

            if (colon >= 0
                    && colon < trimmed.length() - 1) {
                String candidate =
                        trimmed.substring(
                                colon + 1
                        ).trim();

                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
        }

        return details;
    }

    private void setCardsRunning() {
        setCardRunning(
                dnsStatusLabel,
                dnsDurationLabel,
                dnsDetailsLabel
        );

        setCardRunning(
                reachabilityStatusLabel,
                reachabilityDurationLabel,
                reachabilityDetailsLabel
        );

        setCardRunning(
                tcpStatusLabel,
                tcpDurationLabel,
                tcpDetailsLabel
        );

        summaryLabel.setText(
                "<html>Analyzing the target and selected service...</html>"
        );

        targetDetailsLabel.setText(
                "<html>Waiting for diagnostic results...</html>"
        );
    }

    private void setCardRunning(
            JLabel status,
            JLabel duration,
            JLabel details
    ) {
        status.setText("RUNNING...");
        status.setForeground(PRIMARY_HOVER);

        duration.setText("-- ms");

        details.setText(
                "Diagnostic check in progress"
        );
    }

    private void setCardsError() {
        setCardError(
                dnsStatusLabel,
                dnsDurationLabel,
                dnsDetailsLabel
        );

        setCardError(
                reachabilityStatusLabel,
                reachabilityDurationLabel,
                reachabilityDetailsLabel
        );

        setCardError(
                tcpStatusLabel,
                tcpDurationLabel,
                tcpDetailsLabel
        );

        summaryLabel.setText(
                "<html>An unexpected error prevented the diagnostic report from completing.</html>"
        );
    }

    private void setCardError(
            JLabel status,
            JLabel duration,
            JLabel details
    ) {
        status.setText("ERROR");
        status.setForeground(ERROR);

        duration.setText("-- ms");

        details.setText(
                "Unable to complete check"
        );
    }

    private void clearQuickDiagnostics() {
        quickOutputArea.setText("");

        resetCard(
                dnsStatusLabel,
                dnsDurationLabel,
                dnsDetailsLabel
        );

        resetCard(
                reachabilityStatusLabel,
                reachabilityDurationLabel,
                reachabilityDetailsLabel
        );

        resetCard(
                tcpStatusLabel,
                tcpDurationLabel,
                tcpDetailsLabel
        );

        summaryLabel.setText(
                "<html>Run diagnostics to generate a troubleshooting summary.</html>"
        );

        targetDetailsLabel.setText(
                "<html>Target details will appear here after a diagnostic run.</html>"
        );

        statusLabel.setText("Ready");
    }

    private void resetCard(
            JLabel status,
            JLabel duration,
            JLabel details
    ) {
        status.setText("NOT TESTED");
        status.setForeground(TEXT_SECONDARY);

        duration.setText("-- ms");

        details.setText(
                "Waiting for a diagnostic run"
        );
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
        setCommonPortScanRunning();

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

        SwingWorker<PortScanReport, Void> worker =
                new SwingWorker<>() {

            @Override
            protected PortScanReport doInBackground() {
                long scanStart =
                        System.nanoTime();

                Map<ServicePreset, TcpCheckResult> results =
                        new EnumMap<>(
                                ServicePreset.class
                        );

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

                for (ServicePreset preset : COMMON_SERVICES) {
                    TcpCheckResult result =
                            diagnostics.testTcpPort(
                                    host,
                                    preset.getPort(),
                                    1_000
                            );

                    results.put(
                            preset,
                            result
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

                long durationMs =
                        (System.nanoTime() - scanStart)
                                / 1_000_000;

                return new PortScanReport(
                        results,
                        report.toString(),
                        durationMs
                );
            }

            @Override
            protected void done() {
                try {
                    PortScanReport report =
                            get();

                    commonPortsOutputArea.setText(
                            report.fullReport()
                    );

                    commonPortsOutputArea.setCaretPosition(
                            0
                    );

                    updateCommonPortVisuals(
                            report
                    );

                    statusLabel.setText(
                            "Common port test complete"
                    );

                } catch (Exception exception) {
                    commonPortsOutputArea.setText(
                            "Unexpected error: "
                                    + exception.getMessage()
                    );

                    setCommonPortScanError();

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
        setAdvancedToolRunning(
                "NSLOOKUP",
                host
        );

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

                    advancedOutputArea.setText(
                            formatCommandResult(
                                    result
                            )
                    );

                    advancedOutputArea.setCaretPosition(
                            0
                    );

                    updateAdvancedToolResult(
                            "NSLOOKUP",
                            host,
                            result
                    );

                    statusLabel.setText(
                            "NSLookup complete"
                    );

                } catch (Exception exception) {
                    advancedOutputArea.setText(
                            "Unexpected error: "
                                    + exception.getMessage()
                    );

                    setAdvancedToolError(
                            "NSLOOKUP",
                            host
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
        setAdvancedToolRunning(
                "TRACEROUTE",
                host
        );

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

                    advancedOutputArea.setText(
                            formatCommandResult(
                                    result
                            )
                    );

                    advancedOutputArea.setCaretPosition(
                            0
                    );

                    updateAdvancedToolResult(
                            "TRACEROUTE",
                            host,
                            result
                    );

                    statusLabel.setText(
                            "Traceroute complete"
                    );

                } catch (Exception exception) {
                    advancedOutputArea.setText(
                            "Unexpected error: "
                                    + exception.getMessage()
                    );

                    setAdvancedToolError(
                            "TRACEROUTE",
                            host
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
        setLocalNetworkRunning();

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

                    localOutputArea.setText(
                            formatCommandResult(
                                    result
                            )
                    );

                    localOutputArea.setCaretPosition(
                            0
                    );

                    updateLocalNetworkVisuals(
                            result
                    );

                    statusLabel.setText(
                            "Local network information loaded"
                    );

                } catch (Exception exception) {
                    localOutputArea.setText(
                            "Unexpected error: "
                                    + exception.getMessage()
                    );

                    setLocalNetworkError();

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

    private void setCommonPortScanRunning() {
        commonOpenCountLabel.setText("--");
        commonOpenCountLabel.setForeground(PRIMARY_HOVER);

        commonRefusedCountLabel.setText("--");
        commonRefusedCountLabel.setForeground(PRIMARY_HOVER);

        commonTimeoutCountLabel.setText("--");
        commonTimeoutCountLabel.setForeground(PRIMARY_HOVER);

        commonScanDurationLabel.setText("-- ms");
        commonScanDurationLabel.setForeground(PRIMARY_HOVER);

        for (ServicePreset preset : COMMON_SERVICES) {
            JLabel status =
                    commonPortStatusLabels.get(
                            preset
                    );

            JLabel duration =
                    commonPortDurationLabels.get(
                            preset
                    );

            if (status != null) {
                status.setText("TESTING...");
                status.setForeground(PRIMARY_HOVER);
            }

            if (duration != null) {
                duration.setText("-- ms");
                duration.setForeground(TEXT_SECONDARY);
            }
        }
    }

    private void updateCommonPortVisuals(
            PortScanReport report
    ) {
        int open = 0;
        int refused = 0;
        int timedOut = 0;

        for (ServicePreset preset : COMMON_SERVICES) {
            TcpCheckResult result =
                    report.results().get(
                            preset
                    );

            if (result == null) {
                continue;
            }

            JLabel status =
                    commonPortStatusLabels.get(
                            preset
                    );

            JLabel duration =
                    commonPortDurationLabels.get(
                            preset
                    );

            if (status != null) {
                status.setText(
                        result.status().toString()
                );

                status.setForeground(
                        colorForTcpStatus(
                                result.status()
                        )
                );
            }

            if (duration != null) {
                duration.setText(
                        result.durationMs()
                                + " ms"
                );

                duration.setForeground(
                        TEXT_SECONDARY
                );
            }

            switch (result.status()) {
                case OPEN ->
                        open++;

                case CONNECTION_REFUSED ->
                        refused++;

                case TIMEOUT ->
                        timedOut++;

                default -> {
                    // Other failures remain visible in the service matrix.
                }
            }
        }

        commonOpenCountLabel.setText(
                String.valueOf(open)
        );

        commonOpenCountLabel.setForeground(
                open > 0
                        ? SUCCESS
                        : TEXT_PRIMARY
        );

        commonRefusedCountLabel.setText(
                String.valueOf(refused)
        );

        commonRefusedCountLabel.setForeground(
                refused > 0
                        ? WARNING
                        : TEXT_PRIMARY
        );

        commonTimeoutCountLabel.setText(
                String.valueOf(timedOut)
        );

        commonTimeoutCountLabel.setForeground(
                timedOut > 0
                        ? WARNING
                        : TEXT_PRIMARY
        );

        commonScanDurationLabel.setText(
                report.durationMs()
                        + " ms"
        );

        commonScanDurationLabel.setForeground(
                ACCENT
        );
    }

    private void setCommonPortScanError() {
        commonOpenCountLabel.setText("ERROR");
        commonOpenCountLabel.setForeground(ERROR);

        commonRefusedCountLabel.setText("--");
        commonRefusedCountLabel.setForeground(TEXT_SECONDARY);

        commonTimeoutCountLabel.setText("--");
        commonTimeoutCountLabel.setForeground(TEXT_SECONDARY);

        commonScanDurationLabel.setText("-- ms");
        commonScanDurationLabel.setForeground(TEXT_SECONDARY);

        for (ServicePreset preset : COMMON_SERVICES) {
            JLabel status =
                    commonPortStatusLabels.get(
                            preset
                    );

            JLabel duration =
                    commonPortDurationLabels.get(
                            preset
                    );

            if (status != null) {
                status.setText("ERROR");
                status.setForeground(ERROR);
            }

            if (duration != null) {
                duration.setText("-- ms");
            }
        }
    }

    private void clearCommonPorts() {
        commonPortsOutputArea.setText("");

        commonOpenCountLabel.setText("0");
        commonOpenCountLabel.setForeground(TEXT_PRIMARY);

        commonRefusedCountLabel.setText("0");
        commonRefusedCountLabel.setForeground(TEXT_PRIMARY);

        commonTimeoutCountLabel.setText("0");
        commonTimeoutCountLabel.setForeground(TEXT_PRIMARY);

        commonScanDurationLabel.setText("-- ms");
        commonScanDurationLabel.setForeground(TEXT_PRIMARY);

        for (ServicePreset preset : COMMON_SERVICES) {
            JLabel status =
                    commonPortStatusLabels.get(
                            preset
                    );

            JLabel duration =
                    commonPortDurationLabels.get(
                            preset
                    );

            if (status != null) {
                status.setText("NOT TESTED");
                status.setForeground(TEXT_SECONDARY);
            }

            if (duration != null) {
                duration.setText("-- ms");
                duration.setForeground(TEXT_SECONDARY);
            }
        }

        statusLabel.setText("Ready");
    }

    private void setAdvancedToolRunning(
            String tool,
            String target
    ) {
        advancedToolLabel.setText(tool);
        advancedToolLabel.setForeground(ACCENT);

        advancedStatusLabel.setText("RUNNING");
        advancedStatusLabel.setForeground(PRIMARY_HOVER);

        advancedDurationLabel.setText("-- ms");
        advancedDurationLabel.setForeground(TEXT_SECONDARY);

        advancedTargetLabel.setText(
                shortenForMetric(
                        target,
                        20
                )
        );

        advancedTargetLabel.setForeground(TEXT_PRIMARY);
    }

    private void updateAdvancedToolResult(
            String tool,
            String target,
            CommandResult result
    ) {
        advancedToolLabel.setText(tool);
        advancedToolLabel.setForeground(ACCENT);

        advancedStatusLabel.setText(
                result.successful()
                        ? "SUCCESS"
                        : "FAILED"
        );

        advancedStatusLabel.setForeground(
                result.successful()
                        ? SUCCESS
                        : ERROR
        );

        advancedDurationLabel.setText(
                result.durationMs()
                        + " ms"
        );

        advancedDurationLabel.setForeground(TEXT_PRIMARY);

        advancedTargetLabel.setText(
                shortenForMetric(
                        target,
                        20
                )
        );

        advancedTargetLabel.setForeground(TEXT_PRIMARY);
    }

    private void setAdvancedToolError(
            String tool,
            String target
    ) {
        advancedToolLabel.setText(tool);
        advancedToolLabel.setForeground(ACCENT);

        advancedStatusLabel.setText("ERROR");
        advancedStatusLabel.setForeground(ERROR);

        advancedDurationLabel.setText("-- ms");
        advancedDurationLabel.setForeground(TEXT_SECONDARY);

        advancedTargetLabel.setText(
                shortenForMetric(
                        target,
                        20
                )
        );
    }

    private void clearAdvancedTools() {
        advancedOutputArea.setText("");

        advancedToolLabel.setText("NONE");
        advancedToolLabel.setForeground(TEXT_PRIMARY);

        advancedStatusLabel.setText("NOT RUN");
        advancedStatusLabel.setForeground(TEXT_PRIMARY);

        advancedDurationLabel.setText("-- ms");
        advancedDurationLabel.setForeground(TEXT_PRIMARY);

        advancedTargetLabel.setText("--");
        advancedTargetLabel.setForeground(TEXT_PRIMARY);

        statusLabel.setText("Ready");
    }

    private void setLocalNetworkRunning() {
        localStatusLabel.setText("LOADING");
        localStatusLabel.setForeground(PRIMARY_HOVER);

        localDurationLabel.setText("-- ms");
        localDurationLabel.setForeground(TEXT_SECONDARY);

        localIpv4Label.setText("--");
        localIpv4Label.setForeground(TEXT_SECONDARY);

        localAdapterLabel.setText("--");
        localAdapterLabel.setForeground(TEXT_SECONDARY);

        localSnapshotLabel.setText(
                "<html>Collecting active interfaces and Windows IP configuration...</html>"
        );
    }

    private void updateLocalNetworkVisuals(
            CommandResult result
    ) {
        localStatusLabel.setText(
                result.successful()
                        ? "READY"
                        : "FAILED"
        );

        localStatusLabel.setForeground(
                result.successful()
                        ? SUCCESS
                        : ERROR
        );

        localDurationLabel.setText(
                result.durationMs()
                        + " ms"
        );

        localDurationLabel.setForeground(TEXT_PRIMARY);

        String localInfo =
                diagnostics
                        .getLocalNetworkInformation();

        String adapter =
                extractPrimaryAdapter(
                        localInfo
                );

        String ipv4 =
                extractPrimaryIpv4(
                        localInfo
                );

        localAdapterLabel.setText(
                shortenForMetric(
                        adapter,
                        23
                )
        );

        localAdapterLabel.setToolTipText(
                adapter
        );

        localAdapterLabel.setForeground(
                ACCENT
        );

        localIpv4Label.setText(ipv4);
        localIpv4Label.setForeground(TEXT_PRIMARY);

        localSnapshotLabel.setText(
                "<html>"
                        + "<b>Primary adapter:</b> "
                        + escapeHtml(adapter)
                        + "<br>"
                        + "<b>IPv4 address:</b> "
                        + escapeHtml(ipv4)
                        + "<br><br>"
                        + "The complete Windows configuration remains available below."
                        + "</html>"
        );
    }

    private void setLocalNetworkError() {
        localStatusLabel.setText("ERROR");
        localStatusLabel.setForeground(ERROR);

        localDurationLabel.setText("-- ms");
        localDurationLabel.setForeground(TEXT_SECONDARY);

        localIpv4Label.setText("--");
        localIpv4Label.setForeground(TEXT_SECONDARY);

        localAdapterLabel.setText("--");
        localAdapterLabel.setForeground(TEXT_SECONDARY);

        localSnapshotLabel.setText(
                "<html>Unable to load local network information.</html>"
        );
    }

    private void clearLocalNetwork() {
        localOutputArea.setText("");

        localStatusLabel.setText("NOT LOADED");
        localStatusLabel.setForeground(TEXT_PRIMARY);

        localDurationLabel.setText("-- ms");
        localDurationLabel.setForeground(TEXT_PRIMARY);

        localIpv4Label.setText("--");
        localIpv4Label.setForeground(TEXT_PRIMARY);

        localAdapterLabel.setText("--");
        localAdapterLabel.setForeground(TEXT_PRIMARY);
        localAdapterLabel.setToolTipText(null);

        localSnapshotLabel.setText(
                "<html>Run IP Configuration to load a local network snapshot.</html>"
        );

        statusLabel.setText("Ready");
    }

    private Color colorForTcpStatus(
            TcpStatus status
    ) {
        return switch (status) {
            case OPEN ->
                    SUCCESS;

            case CONNECTION_REFUSED,
                 TIMEOUT ->
                    WARNING;

            case UNREACHABLE,
                 DNS_FAILURE,
                 INVALID_PORT,
                 ERROR ->
                    ERROR;
        };
    }

    private String extractPrimaryAdapter(
            String localInfo
    ) {
        if (localInfo == null
                || localInfo.isBlank()) {
            return "Unavailable";
        }

        String[] lines =
                localInfo.split("\\R");

        for (String line : lines) {
            int separator =
                    line.indexOf(" -> ");

            if (separator > 0) {
                return line.substring(
                        0,
                        separator
                ).trim();
            }
        }

        return "Unavailable";
    }

    private String extractPrimaryIpv4(
            String localInfo
    ) {
        if (localInfo == null
                || localInfo.isBlank()) {
            return "Unavailable";
        }

        String[] lines =
                localInfo.split("\\R");

        for (String line : lines) {
            int separator =
                    line.indexOf(" -> ");

            if (separator >= 0
                    && separator + 4 < line.length()) {
                String addressPart =
                        line.substring(
                                separator + 4
                        ).trim();

                int comma =
                        addressPart.indexOf(',');

                return comma >= 0
                        ? addressPart.substring(
                                0,
                                comma
                        ).trim()
                        : addressPart;
            }
        }

        return "Unavailable";
    }

    private String shortenForMetric(
            String value,
            int maxLength
    ) {
        if (value == null
                || value.isBlank()) {
            return "--";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                Math.max(
                        1,
                        maxLength - 3
                )
        ) + "...";
    }

    /*
     * =========================================================
     * Borderless window / fullscreen controls
     * =========================================================
     */

    private void minimizeWindow() {
        setState(JFrame.ICONIFIED);
    }

    private void toggleFullscreen() {
        if (fullScreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    private void enterFullscreen() {
        if (fullScreen) {
            return;
        }

        /*
         * Remember the exact windowed position and size so the user
         * returns to the same layout when leaving fullscreen.
         */
        windowedBounds =
                getBounds();

        fullScreen = true;

        /*
         * GraphicsDevice provides real borderless fullscreen rather
         * than simply maximizing the JFrame inside the desktop area.
         */
        screenDevice.setFullScreenWindow(
                this
        );

        fullscreenButton.setType(
                WindowControlType.RESTORE
        );

        fullscreenButton.setToolTipText(
                "Exit Fullscreen"
        );
    }

    private void exitFullscreen() {
        if (!fullScreen) {
            return;
        }

        screenDevice.setFullScreenWindow(
                null
        );

        fullScreen = false;

        /*
         * Restore the previous window position and dimensions.
         */
        setBounds(
                windowedBounds
        );

        fullscreenButton.setType(
                WindowControlType.MAXIMIZE
        );

        fullscreenButton.setToolTipText(
                "Enter Fullscreen"
        );

        toFront();
        requestFocus();
    }

    private void configureFullscreenShortcut() {
        getRootPane()
                .getInputMap(
                        JComponent.WHEN_IN_FOCUSED_WINDOW
                )
                .put(
                        KeyStroke.getKeyStroke(
                                KeyEvent.VK_ESCAPE,
                                0
                        ),
                        "exitFullscreen"
                );

        getRootPane()
                .getActionMap()
                .put(
                        "exitFullscreen",
                        new AbstractAction() {

                            @Override
                            public void actionPerformed(
                                    java.awt.event.ActionEvent event
                            ) {
                                if (fullScreen) {
                                    exitFullscreen();
                                }
                            }
                        }
                );
    }

    private void configureWindowDragging(
            JPanel header
    ) {
        MouseAdapter dragListener =
                new MouseAdapter() {

                    @Override
                    public void mousePressed(
                            MouseEvent event
                    ) {
                        if (fullScreen
                                || event.getButton()
                                != MouseEvent.BUTTON1) {
                            return;
                        }

                        dragOffset =
                                event.getPoint();
                    }

                    @Override
                    public void mouseReleased(
                            MouseEvent event
                    ) {
                        dragOffset = null;
                    }

                    @Override
                    public void mouseDragged(
                            MouseEvent event
                    ) {
                        if (fullScreen
                                || dragOffset == null) {
                            return;
                        }

                        Point screenLocation =
                                event.getLocationOnScreen();

                        setLocation(
                                screenLocation.x
                                        - dragOffset.x,
                                screenLocation.y
                                        - dragOffset.y
                        );
                    }
                };

        header.addMouseListener(
                dragListener
        );

        header.addMouseMotionListener(
                dragListener
        );
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
                (ServicePreset) serviceBox.getSelectedItem();

        return selected == null
                ? "Unknown"
                : selected.toString();
    }

    private String buildSummary(
            CheckResult dnsResult,
            CheckResult reachabilityResult,
            TcpCheckResult tcpResult
    ) {
        return "SUMMARY\n"
                + getSummaryText(
                        dnsResult,
                        reachabilityResult,
                        tcpResult
                )
                + "\n";
    }

    private String getSummaryText(
            CheckResult dnsResult,
            CheckResult reachabilityResult,
            TcpCheckResult tcpResult
    ) {
        if (!dnsResult.successful()) {
            return "DNS failed. Verify the hostname and your DNS configuration.";
        }

        return switch (tcpResult.status()) {

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

    private String escapeHtml(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /*
     * =========================================================
     * Custom UI components
     * =========================================================
     */

    private enum WindowControlType {
        MINIMIZE,
        MAXIMIZE,
        RESTORE,
        CLOSE
    }

    private static final class WindowControlButton
            extends JButton {

        private WindowControlType type;
        private boolean hovered;

        private WindowControlButton(
                WindowControlType type
        ) {
            this.type = type;

            setPreferredSize(
                    new Dimension(
                            44,
                            34
                    )
            );

            setMinimumSize(
                    new Dimension(
                            44,
                            34
                    )
            );

            setMaximumSize(
                    new Dimension(
                            44,
                            34
                    )
            );

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
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

        private void setType(
                WindowControlType type
        ) {
            this.type = type;
            repaint();
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {
            Graphics2D g2 =
                    (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color background;

            if (type == WindowControlType.CLOSE) {
                background =
                        hovered
                                ? new Color(
                                        220,
                                        38,
                                        38
                                )
                                : new Color(
                                        127,
                                        29,
                                        29
                                );
            } else {
                background =
                        hovered
                                ? SECONDARY_BUTTON_HOVER
                                : SURFACE;
            }

            g2.setColor(background);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    8,
                    8
            );

            g2.setColor(Color.WHITE);

            g2.setStroke(
                    new BasicStroke(
                            1.6f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            int centerX =
                    getWidth() / 2;

            int centerY =
                    getHeight() / 2;

            switch (type) {

                case MINIMIZE -> {
                    g2.drawLine(
                            centerX - 6,
                            centerY + 3,
                            centerX + 6,
                            centerY + 3
                    );
                }

                case MAXIMIZE -> {
                    g2.drawRect(
                            centerX - 6,
                            centerY - 6,
                            12,
                            12
                    );
                }

                case RESTORE -> {
                    /*
                     * Back window.
                     */
                    g2.drawRect(
                            centerX - 2,
                            centerY - 6,
                            10,
                            10
                    );

                    /*
                     * Cover part of the back window with
                     * the button background before drawing
                     * the front window.
                     */
                    g2.setColor(background);

                    g2.fillRect(
                            centerX - 7,
                            centerY - 2,
                            12,
                            12
                    );

                    g2.setColor(Color.WHITE);

                    g2.drawRect(
                            centerX - 7,
                            centerY - 2,
                            10,
                            10
                    );
                }

                case CLOSE -> {
                    g2.drawLine(
                            centerX - 5,
                            centerY - 5,
                            centerX + 5,
                            centerY + 5
                    );

                    g2.drawLine(
                            centerX + 5,
                            centerY - 5,
                            centerX - 5,
                            centerY + 5
                    );
                }
            }

            g2.dispose();
        }
    }

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
                    (Graphics2D) graphics.create();

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
                    (Graphics2D) graphics.create();

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
                    (Graphics2D) graphics.create();

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
            // Flat modern appearance.
        }

        @Override
        protected void paintFocusIndicator(
                Graphics graphics,
                int tabPlacement,
                Rectangle[] rects,
                int tabIndex,
                Rectangle iconRect,
                Rectangle textRect,
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
                Rectangle textRect,
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
