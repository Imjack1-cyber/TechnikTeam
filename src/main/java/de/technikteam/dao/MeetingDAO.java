package de.technikteam.dao;

import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Objects;

@Repository
public class MeetingDAO {
	private static final Logger logger = LogManager.getLogger(MeetingDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public MeetingDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Meeting> meetingRowMapper = (rs, rowNum) -> {
		Meeting meeting = new Meeting();
		meeting.setId(rs.getInt("id"));
		meeting.setCourseId(rs.getInt("course_id"));
		meeting.setName(rs.getString("name"));
		meeting.setMeetingDateTime(rs.getTimestamp("meeting_datetime").toLocalDateTime());
		if (rs.getTimestamp("end_datetime") != null)
			meeting.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
		meeting.setLeaderUserId(rs.getInt("leader_user_id"));
		meeting.setDescription(rs.getString("description"));
		meeting.setLocation(rs.getString("location"));
		meeting.setParentCourseName(rs.getString("parent_course_name"));
		meeting.setLeaderUsername(rs.getString("leader_username"));
		return meeting;
	};

	public int createMeeting(Meeting meeting) {
		String sql = "INSERT INTO meetings (course_id, name, meeting_datetime, end_datetime, leader_user_id, description, location) VALUES (?, ?, ?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, meeting.getCourseId());
				ps.setString(2, meeting.getName());
				ps.setTimestamp(3, Timestamp.valueOf(meeting.getMeetingDateTime()));
				if (meeting.getEndDateTime() != null)
					ps.setTimestamp(4, Timestamp.valueOf(meeting.getEndDateTime()));
				else
					ps.setNull(4, Types.TIMESTAMP);
				if (meeting.getLeaderUserId() > 0)
					ps.setInt(5, meeting.getLeaderUserId());
				else
					ps.setNull(5, Types.INTEGER);
				ps.setString(6, meeting.getDescription());
				ps.setString(7, meeting.getLocation());
				return ps;
			}, keyHolder);
			return Objects.requireNonNull(keyHolder.getKey()).intValue();
		} catch (Exception e) {
			logger.error("Error creating meeting: {}", meeting.getName(), e);
			return 0;
		}
	}

	public Meeting getMeetingById(int meetingId) {
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id WHERE m.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, meetingRowMapper, meetingId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching meeting by ID: {}", meetingId, e);
			return null;
		}
	}

	public List<Meeting> getMeetingsForCourse(int courseId) {
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id WHERE m.course_id = ? ORDER BY meeting_datetime ASC";
		try {
			return jdbcTemplate.query(sql, meetingRowMapper, courseId);
		} catch (Exception e) {
			logger.error("Error fetching meetings for course ID: {}", courseId, e);
			return List.of();
		}
	}

	public boolean updateMeeting(Meeting meeting) {
		String sql = "UPDATE meetings SET name = ?, meeting_datetime = ?, end_datetime = ?, leader_user_id = ?, description = ?, location = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, meeting.getName(), Timestamp.valueOf(meeting.getMeetingDateTime()),
					meeting.getEndDateTime() != null ? Timestamp.valueOf(meeting.getEndDateTime()) : null,
					meeting.getLeaderUserId() > 0 ? meeting.getLeaderUserId() : null, meeting.getDescription(),
					meeting.getLocation(), meeting.getId()) > 0;
		} catch (Exception e) {
			logger.error("Error updating meeting ID: {}", meeting.getId(), e);
			return false;
		}
	}

	public boolean deleteMeeting(int meetingId) {
		String sql = "DELETE FROM meetings WHERE id = ?";
		try {
			jdbcTemplate.update("DELETE FROM meeting_attendance WHERE meeting_id = ?", meetingId);
			return jdbcTemplate.update(sql, meetingId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting meeting ID: {}", meetingId, e);
			return false;
		}
	}

	public List<Meeting> getUpcomingMeetingsForUser(User user) {
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username, ma.attended FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id LEFT JOIN meeting_attendance ma ON m.id = ma.meeting_id AND ma.user_id = ? WHERE m.meeting_datetime >= NOW() ORDER BY m.meeting_datetime ASC";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				Meeting meeting = meetingRowMapper.mapRow(rs, rowNum);
				if (rs.getObject("attended") != null) {
					meeting.setUserAttendanceStatus(rs.getBoolean("attended") ? "ANGEMELDET" : "ABGEMELDET");
				} else {
					meeting.setUserAttendanceStatus("OFFEN");
				}
				return meeting;
			}, user.getId());
		} catch (Exception e) {
			logger.error("Error fetching upcoming meetings for user {}", user.getId(), e);
			return List.of();
		}
	}

	public List<Meeting> getAllUpcomingMeetings() {
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id WHERE m.meeting_datetime >= NOW() - INTERVAL 1 DAY ORDER BY m.meeting_datetime ASC";
		try {
			return jdbcTemplate.query(sql, meetingRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching upcoming meetings for calendar.", e);
			return List.of();
		}
	}

	public boolean isUserAssociatedWithMeeting(int meetingId, int userId) {
		String sql = "SELECT COUNT(*) FROM meeting_attendance WHERE meeting_id = ? AND user_id = ?";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, meetingId, userId);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("Error checking user association for meeting {} and user {}", meetingId, userId, e);
			return false;
		}
	}
}