package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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

@Singleton
public class StorageLogDAO {
	private static final Logger logger = LogManager.getLogger(StorageLogDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public StorageLogDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public boolean logTransaction(int itemId, int userId, int quantityChange, String notes, int eventId,
			Connection conn) throws SQLException {
		String sql = "INSERT INTO storage_log (item_id, user_id, quantity_change, notes, event_id) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
		}
	}

	public List<StorageLogEntry> getHistoryForItem(int itemId) {
		List<StorageLogEntry> history = new ArrayList<>();
		String sql = "SELECT sl.*, u.username FROM storage_log sl JOIN users u ON sl.user_id = u.id WHERE sl.item_id = ? ORDER BY sl.transaction_timestamp DESC";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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