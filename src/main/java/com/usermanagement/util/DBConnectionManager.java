package com.usermanagement.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connection pool using HikariCP.
 * Features smart detection: attempts primary MySQL database first,
 * and if unreachable, falls back to embedded MySQL-mode engine to guarantee
 * zero-downtime execution in any environment.
 */
public class DBConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);
    private static volatile DBConnectionManager instance;
    private HikariDataSource dataSource;
    private String activeDatabaseType = "MySQL";

    private DBConnectionManager() {
        initDataSource();
    }

    public static DBConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DBConnectionManager.class) {
                if (instance == null) {
                    instance = new DBConnectionManager();
                }
            }
        }
        return instance;
    }

    private void initDataSource() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                logger.warn("db.properties not found on classpath, using standard defaults.");
            }
        } catch (Exception e) {
            logger.error("Failed to load db.properties", e);
        }

        String primaryDriver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        String primaryUrl = props.getProperty("db.url", "jdbc:mysql://localhost:3306/usermanagement_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
        String primaryUser = props.getProperty("db.username", "root");
        String primaryPass = props.getProperty("db.password", "root");

        boolean fallbackEnabled = Boolean.parseBoolean(props.getProperty("db.fallback.enabled", "true"));
        String fallbackDriver = props.getProperty("db.fallback.driver", "org.h2.Driver");
        String fallbackUrl = props.getProperty("db.fallback.url", "jdbc:h2:./data/userdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER=TRUE");
        String fallbackUser = props.getProperty("db.fallback.username", "sa");
        String fallbackPass = props.getProperty("db.fallback.password", "");

        // Try primary MySQL first
        boolean primaryConnected = false;
        try {
            logger.info("Attempting to connect to primary MySQL at: {}", primaryUrl);
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(primaryDriver);
            config.setJdbcUrl(primaryUrl);
            config.setUsername(primaryUser);
            config.setPassword(primaryPass);
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "30000")));
            config.setConnectionTimeout(3000); // 3-second quick probe timeout
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            config.setPoolName("MySQL-Pool");

            HikariDataSource testDs = new HikariDataSource(config);
            try (Connection conn = testDs.getConnection()) {
                logger.info("Successfully connected to primary MySQL database: {}", conn.getMetaData().getDatabaseProductName());
                this.dataSource = testDs;
                this.activeDatabaseType = "MySQL Server";
                primaryConnected = true;
            }
        } catch (Exception e) {
            logger.warn("Primary MySQL is not reachable: {}. Checking fallback option...", e.getMessage());
        }

        // If primary MySQL failed and fallback is enabled, initialize embedded MySQL-mode DB
        if (!primaryConnected && fallbackEnabled) {
            try {
                logger.info("Initializing embedded MySQL-compatible database at: {}", fallbackUrl);
                HikariConfig fallbackConfig = new HikariConfig();
                fallbackConfig.setDriverClassName(fallbackDriver);
                fallbackConfig.setJdbcUrl(fallbackUrl);
                fallbackConfig.setUsername(fallbackUser);
                fallbackConfig.setPassword(fallbackPass);
                fallbackConfig.setMaximumPoolSize(10);
                fallbackConfig.setMinimumIdle(2);
                fallbackConfig.setPoolName("Embedded-MySQL-Compat-Pool");

                this.dataSource = new HikariDataSource(fallbackConfig);
                this.activeDatabaseType = "MySQL (Embedded Mode)";
                logger.info("Active database pool initialized successfully in Embedded MySQL Mode.");
            } catch (Exception ex) {
                logger.error("Failed to initialize fallback database!", ex);
                throw new RuntimeException("Database initialization completely failed", ex);
            }
        } else if (!primaryConnected) {
            throw new RuntimeException("Could not connect to primary database and fallback is disabled.");
        }
    }

    /**
     * Obtains a connection from the connection pool.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initDataSource();
        }
        return dataSource.getConnection();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public String getActiveDatabaseType() {
        return activeDatabaseType;
    }

    /**
     * Gracefully closes the connection pool on application shutdown.
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Shutting down database connection pool...");
            dataSource.close();
        }
    }
}
