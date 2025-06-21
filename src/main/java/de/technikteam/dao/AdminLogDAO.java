package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.AdminLog;

/**
 * A Data Access Object (DAO) responsible for all database interactions with the
 * admin_logs table. It provides methods to create new log entries and retrieve
 * all existing logs.
 */
public class AdminLogDAO {
	private static final Logger logger = LogManager.getLogger(AdminLogDAO.class);

	/**
	 * Inserts a new log entry into the database. This should be called after an
	 * admin action.
	 * 
	 * @param adminUsername The username of the admin performing the action.
	 * @param action        A description of the action (e.g., "Created User").
	 * @param targetType    The type of entity affected (e.g., "User", "Event").
	 * @param targetId      The ID of the affected entity.
	 */
	public void logAction(String adminUsername, String action, String targetType, int targetId) {
		logger.info("Logging admin action: [{}] performed action [{}] on {} #{}", adminUsername, action, targetType,
				targetId);
		String sql = "INSERT INTO admin_logs (admin_username, action, target_type, target_id) VALUES (?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, adminUsername);
			pstmt.setString(2, action);
			pstmt.setString(3, targetType);
			pstmt.setInt(4, targetId);

			pstmt.executeUpdate();

		} catch (SQLException e) {
			logger.error("Failed to log admin action.", e);
		}
	}

	/**
	 * Fetches all log entries from the database, newest first.
	 * 
	 * @return A list of AdminLog objects.
	 */
	// Ersetzen Sie die getAllLogs-Methode
	public List<AdminLog> getAllLogs() {
		List<AdminLog> logs = new ArrayList<>();
		// FIX: Spaltenname von 'log_timestamp' zu 'action_timestamp' korrigiert
		String sql = "SELECT * FROM admin_logs ORDER BY action_timestamp DESC";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			// The loop iterates through each row returned by the SQL query
			while (rs.next()) {
				// Step 2: Create a new AdminLog Java object for this row
				AdminLog logEntry = new AdminLog();

				// Steps 1 & 3: Extract data from the row and populate the object
				logEntry.setId(rs.getInt("id")); // Assumes an 'id' column exists
				logEntry.setAdminUsername(rs.getString("admin_username"));
				logEntry.setDetails(rs.getString("details"));
				logEntry.setActionTimestamp(rs.getTimestamp("action_timestamp").toLocalDateTime());

				// Step 4: Add the fully populated object to the list
				logs.add(logEntry);
			}
		} catch (SQLException e) {
			logger.error("Failed to fetch admin logs.", e);
		}
		return logs;
	}

	// Fügen Sie diese Methode zu Ihrem AdminLogDAO hinzu
	public void createLog(AdminLog log) {
		String sql = "INSERT INTO admin_logs (admin_username, action_type, details) VALUES (?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, log.getAdminUsername());
			pstmt.setString(2, log.getActionType());
			pstmt.setString(3, log.getDetails());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to create admin log.", e);
		}
	}
}