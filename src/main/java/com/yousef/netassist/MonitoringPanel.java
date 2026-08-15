package com.yousef.netassist;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicComboBoxUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public final class MonitoringPanel extends JPanel {

    private static final Color APP_BACKGROUND =
            new Color(
                    15,
                    23,
                    42
            );

    private static final Color SURFACE =
            new Color(
                    24,
                    34,
                    53
            );

    private static final Color INPUT_BACKGROUND =
            new Color(
                    17,
                    27,
                    45
            );

    private static final Color INPUT_DISABLED =
            new Color(
                    30,
                    41,
                    59
            );

    private static final Color PRIMARY =
            new Color(
                    37,
                    99,
                    235
            );

    private static final Color PRIMARY_HOVER =
            new Color(
                    59,
                    130,
                    246
            );

    private static final Color ACCENT =
            new Color(
                    20,
                    184,
                    166
            );

    private static final Color SUCCESS =
            new Color(
                    34,
                    197,
                    94
            );

    private static final Color WARNING =
            new Color(
                    245,
                    158,
                    11
            );

    private static final Color ERROR =
            new Color(
                    239,
                    68,
                    68
            );

    private static final Color TEXT_PRIMARY =
            new Color(
                    241,
                    245,
                    249
            );

    private static final Color TEXT_SECONDARY =
            new Color(
                    148,
                    163,
                    184
            );

    private static final Color BORDER_COLOR =
            new Color(
                    51,
                    65,
                    85
            );

    private static final Color SECONDARY_BUTTON =
            new Color(
                    51,
                    65,
                    85
            );

    private static final Color SECONDARY_BUTTON_HOVER =
            new Color(
                    71,
                    85,
                    105
            );

    private final NetworkDiagnostics diagnostics;
    private final Supplier<String> targetSupplier;

    private final JComboBox<ServicePreset> serviceBox =
            new JComboBox<>(
                    ServicePreset.values()
            );

    private final JTextField portField =
            new JTextField(
                    "443",
                    6
            );

    private final JSpinner intervalSpinner =
            new JSpinner(
                    new SpinnerNumberModel(
                            5,
                            1,
                            3600,
                            1
                    )
            );

    private final JSpinner failureThresholdSpinner =
            new JSpinner(
                    new SpinnerNumberModel(
                            3,
                            1,
                            20,
                            1
                    )
            );

    private final JSpinner recoveryThresholdSpinner =
            new JSpinner(
                    new SpinnerNumberModel(
                            2,
                            1,
                            20,
                            1
                    )
            );

    private final JButton startButton =
            new RoundedButton(
                    "Start Monitoring",
                    PRIMARY,
                    PRIMARY_HOVER,
                    Color.WHITE
            );

    private final JButton stopButton =
            new RoundedButton(
                    "Stop",
                    SECONDARY_BUTTON,
                    SECONDARY_BUTTON_HOVER,
                    TEXT_PRIMARY
            );

    private final JButton clearIncidentsButton =
            new RoundedButton(
                    "Clear Incidents",
                    SECONDARY_BUTTON,
                    SECONDARY_BUTTON_HOVER,
                    TEXT_PRIMARY
            );

    private final JLabel statusValueLabel =
            createValueLabel(
                    "IDLE"
            );

    private final JLabel uptimeValueLabel =
            createValueLabel(
                    "--"
            );

    private final JLabel responseValueLabel =
            createValueLabel(
                    "-- ms"
            );

    private final JLabel checksValueLabel =
            createValueLabel(
                    "0"
            );

    private final JLabel averageLatencyLabel =
            createSmallMetricValue(
                    "-- ms"
            );

    private final JLabel minimumLatencyLabel =
            createSmallMetricValue(
                    "-- ms"
            );

    private final JLabel maximumLatencyLabel =
            createSmallMetricValue(
                    "-- ms"
            );

    private final JLabel successfulChecksLabel =
            createSmallMetricValue(
                    "0"
            );

    private final JLabel failedChecksLabel =
            createSmallMetricValue(
                    "0"
            );

    private final JLabel elapsedLabel =
            createSmallMetricValue(
                    "00:00:00"
            );

    private final JLabel activeTargetLabel =
            new JLabel(
                    "No active monitoring session"
            );

    private final DefaultTableModel incidentModel =
            new DefaultTableModel(
                    new String[]{
                            "Time",
                            "Event",
                            "Details",
                            "Duration"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(
                        int row,
                        int column
                ) {
                    return false;
                }
            };

    private final JTable incidentTable =
            new JTable(
                    incidentModel
            );

    private final LatencyGraphPanel latencyGraph =
            new LatencyGraphPanel();

    private MonitoringSession session;

    private final javax.swing.Timer elapsedTimer =
            new javax.swing.Timer(
                    1_000,
                    event -> updateElapsedTime()
            );

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "HH:mm:ss"
            );

    public MonitoringPanel(
            NetworkDiagnostics diagnostics,
            Supplier<String> targetSupplier
    ) {
        this.diagnostics =
                diagnostics;

        this.targetSupplier =
                targetSupplier;

        setBackground(
                APP_BACKGROUND
        );

        setLayout(
                new BorderLayout(
                        0,
                        12
                )
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        14,
                        2,
                        2,
                        2
                )
        );

        configureInputs();
        configureIncidentTable();
        createLayout();
        registerListeners();

        updatePortFromService();

        stopButton.setEnabled(false);
    }

    private void configureInputs() {
        styleTextField(
                portField
        );

        /*
         * Match the Service dropdown styling used by
         * DashboardFrame's Quick Diagnostics tab.
         */
        serviceBox.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        serviceBox.setForeground(
                TEXT_PRIMARY
        );

        serviceBox.setBackground(
                INPUT_BACKGROUND
        );

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
                                (JLabel) super
                                        .getListCellRendererComponent(
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
                         * index == -1 is the selected value shown
                         * while the combo box is closed.
                         */
                        if (index == -1) {
                            label.setBackground(
                                    INPUT_BACKGROUND
                            );

                            label.setForeground(
                                    TEXT_PRIMARY
                            );

                        } else if (isSelected) {
                            label.setBackground(
                                    PRIMARY
                            );

                            label.setForeground(
                                    Color.WHITE
                            );

                        } else {
                            label.setBackground(
                                    INPUT_BACKGROUND
                            );

                            label.setForeground(
                                    TEXT_PRIMARY
                            );
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
                                (Graphics2D)
                                        graphics.create();

                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        g2.setColor(
                                INPUT_BACKGROUND
                        );

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
                                new JButton();

                        button.setFocusPainted(false);
                        button.setBorderPainted(false);
                        button.setContentAreaFilled(false);
                        button.setOpaque(false);

                        button.setPreferredSize(
                                new Dimension(
                                        28,
                                        28
                                )
                        );

                        /*
                         * Draw the arrow ourselves. This avoids
                         * Windows LAF / Unicode rendering differences.
                         */
                        button.setUI(
                                new javax.swing.plaf.basic.BasicButtonUI() {

                                    @Override
                                    public void paint(
                                            Graphics graphics,
                                            JComponent component
                                    ) {
                                        Graphics2D g2 =
                                                (Graphics2D)
                                                        graphics.create();

                                        g2.setRenderingHint(
                                                RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON
                                        );

                                        int centerX =
                                                component.getWidth()
                                                        / 2;

                                        int centerY =
                                                component.getHeight()
                                                        / 2;

                                        g2.setColor(
                                                TEXT_SECONDARY
                                        );

                                        g2.setStroke(
                                                new BasicStroke(
                                                        1.6f,
                                                        BasicStroke.CAP_ROUND,
                                                        BasicStroke.JOIN_ROUND
                                                )
                                        );

                                        g2.drawLine(
                                                centerX - 4,
                                                centerY - 2,
                                                centerX,
                                                centerY + 2
                                        );

                                        g2.drawLine(
                                                centerX,
                                                centerY + 2,
                                                centerX + 4,
                                                centerY - 2
                                        );

                                        g2.dispose();
                                    }
                                }
                        );

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

        /*
         * setUI() can reset some Swing properties.
         */
        serviceBox.setBackground(
                INPUT_BACKGROUND
        );

        serviceBox.setForeground(
                TEXT_PRIMARY
        );

        styleSpinner(
                intervalSpinner
        );

        styleSpinner(
                failureThresholdSpinner
        );

        styleSpinner(
                recoveryThresholdSpinner
        );
    }

    private void styleTextField(
            JTextField field
    ) {
        field.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        field.setForeground(
                TEXT_PRIMARY
        );

        field.setCaretColor(
                TEXT_PRIMARY
        );

        field.setBackground(
                INPUT_BACKGROUND
        );

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory
                                .createLineBorder(
                                        BORDER_COLOR,
                                        1,
                                        true
                                ),
                        BorderFactory
                                .createEmptyBorder(
                                        7,
                                        10,
                                        7,
                                        10
                                )
                )
        );
    }

    private void styleSpinner(
            JSpinner spinner
    ) {
        spinner.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        spinner.setPreferredSize(
                new Dimension(
                        62,
                        34
                )
        );

        JSpinner.DefaultEditor editor =
                (JSpinner.DefaultEditor)
                        spinner.getEditor();

        JTextField textField =
                editor.getTextField();

        textField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        styleTextField(
                textField
        );
    }

    private void configureIncidentTable() {
        incidentTable.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        incidentTable.setRowHeight(
                28
        );

        incidentTable.setBackground(
                INPUT_BACKGROUND
        );

        incidentTable.setForeground(
                TEXT_PRIMARY
        );

        incidentTable.setSelectionBackground(
                PRIMARY
        );

        incidentTable.setSelectionForeground(
                Color.WHITE
        );

        incidentTable.setGridColor(
                BORDER_COLOR
        );

        incidentTable.setShowVerticalLines(
                false
        );

        incidentTable.setFillsViewportHeight(
                true
        );

        JTableHeader header =
                incidentTable.getTableHeader();

        header.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        header.setBackground(
                SURFACE
        );

        header.setForeground(
                ACCENT
        );

        header.setReorderingAllowed(
                false
        );

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();

        renderer.setBackground(
                INPUT_BACKGROUND
        );

        renderer.setForeground(
                TEXT_PRIMARY
        );

        renderer.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        8,
                        0,
                        8
                )
        );

        for (
                int i = 0;
                i < incidentTable
                        .getColumnCount();
                i++
        ) {
            incidentTable
                    .getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(
                            renderer
                    );
        }

        incidentTable
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(
                        90
                );

        incidentTable
                .getColumnModel()
                .getColumn(1)
                .setPreferredWidth(
                        90
                );

        incidentTable
                .getColumnModel()
                .getColumn(2)
                .setPreferredWidth(
                        520
                );

        incidentTable
                .getColumnModel()
                .getColumn(3)
                .setPreferredWidth(
                        90
                );
    }

    private void createLayout() {
        add(
                createControlsPanel(),
                BorderLayout.NORTH
        );

        JPanel content =
                new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel headlineCards =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                12,
                                0
                        )
                );

        headlineCards.setOpaque(false);

        headlineCards.add(
                createMetricCard(
                        "CURRENT STATUS",
                        statusValueLabel,
                        "Confirmed monitoring state"
                )
        );

        headlineCards.add(
                createMetricCard(
                        "UPTIME",
                        uptimeValueLabel,
                        "Successful checks"
                )
        );

        headlineCards.add(
                createMetricCard(
                        "RESPONSE TIME",
                        responseValueLabel,
                        "Latest successful TCP check"
                )
        );

        headlineCards.add(
                createMetricCard(
                        "TOTAL CHECKS",
                        checksValueLabel,
                        "Checks completed this session"
                )
        );

        headlineCards.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        105
                )
        );

        content.add(
                headlineCards
        );

        content.add(
                Box.createVerticalStrut(
                        12
                )
        );

        JPanel middle =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                12,
                                0
                        )
                );

        middle.setOpaque(false);

        middle.add(
                createLatencyCard()
        );

        middle.add(
                createSessionStatisticsCard()
        );

        middle.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        245
                )
        );

        content.add(
                middle
        );

        content.add(
                Box.createVerticalStrut(
                        12
                )
        );

        content.add(
                createIncidentSection()
        );

        add(
                content,
                BorderLayout.CENTER
        );
    }

    private JPanel createControlsPanel() {
        RoundedPanel card =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        card.setLayout(
                new BorderLayout(
                        12,
                        0
                )
        );

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        9,
                        12,
                        9,
                        12
                )
        );

        JPanel settings =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                9,
                                0
                        )
                );

        settings.setOpaque(false);

        settings.add(
                createFieldLabel(
                        "Service"
                )
        );

        settings.add(
                serviceBox
        );

        settings.add(
                createFieldLabel(
                        "Port"
                )
        );

        settings.add(
                portField
        );

        settings.add(
                createFieldLabel(
                        "Every"
                )
        );

        settings.add(
                intervalSpinner
        );

        settings.add(
                createUnitLabel(
                        "sec"
                )
        );

        settings.add(
                createFieldLabel(
                        "Fail after"
                )
        );

        settings.add(
                failureThresholdSpinner
        );

        settings.add(
                createUnitLabel(
                        "checks"
                )
        );

        settings.add(
                createFieldLabel(
                        "Recover after"
                )
        );

        settings.add(
                recoveryThresholdSpinner
        );

        settings.add(
                createUnitLabel(
                        "checks"
                )
        );

        JPanel actions =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        actions.setOpaque(false);

        actions.add(
                startButton
        );

        actions.add(
                stopButton
        );

        card.add(
                settings,
                BorderLayout.CENTER
        );

        card.add(
                actions,
                BorderLayout.EAST
        );

        return card;
    }

    private RoundedPanel createMetricCard(
            String title,
            JLabel value,
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
                new JLabel(
                        title
                );

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        titleLabel.setForeground(
                TEXT_SECONDARY
        );

        JLabel subtitleLabel =
                new JLabel(
                        subtitle
                );

        subtitleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        subtitleLabel.setForeground(
                TEXT_SECONDARY
        );

        titleLabel.setAlignmentX(
                LEFT_ALIGNMENT
        );

        value.setAlignmentX(
                LEFT_ALIGNMENT
        );

        subtitleLabel.setAlignmentX(
                LEFT_ALIGNMENT
        );

        card.add(
                titleLabel
        );

        card.add(
                Box.createVerticalStrut(
                        7
                )
        );

        card.add(
                value
        );

        card.add(
                Box.createVerticalStrut(
                        4
                )
        );

        card.add(
                subtitleLabel
        );

        return card;
    }

    private RoundedPanel createLatencyCard() {
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
                                13,
                                15,
                                13,
                                15
                        )
                )
        );

        JLabel title =
                new JLabel(
                        "LIVE LATENCY"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        title.setForeground(
                ACCENT
        );

        card.add(
                title,
                BorderLayout.NORTH
        );

        card.add(
                latencyGraph,
                BorderLayout.CENTER
        );

        return card;
    }

    private RoundedPanel createSessionStatisticsCard() {
        RoundedPanel card =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        card.setLayout(
                new BorderLayout(
                        0,
                        10
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

        JLabel title =
                new JLabel(
                        "SESSION STATISTICS"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        title.setForeground(
                ACCENT
        );

        JPanel grid =
                new JPanel(
                        new GridLayout(
                                3,
                                2,
                                10,
                                10
                        )
                );

        grid.setOpaque(false);

        grid.add(
                createMiniMetric(
                        "Average latency",
                        averageLatencyLabel
                )
        );

        grid.add(
                createMiniMetric(
                        "Minimum latency",
                        minimumLatencyLabel
                )
        );

        grid.add(
                createMiniMetric(
                        "Maximum latency",
                        maximumLatencyLabel
                )
        );

        grid.add(
                createMiniMetric(
                        "Successful checks",
                        successfulChecksLabel
                )
        );

        grid.add(
                createMiniMetric(
                        "Failed checks",
                        failedChecksLabel
                )
        );

        grid.add(
                createMiniMetric(
                        "Elapsed",
                        elapsedLabel
                )
        );

        activeTargetLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        activeTargetLabel.setForeground(
                TEXT_SECONDARY
        );

        card.add(
                title,
                BorderLayout.NORTH
        );

        card.add(
                grid,
                BorderLayout.CENTER
        );

        card.add(
                activeTargetLabel,
                BorderLayout.SOUTH
        );

        return card;
    }

    private JPanel createMiniMetric(
            String title,
            JLabel value
    ) {
        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                0,
                                4
                        )
                );

        panel.setOpaque(false);

        JLabel label =
                new JLabel(
                        title
                );

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        label.setForeground(
                TEXT_SECONDARY
        );

        panel.add(
                label,
                BorderLayout.NORTH
        );

        panel.add(
                value,
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createIncidentSection() {
        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                0,
                                8
                        )
                );

        panel.setOpaque(false);

        JPanel heading =
                new JPanel(
                        new BorderLayout()
                );

        heading.setOpaque(false);

        JLabel title =
                new JLabel(
                        "INCIDENT HISTORY"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        title.setForeground(
                TEXT_PRIMARY
        );

        heading.add(
                title,
                BorderLayout.WEST
        );

        heading.add(
                clearIncidentsButton,
                BorderLayout.EAST
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        incidentTable
                );

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        BORDER_COLOR,
                        1,
                        true
                )
        );

        scrollPane.getViewport()
                .setBackground(
                        INPUT_BACKGROUND
                );

        panel.add(
                heading,
                BorderLayout.NORTH
        );

        panel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        return panel;
    }

    private JLabel createFieldLabel(
            String text
    ) {
        JLabel label =
                new JLabel(
                        text + ":"
                );

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        label.setForeground(
                TEXT_SECONDARY
        );

        return label;
    }

    private JLabel createUnitLabel(
            String text
    ) {
        JLabel label =
                new JLabel(
                        text
                );

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        label.setForeground(
                TEXT_SECONDARY
        );

        return label;
    }

    private static JLabel createValueLabel(
            String text
    ) {
        JLabel label =
                new JLabel(
                        text
                );

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18
                )
        );

        label.setForeground(
                TEXT_PRIMARY
        );

        return label;
    }

    private static JLabel createSmallMetricValue(
            String text
    ) {
        JLabel label =
                new JLabel(
                        text
                );

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        label.setForeground(
                TEXT_PRIMARY
        );

        return label;
    }

    private void registerListeners() {
        serviceBox.addActionListener(
                event -> updatePortFromService()
        );

        startButton.addActionListener(
                event -> startMonitoring()
        );

        stopButton.addActionListener(
                event -> stopMonitoring()
        );

        clearIncidentsButton.addActionListener(
                event -> incidentModel.setRowCount(
                        0
                )
        );
    }

    private void updatePortFromService() {
        ServicePreset selected =
                (ServicePreset)
                        serviceBox
                                .getSelectedItem();

        if (selected == null) {
            return;
        }

        if (selected.isCustom()) {
            portField.setEditable(
                    true
            );

            portField.setBackground(
                    INPUT_BACKGROUND
            );

        } else {
            portField.setText(
                    String.valueOf(
                            selected.getPort()
                    )
            );

            portField.setEditable(
                    false
            );

            portField.setBackground(
                    INPUT_DISABLED
            );
        }
    }

    private void startMonitoring() {
        if (session != null
                && session.isRunning()) {
            return;
        }

        String host =
                targetSupplier
                        .get()
                        .trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a target hostname or IP address at the top of NetAssist."
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

            if (port < 1
                    || port > 65_535) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a TCP port between 1 and 65535."
            );

            return;
        }

        int intervalSeconds =
                (Integer)
                        intervalSpinner
                                .getValue();

        int failureThreshold =
                (Integer)
                        failureThresholdSpinner
                                .getValue();

        int recoveryThreshold =
                (Integer)
                        recoveryThresholdSpinner
                                .getValue();

        resetSessionVisuals();

        ServicePreset selected =
                (ServicePreset)
                        serviceBox
                                .getSelectedItem();

        String serviceName =
                selected == null
                        ? "Custom"
                        : selected.toString();

        activeTargetLabel.setText(
                "Monitoring "
                        + host
                        + ":"
                        + port
                        + "  •  "
                        + serviceName
        );

        session =
                new MonitoringSession(
                        diagnostics,
                        host,
                        port,
                        intervalSeconds,
                        failureThreshold,
                        recoveryThreshold,
                        new MonitoringSession.Listener() {

                            @Override
                            public void onCheck(
                                    MonitorResult result,
                                    MonitoringStats stats
                            ) {
                                SwingUtilities.invokeLater(
                                        () -> updateFromCheck(
                                                result,
                                                stats
                                        )
                                );
                            }

                            @Override
                            public void onIncident(
                                    MonitorIncident incident
                            ) {
                                SwingUtilities.invokeLater(
                                        () -> addIncident(
                                                incident
                                        )
                                );
                            }

                            @Override
                            public void onStopped(
                                    MonitoringStats finalStats
                            ) {
                                SwingUtilities.invokeLater(
                                        () -> updateStoppedState(
                                                finalStats
                                        )
                                );
                            }
                        }
                );

        setControlsRunning(
                true
        );

        elapsedTimer.start();

        session.start();
    }

    public void stopMonitoring() {
        if (session == null) {
            return;
        }

        session.stop();

        elapsedTimer.stop();

        setControlsRunning(
                false
        );
    }

    private void setControlsRunning(
            boolean running
    ) {
        startButton.setEnabled(
                !running
        );

        stopButton.setEnabled(
                running
        );

        serviceBox.setEnabled(
                !running
        );

        portField.setEnabled(
                !running
        );

        intervalSpinner.setEnabled(
                !running
        );

        failureThresholdSpinner.setEnabled(
                !running
        );

        recoveryThresholdSpinner.setEnabled(
                !running
        );
    }

    private void updateFromCheck(
            MonitorResult result,
            MonitoringStats stats
    ) {
        updateStateLabel(
                stats.state()
        );

        uptimeValueLabel.setText(
                String.format(
                        "%.2f%%",
                        stats.uptimePercent()
                )
        );

        uptimeValueLabel.setForeground(
                stats.uptimePercent() >= 99.0
                        ? SUCCESS
                        : stats.uptimePercent() >= 95.0
                                ? WARNING
                                : ERROR
        );

        responseValueLabel.setText(
                result.online()
                        ? result.responseTimeMs()
                                + " ms"
                        : "-- ms"
        );

        responseValueLabel.setForeground(
                result.online()
                        ? SUCCESS
                        : WARNING
        );

        checksValueLabel.setText(
                String.valueOf(
                        stats.totalChecks()
                )
        );

        averageLatencyLabel.setText(
                stats.successfulChecks() == 0
                        ? "-- ms"
                        : String.format(
                                "%.1f ms",
                                stats.averageLatencyMs()
                        )
        );

        minimumLatencyLabel.setText(
                stats.successfulChecks() == 0
                        ? "-- ms"
                        : stats.minimumLatencyMs()
                                + " ms"
        );

        maximumLatencyLabel.setText(
                stats.successfulChecks() == 0
                        ? "-- ms"
                        : stats.maximumLatencyMs()
                                + " ms"
        );

        successfulChecksLabel.setText(
                String.valueOf(
                        stats.successfulChecks()
                )
        );

        successfulChecksLabel.setForeground(
                SUCCESS
        );

        failedChecksLabel.setText(
                String.valueOf(
                        stats.failedChecks()
                )
        );

        failedChecksLabel.setForeground(
                stats.failedChecks() == 0
                        ? TEXT_PRIMARY
                        : ERROR
        );

        latencyGraph.addResult(
                result
        );

        updateElapsedTime();
    }

    private void updateStateLabel(
            MonitoringSession.State state
    ) {
        statusValueLabel.setText(
                state.toString()
        );

        switch (state) {

            case ONLINE -> {
                statusValueLabel.setForeground(
                        SUCCESS
                );
            }

            case DEGRADED -> {
                statusValueLabel.setForeground(
                        WARNING
                );
            }

            case OFFLINE -> {
                statusValueLabel.setForeground(
                        ERROR
                );
            }

            case STARTING -> {
                statusValueLabel.setForeground(
                        PRIMARY_HOVER
                );
            }

            case STOPPED -> {
                statusValueLabel.setForeground(
                        TEXT_SECONDARY
                );
            }
        }
    }

    private void addIncident(
            MonitorIncident incident
    ) {
        String duration =
                incident.type()
                        == MonitorIncident.Type.RECOVERY
                        ? incident.durationSeconds()
                                + " sec"
                        : "--";

        incidentModel.addRow(
                new Object[]{
                        incident.timestamp()
                                .format(
                                        TIME_FORMAT
                                ),
                        incident.type(),
                        incident.message(),
                        duration
                }
        );

        int lastRow =
                incidentModel.getRowCount()
                        - 1;

        if (lastRow >= 0) {
            incidentTable.scrollRectToVisible(
                    incidentTable.getCellRect(
                            lastRow,
                            0,
                            true
                    )
            );
        }
    }

    private void updateElapsedTime() {
        if (session == null) {
            elapsedLabel.setText(
                    "00:00:00"
            );

            return;
        }

        MonitoringStats stats =
                session.getLastStats();

        if (stats == null
                || stats.startedAt() == null) {

            elapsedLabel.setText(
                    "00:00:00"
            );

            return;
        }

        Duration duration =
                Duration.between(
                        stats.startedAt(),
                        LocalDateTime.now()
                );

        long seconds =
                Math.max(
                        0,
                        duration.getSeconds()
                );

        long hours =
                seconds / 3600;

        long minutes =
                (seconds % 3600)
                        / 60;

        long remainingSeconds =
                seconds % 60;

        elapsedLabel.setText(
                String.format(
                        "%02d:%02d:%02d",
                        hours,
                        minutes,
                        remainingSeconds
                )
        );
    }

    private void updateStoppedState(
            MonitoringStats finalStats
    ) {
        updateStateLabel(
                MonitoringSession.State.STOPPED
        );

        setControlsRunning(
                false
        );

        elapsedTimer.stop();

        if (finalStats != null) {
            uptimeValueLabel.setText(
                    String.format(
                            "%.2f%%",
                            finalStats.uptimePercent()
                    )
            );
        }
    }

    private void resetSessionVisuals() {
        statusValueLabel.setText(
                "STARTING"
        );

        statusValueLabel.setForeground(
                PRIMARY_HOVER
        );

        uptimeValueLabel.setText(
                "--"
        );

        uptimeValueLabel.setForeground(
                TEXT_PRIMARY
        );

        responseValueLabel.setText(
                "-- ms"
        );

        responseValueLabel.setForeground(
                TEXT_PRIMARY
        );

        checksValueLabel.setText(
                "0"
        );

        averageLatencyLabel.setText(
                "-- ms"
        );

        minimumLatencyLabel.setText(
                "-- ms"
        );

        maximumLatencyLabel.setText(
                "-- ms"
        );

        successfulChecksLabel.setText(
                "0"
        );

        failedChecksLabel.setText(
                "0"
        );

        elapsedLabel.setText(
                "00:00:00"
        );

        latencyGraph.clear();
    }

    @Override
    public void removeNotify() {
        stopMonitoring();

        super.removeNotify();
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

            setOpaque(
                    false
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
            super(
                    text
            );

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

            setFocusPainted(
                    false
            );

            setBorderPainted(
                    false
            );

            setContentAreaFilled(
                    false
            );

            setOpaque(
                    false
            );

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

            Color fill;

            if (!isEnabled()) {
                fill =
                        new Color(
                                71,
                                85,
                                105
                        );

            } else if (hovered) {
                fill =
                        hoverColor;

            } else {
                fill =
                        normalColor;
            }

            g2.setColor(
                    fill
            );

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    10,
                    10
            );

            g2.dispose();

            super.paintComponent(
                    graphics
            );
        }
    }

    private static final class LatencyGraphPanel
            extends JPanel {

        private static final int MAX_POINTS =
                60;

        private final Deque<MonitorResult> results =
                new ArrayDeque<>();

        private LatencyGraphPanel() {
            setOpaque(
                    false
            );

            setPreferredSize(
                    new Dimension(
                            300,
                            165
                    )
            );
        }

        private void addResult(
                MonitorResult result
        ) {
            results.addLast(
                    result
            );

            while (results.size()
                    > MAX_POINTS) {

                results.removeFirst();
            }

            repaint();
        }

        private void clear() {
            results.clear();
            repaint();
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {
            super.paintComponent(
                    graphics
            );

            Graphics2D g2 =
                    (Graphics2D)
                            graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width =
                    getWidth();

            int height =
                    getHeight();

            int left = 42;
            int right = 12;
            int top = 14;
            int bottom = 24;

            int graphWidth =
                    Math.max(
                            1,
                            width - left - right
                    );

            int graphHeight =
                    Math.max(
                            1,
                            height - top - bottom
                    );

            g2.setColor(
                    BORDER_COLOR
            );

            g2.setStroke(
                    new BasicStroke(
                            1f
                    )
            );

            for (
                    int i = 0;
                    i <= 4;
                    i++
            ) {
                int y =
                        top
                                + graphHeight
                                * i
                                / 4;

                g2.drawLine(
                        left,
                        y,
                        left + graphWidth,
                        y
                );
            }

            if (results.isEmpty()) {
                g2.setColor(
                        TEXT_SECONDARY
                );

                g2.setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                11
                        )
                );

                g2.drawString(
                        "Latency samples will appear here.",
                        left + 12,
                        top + 24
                );

                g2.dispose();

                return;
            }

            long maxLatency =
                    100;

            for (MonitorResult result : results) {
                if (result.online()) {
                    maxLatency =
                            Math.max(
                                    maxLatency,
                                    result.responseTimeMs()
                            );
                }
            }

            maxLatency =
                    ((maxLatency + 49) / 50)
                            * 50;

            g2.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            9
                    )
            );

            g2.setColor(
                    TEXT_SECONDARY
            );

            g2.drawString(
                    maxLatency + " ms",
                    1,
                    top + 4
            );

            g2.drawString(
                    "0 ms",
                    11,
                    top + graphHeight
            );

            MonitorResult[] points =
                    results.toArray(
                            new MonitorResult[0]
                    );

            double stepX =
                    points.length <= 1
                            ? 0
                            : graphWidth
                                    * 1.0
                                    / (points.length - 1);

            Integer previousX = null;
            Integer previousY = null;

            for (
                    int i = 0;
                    i < points.length;
                    i++
            ) {
                MonitorResult result =
                        points[i];

                int x =
                        (int) Math.round(
                                left
                                        + i
                                        * stepX
                        );

                if (!result.online()) {
                    g2.setColor(
                            MonitoringPanel.ERROR
                    );

                    int y =
                            top + graphHeight;

                    g2.fillOval(
                            x - 3,
                            y - 3,
                            6,
                            6
                    );

                    previousX = null;
                    previousY = null;

                    continue;
                }

                int y =
                        top
                                + graphHeight
                                - (int) Math.round(
                                        result.responseTimeMs()
                                                * graphHeight
                                                * 1.0
                                                / maxLatency
                                );

                if (previousX != null
                        && previousY != null) {

                    g2.setColor(
                            ACCENT
                    );

                    g2.setStroke(
                            new BasicStroke(
                                    2f,
                                    BasicStroke.CAP_ROUND,
                                    BasicStroke.JOIN_ROUND
                            )
                    );

                    g2.drawLine(
                            previousX,
                            previousY,
                            x,
                            y
                    );
                }

                g2.setColor(
                        SUCCESS
                );

                g2.fillOval(
                        x - 3,
                        y - 3,
                        6,
                        6
                );

                previousX = x;
                previousY = y;
            }

            g2.dispose();
        }
    }
}
