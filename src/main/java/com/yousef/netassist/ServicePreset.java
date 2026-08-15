package com.yousef.netassist;

public enum ServicePreset {
    HTTPS("HTTPS", 443),
    HTTP("HTTP", 80),
    DNS("DNS", 53),
    SSH("SSH", 22),
    REMOTE_DESKTOP("Remote Desktop", 3389),
    SQL_SERVER("SQL Server", 1433),
    MYSQL("MySQL", 3306),
    CUSTOM("Custom", -1);

    private final String displayName;
    private final int port;

    ServicePreset(String displayName, int port) {
        this.displayName = displayName;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public boolean isCustom() {
        return this == CUSTOM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
