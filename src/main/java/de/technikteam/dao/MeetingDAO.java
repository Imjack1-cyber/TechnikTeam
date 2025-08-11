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
		meeting.setParentMeetingId(rs.getObject("parent_meeting_id") == null ? 0 : rs.getInt("parent_meeting_id"));
		meeting.setName(rs.getString("name"));
		meeting.setMeetingDateTime(rs.getTimestamp("meeting_datetime").toLocalDateTime());
		if (rs.getTimestamp("end_datetime") != null)
			meeting.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
		meeting.setLeaderUserId(rs.getInt("leader_user_id"));
		meeting.setDescription(rs.getString("description"));
		meeting.setLocation(rs.getString("location"));
		meeting.setMaxParticipants(rs.getObject("max_participants", Integer.class));
		if (rs.getTimestamp("signup_deadline") != null) {
			meeting.setSignupDeadline(rs.getTimestamp("signup_deadline").toLocalDateTime());
		}
		meeting.setParentCourseName(rs.getString("parent_course_name"));
		meeting.setLeaderUsername(rs.getString("leader_username"));
		return meeting;
	};

	private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setUsername(rs.getString("username"));
		return user;
	};

	public int createMeeting(Meeting meeting) {
		String sql = "INSERT INTO meetings (course_id, parent_meeting_id, name, meeting_datetime, end_datetime, leader_user_id, description, location, max_participants, signup_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, meeting.getCourseId());
				if (meeting.getParentMeetingId() > 0)
					ps.setInt(2, meeting.getParentMeetingId());
				else
					ps.setNull(2, Types.INTEGER);
				ps.setString(3, meeting.getName());
				ps.setTimestamp(4, Timestamp.valueOf(meeting.getMeetingDateTime()));
				if (meeting.getEndDateTime() != null)
					ps.setTimestamp(5, Timestamp.valueOf(meeting.getEndDateTime()));
				else
					ps.setNull(5, Types.TIMESTAMP);
				if (meeting.getLeaderUserId() > 0)
					ps.setInt(6, meeting.getLeaderUserId());
				else
					ps.setNull(6, Types.INTEGER);
				ps.setString(7, meeting.getDescription());
				ps.setString(8, meeting.getLocation());
				if (meeting.getMaxParticipants() != null) {
					ps.setInt(9, meeting.getMaxParticipants());
				} else {
					ps.setNull(9, Types.INTEGER);
				}
				if (meeting.getSignupDeadline() != null) {
					ps.setTimestamp(10, Timestamp.valueOf(meeting.getSignupDeadline()));
				} else {
					ps.setNull(10, Types.TIMESTAMP);
				}
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
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username, (SELECT COUNT(*) FROM meeting_attendance ma WHERE ma.meeting_id = m.id AND ma.attended = 1) as participant_count, (SELECT COUNT(*) FROM meeting_waitlist mw WHERE mw.meeting_id = m.id) as waitlist_count FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id WHERE m.course_id = ? ORDER BY meeting_datetime ASC";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				Meeting meeting = meetingRowMapper.mapRow(rs, rowNum);
				meeting.setParticipantCount(rs.getInt("participant_count"));
				meeting.setWaitlistCount(rs.getInt("waitlist_count"));
				return meeting;
			}, courseId);
		} catch (Exception e) {
			logger.error("Error fetching meetings for course ID: {}", courseId, e);
			return List.of();
		}
	}

	public boolean updateMeeting(Meeting meeting) {
		String sql = "UPDATE meetings SET name = ?, meeting_datetime = ?, end_datetime = ?, leader_user_id = ?, description = ?, location = ?, parent_meeting_id = ?, max_participants = ?, signup_deadline = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, meeting.getName(), Timestamp.valueOf(meeting.getMeetingDateTime()),
					meeting.getEndDateTime() != null ? Timestamp.valueOf(meeting.getEndDateTime()) : null,
					meeting.getLeaderUserId() > 0 ? meeting.getLeaderUserId() : null, meeting.getDescription(),
					meeting.getLocation(), meeting.getParentMeetingId() > 0 ? meeting.getParentMeetingId() : null,
					meeting.getMaxParticipants(),
					meeting.getSignupDeadline() != null ? Timestamp.valueOf(meeting.getSignupDeadline()) : null,
					meeting.getId()) > 0;
		} catch (Exception e) {
			logger.error("Error updating meeting ID: {}", meeting.getId(), e);
			return false;
		}
	}

	public boolean deleteMeeting(int meetingId) {
		String sql = "DELETE FROM meetings WHERE id = ?";
		try {
			jdbcTemplate.update("DELETE FROM meeting_attendance WHERE meeting_id = ?", meetingId);
			jdbcTemplate.update("DELETE FROM meeting_waitlist WHERE meeting_id = ?", meetingId);
			return jdbcTemplate.update(sql, meetingId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting meeting ID: {}", meetingId, e);
			return false;
		}
	}

	public List<Meeting> getUpcomingMeetingsForUser(User user) {
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username, ma.attended, (SELECT COUNT(*) FROM meeting_attendance ma_count WHERE ma_count.meeting_id = m.id AND ma_count.attended = 1) as participant_count FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id LEFT JOIN meeting_attendance ma ON m.id = ma.meeting_id AND ma.user_id = ? WHERE m.meeting_datetime >= NOW() ORDER BY m.meeting_datetime ASC";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				Meeting meeting = meetingRowMapper.mapRow(rs, rowNum);
				meeting.setParticipantCount(rs.getInt("participant_count"));
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

	public List<Meeting> search(String query) {
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id WHERE m.name LIKE ? OR m.description LIKE ? OR c.name LIKE ? ORDER BY m.meeting_datetime DESC LIMIT 20";
		String searchTerm = "%" + query + "%";
		try {
			return jdbcTemplate.query(sql, meetingRowMapper, searchTerm, searchTerm, searchTerm);
		} catch (Exception e) {
			logger.error("Error searching meetings for query '{}'", query, e);
			return List.of();
		}
	}

	public List<User> getEnrolledUsersForMeeting(int meetingId) {
		String sql = "SELECT u.id, u.username FROM users u JOIN meeting_attendance ma ON u.id = ma.user_id WHERE ma.meeting_id = ? AND ma.attended = 1 ORDER BY u.username";
		try {
			return jdbcTemplate.query(sql, userRowMapper, meetingId);
		} catch (Exception e) {
			logger.error("Error fetching participant users for meeting {}", meetingId, e);
			return List.of();
		}
	}

	public int getParticipantCount(int meetingId) {
		String sql = "SELECT COUNT(*) FROM meeting_attendance WHERE meeting_id = ? AND attended = 1";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, meetingId);
		return count != null ? count : 0;
	}
}