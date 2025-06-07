package de.technikteam.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.UserQualification;

public class UserQualificationsDAO {
	private static final Logger logger = LogManager.getLogger(UserQualificationsDAO.class);

	public List<UserQualification> getQualificationsForUser(int userId) {
		logger.debug("Fetching qualifications for user ID: {}", userId);
		List<UserQualification> qualifications = new ArrayList<>();
		String sql = "SELECT uq.course_id, c.name, uq.status, uq.completion_date " + "FROM user_qualifications uq "
				+ "JOIN courses c ON uq.course_id = c.id " + "WHERE uq.user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					UserQualification uq = new UserQualification();
					uq.setCourseId(rs.getInt("course_id"));
					uq.setCourseName(rs.getString("name"));
					uq.setStatus(rs.getString("status"));
					Date dbDate = rs.getDate("completion_date");
					if (dbDate != null) {
						uq.setCompletionDate(dbDate.toLocalDate());
					}
					qualifications.add(uq);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching qualifications for user ID: {}", userId, e);
		}
		return qualifications;
	}

	public boolean updateQualificationStatus(int userId, int courseId, String status, LocalDate completionDate) {
		logger.info("Updating qualification for user {} and course {}: status={}, date={}", userId, courseId, status,
				completionDate);
		String sql = "UPDATE user_qualifications SET status = ?, completion_date = ? WHERE user_id = ? AND course_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setDate(2, completionDate != null ? Date.valueOf(completionDate) : null);
			pstmt.setInt(3, userId);
			pstmt.setInt(4, courseId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating qualification for user {} and course {}", userId, courseId, e);
			return false;
		}
	}
}