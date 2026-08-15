package com.yousef.netassist;

public enum TcpStatus {

    OPEN("OPEN"),
    CONNECTION_REFUSED("CONNECTION REFUSED"),
    TIMEOUT("TIMEOUT / POSSIBLY FILTERED"),
    UNREACHABLE("HOST/NETWORK UNREACHABLE"),
    DNS_FAILURE("DNS FAILURE"),
    INVALID_PORT("INVALID PORT"),
    ERROR("ERROR");

    private final String displayName;

    TcpStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}