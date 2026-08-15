package com.yousef.netassist;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import java.awt.image.BufferedImage;

public final class NotificationService
        implements AutoCloseable {

    private TrayIcon trayIcon;
    private boolean enabled = true;

    public NotificationService() {
        initialize();
    }

    private void initialize() {
        try {
            if (!SystemTray.isSupported()) {
                return;
            }

            trayIcon =
                    new TrayIcon(
                            createTrayImage(),
                            "NetAssist"
                    );

            trayIcon.setImageAutoSize(
                    true
            );

            SystemTray.getSystemTray()
                    .add(
                            trayIcon
                    );

        } catch (Exception exception) {
            trayIcon =
                    null;
        }
    }

    public boolean isAvailable() {
        return trayIcon != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(
            boolean enabled
    ) {
        this.enabled =
                enabled;
    }

    public void notifyIncident(
            MonitoringTarget target,
            MonitorIncident incident
    ) {
        if (!enabled
                || trayIcon == null) {
            return;
        }

        String title;
        TrayIcon.MessageType type;

        if (incident.type()
                == MonitorIncident.Type.OUTAGE) {

            title =
                    "Service Down";

            type =
                    TrayIcon.MessageType.ERROR;

        } else {
            title =
                    "Service Recovered";

            type =
                    TrayIcon.MessageType.INFO;
        }

        String message =
                target.name()
                        + "\n"
                        + target.endpoint()
                        + "\n"
                        + incident.message();

        trayIcon.displayMessage(
                title,
                message,
                type
        );
    }

    private static Image createTrayImage() {
        BufferedImage image =
                new BufferedImage(
                        32,
                        32,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D graphics =
                image.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.setColor(
                new Color(
                        37,
                        99,
                        235
                )
        );

        graphics.fillRoundRect(
                1,
                1,
                30,
                30,
                9,
                9
        );

        graphics.setColor(
                Color.WHITE
        );

        graphics.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        21
                )
        );

        graphics.drawString(
                "N",
                8,
                24
        );

        graphics.dispose();

        return image;
    }

    @Override
    public void close() {
        if (trayIcon == null) {
            return;
        }

        try {
            SystemTray.getSystemTray()
                    .remove(
                            trayIcon
                    );

        } catch (Exception ignored) {
        }

        trayIcon =
                null;
    }
}
