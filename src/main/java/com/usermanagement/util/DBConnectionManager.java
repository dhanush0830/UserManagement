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
 * Manages database connection pool using HikariCP for MySQL.
 */
public class DBConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);
    private static volatile DBConnectionManager instance;
    private HikariDataSource dataSource;
    private String activeDatabaseType = "MySQL Server";

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
                logger.warn("db.properties not found on classpath, using standard MySQL defaults.");
            }
        } catch (Exception e) {
            logger.error("Failed to load db.properties", e);
        }

        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/usermanagement_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
        String user = props.getProperty("db.username", "root");
        String pass = props.getProperty("db.password", "root123");

        try {
            logger.info("Initializing MySQL HikariCP Connection Pool at: {}", url);
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(driver);
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "10000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            config.setPoolName("MySQL-HikariPool");

            // Additional MySQL performance recommendations for HikariCP
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            HikariDataSource ds = new HikariDataSource(config);

            // Test connection immediately
            try (Connection conn = ds.getConnection()) {
                String productName = conn.getMetaData().getDatabaseProductName();
                String productVersion = conn.getMetaData().getDatabaseProductVersion();
                this.activeDatabaseType = productName + " " + productVersion;
                this.dataSource = ds;
                logger.info("Successfully connected to MySQL Database: {} (Version: {})", productName, productVersion);
            }
        } catch (Exception e) {
            logger.error("Failed to connect to MySQL database at [{}]. Please verify MySQL is running and credentials in src/main/resources/db.properties are correct.", url, e);
            throw new RuntimeException("MySQL connection failed: " + e.getMessage(), e);
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
