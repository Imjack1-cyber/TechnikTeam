package de.technikteam.dao;

import de.technikteam.model.DamageReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class DamageReportDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public DamageReportDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<DamageReport> reportRowMapper = (rs, rowNum) -> {
		DamageReport report = new DamageReport();
		report.setId(rs.getInt("id"));
		report.setItemId(rs.getInt("item_id"));
		report.setReporterUserId(rs.getInt("reporter_user_id"));
		report.setReportDescription(rs.getString("report_description"));
		report.setReportedAt(rs.getTimestamp("reported_at").toLocalDateTime());
		report.setStatus(rs.getString("status"));
		report.setReviewedByAdminId(rs.getObject("reviewed_by_admin_id", Integer.class));
		if (rs.getTimestamp("reviewed_at") != null) {
			report.setReviewedAt(rs.getTimestamp("reviewed_at").toLocalDateTime());
		}
		report.setAdminNotes(rs.getString("admin_notes"));
		// Joined fields
		report.setItemName(rs.getString("item_name"));
		report.setReporterUsername(rs.getString("reporter_username"));
		return report;
	};

	public DamageReport createReport(int itemId, int reporterUserId, String description) {
		String sql = "INSERT INTO damage_reports (item_id, reporter_user_id, report_description) VALUES (?, ?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, itemId);
			ps.setInt(2, reporterUserId);
			ps.setString(3, description);
			return ps;
		}, keyHolder);

		int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
		return getReportById(newId).orElse(null);
	}

	public List<DamageReport> getPendingReports() {
		String sql = "SELECT dr.*, si.name as item_name, u.username as reporter_username FROM damage_reports dr "
				+ "JOIN storage_items si ON dr.item_id = si.id " + "JOIN users u ON dr.reporter_user_id = u.id "
				+ "WHERE dr.status = 'PENDING' ORDER BY dr.reported_at ASC";
		return jdbcTemplate.query(sql, reportRowMapper);
	}

	public Optional<DamageReport> getReportById(int id) {
		String sql = "SELECT dr.*, si.name as item_name, u.username as reporter_username FROM damage_reports dr "
				+ "JOIN storage_items si ON dr.item_id = si.id " + "JOIN users u ON dr.reporter_user_id = u.id "
				+ "WHERE dr.id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reportRowMapper, id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public boolean updateStatus(int reportId, String status, int adminId, String adminNotes) {
		String sql = "UPDATE damage_reports SET status = ?, reviewed_by_admin_id = ?, reviewed_at = ?, admin_notes = ? WHERE id = ?";
		return jdbcTemplate.update(sql, ps -> {
			ps.setString(1, status);
			ps.setInt(2, adminId);
			ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
			if (adminNotes != null) {
				ps.setString(4, adminNotes);
			} else {
				ps.setNull(4, Types.VARCHAR);
			}
			ps.setInt(5, reportId);
		}) > 0;
	}
}