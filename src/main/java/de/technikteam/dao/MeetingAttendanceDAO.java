package de.technikteam.dao;

import de.technikteam.model.MeetingAttendance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeetingAttendanceDAO {
	private static final Logger logger = LogManager.getLogger(MeetingAttendanceDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public MeetingAttendanceDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean setAttendance(int userId, int meetingId, boolean attended, String remarks) {
		String sql = "INSERT INTO meeting_attendance (user_id, meeting_id, attended, remarks) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE attended = VALUES(attended), remarks = VALUES(remarks)";
		try {
			jdbcTemplate.update(sql, userId, meetingId, attended, remarks);
			return true;
		} catch (Exception e) {
			logger.error("SQL error setting attendance for user {} at meeting {}", userId, meetingId, e);
			return false;
		}
	}

	public List<MeetingAttendance> getAllAttendance() {
		String sql = "SELECT * FROM meeting_attendance";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				MeetingAttendance attendance = new MeetingAttendance();
				attendance.setUserId(rs.getInt("user_id"));
				attendance.setMeetingId(rs.getInt("meeting_id"));
				attendance.setAttended(rs.getBoolean("attended"));
				attendance.setRemarks(rs.getString("remarks"));
				return attendance;
			});
		} catch (Exception e) {
			logger.error("SQL error fetching all attendance records.", e);
			return List.of();
		}
	}
}