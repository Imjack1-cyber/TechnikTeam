package de.technikteam.dao;

import de.technikteam.model.MaintenanceLogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MaintenanceLogDAO {
	private static final Logger logger = LogManager.getLogger(MaintenanceLogDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public MaintenanceLogDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean createLog(MaintenanceLogEntry log) {
		String sql = "INSERT INTO maintenance_log (item_id, user_id, action, notes, cost) VALUES (?, ?, ?, ?, ?)";
		try {
			return jdbcTemplate.update(sql, log.getItemId(), log.getUserId(), log.getAction(), log.getNotes(),
					log.getCost()) > 0;
		} catch (Exception e) {
			logger.error("Error creating maintenance log for item {}", log.getItemId(), e);
			return false;
		}
	}

	public List<MaintenanceLogEntry> getHistoryForItem(int itemId) {
		String sql = "SELECT ml.*, u.username FROM maintenance_log ml JOIN users u ON ml.user_id = u.id WHERE ml.item_id = ? ORDER BY ml.log_date DESC";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				MaintenanceLogEntry entry = new MaintenanceLogEntry();
				entry.setId(rs.getInt("id"));
				entry.setItemId(rs.getInt("item_id"));
				entry.setUserId(rs.getInt("user_id"));
				entry.setUsername(rs.getString("username"));
				entry.setLogDate(rs.getTimestamp("log_date").toLocalDateTime());
				entry.setAction(rs.getString("action"));
				entry.setNotes(rs.getString("notes"));
				entry.setCost(rs.getDouble("cost"));
				return entry;
			}, itemId);
		} catch (Exception e) {
			logger.error("Error fetching maintenance history for item {}", itemId, e);
			return List.of();
		}
	}
}