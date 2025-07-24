package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ReportDAO {
	private static final Logger logger = LogManager.getLogger(ReportDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public ReportDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public List<Map<String, Object>> getEventParticipationSummary() {
		List<Map<String, Object>> summary = new ArrayList<>();
		String sql = "SELECT e.name AS event_name, COUNT(ea.user_id) AS participant_count " + "FROM events e "
				+ "LEFT JOIN event_assignments ea ON e.id = ea.event_id " + "GROUP BY e.id, e.name "
				+ "ORDER BY participant_count DESC, e.event_datetime DESC";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("event_name", rs.getString("event_name"));
				row.put("participant_count", rs.getInt("participant_count"));
				summary.add(row);
			}
		} catch (SQLException e) {
			logger.error("Error generating event participation summary.", e);
		}
		return summary;
	}

	public List<Map<String, Object>> getUserActivityStats() {
		List<Map<String, Object>> stats = new ArrayList<>();
		String sql = "SELECT u.username, " + "COUNT(DISTINCT ea.event_id) AS events_signed_up, "
				+ "COUNT(DISTINCT ma.meeting_id) AS meetings_attended " + "FROM users u "
				+ "LEFT JOIN event_attendance ea ON u.id = ea.user_id AND ea.signup_status = 'ANGEMELDET' "
				+ "LEFT JOIN meeting_attendance ma ON u.id = ma.user_id AND ma.attended = 1 "
				+ "GROUP BY u.id, u.username " + "ORDER BY events_signed_up DESC, meetings_attended DESC";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("username", rs.getString("username"));
				row.put("events_signed_up", rs.getInt("events_signed_up"));
				row.put("meetings_attended", rs.getInt("meetings_attended"));
				stats.add(row);
			}
		} catch (SQLException e) {
			logger.error("Error generating user activity stats.", e);
		}
		return stats;
	}

	public List<Map<String, Object>> getInventoryUsageFrequency() {
		List<Map<String, Object>> stats = new ArrayList<>();
		String sql = "SELECT si.name AS item_name, SUM(ABS(sl.quantity_change)) AS total_quantity_checked_out "
				+ "FROM storage_items si " + "JOIN storage_log sl ON si.id = sl.item_id "
				+ "WHERE sl.quantity_change < 0 " + "GROUP BY si.id, si.name "
				+ "ORDER BY total_quantity_checked_out DESC";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("item_name", rs.getString("item_name"));
				row.put("total_quantity_checked_out", rs.getInt("total_quantity_checked_out"));
				stats.add(row);
			}
		} catch (SQLException e) {
			logger.error("Error generating inventory usage frequency report.", e);
		}
		return stats;
	}

	public double getTotalInventoryValue() {
		String sql = "SELECT SUM(quantity * price_eur) AS total_value FROM storage_items";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				return rs.getDouble("total_value");
			}
		} catch (SQLException e) {
			logger.error("Error calculating total inventory value.", e);
		}
		return 0.0;
	}

	public List<Map<String, Object>> getEventCountByMonth(int months) {
		List<Map<String, Object>> data = new ArrayList<>();
		String sql = "SELECT CONCAT(YEAR(event_datetime), '-', LPAD(MONTH(event_datetime), 2, '0')) AS month, COUNT(*) AS count "
				+ "FROM events " + "WHERE event_datetime >= DATE_SUB(NOW(), INTERVAL ? MONTH) "
				+ "GROUP BY YEAR(event_datetime), MONTH(event_datetime) " + "ORDER BY month ASC";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, months);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Map<String, Object> row = new HashMap<>();
					row.put("month", rs.getString("month"));
					row.put("count", rs.getInt("count"));
					data.add(row);
				}
			}
		} catch (SQLException e) {
			logger.error("Error generating event count by month report.", e);
		}
		return data;
	}

	public List<Map<String, Object>> getUserParticipationStats(int limit) {
		List<Map<String, Object>> data = new ArrayList<>();
		String sql = "SELECT u.username, COUNT(ea.user_id) as participation_count " + "FROM event_assignments ea "
				+ "JOIN users u ON ea.user_id = u.id " + "GROUP BY u.id, u.username "
				+ "ORDER BY participation_count DESC " + "LIMIT ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, limit);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Map<String, Object> row = new HashMap<>();
					row.put("username", rs.getString("username"));
					row.put("participation_count", rs.getInt("participation_count"));
					data.add(row);
				}
			}
		} catch (SQLException e) {
			logger.error("Error generating user participation stats.", e);
		}
		return data;
	}
}