package com.yousef.netassist;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public final class NetworkDiagnostics {

    public CheckResult resolveHost(String host) {
        long start = System.nanoTime();

        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            String resolvedAddresses = List.of(addresses).stream()
                    .map(InetAddress::getHostAddress)
                    .distinct()
                    .collect(Collectors.joining(", "));

            return result("DNS lookup", true,
                    host + " resolved to: " + resolvedAddresses, start);
        } catch (IOException exception) {
            return result("DNS lookup", false,
                    "Could not resolve " + host + ": " + exception.getMessage(), start);
        }
    }

    public CheckResult checkReachability(String host, int timeoutMs) {
        long start = System.nanoTime();

        try {
            InetAddress address = InetAddress.getByName(host);
            boolean reachable = address.isReachable(timeoutMs);

            String details = reachable
                    ? address.getHostAddress() + " responded to the reachability check."
                    : "No reachability response was received. The host may still be online if ICMP is blocked.";

            return result("Host reachability", reachable, details, start);
        } catch (IOException exception) {
            return result("Host reachability", false,
                    "Reachability check failed: " + exception.getMessage(), start);
        }
    }

   public TcpCheckResult testTcpPort(
        String host,
        int port,
        int timeoutMs
) {

    long start = System.nanoTime();

    if (port < 1 || port > 65_535) {

        return tcpResult(
                TcpStatus.INVALID_PORT,
                "Port must be between 1 and 65535.",
                start
        );
    }

    final InetAddress address;

    try {

        address = InetAddress.getByName(host);

    } catch (UnknownHostException exception) {

        return tcpResult(
                TcpStatus.DNS_FAILURE,
                "Could not resolve hostname: " + host,
                start
        );
    }

    try (Socket socket = new Socket()) {

        socket.connect(
                new InetSocketAddress(address, port),
                timeoutMs
        );

        return tcpResult(
                TcpStatus.OPEN,
                "Connected successfully to "
                        + address.getHostAddress()
                        + ":"
                        + port
                        + ".",
                start
        );

    } catch (SocketTimeoutException exception) {

        return tcpResult(
                TcpStatus.TIMEOUT,
                "The connection attempt timed out. "
                        + "A firewall may be silently filtering "
                        + "the port, or the target may not be responding.",
                start
        );

    } catch (ConnectException exception) {

        return tcpResult(
                TcpStatus.CONNECTION_REFUSED,
                "The host actively refused the connection. "
                        + "The host is reachable, but no service "
                        + "appears to be accepting TCP connections "
                        + "on port "
                        + port
                        + ".",
                start
        );

    } catch (NoRouteToHostException exception) {

        return tcpResult(
                TcpStatus.UNREACHABLE,
                "No route to the target was available, "
                        + "or the network rejected the connection.",
                start
        );

    } catch (IOException exception) {

        return tcpResult(
                TcpStatus.ERROR,
                exception.getClass().getSimpleName()
                        + ": "
                        + exception.getMessage(),
                start
        );
    }
}

    public String getLocalNetworkInformation() {
        List<String> lines = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return "No network interfaces were found.";
            }

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                List<String> addresses = networkInterface.inetAddresses()
                        .filter(address -> address instanceof Inet4Address)
                        .map(InetAddress::getHostAddress)
                        .toList();

                if (!addresses.isEmpty()) {
                    lines.add(networkInterface.getDisplayName()
                            + " -> " + String.join(", ", addresses));
                }
            }
        } catch (SocketException exception) {
            return "Could not read local network information: " + exception.getMessage();
        }

        return lines.isEmpty()
                ? "No active IPv4 interfaces were found."
                : String.join(System.lineSeparator(), lines);
    }

    private CheckResult result(
            String name,
            boolean successful,
            String details,
            long startNanos
    ) {
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        return new CheckResult(name, successful, details, durationMs);
    }
    
    private TcpCheckResult tcpResult(
            TcpStatus status,
            String details,
            long startNanos
    ) {

        long durationMs =
                (System.nanoTime() - startNanos)
                        / 1_000_000;

        return new TcpCheckResult(
                status,
                details,
                durationMs
        );
    }
    
}
