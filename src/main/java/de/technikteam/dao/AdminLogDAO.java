package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.AdminLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AdminLogDAO {
	private static final Logger logger = LogManager.getLogger(AdminLogDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public AdminLogDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public void createLog(AdminLog log) {
		String sql = "INSERT INTO admin_logs (admin_username, action_type, details) VALUES (?, ?, ?)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, log.getAdminUsername());
			pstmt.setString(2, log.getActionType());
			pstmt.setString(3, log.getDetails());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to create admin log for user '{}'.", log.getAdminUsername(), e);
		}
	}

	public List<AdminLog> getAllLogs() {
		List<AdminLog> logs = new ArrayList<>();
		String sql = "SELECT id, admin_username, action_type, details, action_timestamp FROM admin_logs ORDER BY action_timestamp DESC";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				AdminLog logEntry = new AdminLog();
				logEntry.setId(rs.getInt("id"));
				logEntry.setAdminUsername(rs.getString("admin_username"));
				logEntry.setActionType(rs.getString("action_type"));
				logEntry.setDetails(rs.getString("details"));
				logEntry.setActionTimestamp(rs.getTimestamp("action_timestamp").toLocalDateTime());
				logs.add(logEntry);
			}
		} catch (SQLException e) {
			logger.error("Failed to fetch admin logs from the database.", e);
		}
		return logs;
	}

	public List<AdminLog> getRecentLogs(int limit) {
		List<AdminLog> logs = new ArrayList<>();
		String sql = "SELECT id, admin_username, action_type, details, action_timestamp FROM admin_logs ORDER BY action_timestamp DESC LIMIT ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, limit);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					AdminLog logEntry = new AdminLog();
					logEntry.setId(rs.getInt("id"));
					logEntry.setAdminUsername(rs.getString("admin_username"));
					logEntry.setActionType(rs.getString("action_type"));
					logEntry.setDetails(rs.getString("details"));
					logEntry.setActionTimestamp(rs.getTimestamp("action_timestamp").toLocalDateTime());
					logs.add(logEntry);
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to fetch recent admin logs from the database.", e);
		}
		return logs;
	}
}