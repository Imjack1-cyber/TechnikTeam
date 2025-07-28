package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.MeetingAttendance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MeetingAttendanceDAO {
	private static final Logger logger = LogManager.getLogger(MeetingAttendanceDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public MeetingAttendanceDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public boolean setAttendance(int userId, int meetingId, boolean attended, String remarks) {
		String sql = "INSERT INTO meeting_attendance (user_id, meeting_id, attended, remarks) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE attended = VALUES(attended), remarks = VALUES(remarks)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, meetingId);
			pstmt.setBoolean(3, attended);
			pstmt.setString(4, remarks);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("SQL error setting attendance for user {} at meeting {}", userId, meetingId, e);
			return false;
		}
	}

	public List<MeetingAttendance> getAllAttendance() {
		List<MeetingAttendance> allAttendance = new ArrayList<>();
		String sql = "SELECT * FROM meeting_attendance";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				allAttendance.add(mapResultSetToAttendance(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching all attendance records.", e);
		}
		return allAttendance;
	}

	private MeetingAttendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
		MeetingAttendance attendance = new MeetingAttendance();
		attendance.setUserId(rs.getInt("user_id"));
		attendance.setMeetingId(rs.getInt("meeting_id"));
		attendance.setAttended(rs.getBoolean("attended"));
		attendance.setRemarks(rs.getString("remarks"));
		return attendance;
	}
}