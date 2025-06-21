// Create this new file in your dao package: src/main/java/de/technikteam/dao/MeetingAttendanceDAO.java
package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.MeetingAttendance;

/**
 * Data Access Object for handling user attendance at specific meetings.
 */
public class MeetingAttendanceDAO {
    private static final Logger logger = LogManager.getLogger(MeetingAttendanceDAO.class);

    /**
     * Sets or updates a user's attendance status for a specific meeting.
     * Uses "INSERT ... ON DUPLICATE KEY UPDATE" for efficiency.
     * If attended is false, the record is deleted.
     * @param userId The user's ID.
     * @param meetingId The meeting's ID.
     * @param attended true if the user attended, false otherwise.
     * @param remarks Any notes about the attendance.
     * @return true if the operation was successful.
     */
    public boolean setAttendance(int userId, int meetingId, boolean attended, String remarks) {
        if (!attended) {
            // If the user did not attend, we remove the record entirely.
            return deleteAttendance(userId, meetingId);
        }

        // If the user attended, we insert a new record or update an existing one.
        String sql = "INSERT INTO meeting_attendance (user_id, meeting_id, attended, remarks) VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE attended = VALUES(attended), remarks = VALUES(remarks)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, meetingId);
            pstmt.setBoolean(3, attended);
            pstmt.setString(4, remarks);
            
            pstmt.executeUpdate();
            logger.info("Successfully set attendance for user {} at meeting {}", userId, meetingId);
            return true;

        } catch (SQLException e) {
            logger.error("SQL error setting attendance for user {} at meeting {}", userId, meetingId, e);
            return false;
        }
    }

    /**
     * Deletes an attendance record. Called when a user's status is set to not attended.
     * @param userId The user's ID.
     * @param meetingId The meeting's ID.
     * @return true if a record was deleted.
     */
    private boolean deleteAttendance(int userId, int meetingId) {
        String sql = "DELETE FROM meeting_attendance WHERE user_id = ? AND meeting_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, meetingId);
            
            int affectedRows = pstmt.executeUpdate();
            logger.info("Deleted attendance record for user {} at meeting {}. Rows affected: {}", userId, meetingId, affectedRows);
            return true; // Return true even if no rows were deleted, as the desired state is achieved.

        } catch (SQLException e) {
            logger.error("SQL error deleting attendance for user {} at meeting {}", userId, meetingId, e);
            return false;
        }
    }

    /**
     * Fetches all attendance records from the database.
     * This is highly efficient for building the qualification matrix.
     * @return A list of all MeetingAttendance objects.
     */
    public List<MeetingAttendance> getAllAttendance() {
        List<MeetingAttendance> allAttendance = new ArrayList<>();
        String sql = "SELECT * FROM meeting_attendance";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                allAttendance.add(mapResultSetToAttendance(rs));
            }
            logger.debug("Fetched {} total attendance records.", allAttendance.size());
        } catch (SQLException e) {
            logger.error("SQL error fetching all attendance records.", e);
        }
        return allAttendance;
    }

    /**
     * Helper method to map a ResultSet row to a MeetingAttendance object.
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