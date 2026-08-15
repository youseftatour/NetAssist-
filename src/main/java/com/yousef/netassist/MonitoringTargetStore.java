package com.yousef.netassist;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public final class MonitoringTargetStore {

    private static final String FILE_NAME =
            "monitor-targets.properties";

    private final Path directory;
    private final Path file;

    public MonitoringTargetStore() {
        this(
                Path.of(
                        System.getProperty("user.home"),
                        ".netassist"
                )
        );
    }

    MonitoringTargetStore(
            Path directory
    ) {
        this.directory = directory;
        this.file =
                directory.resolve(
                        FILE_NAME
                );
    }

    public synchronized List<MonitoringTarget> load()
            throws IOException {

        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        Properties properties =
                new Properties();

        try (
                InputStream input =
                        Files.newInputStream(file)
        ) {
            properties.load(input);
        }

        Set<String> ids =
                new TreeSet<>();

        for (String key
                : properties.stringPropertyNames()) {

            if (!key.startsWith("target.")
                    || !key.endsWith(".name")) {
                continue;
            }

            String middle =
                    key.substring(
                            "target.".length(),
                            key.length()
                                    - ".name".length()
                    );

            if (!middle.isBlank()) {
                ids.add(middle);
            }
        }

        List<MonitoringTarget> targets =
                new ArrayList<>();

        for (String id : ids) {
            String prefix =
                    "target."
                            + id
                            + ".";

            try {
                String name =
                        requireProperty(
                                properties,
                                prefix + "name"
                        );

                String host =
                        requireProperty(
                                properties,
                                prefix + "host"
                        );

                ServicePreset service =
                        ServicePreset.valueOf(
                                requireProperty(
                                        properties,
                                        prefix + "service"
                                )
                        );

                int port =
                        Integer.parseInt(
                                requireProperty(
                                        properties,
                                        prefix + "port"
                                )
                        );

                int interval =
                        Integer.parseInt(
                                properties.getProperty(
                                        prefix + "interval",
                                        "5"
                                )
                        );

                int fail =
                        Integer.parseInt(
                                properties.getProperty(
                                        prefix + "failureThreshold",
                                        "3"
                                )
                        );

                int recover =
                        Integer.parseInt(
                                properties.getProperty(
                                        prefix + "recoveryThreshold",
                                        "2"
                                )
                        );

                targets.add(
                        new MonitoringTarget(
                                id,
                                name,
                                host,
                                service,
                                port,
                                interval,
                                fail,
                                recover
                        )
                );

            } catch (
                    IllegalArgumentException exception
            ) {
                /*
                 * Skip only the malformed profile instead of making
                 * every saved target unusable.
                 */
                System.err.println(
                        "NetAssist: skipping malformed monitoring target "
                                + id
                                + ": "
                                + exception.getMessage()
                );
            }
        }

        targets.sort(
                Comparator.comparing(
                        MonitoringTarget::name,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return targets;
    }

    public synchronized void save(
            List<MonitoringTarget> targets
    ) throws IOException {

        Files.createDirectories(
                directory
        );

        Properties properties =
                new Properties();

        for (MonitoringTarget target
                : targets) {

            String prefix =
                    "target."
                            + target.id()
                            + ".";

            properties.setProperty(
                    prefix + "name",
                    target.name()
            );

            properties.setProperty(
                    prefix + "host",
                    target.host()
            );

            properties.setProperty(
                    prefix + "service",
                    target.service().name()
            );

            properties.setProperty(
                    prefix + "port",
                    String.valueOf(
                            target.port()
                    )
            );

            properties.setProperty(
                    prefix + "interval",
                    String.valueOf(
                            target.intervalSeconds()
                    )
            );

            properties.setProperty(
                    prefix + "failureThreshold",
                    String.valueOf(
                            target.failureThreshold()
                    )
            );

            properties.setProperty(
                    prefix + "recoveryThreshold",
                    String.valueOf(
                            target.recoveryThreshold()
                    )
            );
        }

        Path temporary =
                directory.resolve(
                        FILE_NAME + ".tmp"
                );

        try (
                OutputStream output =
                        Files.newOutputStream(
                                temporary
                        )
        ) {
            properties.store(
                    output,
                    "NetAssist saved monitoring targets"
            );
        }

        try {
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (
                AtomicMoveNotSupportedException exception
        ) {
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    public Path getFile() {
        return file;
    }

    private static String requireProperty(
            Properties properties,
            String key
    ) {
        String value =
                properties.getProperty(key);

        if (value == null
                || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing property "
                            + key
            );
        }

        return value.trim();
    }
}
