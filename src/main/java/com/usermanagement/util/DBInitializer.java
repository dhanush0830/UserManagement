package com.usermanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes database tables and guarantees verified seed accounts upon application startup.
 */
public class DBInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DBInitializer.class);
    private static volatile boolean initialized = false;

    // Verified BCrypt Hashes (Cost factor 10)
    public static final String ADMIN_HASH = "$2a$10$oBPFqFwWB/.qIDFnMyr5neICAvgd/HonvjSIxIR.Sz5.x7TyXRqBm";   // Admin@123
    public static final String MANAGER_HASH = "$2a$10$zlOd64undL1ogQUywBvHie45/0rDFnA3fuIOJirNIu/CkXZNCGd8C"; // Manager@123
    public static final String USER_HASH = "$2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y";    // User@123

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try (Connection conn = DBConnectionManager.getInstance().getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            logger.info("Connected to database: {} {}", meta.getDatabaseProductName(), meta.getDatabaseProductVersion());

            // Guarantee users table exists
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50) NOT NULL UNIQUE, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "salt VARCHAR(64) DEFAULT NULL, " +
                        "full_name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                        "role VARCHAR(20) NOT NULL DEFAULT 'USER', " +
                        "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS activity_logs (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT, " +
                        "username VARCHAR(50), " +
                        "action VARCHAR(50) NOT NULL, " +
                        "details VARCHAR(255), " +
                        "ip_address VARCHAR(45), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                logger.info("Database tables 'users' and 'activity_logs' confirmed ready.");
            } catch (SQLException e) {
                logger.error("Failed executing table creation DDL: {}", e.getMessage(), e);
            }

            // Always guarantee seed accounts have verified password hashes and ACTIVE status
            syncSeedAccounts(conn);

            initialized = true;
        } catch (Exception e) {
            logger.error("Error during database initialization: {}", e.getMessage(), e);
        }
    }

    /**
     * Guarantees default demo accounts (admin, john_doe, jane_smith) are present,
     * active, and have valid BCrypt password hashes.
     */
    private static void syncSeedAccounts(Connection conn) {
        syncUser(conn, "admin", ADMIN_HASH, "adminsalt", "Administrator", "admin@enterprise.com", "ADMIN", "ACTIVE");
        syncUser(conn, "john_doe", MANAGER_HASH, "managersalt", "Johnathan Doe", "john.doe@enterprise.com", "MANAGER", "ACTIVE");
        syncUser(conn, "jane_smith", USER_HASH, "usersalt", "Jane Smith", "jane.smith@enterprise.com", "USER", "ACTIVE");
        syncUser(conn, "robert_chen", USER_HASH, "usersalt", "Robert Chen", "robert.chen@enterprise.com", "USER", "INACTIVE");
        syncUser(conn, "sarah_connor", USER_HASH, "usersalt", "Sarah Connor", "sarah.connor@cyberdyne.io", "USER", "ACTIVE");
        logger.info("Seed accounts synchronized successfully.");
    }

    private static void syncUser(Connection conn, String username, String hash, String salt,
                                 String fullName, String email, String role, String status) {
        String checkSql = "SELECT id FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    // Update existing account to ensure password hash and status are valid
                    String updateSql = "UPDATE users SET password_hash = ?, salt = ?, full_name = ?, role = ?, status = ? WHERE LOWER(username) = LOWER(?)";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, hash);
                        updatePs.setString(2, salt);
                        updatePs.setString(3, fullName);
                        updatePs.setString(4, role);
                        updatePs.setString(5, status);
                        updatePs.setString(6, username);
                        updatePs.executeUpdate();
                    }
                } else {
                    // Insert new account
                    String insertSql = "INSERT INTO users (username, password_hash, salt, full_name, email, role, status, created_at, updated_at) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setString(1, username);
                        insertPs.setString(2, hash);
                        insertPs.setString(3, salt);
                        insertPs.setString(4, fullName);
                        insertPs.setString(5, email);
                        insertPs.setString(6, role);
                        insertPs.setString(7, status);
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not sync seed user {}: {}", username, e.getMessage());
        }
    }

    private static void executeSqlScript(Connection conn, String scriptPath) {
        try (InputStream is = DBInitializer.class.getClassLoader().getResourceAsStream(scriptPath)) {
            if (is == null) {
                logger.error("SQL script not found on classpath: {}", scriptPath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {

                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("//")) {
                        continue;
                    }
                    sqlBuilder.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String sql = sqlBuilder.toString().trim();
                        sql = sql.substring(0, sql.length() - 1);
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (Exception ex) {
                                logger.debug("Statement notice or non-fatal exception: {} - {}", sql, ex.getMessage());
                            }
                        }
                        sqlBuilder.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to execute script: {}", scriptPath, e);
        }
    }
}
