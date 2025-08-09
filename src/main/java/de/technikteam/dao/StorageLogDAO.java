package de.technikteam.dao;

import de.technikteam.model.StorageLogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class StorageLogDAO {
	private static final Logger logger = LogManager.getLogger(StorageLogDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public StorageLogDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean logTransaction(int itemId, int userId, int quantityChange, String notes, int eventId) {
		String sql = "INSERT INTO storage_log (item_id, user_id, quantity_change, notes, event_id) VALUES (?, ?, ?, ?, ?)";
		try {
			Object eventIdObj = eventId > 0 ? eventId : null;
			int[] types = { Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.INTEGER };
			return jdbcTemplate.update(sql, new Object[] { itemId, userId, quantityChange, notes, eventIdObj },
					types) > 0;
		} catch (Exception e) {
			logger.error("Error logging storage transaction for item {}", itemId, e);
			return false;
		}
	}

	public List<StorageLogEntry> getHistoryForItem(int itemId) {
		String sql = "SELECT sl.*, u.username FROM storage_log sl JOIN users u ON sl.user_id = u.id WHERE sl.item_id = ? ORDER BY sl.transaction_timestamp DESC";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				StorageLogEntry entry = new StorageLogEntry();
				entry.setId(rs.getInt("id"));
				entry.setItemId(rs.getInt("item_id"));
				entry.setUserId(rs.getInt("user_id"));
				entry.setUsername(rs.getString("username"));
				entry.setQuantityChange(rs.getInt("quantity_change"));
				entry.setNotes(rs.getString("notes"));
				entry.setEventId(rs.getInt("event_id"));
				entry.setTransactionTimestamp(rs.getTimestamp("transaction_timestamp").toLocalDateTime());
				return entry;
			}, itemId);
		} catch (Exception e) {
			logger.error("SQL error fetching storage history for item ID {}", itemId, e);
			return List.of();
		}
	}
}