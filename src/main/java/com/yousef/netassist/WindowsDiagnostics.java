package com.yousef.netassist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class WindowsDiagnostics {

    public CommandResult runNslookup(String host) {
        return runCommand("NSLOOKUP", "nslookup", host);
    }

    public CommandResult runTraceroute(String host) {
        return runCommand("TRACERT", "tracert", host);
    }

    public CommandResult runIpConfig() {
        return runCommand("IPCONFIG", "ipconfig", "/all");
    }

    private CommandResult runCommand(
            String commandName,
            String... command
    ) {

        long start = System.nanoTime();

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();

        try {

            Process process = processBuilder.start();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream(),
                                         StandardCharsets.UTF_8
                                 )
                         )) {

                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            long durationMs =
                    (System.nanoTime() - start)
                            / 1_000_000;

            return new CommandResult(
                    commandName,
                    exitCode == 0,
                    output.toString(),
                    durationMs
            );

        } catch (IOException | InterruptedException exception) {

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            long durationMs =
                    (System.nanoTime() - start)
                            / 1_000_000;

            return new CommandResult(
                    commandName,
                    false,
                    exception.getMessage(),
                    durationMs
            );
        }
    }
}