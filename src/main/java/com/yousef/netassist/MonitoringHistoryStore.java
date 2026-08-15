package com.yousef.netassist;

import java.io.IOException;

import java.net.URLDecoder;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class MonitoringHistoryStore {

    private static final String INCIDENT_FILE =
            "monitor-incidents.tsv";

    private static final String SESSION_FILE =
            "monitor-sessions.tsv";

    private final Path directory;
    private final Path incidentFile;
    private final Path sessionFile;

    public MonitoringHistoryStore() {
        this(
                Path.of(
                        System.getProperty(
                                "user.home"
                        ),
                        ".netassist"
                )
        );
    }

    MonitoringHistoryStore(
            Path directory
    ) {
        this.directory =
                directory;

        this.incidentFile =
                directory.resolve(
                        INCIDENT_FILE
                );

        this.sessionFile =
                directory.resolve(
                        SESSION_FILE
                );
    }

    public synchronized void appendIncident(
            MonitoringTarget target,
            MonitorIncident incident
    ) throws IOException {

        ensureDirectory();

        String line =
                String.join(
                        "\t",
                        incident.timestamp()
                                .toString(),
                        encode(
                                target.id()
                        ),
                        encode(
                                target.name()
                        ),
                        encode(
                                target.host()
                        ),
                        String.valueOf(
                                target.port()
                        ),
                        encode(
                                target.service()
                                        .toString()
                        ),
                        incident.type()
                                .name(),
                        String.valueOf(
                                incident.durationSeconds()
                        ),
                        encode(
                                incident.message()
                        )
                )
                        + System.lineSeparator();

        Files.writeString(
                incidentFile,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public synchronized void appendSession(
            MonitoringTarget target,
            MonitoringStats stats
    ) throws IOException {

        if (stats == null
                || stats.startedAt() == null) {

            return;
        }

        ensureDirectory();

        String line =
                String.join(
                        "\t",
                        stats.startedAt()
                                .toString(),
                        LocalDateTime.now()
                                .toString(),
                        encode(
                                target.id()
                        ),
                        encode(
                                target.name()
                        ),
                        encode(
                                target.host()
                        ),
                        String.valueOf(
                                target.port()
                        ),
                        encode(
                                target.service()
                                        .toString()
                        ),
                        String.valueOf(
                                stats.totalChecks()
                        ),
                        String.valueOf(
                                stats.successfulChecks()
                        ),
                        String.valueOf(
                                stats.failedChecks()
                        ),
                        String.valueOf(
                                stats.uptimePercent()
                        ),
                        String.valueOf(
                                stats.averageLatencyMs()
                        ),
                        String.valueOf(
                                stats.minimumLatencyMs()
                        ),
                        String.valueOf(
                                stats.maximumLatencyMs()
                        )
                )
                        + System.lineSeparator();

        Files.writeString(
                sessionFile,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public synchronized List<HistoricalIncident> loadIncidents()
            throws IOException {

        List<HistoricalIncident> result =
                new ArrayList<>();

        if (!Files.exists(
                incidentFile
        )) {
            return result;
        }

        for (String line
                : Files.readAllLines(
                        incidentFile,
                        StandardCharsets.UTF_8
                )) {

            if (line.isBlank()) {
                continue;
            }

            String[] values =
                    line.split(
                            "\t",
                            -1
                    );

            if (values.length != 9) {
                continue;
            }

            try {
                result.add(
                        new HistoricalIncident(
                                LocalDateTime.parse(
                                        values[0]
                                ),
                                decode(
                                        values[1]
                                ),
                                decode(
                                        values[2]
                                ),
                                decode(
                                        values[3]
                                ),
                                Integer.parseInt(
                                        values[4]
                                ),
                                decode(
                                        values[5]
                                ),
                                MonitorIncident.Type.valueOf(
                                        values[6]
                                ),
                                decode(
                                        values[8]
                                ),
                                Long.parseLong(
                                        values[7]
                                )
                        )
                );

            } catch (
                    IllegalArgumentException exception
            ) {
                /*
                 * Ignore only the malformed row. Historical data written
                 * by other valid rows remains available.
                 */
            }
        }

        return result;
    }

    public synchronized List<MonitoringSessionRecord> loadSessions()
            throws IOException {

        List<MonitoringSessionRecord> result =
                new ArrayList<>();

        if (!Files.exists(
                sessionFile
        )) {
            return result;
        }

        for (String line
                : Files.readAllLines(
                        sessionFile,
                        StandardCharsets.UTF_8
                )) {

            if (line.isBlank()) {
                continue;
            }

            String[] values =
                    line.split(
                            "\t",
                            -1
                    );

            if (values.length != 14) {
                continue;
            }

            try {
                result.add(
                        new MonitoringSessionRecord(
                                LocalDateTime.parse(
                                        values[0]
                                ),
                                LocalDateTime.parse(
                                        values[1]
                                ),
                                decode(
                                        values[2]
                                ),
                                decode(
                                        values[3]
                                ),
                                decode(
                                        values[4]
                                ),
                                Integer.parseInt(
                                        values[5]
                                ),
                                decode(
                                        values[6]
                                ),
                                Integer.parseInt(
                                        values[7]
                                ),
                                Integer.parseInt(
                                        values[8]
                                ),
                                Integer.parseInt(
                                        values[9]
                                ),
                                Double.parseDouble(
                                        values[10]
                                ),
                                Double.parseDouble(
                                        values[11]
                                ),
                                Long.parseLong(
                                        values[12]
                                ),
                                Long.parseLong(
                                        values[13]
                                )
                        )
                );

            } catch (
                    IllegalArgumentException exception
            ) {
                /*
                 * Skip malformed historical rows.
                 */
            }
        }

        return result;
    }

    public Path getIncidentFile() {
        return incidentFile;
    }

    public Path getSessionFile() {
        return sessionFile;
    }

    private void ensureDirectory()
            throws IOException {

        Files.createDirectories(
                directory
        );
    }

    private static String encode(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    private static String decode(
            String value
    ) {
        if (value == null
                || value.isEmpty()) {

            return "";
        }

        return new String(
                Base64.getUrlDecoder()
                        .decode(
                                value
                        ),
                StandardCharsets.UTF_8
        );
    }
}
