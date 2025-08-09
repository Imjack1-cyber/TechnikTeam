package de.technikteam.dao;

import de.technikteam.model.AdminLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdminLogDAO {
	private static final Logger logger = LogManager.getLogger(AdminLogDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AdminLogDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void createLog(AdminLog log) {
		String sql = "INSERT INTO admin_logs (admin_username, action_type, details) VALUES (?, ?, ?)";
		try {
			jdbcTemplate.update(sql, log.getAdminUsername(), log.getActionType(), log.getDetails());
		} catch (Exception e) {
			logger.error("Failed to create admin log for user '{}'.", log.getAdminUsername(), e);
		}
	}

	public List<AdminLog> getAllLogs() {
		String sql = "SELECT id, admin_username, action_type, details, action_timestamp FROM admin_logs ORDER BY action_timestamp DESC";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				AdminLog logEntry = new AdminLog();
				logEntry.setId(rs.getInt("id"));
				logEntry.setAdminUsername(rs.getString("admin_username"));
				logEntry.setActionType(rs.getString("action_type"));
				logEntry.setDetails(rs.getString("details"));
				logEntry.setActionTimestamp(rs.getTimestamp("action_timestamp").toLocalDateTime());
				return logEntry;
			});
		} catch (Exception e) {
			logger.error("Failed to fetch admin logs from the database.", e);
			return List.of();
		}
	}

	public List<AdminLog> getRecentLogs(int limit) {
		String sql = "SELECT id, admin_username, action_type, details, action_timestamp FROM admin_logs ORDER BY action_timestamp DESC LIMIT ?";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				AdminLog logEntry = new AdminLog();
				logEntry.setId(rs.getInt("id"));
				logEntry.setAdminUsername(rs.getString("admin_username"));
				logEntry.setActionType(rs.getString("action_type"));
				logEntry.setDetails(rs.getString("details"));
				logEntry.setActionTimestamp(rs.getTimestamp("action_timestamp").toLocalDateTime());
				return logEntry;
			}, limit);
		} catch (Exception e) {
			logger.error("Failed to fetch recent admin logs from the database.", e);
			return List.of();
		}
	}
}