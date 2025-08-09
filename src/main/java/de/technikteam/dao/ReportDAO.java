package de.technikteam.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ReportDAO {
	private static final Logger logger = LogManager.getLogger(ReportDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ReportDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Map<String, Object>> getEventParticipationSummary() {
		String sql = "SELECT e.name AS event_name, COUNT(ea.user_id) AS participant_count " + "FROM events e "
				+ "LEFT JOIN event_assignments ea ON e.id = ea.event_id " + "GROUP BY e.id, e.name "
				+ "ORDER BY participant_count DESC, e.event_datetime DESC";
		try {
			return jdbcTemplate.query(sql, new ColumnMapRowMapper());
		} catch (Exception e) {
			logger.error("Error generating event participation summary.", e);
			return List.of();
		}
	}

	public List<Map<String, Object>> getUserActivityStats() {
		String sql = "SELECT u.username, " + "COUNT(DISTINCT ea.event_id) AS events_signed_up, "
				+ "COUNT(DISTINCT ma.meeting_id) AS meetings_attended " + "FROM users u "
				+ "LEFT JOIN event_attendance ea ON u.id = ea.user_id AND ea.signup_status = 'ANGEMELDET' "
				+ "LEFT JOIN meeting_attendance ma ON u.id = ma.user_id AND ma.attended = 1 "
				+ "GROUP BY u.id, u.username " + "ORDER BY events_signed_up DESC, meetings_attended DESC";
		try {
			return jdbcTemplate.query(sql, new ColumnMapRowMapper());
		} catch (Exception e) {
			logger.error("Error generating user activity stats.", e);
			return List.of();
		}
	}

	public List<Map<String, Object>> getInventoryUsageFrequency() {
		String sql = "SELECT si.name AS item_name, SUM(ABS(sl.quantity_change)) AS total_quantity_checked_out "
				+ "FROM storage_items si " + "JOIN storage_log sl ON si.id = sl.item_id "
				+ "WHERE sl.quantity_change < 0 " + "GROUP BY si.id, si.name "
				+ "ORDER BY total_quantity_checked_out DESC";
		try {
			return jdbcTemplate.query(sql, new ColumnMapRowMapper());
		} catch (Exception e) {
			logger.error("Error generating inventory usage frequency report.", e);
			return List.of();
		}
	}

	public double getTotalInventoryValue() {
		String sql = "SELECT SUM(quantity * price_eur) AS total_value FROM storage_items";
		try {
			Double totalValue = jdbcTemplate.queryForObject(sql, Double.class);
			return totalValue != null ? totalValue : 0.0;
		} catch (Exception e) {
			logger.error("Error calculating total inventory value.", e);
			return 0.0;
		}
	}

	public List<Map<String, Object>> getEventCountByMonth(int months) {
		String sql = "SELECT DATE_FORMAT(event_datetime, '%Y-%m') AS month, COUNT(*) AS count " + "FROM events "
				+ "WHERE event_datetime >= DATE_SUB(NOW(), INTERVAL ? MONTH) "
				+ "GROUP BY YEAR(event_datetime), MONTH(event_datetime) " + "ORDER BY month ASC";
		try {
			return jdbcTemplate.query(sql, new ColumnMapRowMapper(), months);
		} catch (Exception e) {
			logger.error("Error generating event count by month report.", e);
			return List.of();
		}
	}

	public List<Map<String, Object>> getUserParticipationStats(int limit) {
		String sql = "SELECT u.username, COUNT(ea.user_id) as participation_count " + "FROM event_assignments ea "
				+ "JOIN users u ON ea.user_id = u.id " + "GROUP BY u.id, u.username "
				+ "ORDER BY participation_count DESC " + "LIMIT ?";
		try {
			return jdbcTemplate.query(sql, new ColumnMapRowMapper(), limit);
		} catch (Exception e) {
			logger.error("Error generating user participation stats.", e);
			return List.of();
		}
	}
}