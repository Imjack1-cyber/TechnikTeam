package de.technikteam.dao;

import de.technikteam.dao.DatabaseManager;
import de.technikteam.model.AdminLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Data Access Object (DAO) responsible for all database interactions with the
 * `admin_logs` table. It provides methods to create new log entries, which are
 * used for auditing administrative actions, and to retrieve all existing logs
 * for display in the admin panel.
 */
public class AdminLogDAO {
	private static final Logger logger = LogManager.getLogger(AdminLogDAO.class);

	/**
	 * Creates a new log entry in the database. This is the primary method for
	 * recording an administrative action.
	 * 
	 * @param log The AdminLog object to persist.
	 */
	public void createLog(AdminLog log) {
		String sql = "INSERT INTO admin_logs (admin_username, action_type, details) VALUES (?, ?, ?)";
		logger.debug("Attempting to create admin log: [User: {}, Action: {}]", log.getAdminUsername(),
				log.getActionType());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, log.getAdminUsername());
			pstmt.setString(2, log.getActionType());
			pstmt.setString(3, log.getDetails());
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				logger.info("Successfully created admin log for user '{}'.", log.getAdminUsername());
			}
		} catch (SQLException e) {
			logger.error("Failed to create admin log for user '{}'. Details: {}", log.getAdminUsername(),
					log.getDetails(), e);
		}
	}

	/**
	 * Fetches all log entries from the database, ordered with the newest first.
	 * 
	 * @return A list of AdminLog objects.
	 */
	public List<AdminLog> getAllLogs() {
		List<AdminLog> logs = new ArrayList<>();
		String sql = "SELECT id, admin_username, action_type, details, action_timestamp FROM admin_logs ORDER BY action_timestamp DESC";
		logger.debug("Executing query to fetch all admin logs.");
		try (Connection conn = DatabaseManager.getConnection();
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
			logger.info("Fetched {} admin log entries from the database.", logs.size());
		} catch (SQLException e) {
			logger.error("Failed to fetch admin logs from the database.", e);
		}
		return logs;
	}

	/**
	 * Fetches the most recent log entries from the database.
	 *
	 * @param limit The maximum number of log entries to return.
	 * @return A list of AdminLog objects.
	 */
	public List<AdminLog> getRecentLogs(int limit) {
		List<AdminLog> logs = new ArrayList<>();
		String sql = "SELECT id, admin_username, action_type, details, action_timestamp FROM admin_logs ORDER BY action_timestamp DESC LIMIT ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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