package com.yousef.netassist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class WindowsDiagnostics {

    private static final long COMMAND_TIMEOUT_SECONDS = 45;

    public CommandResult runNslookup(
            String host
    ) {
        return runCommand(
                "NSLOOKUP",
                List.of(
                        "nslookup",
                        host
                )
        );
    }

    public CommandResult runTraceroute(
            String host
    ) {
        if (isWindows()) {
            return runCommand(
                    "TRACERT",
                    List.of(
                            "tracert",
                            "-d",
                            "-w",
                            "1000",
                            host
                    )
            );
        }

        return runCommand(
                "TRACEROUTE",
                List.of(
                        "traceroute",
                        "-n",
                        "-w",
                        "1",
                        host
                )
        );
    }

    public CommandResult runIpConfig() {
        if (isWindows()) {
            return runCommand(
                    "IPCONFIG",
                    List.of(
                            "ipconfig",
                            "/all"
                    )
            );
        }

        CommandResult ipResult =
                runCommand(
                        "IP ADDRESS",
                        List.of(
                                "ip",
                                "addr"
                        )
                );

        if (ipResult.successful()) {
            return ipResult;
        }

        return runCommand(
                "IFCONFIG",
                List.of(
                        "ifconfig"
                )
        );
    }

    private CommandResult runCommand(
            String commandName,
            List<String> command
    ) {
        long start =
                System.nanoTime();

        Process process =
                null;

        try {
            ProcessBuilder builder =
                    new ProcessBuilder(
                            new ArrayList<>(
                                    command
                            )
                    );

            builder.redirectErrorStream(
                    true
            );

            process =
                    builder.start();

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Process runningProcess =
                    process;

            Thread reader =
                    new Thread(
                            () -> {
                                try {
                                    runningProcess
                                            .getInputStream()
                                            .transferTo(
                                                    output
                                            );
                                } catch (IOException ignored) {
                                }
                            },
                            "NetAssist-CommandReader"
                    );

            reader.setDaemon(
                    true
            );

            reader.start();

            boolean finished =
                    process.waitFor(
                            COMMAND_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    );

            if (!finished) {
                process.destroyForcibly();

                reader.join(
                        1_000
                );

                return new CommandResult(
                        commandName,
                        false,
                        "Command timed out after "
                                + COMMAND_TIMEOUT_SECONDS
                                + " seconds.\n\nCommand: "
                                + String.join(
                                        " ",
                                        command
                                ),
                        elapsedMs(
                                start
                        )
                );
            }

            reader.join(
                    2_000
            );

            String text =
                    output.toString(
                            StandardCharsets.UTF_8
                    );

            if (text.isBlank()) {
                text =
                        "(No command output.)";
            }

            int exitCode =
                    process.exitValue();

            return new CommandResult(
                    commandName,
                    exitCode == 0,
                    text.stripTrailing(),
                    elapsedMs(
                            start
                    )
            );

        } catch (IOException exception) {
            return new CommandResult(
                    commandName,
                    false,
                    "Could not start command.\n\n"
                            + exception.getClass()
                                    .getSimpleName()
                            + ": "
                            + exception.getMessage()
                            + "\n\nCommand: "
                            + String.join(
                                    " ",
                                    command
                            ),
                    elapsedMs(
                            start
                    )
            );

        } catch (InterruptedException exception) {
            Thread.currentThread()
                    .interrupt();

            if (process != null) {
                process.destroyForcibly();
            }

            return new CommandResult(
                    commandName,
                    false,
                    "Command was interrupted.",
                    elapsedMs(
                            start
                    )
            );
        }
    }

    private static long elapsedMs(
            long startNanos
    ) {
        return (System.nanoTime()
                - startNanos)
                / 1_000_000;
    }

    private static boolean isWindows() {
        return System.getProperty(
                        "os.name",
                        ""
                )
                .toLowerCase(
                        Locale.ROOT
                )
                .contains(
                        "win"
                );
    }
}
