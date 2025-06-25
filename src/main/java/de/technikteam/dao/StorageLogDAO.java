package de.technikteam.dao;

import de.technikteam.model.StorageLogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for logging storage item transactions (check-ins and
 * check-outs) into the `storage_log` table. This provides a history of
 * inventory movements.
 */
public class StorageLogDAO {
	private static final Logger logger = LogManager.getLogger(StorageLogDAO.class);

	/**
	 * Logs a single transaction (check-in or check-out) to the database.
	 * 
	 * @param itemId         The ID of the item involved in the transaction.
	 * @param userId         The ID of the user performing the transaction.
	 * @param quantityChange The number of items moved (positive for check-in,
	 *                       negative for check-out).
	 * @param notes          Optional notes for the transaction (e.g., purpose,
	 *                       event).
	 * @param eventId        Optional ID of the event this transaction is for.
	 * @return true if the log entry was created successfully.
	 */
	public boolean logTransaction(int itemId, int userId, int quantityChange, String notes, int eventId) {
		String sql = "INSERT INTO storage_log (item_id, user_id, quantity_change, notes, event_id) VALUES (?, ?, ?, ?, ?)";
		logger.debug("Logging storage transaction for item {}, user {}, change {}, event {}", itemId, userId,
				quantityChange, eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			pstmt.setInt(2, userId);
			pstmt.setInt(3, quantityChange);
			pstmt.setString(4, notes);
			if (eventId > 0) {
				pstmt.setInt(5, eventId);
			} else {
				pstmt.setNull(5, Types.INTEGER);
			}
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Failed to log storage transaction for item {}", itemId, e);
			return false;
		}
	}

	/**
	 * Fetches the transaction history for a specific storage item.
	 * 
	 * @param itemId The ID of the item.
	 * @return A list of storage log entries.
	 */
	public List<StorageLogEntry> getHistoryForItem(int itemId) {
		List<StorageLogEntry> history = new ArrayList<>();
		String sql = "SELECT sl.*, u.username FROM storage_log sl " + "JOIN users u ON sl.user_id = u.id "
				+ "WHERE sl.item_id = ? ORDER BY sl.transaction_timestamp DESC";
		logger.debug("Fetching storage history for item ID: {}", itemId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					StorageLogEntry entry = new StorageLogEntry();
					entry.setId(rs.getInt("id"));
					entry.setItemId(rs.getInt("item_id"));
					entry.setUserId(rs.getInt("user_id"));
					entry.setUsername(rs.getString("username"));
					entry.setQuantityChange(rs.getInt("quantity_change"));
					entry.setNotes(rs.getString("notes"));
					entry.setEventId(rs.getInt("event_id"));
					entry.setTransactionTimestamp(rs.getTimestamp("transaction_timestamp").toLocalDateTime());
					history.add(entry);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching storage history for item ID {}", itemId, e);
		}
		return history;
	}
}