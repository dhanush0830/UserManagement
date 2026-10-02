package com.usermanagement.dao;

import com.usermanagement.model.User;
import com.usermanagement.util.DBConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JDBC Implementation of UserDAO.
 * Uses PreparedStatements for all parameterized queries to safeguard against SQL Injection.
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    // Whitelist allowed sort columns to prevent SQL injection in ORDER BY clauses
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
            "id", "username", "full_name", "email", "role", "status", "created_at"
    );

    @Override
    public User findById(int id) {
        String sql = "SELECT id, username, password_hash, salt, full_name, email, role, status, created_at, updated_at " +
                     "FROM users WHERE id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by ID: {}", id, e);
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id, username, password_hash, salt, full_name, email, role, status, created_at, updated_at " +
                     "FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new RuntimeException("Database error looking up user: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id, username, password_hash, salt, full_name, email, role, status, created_at, updated_at " +
                     "FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
        }
        return null;
    }

    @Override
    public boolean isUsernameTaken(String username, int excludeUserId) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND id != ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking username uniqueness: {}", username, e);
            return true;
        }
    }

    @Override
    public boolean isEmailTaken(String email, int excludeUserId) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) AND id != ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking email uniqueness: {}", email, e);
            return true;
        }
    }

    @Override
    public List<User> findAll(String search, String roleFilter, String statusFilter,
                              String sortBy, String sortOrder, int page, int pageSize) {
        List<User> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT id, username, password_hash, salt, full_name, email, role, status, created_at, updated_at " +
                "FROM users WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        // Dynamic Filtering
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(username) LIKE ? OR LOWER(full_name) LIKE ? OR LOWER(email) LIKE ?) ");
            String wildcard = "%" + search.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        if (roleFilter != null && !roleFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(roleFilter)) {
            sql.append("AND role = ? ");
            params.add(roleFilter.trim().toUpperCase());
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append("AND status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }

        // Whitelisted Safe Sorting
        String cleanSortBy = (sortBy != null && ALLOWED_SORT_COLUMNS.contains(sortBy.toLowerCase()))
                ? sortBy.toLowerCase() : "id";
        String cleanSortOrder = ("DESC".equalsIgnoreCase(sortOrder)) ? "DESC" : "ASC";
        sql.append("ORDER BY ").append(cleanSortBy).append(" ").append(cleanSortOrder).append(" ");

        // Safe Pagination (Limit / Offset)
        int validPage = Math.max(1, page);
        int validPageSize = Math.max(1, Math.min(100, pageSize));
        int offset = (validPage - 1) * validPageSize;

        sql.append("LIMIT ? OFFSET ?");
        params.add(validPageSize);
        params.add(offset);

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing findAll query: {}", e.getMessage(), e);
        }

        return list;
    }

    @Override
    public int count(String search, String roleFilter, String statusFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(username) LIKE ? OR LOWER(full_name) LIKE ? OR LOWER(email) LIKE ?) ");
            String wildcard = "%" + search.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        if (roleFilter != null && !roleFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(roleFilter)) {
            sql.append("AND role = ? ");
            params.add(roleFilter.trim().toUpperCase());
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append("AND status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing count query: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean create(User user) {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, email, role, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getRole() != null ? user.getRole() : "USER");
            ps.setString(7, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error inserting user: {}", user.getUsername(), e);
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getStatus());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user ID: {}", user.getId(), e);
        }
        return false;
    }

    @Override
    public boolean updatePassword(int id, String passwordHash, String salt) {
        String sql = "UPDATE users SET password_hash = ?, salt = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password for user ID: {}", id, e);
        }
        return false;
    }

    @Override
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.toUpperCase());
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for user ID: {}", id, e);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user ID: {}", id, e);
        }
        return false;
    }

    @Override
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", 0);
        stats.put("active", 0);
        stats.put("inactive", 0);
        stats.put("admins", 0);

        String sql = "SELECT " +
                     "COUNT(*) AS total_count, " +
                     "SUM(CASE WHEN UPPER(status) = 'ACTIVE' THEN 1 ELSE 0 END) AS active_count, " +
                     "SUM(CASE WHEN UPPER(status) = 'INACTIVE' THEN 1 ELSE 0 END) AS inactive_count, " +
                     "SUM(CASE WHEN UPPER(role) = 'ADMIN' THEN 1 ELSE 0 END) AS admin_count " +
                     "FROM users";

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats.put("total", rs.getInt("total_count"));
                stats.put("active", rs.getInt("active_count"));
                stats.put("inactive", rs.getInt("inactive_count"));
                stats.put("admins", rs.getInt("admin_count"));
            }
        } catch (SQLException e) {
            logger.error("Error calculating user statistics", e);
        }
        return stats;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
