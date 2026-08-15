package com.yousef.netassist;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import javax.swing.event.ListSelectionEvent;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

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
import java.awt.Window;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MonitoringDashboardPanel
        extends JPanel {

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

    private final MonitoringManager manager;

    private final MonitoringTableModel tableModel =
            new MonitoringTableModel();

    private final JTable targetTable =
            new JTable(
                    tableModel
            );

    private final JButton addButton =
            createPrimaryButton(
                    "Add Target"
            );

    private final JButton editButton =
            createSecondaryButton(
                    "Edit"
            );

    private final JButton deleteButton =
            createSecondaryButton(
                    "Delete"
            );

    private final JButton startButton =
            createPrimaryButton(
                    "Start"
            );

    private final JButton stopButton =
            createSecondaryButton(
                    "Stop"
            );

    private final JButton startAllButton =
            createPrimaryButton(
                    "Start All"
            );

    private final JButton stopAllButton =
            createSecondaryButton(
                    "Stop All"
            );

    private final JLabel totalTargetsLabel =
            createMetricValue(
                    "0"
            );

    private final JLabel runningTargetsLabel =
            createMetricValue(
                    "0"
            );

    private final JLabel onlineTargetsLabel =
            createMetricValue(
                    "0"
            );

    private final JLabel activeIncidentsLabel =
            createMetricValue(
                    "0"
            );

    private final JLabel detailNameLabel =
            new JLabel(
                    "Select a monitoring target"
            );

    private final JLabel detailEndpointLabel =
            new JLabel(
                    "No target selected"
            );

    private final JLabel detailStatusLabel =
            createDetailValue(
                    "--"
            );

    private final JLabel detailLatencyLabel =
            createDetailValue(
                    "-- ms"
            );

    private final JLabel detailUptimeLabel =
            createDetailValue(
                    "--"
            );

    private final JLabel detailChecksLabel =
            createDetailValue(
                    "0"
            );

    private final JLabel detailAverageLabel =
            createDetailValue(
                    "-- ms"
            );

    private final JLabel detailRangeLabel =
            createDetailValue(
                    "-- / -- ms"
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

    private String selectedTargetId;

    private final MonitoringManager.Listener managerListener =
            new MonitoringManager.Listener() {

                @Override
                public void onTargetsChanged() {
                    SwingUtilities.invokeLater(
                            () -> refreshFromManager(
                                    true
                            )
                    );
                }

                @Override
                public void onPersistenceError(
                        String message
                ) {
                    SwingUtilities.invokeLater(
                            () -> JOptionPane.showMessageDialog(
                                    MonitoringDashboardPanel.this,
                                    message,
                                    "Monitoring Storage Warning",
                                    JOptionPane.WARNING_MESSAGE
                            )
                    );
                }
            };

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "HH:mm:ss"
            );

    public MonitoringDashboardPanel(
            NetworkDiagnostics diagnostics
    ) {
        this.manager =
                new MonitoringManager(
                        diagnostics
                );

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

        configureTargetTable();
        configureIncidentTable();
        createLayout();
        registerListeners();

        manager.addListener(
                managerListener
        );

        refreshFromManager(
                false
        );
    }

    private void configureTargetTable() {
        targetTable.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        targetTable.setRowHeight(
                30
        );

        targetTable.setBackground(
                INPUT_BACKGROUND
        );

        targetTable.setForeground(
                TEXT_PRIMARY
        );

        targetTable.setSelectionBackground(
                PRIMARY
        );

        targetTable.setSelectionForeground(
                Color.WHITE
        );

        targetTable.setGridColor(
                BORDER_COLOR
        );

        targetTable.setShowVerticalLines(
                false
        );

        targetTable.setFillsViewportHeight(
                true
        );

        targetTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        targetTable.setAutoCreateRowSorter(
                true
        );

        JTableHeader header =
                targetTable.getTableHeader();

        header.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
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

        targetTable.setDefaultRenderer(
                Object.class,
                new TargetTableRenderer()
        );

        int[] widths = {
                150,
                180,
                110,
                65,
                95,
                80,
                80,
                75,
                90
        };

        for (
                int i = 0;
                i < widths.length;
                i++
        ) {
            targetTable
                    .getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(
                            widths[i]
                    );
        }
    }

    private void configureIncidentTable() {
        incidentTable.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        incidentTable.setRowHeight(
                25
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

        header.setBackground(
                SURFACE
        );

        header.setForeground(
                ACCENT
        );

        header.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
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
                        7,
                        0,
                        7
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
    }

    private void createLayout() {
        JPanel top =
                new JPanel();

        top.setOpaque(
                false
        );

        top.setLayout(
                new BoxLayout(
                        top,
                        BoxLayout.Y_AXIS
                )
        );

        top.add(
                createToolbar()
        );

        top.add(
                Box.createVerticalStrut(
                        10
                )
        );

        top.add(
                createSummaryCards()
        );

        add(
                top,
                BorderLayout.NORTH
        );

        JPanel center =
                new JPanel(
                        new BorderLayout(
                                0,
                                10
                        )
                );

        center.setOpaque(
                false
        );

        center.add(
                createTargetTableCard(),
                BorderLayout.CENTER
        );

        center.add(
                createDetailsArea(),
                BorderLayout.SOUTH
        );

        add(
                center,
                BorderLayout.CENTER
        );
    }

    private JPanel createToolbar() {
        RoundedPanel panel =
                new RoundedPanel(
                        SURFACE,
                        14
                );

        panel.setLayout(
                new BorderLayout(
                        12,
                        0
                )
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        9,
                        12,
                        9,
                        12
                )
        );

        JPanel left =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                8,
                                0
                        )
                );

        left.setOpaque(
                false
        );

        left.add(
                addButton
        );

        left.add(
                editButton
        );

        left.add(
                deleteButton
        );

        left.add(
                Box.createHorizontalStrut(
                        8
                )
        );

        left.add(
                startButton
        );

        left.add(
                stopButton
        );

        JPanel right =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        right.setOpaque(
                false
        );

        right.add(
                startAllButton
        );

        right.add(
                stopAllButton
        );

        panel.add(
                left,
                BorderLayout.WEST
        );

        panel.add(
                right,
                BorderLayout.EAST
        );

        return panel;
    }

    private JPanel createSummaryCards() {
        JPanel cards =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                12,
                                0
                        )
                );

        cards.setOpaque(
                false
        );

        cards.add(
                createMetricCard(
                        "SAVED TARGETS",
                        totalTargetsLabel,
                        "Persistent monitoring profiles"
                )
        );

        cards.add(
                createMetricCard(
                        "RUNNING",
                        runningTargetsLabel,
                        "Active monitoring sessions"
                )
        );

        cards.add(
                createMetricCard(
                        "ONLINE",
                        onlineTargetsLabel,
                        "Targets currently reachable"
                )
        );

        cards.add(
                createMetricCard(
                        "ACTIVE INCIDENTS",
                        activeIncidentsLabel,
                        "Targets confirmed offline"
                )
        );

        cards.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        100
                )
        );

        return cards;
    }

    private JPanel createTargetTableCard() {
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
                                11,
                                12,
                                12,
                                12
                        )
                )
        );

        JLabel title =
                new JLabel(
                        "MONITORED SERVICES"
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

        JScrollPane scrollPane =
                new JScrollPane(
                        targetTable
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

        card.add(
                title,
                BorderLayout.NORTH
        );

        card.add(
                scrollPane,
                BorderLayout.CENTER
        );

        return card;
    }

    private JPanel createDetailsArea() {
        JPanel details =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                12,
                                0
                        )
                );

        details.setOpaque(
                false
        );

        details.setPreferredSize(
                new Dimension(
                        100,
                        250
                )
        );

        details.add(
                createSelectedTargetCard()
        );

        details.add(
                createLatencyCard()
        );

        details.add(
                createIncidentCard()
        );

        return details;
    }

    private JPanel createSelectedTargetCard() {
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

        JPanel heading =
                new JPanel();

        heading.setOpaque(
                false
        );

        heading.setLayout(
                new BoxLayout(
                        heading,
                        BoxLayout.Y_AXIS
                )
        );

        detailNameLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        detailNameLabel.setForeground(
                TEXT_PRIMARY
        );

        detailEndpointLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        detailEndpointLabel.setForeground(
                TEXT_SECONDARY
        );

        heading.add(
                detailNameLabel
        );

        heading.add(
                Box.createVerticalStrut(
                        3
                )
        );

        heading.add(
                detailEndpointLabel
        );

        JPanel metrics =
                new JPanel(
                        new GridLayout(
                                3,
                                2,
                                8,
                                8
                        )
                );

        metrics.setOpaque(
                false
        );

        metrics.add(
                createDetailMetric(
                        "Status",
                        detailStatusLabel
                )
        );

        metrics.add(
                createDetailMetric(
                        "Latency",
                        detailLatencyLabel
                )
        );

        metrics.add(
                createDetailMetric(
                        "Uptime",
                        detailUptimeLabel
                )
        );

        metrics.add(
                createDetailMetric(
                        "Checks",
                        detailChecksLabel
                )
        );

        metrics.add(
                createDetailMetric(
                        "Average",
                        detailAverageLabel
                )
        );

        metrics.add(
                createDetailMetric(
                        "Min / Max",
                        detailRangeLabel
                )
        );

        card.add(
                heading,
                BorderLayout.NORTH
        );

        card.add(
                metrics,
                BorderLayout.CENTER
        );

        return card;
    }

    private JPanel createLatencyCard() {
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
                        "SELECTED TARGET LATENCY"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
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

    private JPanel createIncidentCard() {
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
                        "INCIDENT HISTORY"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        title.setForeground(
                ACCENT
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

        card.add(
                title,
                BorderLayout.NORTH
        );

        card.add(
                scrollPane,
                BorderLayout.CENTER
        );

        return card;
    }

    private JPanel createMetricCard(
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
                                12,
                                14,
                                12,
                                14
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
                        9
                )
        );

        subtitleLabel.setForeground(
                TEXT_SECONDARY
        );

        card.add(
                titleLabel
        );

        card.add(
                Box.createVerticalStrut(
                        5
                )
        );

        card.add(
                value
        );

        card.add(
                Box.createVerticalStrut(
                        2
                )
        );

        card.add(
                subtitleLabel
        );

        return card;
    }

    private JPanel createDetailMetric(
            String title,
            JLabel value
    ) {
        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                0,
                                2
                        )
                );

        panel.setOpaque(
                false
        );

        JLabel label =
                new JLabel(
                        title
                );

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        9
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

    private void registerListeners() {
        addButton.addActionListener(
                event -> addTarget()
        );

        editButton.addActionListener(
                event -> editSelectedTarget()
        );

        deleteButton.addActionListener(
                event -> deleteSelectedTarget()
        );

        startButton.addActionListener(
                event -> startSelectedTarget()
        );

        stopButton.addActionListener(
                event -> stopSelectedTarget()
        );

        startAllButton.addActionListener(
                event -> manager.startAll()
        );

        stopAllButton.addActionListener(
                event -> manager.stopAll()
        );

        targetTable
                .getSelectionModel()
                .addListSelectionListener(
                        this::selectionChanged
                );

        targetTable.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent event
                    ) {
                        if (event.getClickCount() == 2
                                && event.getButton()
                                == MouseEvent.BUTTON1) {

                            editSelectedTarget();
                        }
                    }
                }
        );
    }

    private void addTarget() {
        Optional<MonitoringTarget> result =
                MonitoringTargetDialog.showDialog(
                        ownerWindow(),
                        null
                );

        result.ifPresent(
                manager::addTarget
        );
    }

    private void editSelectedTarget() {
        MonitoringTargetSnapshot snapshot =
                selectedSnapshot();

        if (snapshot == null) {
            return;
        }

        Optional<MonitoringTarget> result =
                MonitoringTargetDialog.showDialog(
                        ownerWindow(),
                        snapshot.target()
                );

        result.ifPresent(
                manager::updateTarget
        );
    }

    private void deleteSelectedTarget() {
        MonitoringTargetSnapshot snapshot =
                selectedSnapshot();

        if (snapshot == null) {
            return;
        }

        int answer =
                JOptionPane.showConfirmDialog(
                        this,
                        "Delete monitoring target \""
                                + snapshot
                                        .target()
                                        .name()
                                + "\"?\n\n"
                                + "Any active monitoring session for this target will be stopped.",
                        "Delete Monitoring Target",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (answer
                != JOptionPane.YES_OPTION) {
            return;
        }

        manager.removeTarget(
                snapshot
                        .target()
                        .id()
        );

        selectedTargetId =
                null;
    }

    private void startSelectedTarget() {
        MonitoringTargetSnapshot snapshot =
                selectedSnapshot();

        if (snapshot != null) {
            manager.startTarget(
                    snapshot
                            .target()
                            .id()
            );
        }
    }

    private void stopSelectedTarget() {
        MonitoringTargetSnapshot snapshot =
                selectedSnapshot();

        if (snapshot != null) {
            manager.stopTarget(
                    snapshot
                            .target()
                            .id()
            );
        }
    }

    private void selectionChanged(
            ListSelectionEvent event
    ) {
        if (event.getValueIsAdjusting()) {
            return;
        }

        int viewRow =
                targetTable.getSelectedRow();

        if (viewRow < 0) {
            selectedTargetId =
                    null;

            updateSelectedTargetDetails(
                    null
            );

            return;
        }

        int modelRow =
                targetTable.convertRowIndexToModel(
                        viewRow
                );

        MonitoringTargetSnapshot snapshot =
                tableModel.getSnapshotAt(
                        modelRow
                );

        selectedTargetId =
                snapshot
                        .target()
                        .id();

        updateSelectedTargetDetails(
                snapshot
        );

        updateActionStates(
                snapshot
        );
    }

    private MonitoringTargetSnapshot selectedSnapshot() {
        if (selectedTargetId == null) {
            return null;
        }

        return manager.snapshot(
                selectedTargetId
        );
    }

    private void refreshFromManager(
            boolean preserveSelection
    ) {
        String desiredSelection =
                preserveSelection
                        ? selectedTargetId
                        : null;

        List<MonitoringTargetSnapshot> snapshots =
                manager.snapshots();

        tableModel.setSnapshots(
                snapshots
        );

        updateSummary(
                snapshots
        );

        if (desiredSelection != null) {
            selectTargetById(
                    desiredSelection
            );
        }

        MonitoringTargetSnapshot selected =
                selectedSnapshot();

        if (selected != null) {
            updateSelectedTargetDetails(
                    selected
            );

            updateActionStates(
                    selected
            );

        } else {
            updateActionStates(
                    null
            );
        }
    }

    private void updateSummary(
            List<MonitoringTargetSnapshot> snapshots
    ) {
        int running = 0;
        int online = 0;
        int incidents = 0;

        for (MonitoringTargetSnapshot snapshot
                : snapshots) {

            if (snapshot.running()) {
                running++;
            }

            if (snapshot.state()
                    == MonitoringSession.State.ONLINE) {

                online++;
            }

            if (snapshot.activeIncident()) {
                incidents++;
            }
        }

        totalTargetsLabel.setText(
                String.valueOf(
                        snapshots.size()
                )
        );

        runningTargetsLabel.setText(
                String.valueOf(
                        running
                )
        );

        runningTargetsLabel.setForeground(
                running > 0
                        ? PRIMARY_HOVER
                        : TEXT_PRIMARY
        );

        onlineTargetsLabel.setText(
                String.valueOf(
                        online
                )
        );

        onlineTargetsLabel.setForeground(
                online > 0
                        ? SUCCESS
                        : TEXT_PRIMARY
        );

        activeIncidentsLabel.setText(
                String.valueOf(
                        incidents
                )
        );

        activeIncidentsLabel.setForeground(
                incidents > 0
                        ? ERROR
                        : TEXT_PRIMARY
        );
    }

    private void updateSelectedTargetDetails(
            MonitoringTargetSnapshot snapshot
    ) {
        incidentModel.setRowCount(
                0
        );

        latencyGraph.setResults(
                snapshot == null
                        ? List.of()
                        : snapshot.recentResults()
        );

        if (snapshot == null) {
            detailNameLabel.setText(
                    "Select a monitoring target"
            );

            detailEndpointLabel.setText(
                    "No target selected"
            );

            detailStatusLabel.setText(
                    "--"
            );

            detailStatusLabel.setForeground(
                    TEXT_PRIMARY
            );

            detailLatencyLabel.setText(
                    "-- ms"
            );

            detailUptimeLabel.setText(
                    "--"
            );

            detailChecksLabel.setText(
                    "0"
            );

            detailAverageLabel.setText(
                    "-- ms"
            );

            detailRangeLabel.setText(
                    "-- / -- ms"
            );

            return;
        }

        MonitoringTarget target =
                snapshot.target();

        MonitoringStats stats =
                snapshot.stats();

        MonitorResult result =
                snapshot.lastResult();

        detailNameLabel.setText(
                target.name()
        );

        detailEndpointLabel.setText(
                target.service()
                        + "  •  "
                        + target.endpoint()
                        + "  •  every "
                        + target.intervalSeconds()
                        + " sec"
        );

        detailStatusLabel.setText(
                displayState(
                        snapshot
                )
        );

        detailStatusLabel.setForeground(
                colorForState(
                        snapshot.state(),
                        snapshot.running()
                )
        );

        detailLatencyLabel.setText(
                result != null
                        && result.online()
                        ? result.responseTimeMs()
                                + " ms"
                        : "-- ms"
        );

        if (stats == null) {
            detailUptimeLabel.setText(
                    "--"
            );

            detailChecksLabel.setText(
                    "0"
            );

            detailAverageLabel.setText(
                    "-- ms"
            );

            detailRangeLabel.setText(
                    "-- / -- ms"
            );

        } else {
            detailUptimeLabel.setText(
                    String.format(
                            "%.2f%%",
                            stats.uptimePercent()
                    )
            );

            detailChecksLabel.setText(
                    String.valueOf(
                            stats.totalChecks()
                    )
            );

            detailAverageLabel.setText(
                    stats.successfulChecks() == 0
                            ? "-- ms"
                            : String.format(
                                    "%.1f ms",
                                    stats.averageLatencyMs()
                            )
            );

            detailRangeLabel.setText(
                    stats.successfulChecks() == 0
                            ? "-- / -- ms"
                            : stats.minimumLatencyMs()
                                    + " / "
                                    + stats.maximumLatencyMs()
                                    + " ms"
            );
        }

        for (MonitorIncident incident
                : snapshot.incidents()) {

            incidentModel.addRow(
                    new Object[]{
                            incident.timestamp()
                                    .format(
                                            TIME_FORMAT
                                    ),
                            incident.type(),
                            incident.message(),
                            incident.type()
                                    == MonitorIncident.Type.RECOVERY
                                    ? incident.durationSeconds()
                                            + " sec"
                                    : "--"
                    }
            );
        }
    }

    private void updateActionStates(
            MonitoringTargetSnapshot snapshot
    ) {
        boolean hasSelection =
                snapshot != null;

        editButton.setEnabled(
                hasSelection
        );

        deleteButton.setEnabled(
                hasSelection
        );

        startButton.setEnabled(
                hasSelection
                        && !snapshot.running()
        );

        stopButton.setEnabled(
                hasSelection
                        && snapshot.running()
        );

        boolean anyTargets =
                manager.targetCount()
                        > 0;

        startAllButton.setEnabled(
                anyTargets
        );

        stopAllButton.setEnabled(
                manager.runningCount()
                        > 0
        );
    }

    private void selectTargetById(
            String targetId
    ) {
        for (
                int modelRow = 0;
                modelRow
                        < tableModel.getRowCount();
                modelRow++
        ) {
            MonitoringTargetSnapshot snapshot =
                    tableModel.getSnapshotAt(
                            modelRow
                    );

            if (!snapshot
                    .target()
                    .id()
                    .equals(
                            targetId
                    )) {

                continue;
            }

            int viewRow =
                    targetTable.convertRowIndexToView(
                            modelRow
                    );

            if (viewRow >= 0) {
                targetTable.setRowSelectionInterval(
                        viewRow,
                        viewRow
                );
            }

            return;
        }
    }

    private Window ownerWindow() {
        return SwingUtilities.getWindowAncestor(
                this
        );
    }

    public void shutdown() {
        manager.removeListener(
                managerListener
        );

        manager.close();
    }

    private static String displayState(
            MonitoringTargetSnapshot snapshot
    ) {
        if (!snapshot.running()) {
            if (snapshot.stats() == null) {
                return "IDLE";
            }

            return "STOPPED";
        }

        return snapshot.state()
                .toString();
    }

    private static Color colorForState(
            MonitoringSession.State state,
            boolean running
    ) {
        if (!running) {
            return TEXT_SECONDARY;
        }

        return switch (state) {
            case ONLINE ->
                    SUCCESS;

            case DEGRADED ->
                    WARNING;

            case OFFLINE ->
                    ERROR;

            case STARTING ->
                    PRIMARY_HOVER;

            case STOPPED ->
                    TEXT_SECONDARY;
        };
    }

    private static JLabel createMetricValue(
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

    private static JLabel createDetailValue(
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
                        12
                )
        );

        label.setForeground(
                TEXT_PRIMARY
        );

        return label;
    }

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

    private final class TargetTableRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label =
                    (JLabel) super
                            .getTableCellRendererComponent(
                                    table,
                                    value,
                                    isSelected,
                                    hasFocus,
                                    row,
                                    column
                            );

            label.setBorder(
                    BorderFactory.createEmptyBorder(
                            0,
                            8,
                            0,
                            8
                    )
            );

            label.setFont(
                    new Font(
                            "Segoe UI",
                            column == 0
                                    ? Font.BOLD
                                    : Font.PLAIN,
                            11
                    )
            );

            if (isSelected) {
                label.setBackground(
                        PRIMARY
                );

                label.setForeground(
                        Color.WHITE
                );

                return label;
            }

            label.setBackground(
                    INPUT_BACKGROUND
            );

            int modelRow =
                    targetTable.convertRowIndexToModel(
                            row
                    );

            MonitoringTargetSnapshot snapshot =
                    tableModel.getSnapshotAt(
                            modelRow
                    );

            if (column == 4) {
                label.setForeground(
                        colorForState(
                                snapshot.state(),
                                snapshot.running()
                        )
                );

            } else {
                label.setForeground(
                        TEXT_PRIMARY
                );
            }

            return label;
        }
    }

    private static final class MonitoringTableModel
            extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "Name",
                "Host",
                "Service",
                "Port",
                "Status",
                "Latency",
                "Uptime",
                "Incidents",
                "Monitoring"
        };

        private List<MonitoringTargetSnapshot> snapshots =
                new ArrayList<>();

        private void setSnapshots(
                List<MonitoringTargetSnapshot> snapshots
        ) {
            this.snapshots =
                    new ArrayList<>(
                            snapshots
                    );

            fireTableDataChanged();
        }

        private MonitoringTargetSnapshot getSnapshotAt(
                int row
        ) {
            return snapshots.get(
                    row
            );
        }

        @Override
        public int getRowCount() {
            return snapshots.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(
                int column
        ) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(
                int rowIndex,
                int columnIndex
        ) {
            MonitoringTargetSnapshot snapshot =
                    snapshots.get(
                            rowIndex
                    );

            MonitoringTarget target =
                    snapshot.target();

            MonitoringStats stats =
                    snapshot.stats();

            MonitorResult result =
                    snapshot.lastResult();

            return switch (columnIndex) {

                case 0 ->
                        target.name();

                case 1 ->
                        target.host();

                case 2 ->
                        target.service();

                case 3 ->
                        target.port();

                case 4 ->
                        displayState(
                                snapshot
                        );

                case 5 ->
                        result != null
                                && result.online()
                                ? result.responseTimeMs()
                                        + " ms"
                                : "--";

                case 6 ->
                        stats == null
                                ? "--"
                                : String.format(
                                        "%.2f%%",
                                        stats.uptimePercent()
                                );

                case 7 ->
                        snapshot.incidentCount();

                case 8 ->
                        snapshot.running()
                                ? "RUNNING"
                                : "STOPPED";

                default ->
                        "";
            };
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
                Color foreground
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
                            11
                    )
            );

            setForeground(
                    foreground
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
                            7,
                            12,
                            7,
                            12
                    )
            );

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
                                45,
                                55,
                                72
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

        private List<MonitorResult> results =
                List.of();

        private LatencyGraphPanel() {
            setOpaque(
                    false
            );
        }

        private void setResults(
                List<MonitorResult> results
        ) {
            this.results =
                    List.copyOf(
                            results
                    );

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

            int left = 36;
            int right = 10;
            int top = 12;
            int bottom = 20;

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
                                10
                        )
                );

                g2.drawString(
                        "No latency samples yet.",
                        left + 8,
                        top + 22
                );

                g2.dispose();

                return;
            }

            long maxLatency =
                    100;

            for (MonitorResult result
                    : results) {

                if (result.online()) {
                    maxLatency =
                            Math.max(
                                    maxLatency,
                                    result.responseTimeMs()
                            );
                }
            }

            maxLatency =
                    ((maxLatency + 49)
                            / 50)
                            * 50;

            g2.setColor(
                    TEXT_SECONDARY
            );

            g2.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            8
                    )
            );

            g2.drawString(
                    maxLatency + " ms",
                    1,
                    top + 3
            );

            g2.drawString(
                    "0 ms",
                    8,
                    top + graphHeight
            );

            double stepX =
                    results.size() <= 1
                            ? 0
                            : graphWidth
                                    * 1.0
                                    / (results.size() - 1);

            Integer previousX =
                    null;

            Integer previousY =
                    null;

            for (
                    int i = 0;
                    i < results.size();
                    i++
            ) {
                MonitorResult result =
                        results.get(i);

                int x =
                        (int)
                                Math.round(
                                        left
                                                + i
                                                * stepX
                                );

                if (!result.online()) {
                    int y =
                            top + graphHeight;

                    g2.setColor(
                            MonitoringDashboardPanel.ERROR
                    );

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
                                - (int)
                                        Math.round(
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
