package de.technikteam.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.UserQualification;

/**
 * This DAO manages the relationship between users and courses in the
 * user_qualifications table. It's used to track which courses a user has
 * attended or completed. Its key functions are fetching qualifications for a
 * single user or all users (for a matrix view) and updating a user's status for
 * a specific course.
 */

public class UserQualificationsDAO {
	private static final Logger logger = LogManager.getLogger(UserQualificationsDAO.class);

	/**
	 * Fetches all qualifications for a single user. This is used for the user
	 * details page.
	 * 
	 * @param userId The ID of the user.
	 * @return A list of UserQualification objects.
	 */
	public List<UserQualification> getQualificationsForUser(int userId) {
		logger.debug("Fetching qualifications for user ID: {}", userId);
		List<UserQualification> qualifications = new ArrayList<>();
		// The SQL query joins with the courses table to get the course name.
		String sql = "SELECT uq.course_id, c.name, uq.status, uq.completion_date, uq.remarks "
				+ "FROM user_qualifications uq " + "JOIN courses c ON uq.course_id = c.id " + "WHERE uq.user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					qualifications.add(mapResultSetToUserQualification(rs));
				}
				logger.info("Found {} qualifications for user ID: {}", qualifications.size(), userId);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching qualifications for user ID: {}", userId, e);
		}
		return qualifications;
	}

	/**
	 * Fetches all qualifications for all users. This is used to build the data map
	 * for the qualification matrix page.
	 * 
	 * @return A list of all UserQualification objects in the database.
	 */
	public List<UserQualification> getAllQualifications() {
		logger.debug("Fetching all user qualifications for the matrix.");
		List<UserQualification> qualifications = new ArrayList<>();
		// This query needs the user_id to build the lookup map later.
		String sql = "SELECT uq.user_id, uq.course_id, c.name, uq.status, uq.completion_date, uq.remarks "
				+ "FROM user_qualifications uq " + "JOIN courses c ON uq.course_id = c.id";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				qualifications.add(mapResultSetToUserQualification(rs));
			}
			logger.info("Fetched a total of {} qualification entries.", qualifications.size());
		} catch (SQLException e) {
			logger.error("SQL error fetching all qualifications.", e);
		}
		return qualifications;
	}

	/**
	 * Updates or creates a qualification status for a user. If the status is set to
	 * "NICHT BESUCHT", the corresponding record is deleted. Otherwise, it performs
	 * an "upsert" (INSERT ... ON DUPLICATE KEY UPDATE) to create or modify the
	 * record.
	 * 
	 * @param userId         The ID of the user.
	 * @param courseId       The ID of the course.
	 * @param status         The new status ('BESUCHT', 'ABSOLVIERT', or 'NICHT
	 *                       BESUCHT').
	 * @param completionDate The date of completion (can be null).
	 * @param remarks        Additional remarks (can be null).
	 * @return true if the operation was successful, false otherwise.
	 */
	public boolean updateQualificationStatus(int userId, int courseId, String status, LocalDate completionDate,
			String remarks) {
		logger.debug("DAO: Updating qualification for user {}, course {}. New status: {}, Date: {}, Remarks: '{}'",
				userId, courseId, status, completionDate, remarks);

		if ("NICHT BESUCHT".equals(status)) {
			// If status is "Not Attended", we delete the record from the database.
			String deleteSql = "DELETE FROM user_qualifications WHERE user_id = ? AND course_id = ?";
			try (Connection conn = DatabaseManager.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
				pstmt.setInt(1, userId);
				pstmt.setInt(2, courseId);
				int affectedRows = pstmt.executeUpdate();
				logger.info("Deleted qualification entry for user {} and course {}. Rows affected: {}", userId,
						courseId, affectedRows);
				return true; // The desired state is achieved, so return true.
			} catch (SQLException e) {
				logger.error("DAO Error deleting qualification for user {} course {}", userId, courseId, e);
				return false;
			}
		} else {
			// Otherwise, we insert a new record or update an existing one.
			String upsertSql = "INSERT INTO user_qualifications (user_id, course_id, status, completion_date, remarks) VALUES (?, ?, ?, ?, ?) "
					+ "ON DUPLICATE KEY UPDATE status = VALUES(status), completion_date = VALUES(completion_date), remarks = VALUES(remarks)";
			try (Connection conn = DatabaseManager.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
				pstmt.setInt(1, userId);
				pstmt.setInt(2, courseId);
				pstmt.setString(3, status);

				if (completionDate != null) {
					pstmt.setDate(4, Date.valueOf(completionDate));
				} else {
					pstmt.setNull(4, Types.DATE);
				}

				pstmt.setString(5, remarks);

				int affectedRows = pstmt.executeUpdate();
				logger.info("Upserted qualification for user {} and course {}. Rows affected: {}", userId, courseId,
						affectedRows);
				// Can return 0 (no change), 1 (insert), or 2 (update), so >= 0 is a good check
				// for success.
				return affectedRows >= 0;
			} catch (SQLException e) {
				logger.error("DAO Error upserting qualification for user {} course {}", userId, courseId, e);
				return false;
			}
		}
	}

	// --- Helper Methods ---

	/**
	 * Maps a row from a ResultSet to a UserQualification object.
	 * 
	 * @param rs The ResultSet to map.
	 * @return A populated UserQualification object.
	 * @throws SQLException If a database access error occurs.
	 */
	private UserQualification mapResultSetToUserQualification(ResultSet rs) throws SQLException {
		UserQualification uq = new UserQualification();
		// user_id is only present in the getAllQualifications query
		if (hasColumn(rs, "user_id")) {
			uq.setUserId(rs.getInt("user_id"));
		}
		uq.setCourseId(rs.getInt("course_id"));
		uq.setCourseName(rs.getString("name"));
		uq.setStatus(rs.getString("status"));
		uq.setRemarks(rs.getString("remarks"));
		Date dbDate = rs.getDate("completion_date");
		if (dbDate != null) {
			uq.setCompletionDate(dbDate.toLocalDate());
		}
		return uq;
	}

	/**
	 * Checks if a ResultSet contains a column with the given name
	 * (case-insensitive).
	 * 
	 * @param rs         The ResultSet to check.
	 * @param columnName The name of the column.
	 * @return true if the column exists, false otherwise.
	 * @throws SQLException If a database error occurs.
	 */
	private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
				return true;
			}
		}
		return false;
	}
}