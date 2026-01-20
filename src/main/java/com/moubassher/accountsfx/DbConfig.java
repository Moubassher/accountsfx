package com.moubassher.accountsfx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConfig {

    private final String appName, dbName, driver;

    public DbConfig(
            String appName,
            String dbName,
            String driver
    ) {
        this.appName = require(appName, "appName");
        this.dbName = require(dbName, "dbName");
        this.driver = require(driver, "driver");
    }

    /**
     * Main entry point used by DAOs.
     */
    public Connection openConnection() {
        try {
            Path dbPath = resolveDatabasePath();
            ensureDatabaseExists(dbPath);
            return openJdbcConnection(dbPath);
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to open database", e);
        }
    }

    /* ---------------- INTERNALS ---------------- */

    private Path resolveDatabasePath() {
        return Paths.get(
                System.getProperty("user.home"),
                "." + appName,
                dbName
        );
    }

    /**
     * Copies the DB from resources if it does not exist.
     * Resource path is derived from db name:
     * /db/<dbName>
     */
    private void ensureDatabaseExists(Path dbPath) throws IOException {
        if (Files.exists(dbPath)) {
            return;
        }

        Files.createDirectories(dbPath.getParent());

        String resourcePath =  dbName;

        try (InputStream in =
                     DbConfig.class.getResourceAsStream(resourcePath)) {

            if (in == null) {
                throw new IllegalStateException("Database resource not found: " + resourcePath);
            }

            Files.copy(in, dbPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Connection openJdbcConnection(Path dbPath)
            throws SQLException {

        String url = driver  + dbPath.toUri();
        return DriverManager.getConnection(url);
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public void deleteDatabase() {
        try {
            Files.deleteIfExists(resolveDatabasePath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete database", e);
        }
    }
}
