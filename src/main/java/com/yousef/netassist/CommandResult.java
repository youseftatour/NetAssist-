package com.yousef.netassist;

public record CommandResult(
        String commandName,
        boolean successful,
        String output,
        long durationMs
) {
}