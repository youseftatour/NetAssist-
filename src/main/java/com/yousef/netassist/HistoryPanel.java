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
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.IOException;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public final class HistoryPanel extends JPanel {

    private enum ViewMode {
        INCIDENTS,
        SESSIONS
    }

    private static final Color APP_BACKGROUND = new Color(15, 23, 42);
    private static final Color SURFACE = new Color(24, 34, 53);
    private static final Color INPUT_BACKGROUND = new Color(17, 27, 45);

    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_HOVER = new Color(59, 130, 246);
    private static final Color ACCENT = new Color(20, 184, 166);

    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color ERROR = new Color(239, 68, 68);

    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);

    private static final Color SECONDARY_BUTTON = new Color(51, 65, 85);
    private static final Color SECONDARY_BUTTON_HOVER = new Color(71, 85, 105);

    private final MonitoringHistoryStore store;

    private final JButton incidentsButton =
            createPrimaryButton("Incidents");

    private final JButton sessionsButton =
            createSecondaryButton("Sessions");

    private final JButton refreshButton =
            createSecondaryButton("Refresh");

    private final JButton exportButton =
            createPrimaryButton("Export CSV");

    private final JTextField searchField =
            new JTextField(22);

    private final JLabel incidentsCountLabel =
            createMetricValue("0");

    private final JLabel outagesCountLabel =
            createMetricValue("0");

    private final JLabel recoveriesCountLabel =
            createMetricValue("0");

    private final JLabel sessionsCountLabel =
            createMetricValue("0");

    private final HistoryTableModel model =
            new HistoryTableModel();

    private final JTable table =
            new JTable(model);

    private final TableRowSorter<HistoryTableModel> sorter =
            new TableRowSorter<>(model);

    private ViewMode mode =
            ViewMode.INCIDENTS;

    private List<HistoricalIncident> incidents =
            List.of();

    private List<MonitoringSessionRecord> sessions =
            List.of();

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public HistoryPanel(
            MonitoringHistoryStore store
    ) {
        this.store = store;

        setBackground(APP_BACKGROUND);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 2, 2, 2));

        configureSearch();
        configureTable();
        createLayout();
        registerListeners();

        refresh();
    }

    private void configureSearch() {
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(TEXT_PRIMARY);
        searchField.setBackground(INPUT_BACKGROUND);

        searchField.setBorder(
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
    }

    private void configureTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(29);
        table.setBackground(INPUT_BACKGROUND);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setRowSorter(sorter);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        table.getTableHeader().setBackground(SURFACE);
        table.getTableHeader().setForeground(ACCENT);
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(
                Object.class,
                new HistoryRenderer()
        );
    }

    private void createLayout() {
        JPanel top =
                new JPanel();

        top.setOpaque(false);
        top.setLayout(
                new BoxLayout(
                        top,
                        BoxLayout.Y_AXIS
                )
        );

        top.add(createToolbar());
        top.add(Box.createVerticalStrut(10));
        top.add(createSummaryCards());

        add(top, BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
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

        left.setOpaque(false);

        left.add(incidentsButton);
        left.add(sessionsButton);

        JPanel right =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        right.setOpaque(false);

        JLabel searchLabel =
                new JLabel("Search:");

        searchLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        searchLabel.setForeground(TEXT_SECONDARY);

        right.add(searchLabel);
        right.add(searchField);
        right.add(refreshButton);
        right.add(exportButton);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

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

        cards.setOpaque(false);

        cards.add(
                createMetricCard(
                        "INCIDENT EVENTS",
                        incidentsCountLabel,
                        "Persistent outage and recovery records"
                )
        );

        cards.add(
                createMetricCard(
                        "OUTAGES",
                        outagesCountLabel,
                        "Confirmed service-down events"
                )
        );

        cards.add(
                createMetricCard(
                        "RECOVERIES",
                        recoveriesCountLabel,
                        "Services that returned online"
                )
        );

        cards.add(
                createMetricCard(
                        "MONITORING SESSIONS",
                        sessionsCountLabel,
                        "Completed monitoring runs"
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

    private JPanel createTableCard() {
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
                        "PERSISTENT MONITORING HISTORY"
                );

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        title.setForeground(ACCENT);

        JScrollPane scrollPane =
                new JScrollPane(table);

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

        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

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
                new JLabel(title);

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel subtitleLabel =
                new JLabel(subtitle);

        subtitleLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 9)
        );

        subtitleLabel.setForeground(TEXT_SECONDARY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(value);
        card.add(Box.createVerticalStrut(2));
        card.add(subtitleLabel);

        return card;
    }

    private void registerListeners() {
        incidentsButton.addActionListener(
                event -> setMode(ViewMode.INCIDENTS)
        );

        sessionsButton.addActionListener(
                event -> setMode(ViewMode.SESSIONS)
        );

        refreshButton.addActionListener(
                event -> refresh()
        );

        exportButton.addActionListener(
                event -> exportCurrentView()
        );

        searchField.getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                            @Override
                            public void insertUpdate(
                                    DocumentEvent event
                            ) {
                                applyFilter();
                            }

                            @Override
                            public void removeUpdate(
                                    DocumentEvent event
                            ) {
                                applyFilter();
                            }

                            @Override
                            public void changedUpdate(
                                    DocumentEvent event
                            ) {
                                applyFilter();
                            }
                        }
                );
    }

    public void refresh() {
        try {
            incidents =
                    store.loadIncidents()
                            .stream()
                            .sorted(
                                    Comparator.comparing(
                                            HistoricalIncident::timestamp
                                    ).reversed()
                            )
                            .toList();

            sessions =
                    store.loadSessions()
                            .stream()
                            .sorted(
                                    Comparator.comparing(
                                            MonitoringSessionRecord::endedAt
                                    ).reversed()
                            )
                            .toList();

            updateSummary();
            model.setData(
                    mode,
                    incidents,
                    sessions
            );

            applyFilter();

        } catch (IOException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load monitoring history.\n\n"
                            + exception.getMessage(),
                    "History Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setMode(
            ViewMode mode
    ) {
        this.mode = mode;

        incidentsButton.setEnabled(
                mode != ViewMode.INCIDENTS
        );

        sessionsButton.setEnabled(
                mode != ViewMode.SESSIONS
        );

        model.setData(
                mode,
                incidents,
                sessions
        );

        applyFilter();
    }

    private void updateSummary() {
        long outages =
                incidents.stream()
                        .filter(
                                incident ->
                                        incident.type()
                                                == MonitorIncident.Type.OUTAGE
                        )
                        .count();

        long recoveries =
                incidents.size()
                        - outages;

        incidentsCountLabel.setText(
                String.valueOf(
                        incidents.size()
                )
        );

        outagesCountLabel.setText(
                String.valueOf(
                        outages
                )
        );

        outagesCountLabel.setForeground(
                outages > 0
                        ? ERROR
                        : TEXT_PRIMARY
        );

        recoveriesCountLabel.setText(
                String.valueOf(
                        recoveries
                )
        );

        recoveriesCountLabel.setForeground(
                recoveries > 0
                        ? SUCCESS
                        : TEXT_PRIMARY
        );

        sessionsCountLabel.setText(
                String.valueOf(
                        sessions.size()
                )
        );
    }

    private void applyFilter() {
        String query =
                searchField
                        .getText()
                        .trim();

        if (query.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        sorter.setRowFilter(
                RowFilter.regexFilter(
                        "(?i)"
                                + Pattern.quote(
                                        query
                                )
                )
        );
    }

    private void exportCurrentView() {
        ExportUtils.exportCsv(
                this,
                mode == ViewMode.INCIDENTS
                        ? "netassist-incidents.csv"
                        : "netassist-monitoring-sessions.csv",
                model.headers(),
                model.rowsForExport()
        );
    }

    private static JLabel createMetricValue(
            String text
    ) {
        JLabel label =
                new JLabel(text);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );

        label.setForeground(TEXT_PRIMARY);

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

    private static final class HistoryTableModel
            extends AbstractTableModel {

        private ViewMode mode =
                ViewMode.INCIDENTS;

        private List<HistoricalIncident> incidents =
                List.of();

        private List<MonitoringSessionRecord> sessions =
                List.of();

        private void setData(
                ViewMode mode,
                List<HistoricalIncident> incidents,
                List<MonitoringSessionRecord> sessions
        ) {
            this.mode = mode;
            this.incidents = List.copyOf(incidents);
            this.sessions = List.copyOf(sessions);

            fireTableStructureChanged();
        }

        private List<String> headers() {
            return mode == ViewMode.INCIDENTS
                    ? List.of(
                            "Time",
                            "Target",
                            "Host",
                            "Service",
                            "Event",
                            "Duration",
                            "Details"
                    )
                    : List.of(
                            "Started",
                            "Ended",
                            "Target",
                            "Host",
                            "Service",
                            "Checks",
                            "Uptime",
                            "Avg Latency",
                            "Min",
                            "Max"
                    );
        }

        private List<List<String>> rowsForExport() {
            List<List<String>> rows =
                    new ArrayList<>();

            for (
                    int row = 0;
                    row < getRowCount();
                    row++
            ) {
                List<String> values =
                        new ArrayList<>();

                for (
                        int column = 0;
                        column < getColumnCount();
                        column++
                ) {
                    values.add(
                            String.valueOf(
                                    getValueAt(
                                            row,
                                            column
                                    )
                            )
                    );
                }

                rows.add(values);
            }

            return rows;
        }

        @Override
        public int getRowCount() {
            return mode == ViewMode.INCIDENTS
                    ? incidents.size()
                    : sessions.size();
        }

        @Override
        public int getColumnCount() {
            return headers().size();
        }

        @Override
        public String getColumnName(
                int column
        ) {
            return headers().get(column);
        }

        @Override
        public Object getValueAt(
                int rowIndex,
                int columnIndex
        ) {
            if (mode == ViewMode.INCIDENTS) {
                HistoricalIncident incident =
                        incidents.get(rowIndex);

                return switch (columnIndex) {
                    case 0 ->
                            incident.timestamp()
                                    .format(
                                            DATE_TIME_FORMAT
                                    );

                    case 1 ->
                            incident.targetName();

                    case 2 ->
                            incident.host()
                                    + ":"
                                    + incident.port();

                    case 3 ->
                            incident.service();

                    case 4 ->
                            incident.type();

                    case 5 ->
                            incident.type()
                                    == MonitorIncident.Type.RECOVERY
                                    ? incident.durationSeconds()
                                            + " sec"
                                    : "--";

                    case 6 ->
                            incident.message();

                    default ->
                            "";
                };
            }

            MonitoringSessionRecord session =
                    sessions.get(rowIndex);

            Duration duration =
                    Duration.between(
                            session.startedAt(),
                            session.endedAt()
                    );

            return switch (columnIndex) {
                case 0 ->
                        session.startedAt()
                                .format(
                                        DATE_TIME_FORMAT
                                );

                case 1 ->
                        session.endedAt()
                                .format(
                                        DATE_TIME_FORMAT
                                )
                                + " ("
                                + formatDuration(
                                        duration
                                )
                                + ")";

                case 2 ->
                        session.targetName();

                case 3 ->
                        session.host()
                                + ":"
                                + session.port();

                case 4 ->
                        session.service();

                case 5 ->
                        session.totalChecks();

                case 6 ->
                        String.format(
                                "%.2f%%",
                                session.uptimePercent()
                        );

                case 7 ->
                        String.format(
                                "%.1f ms",
                                session.averageLatencyMs()
                        );

                case 8 ->
                        session.minimumLatencyMs()
                                + " ms";

                case 9 ->
                        session.maximumLatencyMs()
                                + " ms";

                default ->
                        "";
            };
        }

        private static String formatDuration(
                Duration duration
        ) {
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

            long remaining =
                    seconds % 60;

            return String.format(
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    remaining
            );
        }
    }

    private static final class HistoryRenderer
            extends DefaultTableCellRenderer {

        @Override
        public java.awt.Component getTableCellRendererComponent(
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
                            Font.PLAIN,
                            11
                    )
            );

            if (!isSelected) {
                label.setBackground(INPUT_BACKGROUND);
                label.setForeground(TEXT_PRIMARY);
            }

            return label;
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
            this.backgroundColor = backgroundColor;
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

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    arc,
                    arc
            );

            g2.dispose();

            super.paintComponent(graphics);
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
            super(text);

            this.normalColor = normalColor;
            this.hoverColor = hoverColor;

            setFont(
                    new Font("Segoe UI", Font.BOLD, 11)
            );

            setForeground(foreground);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

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
                    (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    isEnabled()
                            ? hovered
                                    ? hoverColor
                                    : normalColor
                            : new Color(45, 55, 72)
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

            super.paintComponent(graphics);
        }
    }
}
