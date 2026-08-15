package com.yousef.netassist;

import java.util.Objects;
import java.util.UUID;

public record MonitoringTarget(
        String id,
        String name,
        String host,
        ServicePreset service,
        int port,
        int intervalSeconds,
        int failureThreshold,
        int recoveryThreshold
) {

    public MonitoringTarget {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(service, "service");

        name = name.trim();
        host = host.trim();

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "Target ID cannot be empty."
            );
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Target name cannot be empty."
            );
        }

        if (host.isBlank()) {
            throw new IllegalArgumentException(
                    "Host cannot be empty."
            );
        }

        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException(
                    "Port must be between 1 and 65535."
            );
        }

        if (intervalSeconds < 1) {
            throw new IllegalArgumentException(
                    "Monitoring interval must be at least 1 second."
            );
        }

        if (failureThreshold < 1) {
            throw new IllegalArgumentException(
                    "Failure threshold must be at least 1."
            );
        }

        if (recoveryThreshold < 1) {
            throw new IllegalArgumentException(
                    "Recovery threshold must be at least 1."
            );
        }
    }

    public static MonitoringTarget create(
            String name,
            String host,
            ServicePreset service,
            int port,
            int intervalSeconds,
            int failureThreshold,
            int recoveryThreshold
    ) {
        return new MonitoringTarget(
                UUID.randomUUID().toString(),
                name,
                host,
                service,
                port,
                intervalSeconds,
                failureThreshold,
                recoveryThreshold
        );
    }

    public String endpoint() {
        return host + ":" + port;
    }
}
