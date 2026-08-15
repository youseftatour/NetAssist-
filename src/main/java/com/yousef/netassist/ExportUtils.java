package com.yousef.netassist;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.awt.Component;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

public final class ExportUtils {

    private ExportUtils() {
    }

    public static void exportText(
            Component parent,
            String suggestedName,
            String content
    ) {
        if (content == null
                || content.isBlank()) {

            JOptionPane.showMessageDialog(
                    parent,
                    "There is no report to export.",
                    "Nothing to Export",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        JFileChooser chooser =
                new JFileChooser();

        chooser.setSelectedFile(
                new java.io.File(
                        suggestedName
                )
        );

        if (chooser.showSaveDialog(
                parent
        ) != JFileChooser.APPROVE_OPTION) {

            return;
        }

        Path path =
                chooser.getSelectedFile()
                        .toPath();

        try {
            Files.writeString(
                    path,
                    content,
                    StandardCharsets.UTF_8
            );

            JOptionPane.showMessageDialog(
                    parent,
                    "Exported successfully to:\n"
                            + path,
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException exception) {
            showExportError(
                    parent,
                    exception
            );
        }
    }

    public static void exportCsv(
            Component parent,
            String suggestedName,
            List<String> headers,
            List<List<String>> rows
    ) {
        JFileChooser chooser =
                new JFileChooser();

        chooser.setSelectedFile(
                new java.io.File(
                        suggestedName
                )
        );

        if (chooser.showSaveDialog(
                parent
        ) != JFileChooser.APPROVE_OPTION) {

            return;
        }

        StringBuilder csv =
                new StringBuilder();

        appendRow(
                csv,
                headers
        );

        for (List<String> row
                : rows) {

            appendRow(
                    csv,
                    row
            );
        }

        try {
            Files.writeString(
                    chooser.getSelectedFile()
                            .toPath(),
                    csv.toString(),
                    StandardCharsets.UTF_8
            );

            JOptionPane.showMessageDialog(
                    parent,
                    "CSV export completed successfully.",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException exception) {
            showExportError(
                    parent,
                    exception
            );
        }
    }

    private static void appendRow(
            StringBuilder csv,
            List<String> values
    ) {
        for (
                int index = 0;
                index < values.size();
                index++
        ) {
            if (index > 0) {
                csv.append(',');
            }

            csv.append(
                    escapeCsv(
                            values.get(
                                    index
                            )
                    )
            );
        }

        csv.append(
                System.lineSeparator()
        );
    }

    private static String escapeCsv(
            String value
    ) {
        String safe =
                value == null
                        ? ""
                        : value;

        boolean quote =
                safe.contains(",")
                        || safe.contains("\"")
                        || safe.contains("\n")
                        || safe.contains("\r");

        safe =
                safe.replace(
                        "\"",
                        "\"\""
                );

        return quote
                ? "\""
                        + safe
                        + "\""
                : safe;
    }

    private static void showExportError(
            Component parent,
            IOException exception
    ) {
        JOptionPane.showMessageDialog(
                parent,
                "Could not export the file.\n\n"
                        + exception.getMessage(),
                "Export Failed",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
