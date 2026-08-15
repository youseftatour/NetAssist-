package com.yousef.netassist;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import javax.swing.plaf.basic.BasicComboBoxUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Optional;

public final class MonitoringTargetDialog
        extends JDialog {

    private static final Color BACKGROUND =
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

    private final JTextField nameField =
            new JTextField(
                    22
            );

    private final JTextField hostField =
            new JTextField(
                    22
            );

    private final JComboBox<ServicePreset> serviceBox =
            new JComboBox<>(
                    ServicePreset.values()
            );

    private final JTextField portField =
            new JTextField(
                    "443",
                    8
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

    private final JSpinner failureSpinner =
            new JSpinner(
                    new SpinnerNumberModel(
                            3,
                            1,
                            20,
                            1
                    )
            );

    private final JSpinner recoverySpinner =
            new JSpinner(
                    new SpinnerNumberModel(
                            2,
                            1,
                            20,
                            1
                    )
            );

    private final JButton saveButton =
            new RoundedButton(
                    "Save Target",
                    PRIMARY,
                    PRIMARY_HOVER,
                    Color.WHITE
            );

    private final JButton cancelButton =
            new RoundedButton(
                    "Cancel",
                    SECONDARY_BUTTON,
                    SECONDARY_BUTTON_HOVER,
                    TEXT_PRIMARY
            );

    private MonitoringTarget result;
    private final MonitoringTarget original;

    private MonitoringTargetDialog(
            Window owner,
            MonitoringTarget original
    ) {
        super(
                owner,
                original == null
                        ? "Add Monitoring Target"
                        : "Edit Monitoring Target",
                ModalityType.APPLICATION_MODAL
        );

        this.original =
                original;

        configureWindow();
        configureInputs();
        createLayout();
        registerListeners();

        if (original != null) {
            populate(
                    original
            );
        } else {
            serviceBox.setSelectedItem(
                    ServicePreset.HTTPS
            );

            updatePortFromService();
        }
    }

    public static Optional<MonitoringTarget> showDialog(
            Window owner,
            MonitoringTarget target
    ) {
        MonitoringTargetDialog dialog =
                new MonitoringTargetDialog(
                        owner,
                        target
                );

        dialog.setVisible(
                true
        );

        return Optional.ofNullable(
                dialog.result
        );
    }

    private void configureWindow() {
        setDefaultCloseOperation(
                DISPOSE_ON_CLOSE
        );

        setSize(
                520,
                500
        );

        setResizable(
                false
        );

        getContentPane().setBackground(
                BACKGROUND
        );

        setLocationRelativeTo(
                getOwner()
        );
    }

    private void configureInputs() {
        styleTextField(
                nameField
        );

        styleTextField(
                hostField
        );

        styleTextField(
                portField
        );

        styleSpinner(
                intervalSpinner
        );

        styleSpinner(
                failureSpinner
        );

        styleSpinner(
                recoverySpinner
        );

        configureDarkComboBox();
    }

    private void configureDarkComboBox() {
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

        serviceBox.setFocusable(
                false
        );

        serviceBox.setOpaque(
                false
        );

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

                        label.setOpaque(
                                true
                        );

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

                        button.setFocusPainted(
                                false
                        );

                        button.setBorderPainted(
                                false
                        );

                        button.setContentAreaFilled(
                                false
                        );

                        button.setOpaque(
                                false
                        );

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

                        return button;
                    }
                }
        );

        serviceBox.setBorder(
                BorderFactory.createLineBorder(
                        BORDER_COLOR,
                        1,
                        true
                )
        );

        serviceBox.setBackground(
                INPUT_BACKGROUND
        );

        serviceBox.setForeground(
                TEXT_PRIMARY
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
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                8,
                                10,
                                8,
                                10
                        )
                )
        );
    }

    private void styleSpinner(
            JSpinner spinner
    ) {
        spinner.setPreferredSize(
                new Dimension(
                        90,
                        34
                )
        );

        JSpinner.DefaultEditor editor =
                (JSpinner.DefaultEditor)
                        spinner.getEditor();

        JTextField field =
                editor.getTextField();

        field.setHorizontalAlignment(
                JTextField.CENTER
        );

        styleTextField(
                field
        );
    }

    private void createLayout() {
        JPanel content =
                new JPanel(
                        new BorderLayout(
                                0,
                                18
                        )
                );

        content.setBackground(
                BACKGROUND
        );

        content.setBorder(
                BorderFactory.createEmptyBorder(
                        22,
                        24,
                        22,
                        24
                )
        );

        JPanel form =
                new JPanel(
                        new GridBagLayout()
                );

        form.setBackground(
                SURFACE
        );

        form.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                18,
                                18,
                                18,
                                18
                        )
                )
        );

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.insets =
                new Insets(
                        7,
                        7,
                        7,
                        7
                );

        constraints.fill =
                GridBagConstraints.HORIZONTAL;

        constraints.weightx =
                1.0;

        addRow(
                form,
                constraints,
                0,
                "Name",
                nameField
        );

        addRow(
                form,
                constraints,
                1,
                "Host / IP",
                hostField
        );

        addRow(
                form,
                constraints,
                2,
                "Service",
                serviceBox
        );

        addRow(
                form,
                constraints,
                3,
                "TCP Port",
                portField
        );

        addRow(
                form,
                constraints,
                4,
                "Check every (sec)",
                intervalSpinner
        );

        addRow(
                form,
                constraints,
                5,
                "Fail after (checks)",
                failureSpinner
        );

        addRow(
                form,
                constraints,
                6,
                "Recover after (checks)",
                recoverySpinner
        );

        JPanel actions =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        actions.setOpaque(
                false
        );

        actions.add(
                cancelButton
        );

        actions.add(
                saveButton
        );

        content.add(
                form,
                BorderLayout.CENTER
        );

        content.add(
                actions,
                BorderLayout.SOUTH
        );

        setContentPane(
                content
        );
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            Component input
    ) {
        GridBagConstraints labelConstraints =
                (GridBagConstraints)
                        constraints.clone();

        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.weightx = 0.0;

        JLabel label =
                new JLabel(
                        labelText
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

        panel.add(
                label,
                labelConstraints
        );

        GridBagConstraints inputConstraints =
                (GridBagConstraints)
                        constraints.clone();

        inputConstraints.gridx = 1;
        inputConstraints.gridy = row;
        inputConstraints.weightx = 1.0;

        panel.add(
                input,
                inputConstraints
        );
    }

    private void registerListeners() {
        serviceBox.addActionListener(
                event -> updatePortFromService()
        );

        cancelButton.addActionListener(
                event -> dispose()
        );

        saveButton.addActionListener(
                event -> saveTarget()
        );

        getRootPane().setDefaultButton(
                saveButton
        );
    }

    private void updatePortFromService() {
        ServicePreset service =
                (ServicePreset)
                        serviceBox
                                .getSelectedItem();

        if (service == null) {
            return;
        }

        if (service.isCustom()) {
            portField.setEditable(
                    true
            );

            portField.setBackground(
                    INPUT_BACKGROUND
            );

        } else {
            portField.setText(
                    String.valueOf(
                            service.getPort()
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

    private void populate(
            MonitoringTarget target
    ) {
        nameField.setText(
                target.name()
        );

        hostField.setText(
                target.host()
        );

        serviceBox.setSelectedItem(
                target.service()
        );

        portField.setText(
                String.valueOf(
                        target.port()
                )
        );

        intervalSpinner.setValue(
                target.intervalSeconds()
        );

        failureSpinner.setValue(
                target.failureThreshold()
        );

        recoverySpinner.setValue(
                target.recoveryThreshold()
        );

        updatePortFromService();

        if (target.service().isCustom()) {
            portField.setText(
                    String.valueOf(
                            target.port()
                    )
            );
        }
    }

    private void saveTarget() {
        String name =
                nameField
                        .getText()
                        .trim();

        String host =
                hostField
                        .getText()
                        .trim();

        if (name.isEmpty()
                || host.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Name and host are required.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        ServicePreset service =
                (ServicePreset)
                        serviceBox
                                .getSelectedItem();

        if (service == null) {
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
                    "TCP port must be between 1 and 65535.",
                    "Invalid Port",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int interval =
                (Integer)
                        intervalSpinner
                                .getValue();

        int fail =
                (Integer)
                        failureSpinner
                                .getValue();

        int recover =
                (Integer)
                        recoverySpinner
                                .getValue();

        try {
            if (original == null) {
                result =
                        MonitoringTarget.create(
                                name,
                                host,
                                service,
                                port,
                                interval,
                                fail,
                                recover
                        );

            } else {
                result =
                        new MonitoringTarget(
                                original.id(),
                                name,
                                host,
                                service,
                                port,
                                interval,
                                fail,
                                recover
                        );
            }

            dispose();

        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Invalid Target",
                    JOptionPane.WARNING_MESSAGE
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
                            8,
                            14,
                            8,
                            14
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

            g2.setColor(
                    isEnabled()
                            ? hovered
                                    ? hoverColor
                                    : normalColor
                            : SECONDARY_BUTTON
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
}
