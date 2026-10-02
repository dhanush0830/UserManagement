package com.usermanagement.dao;

import com.usermanagement.model.ActivityLog;
import com.usermanagement.util.DBConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for audit trail / activity logging.
 */
public class ActivityLogDAO {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogDAO.class);

    public boolean log(Integer userId, String username, String action, String details, String ipAddress) {
        String sql = "INSERT INTO activity_logs (user_id, username, action, details, ip_address, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, username);
            ps.setString(3, action);
            ps.setString(4, details);
            ps.setString(5, ipAddress);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to insert activity log", e);
            return false;
        }
    }

    public List<ActivityLog> getRecentLogs(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT id, user_id, username, action, details, ip_address, created_at " +
                     "FROM activity_logs ORDER BY id DESC LIMIT ?";

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActivityLog log = new ActivityLog();
                    log.setId(rs.getInt("id"));
                    int uid = rs.getInt("user_id");
                    if (!rs.wasNull()) {
                        log.setUserId(uid);
                    }
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setDetails(rs.getString("details"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to query recent activity logs", e);
        }
        return logs;
    }
}
