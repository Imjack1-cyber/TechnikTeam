package de.technikteam.dao;

import de.technikteam.model.MeetingAttendance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for handling user attendance at specific course meetings.
 * It interacts with the `meeting_attendance` table to record whether a user
 * attended a meeting, along with any relevant remarks.
 */
public class MeetingAttendanceDAO {
	private static final Logger logger = LogManager.getLogger(MeetingAttendanceDAO.class.getName());

	/**
	 * Sets or updates a user's attendance status for a specific meeting. Uses an
	 * efficient "INSERT ... ON DUPLICATE KEY UPDATE" (upsert) operation. This
	 * single method handles both signing up (attended=true) and signing off
	 * (attended=false).
	 *
	 * @param userId    The user's ID.
	 * @param meetingId The meeting's ID.
	 * @param attended  true if the user attended, false otherwise.
	 * @param remarks   Any notes about the attendance (e.g., "excused absence").
	 * @return true if the operation was successful.
	 */
	public boolean setAttendance(int userId, int meetingId, boolean attended, String remarks) {
		String sql = "INSERT INTO meeting_attendance (user_id, meeting_id, attended, remarks) VALUES (?, ?, ?, ?) "
				+ "ON DUPLICATE KEY UPDATE attended = VALUES(attended), remarks = VALUES(remarks)";

		logger.debug("Setting attendance for user {} at meeting {} to attended={}", userId, meetingId, attended);

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			pstmt.setInt(2, meetingId);
			pstmt.setBoolean(3, attended);
			pstmt.setString(4, remarks);

			pstmt.executeUpdate();
			logger.info("Successfully set attendance for user {} at meeting {} to attended={}", userId, meetingId,
					attended);
			return true;

		} catch (SQLException e) {
			logger.error("SQL error setting attendance for user {} at meeting {}", userId, meetingId, e);
			return false;
		}
	}

	/**
	 * Fetches all attendance records from the database. This is highly efficient
	 * for building the data map used by the qualification matrix view.
	 * 
	 * @return A list of all MeetingAttendance objects in the database.
	 */
	public List<MeetingAttendance> getAllAttendance() {
		List<MeetingAttendance> allAttendance = new ArrayList<>();
		String sql = "SELECT * FROM meeting_attendance";
		logger.debug("Fetching all meeting attendance records for matrix.");
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				allAttendance.add(mapResultSetToAttendance(rs));
			}
			logger.info("Fetched {} total attendance records.", allAttendance.size());
		} catch (SQLException e) {
			logger.error("SQL error fetching all attendance records.", e);
		}
		return allAttendance;
	}

	/**
	 * Helper method to map a ResultSet row to a MeetingAttendance object.
	 * 
	 * @param rs The ResultSet to map.
	 * @return A populated MeetingAttendance object.
	 * @throws SQLException If a database error occurs.
	 */
	private MeetingAttendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
		MeetingAttendance attendance = new MeetingAttendance();
		attendance.setUserId(rs.getInt("user_id"));
		attendance.setMeetingId(rs.getInt("meeting_id"));
		attendance.setAttended(rs.getBoolean("attended"));
		attendance.setRemarks(rs.getString("remarks"));
		return attendance;
	}
}