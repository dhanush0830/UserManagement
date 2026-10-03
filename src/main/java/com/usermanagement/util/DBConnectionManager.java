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

        // --- Railway / PaaS environment variable overrides ---
        // Railway MySQL plugin provides: MYSQL_URL (full JDBC URL) OR individual vars
        // Priority: env vars > db.properties > hardcoded defaults
        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        String url    = props.getProperty("db.url",
                "jdbc:mysql://localhost:3306/usermanagement_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
        String user   = props.getProperty("db.username", "root");
        String pass   = props.getProperty("db.password", "root123");

        // 1. Full JDBC URL (Railway sets MYSQL_URL or DATABASE_URL in jdbc:mysql://... format)
        String envUrl = System.getenv("MYSQL_URL");
        if (envUrl == null || envUrl.isBlank()) envUrl = System.getenv("DATABASE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            // Railway sets MYSQL_URL / DATABASE_URL as "mysql://..." (no jdbc: prefix).
            // HikariCP requires "jdbc:mysql://...", so we normalise it here.
            if (envUrl.startsWith("mysql://") || envUrl.startsWith("mysql+tcp://")) {
                envUrl = "jdbc:" + envUrl;
                logger.info("Prepended 'jdbc:' prefix to env URL (Railway format detected).");
            }
            url = envUrl;
            logger.info("Using JDBC URL from environment variable.");
        } else {
            // 2. Individual Railway MySQL vars: MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD
            String host = System.getenv("MYSQLHOST");
            if (host == null || host.isBlank()) host = System.getenv("MYSQL_HOST");
            String portStr = System.getenv("MYSQLPORT");
            if (portStr == null || portStr.isBlank()) portStr = System.getenv("MYSQL_PORT");
            String dbName = System.getenv("MYSQLDATABASE");
            if (dbName == null || dbName.isBlank()) dbName = System.getenv("MYSQL_DATABASE");
            if (host != null && !host.isBlank()) {
                String port = (portStr != null && !portStr.isBlank()) ? portStr : "3306";
                String db   = (dbName  != null && !dbName.isBlank())  ? dbName  : "usermanagement_db";
                url = "jdbc:mysql://" + host + ":" + port + "/" + db
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
                logger.info("Built JDBC URL from MYSQLHOST/MYSQLPORT/MYSQLDATABASE env vars: jdbc:mysql://{}:{}/{}", host, port, db);
            }
        }

        // 3. Credentials
        String envUser = System.getenv("MYSQLUSER");
        if (envUser == null || envUser.isBlank()) envUser = System.getenv("MYSQL_USER");
        if (envUser != null && !envUser.isBlank()) user = envUser;

        String envPass = System.getenv("MYSQLPASSWORD");
        if (envPass == null || envPass.isBlank()) envPass = System.getenv("MYSQL_PASSWORD");
        if (envPass == null || envPass.isBlank()) envPass = System.getenv("MYSQL_ROOT_PASSWORD");
        if (envPass != null && !envPass.isBlank()) pass = envPass;
        // --- End Railway overrides ---

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
