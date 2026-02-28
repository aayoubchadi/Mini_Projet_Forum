package com.forum.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class OracleConnectionFactory {
    private final AppConfig config = AppConfig.getInstance();
    private final boolean isPostgres;

    public OracleConnectionFactory() {
        String url = config.getRequired("db.url");
        isPostgres = url.startsWith("jdbc:postgresql");

        String driverClass = isPostgres
                ? "org.postgresql.Driver"
                : "oracle.jdbc.OracleDriver";
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "JDBC driver not found (" + driverClass + "). Add the driver jar to WEB-INF/lib.", e);
        }
    }

    public boolean isPostgres() {
        return isPostgres;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getRequired("db.url"),
                config.getRequired("db.user"),
                config.getRequired("db.password"));
    }
}
