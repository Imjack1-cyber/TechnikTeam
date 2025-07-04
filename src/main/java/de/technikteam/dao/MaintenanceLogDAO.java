package de.technikteam.dao;

import de.technikteam.model.MaintenanceLogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing maintenance log entries in the `maintenance_log` table.
 */
public class MaintenanceLogDAO {
	private static final Logger logger = LogManager.getLogger(MaintenanceLogDAO.class);

	public boolean createLog(MaintenanceLogEntry log) {
		String sql = "INSERT INTO maintenance_log (item_id, user_id, action, notes, cost) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, log.getItemId());
			pstmt.setInt(2, log.getUserId());
			pstmt.setString(3, log.getAction());
			pstmt.setString(4, log.getNotes());
			pstmt.setDouble(5, log.getCost());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error creating maintenance log for item {}", log.getItemId(), e);
			return false;
		}
	}

	public List<MaintenanceLogEntry> getHistoryForItem(int itemId) {
		List<MaintenanceLogEntry> history = new ArrayList<>();
		String sql = "SELECT ml.*, u.username FROM maintenance_log ml " + "JOIN users u ON ml.user_id = u.id "
				+ "WHERE ml.item_id = ? ORDER BY ml.log_date DESC";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					MaintenanceLogEntry entry = new MaintenanceLogEntry();
					entry.setId(rs.getInt("id"));
					entry.setItemId(rs.getInt("item_id"));
					entry.setUserId(rs.getInt("user_id"));
					entry.setUsername(rs.getString("username"));
					entry.setLogDate(rs.getTimestamp("log_date").toLocalDateTime());
					entry.setAction(rs.getString("action"));
					entry.setNotes(rs.getString("notes"));
					entry.setCost(rs.getDouble("cost"));
					history.add(entry);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching maintenance history for item {}", itemId, e);
		}
		return history;
	}
}