package de.technikteam.dao;

import de.technikteam.model.AdminLog;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

	private final RowMapper<AdminLog> rowMapper = (rs, rowNum) -> {
		AdminLog logEntry = new AdminLog();
		logEntry.setId(rs.getInt("id"));
		logEntry.setAdminUsername(rs.getString("admin_username"));
		logEntry.setActionType(rs.getString("action_type"));
		logEntry.setDetails(rs.getString("details"));
		logEntry.setActionTimestamp(rs.getTimestamp("action_timestamp").toLocalDateTime());
		logEntry.setStatus(rs.getString("status"));
		logEntry.setContext(rs.getString("context"));
		logEntry.setRevokedByAdminId(rs.getObject("revoked_by_admin_id", Integer.class));
		if (rs.getTimestamp("revoked_at") != null) {
			logEntry.setRevokedAt(rs.getTimestamp("revoked_at").toLocalDateTime());
		}
		if (DaoUtils.hasColumn(rs, "revoking_admin_username")) {
			logEntry.setRevokingAdminUsername(rs.getString("revoking_admin_username"));
		}
		return logEntry;
	};

	public void createLog(AdminLog log) {
		String sql = "INSERT INTO admin_logs (admin_username, action_type, details, context) VALUES (?, ?, ?, ?)";
		try {
			jdbcTemplate.update(sql, log.getAdminUsername(), log.getActionType(), log.getDetails(), log.getContext());
		} catch (Exception e) {
			logger.error("Failed to create admin log for user '{}'.", log.getAdminUsername(), e);
		}
	}

	public List<AdminLog> getAllLogs() {
		String sql = "SELECT l.*, a.username as revoking_admin_username " + "FROM admin_logs l "
				+ "LEFT JOIN users a ON l.revoked_by_admin_id = a.id " + "ORDER BY l.action_timestamp DESC";
		try {
			return jdbcTemplate.query(sql, rowMapper);
		} catch (Exception e) {
			logger.error("Failed to fetch admin logs from the database.", e);
			return List.of();
		}
	}

	public AdminLog getLogById(long logId) {
		String sql = "SELECT l.*, a.username as revoking_admin_username " + "FROM admin_logs l "
				+ "LEFT JOIN users a ON l.revoked_by_admin_id = a.id " + "WHERE l.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, rowMapper, logId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Failed to fetch admin log with ID: {}", logId, e);
			return null;
		}
	}

	public boolean updateStatus(long logId, String status, int revokingAdminId) {
		String sql = "UPDATE admin_logs SET status = ?, revoked_by_admin_id = ?, revoked_at = NOW() WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, status, revokingAdminId, logId) > 0;
		} catch (Exception e) {
			logger.error("Failed to update status for log ID: {}", logId, e);
			return false;
		}
	}

	public List<AdminLog> getRecentLogs(int limit) {
		String sql = "SELECT l.*, a.username as revoking_admin_username " + "FROM admin_logs l "
				+ "LEFT JOIN users a ON l.revoked_by_admin_id = a.id " + "ORDER BY l.action_timestamp DESC LIMIT ?";
		try {
			return jdbcTemplate.query(sql, rowMapper, limit);
		} catch (Exception e) {
			logger.error("Failed to fetch recent admin logs from the database.", e);
			return List.of();
		}
	}
}