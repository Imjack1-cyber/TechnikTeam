// Create this new file in your dao package: src/main/java/de/technikteam/dao/MeetingDAO.java
package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.Meeting;
import de.technikteam.model.User;

/**
 * Data Access Object for all Meeting-related database operations. Handles CRUD
 * for individual, schedulable course meetings.
 */
public class MeetingDAO {
	private static final Logger logger = LogManager.getLogger(MeetingDAO.class);

	/**
	 * Creates a new meeting in the database.
	 * 
	 * @param meeting The Meeting object to create.
	 * @return true if creation was successful, false otherwise.
	 */
	public boolean createMeeting(Meeting meeting) {
		String sql = "INSERT INTO meetings (course_id, name, meeting_datetime, leader, description) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, meeting.getCourseId());
			pstmt.setString(2, meeting.getName());
			pstmt.setTimestamp(3, Timestamp.valueOf(meeting.getMeetingDateTime()));
			pstmt.setString(4, meeting.getLeader());
			pstmt.setString(5, meeting.getDescription());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				logger.info("Successfully created meeting '{}'", meeting.getName());
				return true;
			}
		} catch (SQLException e) {
			logger.error("SQL error creating meeting: {}", meeting.getName(), e);
		}
		return false;
	}

	/**
	 * Fetches a single meeting by its ID.
	 * 
	 * @param meetingId The ID of the meeting to retrieve.
	 * @return A Meeting object, or null if not found.
	 */
	public Meeting getMeetingById(int meetingId) {
		String sql = "SELECT m.*, c.name as parent_course_name FROM meetings m "
				+ "JOIN courses c ON m.course_id = c.id WHERE m.id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, meetingId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToMeeting(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching meeting by ID: {}", meetingId, e);
		}
		return null;
	}

	/**
	 * Fetches all scheduled meetings that belong to a specific parent course.
	 * Crucial for building the matrix view.
	 * 
	 * @param courseId The ID of the parent course.
	 * @return A list of Meeting objects.
	 */
	public List<Meeting> getMeetingsForCourse(int courseId) {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT * FROM meetings WHERE course_id = ? ORDER BY meeting_datetime ASC";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, courseId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					meetings.add(mapResultSetToMeeting(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching meetings for course ID: {}", courseId, e);
		}
		return meetings;
	}

	/**
	 * Fetches all meetings from the database, typically for an admin list view.
	 * 
	 * @return A list of all Meeting objects.
	 */
	public List<Meeting> getAllMeetings() {
		List<Meeting> meetings = new ArrayList<>();
		String sql = "SELECT m.*, c.name as parent_course_name FROM meetings m "
				+ "JOIN courses c ON m.course_id = c.id ORDER BY m.meeting_datetime DESC";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				meetings.add(mapResultSetToMeeting(rs));
			}
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
		meeting.setLeader(rs.getString("leader"));
		meeting.setDescription(rs.getString("description"));

		// If the parent course name was joined, add it.
		if (rs.getMetaData().getColumnCount() > 6) { // Simple check if extra columns exist
			meeting.setParentCourseName(rs.getString("parent_course_name"));
		}

		return meeting;
	}

	// Add these two methods to your MeetingDAO.java file

	/**
	 * Updates an existing meeting in the database.
	 * 
	 * @param meeting The Meeting object with updated data.
	 * @return true if the update was successful.
	 */
	public boolean updateMeeting(Meeting meeting) {
		String sql = "UPDATE meetings SET name = ?, meeting_datetime = ?, leader = ?, description = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, meeting.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(meeting.getMeetingDateTime()));
			pstmt.setString(3, meeting.getLeader());
			pstmt.setString(4, meeting.getDescription());
			pstmt.setInt(5, meeting.getId());

			logger.info("Updating meeting with ID: {}", meeting.getId());
			return pstmt.executeUpdate() > 0;

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
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, meetingId);
			logger.warn("Deleting meeting with ID: {}", meetingId);
			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			logger.error("SQL error deleting meeting ID: {}", meetingId, e);
			return false;
		}
	}

	// Add this new method to MeetingDAO.java
	/**
	 * Fetches all upcoming meetings, enriched with the attendance status for a
	 * specific user.
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

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, user.getId());
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Meeting meeting = mapResultSetToMeeting(rs); // Use your existing helper

					// Set the user-specific status
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
}