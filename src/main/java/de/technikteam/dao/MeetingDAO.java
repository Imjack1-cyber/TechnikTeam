package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.Meeting;
import de.technikteam.model.User;

/**
 * Data Access Object for all Meeting-related database operations. It handles
 * CRUD for individual, schedulable course meetings stored in the `meetings`
 * table. It's distinct from CourseDAO, which manages the parent course
 * templates.
 */
public class MeetingDAO {
	private static final Logger logger = LogManager.getLogger(MeetingDAO.class);

	/**
	 * Creates a new meeting in the database, linked to a parent course.
	 * 
	 * @param meeting The Meeting object to create.
	 * @return The ID of the newly created meeting, or 0 on failure.
	 */
	public int createMeeting(Meeting meeting) {
		String sql = "INSERT INTO meetings (course_id, name, meeting_datetime, end_datetime, leader, description) VALUES (?, ?, ?, ?, ?, ?)";
		logger.debug("Attempting to create meeting '{}' for course ID {}", meeting.getName(), meeting.getCourseId());
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setInt(1, meeting.getCourseId());
			pstmt.setString(2, meeting.getName());
			pstmt.setTimestamp(3, Timestamp.valueOf(meeting.getMeetingDateTime()));
			if (meeting.getEndDateTime() != null) {
				pstmt.setTimestamp(4, Timestamp.valueOf(meeting.getEndDateTime()));
			} else {
				pstmt.setNull(4, Types.TIMESTAMP);
			}
			pstmt.setString(5, meeting.getLeader());
			pstmt.setString(6, meeting.getDescription());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						int newId = rs.getInt(1);
						logger.info("Successfully created meeting '{}' with new ID {}", meeting.getName(), newId);
						return newId;
					}
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error creating meeting: {}", meeting.getName(), e);
		}
		return 0; // Return 0 on failure
	}

	/**
	 * Fetches a single meeting by its ID, joining with the courses table to get the
	 * parent course name.
	 * 
	 * @param meetingId The ID of the meeting to retrieve.
	 * @return A Meeting object, or null if not found.
	 */
	public Meeting getMeetingById(int meetingId) {
		String sql = "SELECT m.*, c.name as parent_course_name FROM meetings m "
				+ "JOIN courses c ON m.course_id = c.id WHERE m.id = ?";
		logger.debug("Fetching meeting by ID: {}", meetingId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, meetingId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("Found meeting with ID: {}", meetingId);
					return mapResultSetToMeeting(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching meeting by ID: {}", meetingId, e);
		}
		logger.warn("No meeting found with ID: {}", meetingId);
		return null;
	}

	/**
	 * Fetches all scheduled meetings that belong to a specific parent course.
	 * Crucial for building the qualification matrix view.
	 * 
	 * @param courseId The ID of the parent course.
	 * @return A list of Meeting objects, sorted by date.
	 */
	public List<Meeting> getMeetingsForCourse(int courseId) {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT m.*, c.name as parent_course_name FROM meetings m JOIN courses c ON m.course_id = c.id WHERE m.course_id = ? ORDER BY meeting_datetime ASC";
		logger.debug("Fetching all meetings for course ID: {}", courseId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, courseId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					meetings.add(mapResultSetToMeeting(rs));
				}
				logger.info("Found {} meetings for course ID: {}", meetings.size(), courseId);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching meetings for course ID: {}", courseId, e);
		}
		return meetings;
	}

	/**
	 * Fetches all meetings from the database, typically for an admin list view.
	 * Includes the parent course name.
	 * 
	 * @return A list of all Meeting objects.
	 */
	public List<Meeting> getAllMeetings() {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT m.*, c.name as parent_course_name FROM meetings m "
				+ "JOIN courses c ON m.course_id = c.id ORDER BY m.meeting_datetime DESC";
		logger.debug("Fetching all meetings from the database.");
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				meetings.add(mapResultSetToMeeting(rs));
			}
			logger.info("Fetched a total of {} meetings.", meetings.size());
		} catch (SQLException e) {
			logger.error("SQL error fetching all meetings.", e);
		}
		return meetings;
	}

	/**
	 * Helper method to map a row from a ResultSet to a Meeting object.
	 * 
	 * @param rs The ResultSet to map.
	 * @return A populated Meeting object.
	 * @throws SQLException If a database error occurs.
	 */
	private Meeting mapResultSetToMeeting(ResultSet rs) throws SQLException {
		Meeting meeting = new Meeting();
		meeting.setId(rs.getInt("id"));
		meeting.setCourseId(rs.getInt("course_id"));
		meeting.setName(rs.getString("name"));
		meeting.setMeetingDateTime(rs.getTimestamp("meeting_datetime").toLocalDateTime());
		if (rs.getTimestamp("end_datetime") != null) {
			meeting.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
		}
		meeting.setLeader(rs.getString("leader"));
		meeting.setDescription(rs.getString("description"));

		// If the parent course name was joined, add it.
		// A more robust check for the column's existence
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			if ("parent_course_name".equalsIgnoreCase(rsmd.getColumnName(i))) {
				meeting.setParentCourseName(rs.getString("parent_course_name"));
				break;
			}
		}

		return meeting;
	}

	/**
	 * Updates an existing meeting in the database.
	 * 
	 * @param meeting The Meeting object with updated data.
	 * @return true if the update was successful.
	 */
	public boolean updateMeeting(Meeting meeting) {
		String sql = "UPDATE meetings SET name = ?, meeting_datetime = ?, end_datetime = ?, leader = ?, description = ? WHERE id = ?";
		logger.debug("Attempting to update meeting ID: {}", meeting.getId());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, meeting.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(meeting.getMeetingDateTime()));
			if (meeting.getEndDateTime() != null) {
				pstmt.setTimestamp(3, Timestamp.valueOf(meeting.getEndDateTime()));
			} else {
				pstmt.setNull(3, Types.TIMESTAMP);
			}
			pstmt.setString(4, meeting.getLeader());
			pstmt.setString(5, meeting.getDescription());
			pstmt.setInt(6, meeting.getId());

			boolean success = pstmt.executeUpdate() > 0;
			if (success)
				logger.info("Successfully updated meeting with ID: {}", meeting.getId());
			return success;

		} catch (SQLException e) {
			logger.error("SQL error updating meeting ID: {}", meeting.getId(), e);
			return false;
		}
	}

	/**
	 * Deletes a meeting from the database.
	 * 
	 * @param meetingId The ID of the meeting to delete.
	 * @return true if deletion was successful.
	 */
	public boolean deleteMeeting(int meetingId) {
		String sql = "DELETE FROM meetings WHERE id = ?";
		logger.warn("Attempting to delete meeting with ID: {}", meetingId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, meetingId);
			boolean success = pstmt.executeUpdate() > 0;
			if (success)
				logger.info("Successfully deleted meeting with ID: {}", meetingId);
			return success;
		} catch (SQLException e) {
			logger.error("SQL error deleting meeting ID: {}", meetingId, e);
			return false;
		}
	}

	/**
	 * Fetches all upcoming meetings, enriched with the attendance status for a
	 * specific user (ANGEMELDET, ABGEMELDET, OFFEN).
	 * 
	 * @param user The currently logged-in user.
	 * @return A list of upcoming Meeting objects with user-specific status.
	 */
	public List<Meeting> getUpcomingMeetingsForUser(User user) {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT m.*, c.name as parent_course_name, ma.attended " + "FROM meetings m "
				+ "JOIN courses c ON m.course_id = c.id "
				+ "LEFT JOIN meeting_attendance ma ON m.id = ma.meeting_id AND ma.user_id = ? "
				+ "WHERE m.meeting_datetime >= NOW() ORDER BY m.meeting_datetime ASC";

		logger.debug("Fetching upcoming meetings for user ID: {}", user.getId());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, user.getId());
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Meeting meeting = mapResultSetToMeeting(rs);

					// Set the user-specific status based on the 'attended' flag from the join
					if (rs.getObject("attended") != null) {
						meeting.setUserAttendanceStatus(rs.getBoolean("attended") ? "ANGEMELDET" : "ABGEMELDET");
					} else {
						meeting.setUserAttendanceStatus("OFFEN");
					}
					meetings.add(meeting);
				}
				logger.info("Found {} upcoming meetings for user ID: {}", meetings.size(), user.getId());
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching upcoming meetings for user {}", user.getId(), e);
		}
		return meetings;
	}
}