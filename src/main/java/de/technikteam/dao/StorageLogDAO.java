package de.technikteam.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
	 * @return true if the log entry was created successfully.
	 */
	public boolean logTransaction(int itemId, int userId, int quantityChange, String notes) {
		String sql = "INSERT INTO storage_log (item_id, user_id, quantity_change, notes) VALUES (?, ?, ?, ?)";
		logger.debug("Logging storage transaction for item {}, user {}, change {}", itemId, userId, quantityChange);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			pstmt.setInt(2, userId);
			pstmt.setInt(3, quantityChange);
			pstmt.setString(4, notes);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Failed to log storage transaction for item {}", itemId, e);
			return false;
		}
	}
}