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

import de.technikteam.model.AdminLog; // Sie müssen dieses Model noch erstellen

/**
 * Data Access Object for handling admin log operations.
 */
public class AdminLogDAO {
	private static final Logger logger = LogManager.getLogger(AdminLogDAO.class);

	/**
	 * Creates a new log entry in the database.
	 * 
	 * @param adminUsername The username of the admin performing the action.
	 * @param actionType    A description of the action (e.g., "USER_DELETED").
	 * @param targetEntity  The entity that was affected (e.g., the username
	 *                      "j_mueller").
	 * @param details       Additional details about the action.
	 * @return true if the log was created successfully, false otherwise.
	 */
	public boolean createLog(String adminUsername, String actionType, String targetEntity, String details) {
		logger.info("Logging admin action: [{}] performed by '{}' on '{}'", actionType, adminUsername, targetEntity);
		String sql = "INSERT INTO admin_logs (admin_username, action_type, target_entity, details) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, adminUsername);
			pstmt.setString(2, actionType);
			pstmt.setString(3, targetEntity);
			pstmt.setString(4, details);

			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			logger.error("SQL error while creating admin log entry.", e);
			return false;
		}
	}

	/**
	 * Fetches all log entries from the database, ordered by the most recent first.
	 * 
	 * @return A list of AdminLog objects.
	 */
	public List<AdminLog> getAllLogs() {
		logger.debug("Fetching all admin logs.");
		List<AdminLog> logs = new ArrayList<>();
		String sql = "SELECT * FROM admin_logs ORDER BY action_timestamp DESC";

		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				AdminLog log = new AdminLog();
				log.setId(rs.getInt("id"));
				log.setAdminUsername(rs.getString("admin_username"));
				log.setActionType(rs.getString("action_type"));
				log.setTargetEntity(rs.getString("target_entity"));
				log.setActionTimestamp(rs.getTimestamp("action_timestamp").toLocalDateTime());
				log.setDetails(rs.getString("details"));
				logs.add(log);
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching all admin logs.", e);
		}
		return logs;
	}
}