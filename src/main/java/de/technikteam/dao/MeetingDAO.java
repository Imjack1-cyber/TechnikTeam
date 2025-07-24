package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MeetingDAO {
	private static final Logger logger = LogManager.getLogger(MeetingDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public MeetingDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public int createMeeting(Meeting meeting) {
		String sql = "INSERT INTO meetings (course_id, name, meeting_datetime, end_datetime, leader_user_id, description, location) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setInt(1, meeting.getCourseId());
			pstmt.setString(2, meeting.getName());
			pstmt.setTimestamp(3, Timestamp.valueOf(meeting.getMeetingDateTime()));
			if (meeting.getEndDateTime() != null)
				pstmt.setTimestamp(4, Timestamp.valueOf(meeting.getEndDateTime()));
			else
				pstmt.setNull(4, Types.TIMESTAMP);
			if (meeting.getLeaderUserId() > 0)
				pstmt.setInt(5, meeting.getLeaderUserId());
			else
				pstmt.setNull(5, Types.INTEGER);
			pstmt.setString(6, meeting.getDescription());
			pstmt.setString(7, meeting.getLocation());
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next())
						return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error creating meeting: {}", meeting.getName(), e);
		}
		return 0;
	}

	public Meeting getMeetingById(int meetingId) {
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id WHERE m.id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, meetingId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next())
					return mapResultSetToMeeting(rs);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching meeting by ID: {}", meetingId, e);
		}
		return null;
	}

	public List<Meeting> getMeetingsForCourse(int courseId) {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id WHERE m.course_id = ? ORDER BY meeting_datetime ASC";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, courseId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next())
					meetings.add(mapResultSetToMeeting(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching meetings for course ID: {}", courseId, e);
		}
		return meetings;
	}

	public List<Meeting> getAllMeetings() {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id ORDER BY m.meeting_datetime DESC";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next())
				meetings.add(mapResultSetToMeeting(rs));
		} catch (SQLException e) {
			logger.error("SQL error fetching all meetings.", e);
		}
		return meetings;
	}

	private Meeting mapResultSetToMeeting(ResultSet rs) throws SQLException {
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
	}

	public boolean updateMeeting(Meeting meeting) {
		String sql = "UPDATE meetings SET name = ?, meeting_datetime = ?, end_datetime = ?, leader_user_id = ?, description = ?, location = ? WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, meeting.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(meeting.getMeetingDateTime()));
			if (meeting.getEndDateTime() != null)
				pstmt.setTimestamp(3, Timestamp.valueOf(meeting.getEndDateTime()));
			else
				pstmt.setNull(3, Types.TIMESTAMP);
			if (meeting.getLeaderUserId() > 0)
				pstmt.setInt(4, meeting.getLeaderUserId());
			else
				pstmt.setNull(4, Types.INTEGER);
			pstmt.setString(5, meeting.getDescription());
			pstmt.setString(6, meeting.getLocation());
			pstmt.setInt(7, meeting.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating meeting ID: {}", meeting.getId(), e);
			return false;
		}
	}

	public boolean deleteMeeting(int meetingId) {
		String sql = "DELETE FROM meetings WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, meetingId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting meeting ID: {}", meetingId, e);
			return false;
		}
	}

	public List<Meeting> getUpcomingMeetingsForUser(User user) {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username, ma.attended FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id LEFT JOIN meeting_attendance ma ON m.id = ma.meeting_id AND ma.user_id = ? WHERE m.meeting_datetime >= NOW() ORDER BY m.meeting_datetime ASC";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, user.getId());
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Meeting meeting = mapResultSetToMeeting(rs);
					if (rs.getObject("attended") != null) {
						meeting.setUserAttendanceStatus(rs.getBoolean("attended") ? "ANGEMELDET" : "ABGEMELDET");
					} else {
						meeting.setUserAttendanceStatus("OFFEN");
					}
					meetings.add(meeting);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching upcoming meetings for user {}", user.getId(), e);
		}
		return meetings;
	}

	public List<Meeting> getAllUpcomingMeetings() {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT m.*, c.name as parent_course_name, u.username as leader_username FROM meetings m JOIN courses c ON m.course_id = c.id LEFT JOIN users u ON m.leader_user_id = u.id WHERE m.meeting_datetime >= NOW() - INTERVAL 1 DAY ORDER BY m.meeting_datetime ASC";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				meetings.add(mapResultSetToMeeting(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching upcoming meetings for calendar.", e);
		}
		return meetings;
	}

	public boolean isUserAssociatedWithMeeting(int meetingId, int userId) {
		String sql = "SELECT 1 FROM meeting_attendance WHERE meeting_id = ? AND user_id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, meetingId);
			pstmt.setInt(2, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			logger.error("Error checking user association for meeting {} and user {}", meetingId, userId, e);
			return false;
		}
	}
}