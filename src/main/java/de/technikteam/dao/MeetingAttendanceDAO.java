package de.technikteam.dao;

import de.technikteam.model.MeetingAttendance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

	/**
	 * Upsert attendance/enrollment record. attended == true means the user is
	 * enrolled for the meeting.
	 */
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

	/**
	 * Convenience: enroll user (attended = true)
	 */
	public boolean enrollUser(int userId, int meetingId) {
		return setAttendance(userId, meetingId, true, "");
	}

	/**
	 * Convenience: unenroll user (attended = false)
	 */
	public boolean unenrollUser(int userId, int meetingId) {
		return setAttendance(userId, meetingId, false, "");
	}

	/**
	 * Return true if a record exists for user+meeting (regardless of attended flag)
	 */
	public boolean existsForUserAndMeeting(int userId, int meetingId) {
		String sql = "SELECT COUNT(*) FROM meeting_attendance WHERE user_id = ? AND meeting_id = ?";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, meetingId);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("SQL error checking existsForUserAndMeeting {},{}", userId, meetingId, e);
			return false;
		}
	}

	/**
	 * Return true if the user has an attendance record with attended = 1 for a
	 * specific meeting.
	 */
	public boolean hasAttendedMeeting(int userId, int meetingId) {
		String sql = "SELECT COUNT(*) FROM meeting_attendance WHERE user_id = ? AND meeting_id = ? AND attended = 1";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, meetingId);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("SQL error checking hasAttendedMeeting {},{}", userId, meetingId, e);
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
