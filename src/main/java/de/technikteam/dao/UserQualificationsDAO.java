package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.UserQualification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

@Singleton
public class UserQualificationsDAO {
	private static final Logger logger = LogManager.getLogger(UserQualificationsDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public UserQualificationsDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public List<UserQualification> getQualificationsForUser(int userId) {
		List<UserQualification> qualifications = new ArrayList<>();
		String sql = "SELECT uq.course_id, c.name, uq.status, uq.completion_date, uq.remarks FROM user_qualifications uq JOIN courses c ON uq.course_id = c.id WHERE uq.user_id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					qualifications.add(mapResultSetToUserQualification(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching qualifications for user ID: {}", userId, e);
		}
		return qualifications;
	}

	public List<UserQualification> getAllQualifications() {
		List<UserQualification> qualifications = new ArrayList<>();
		String sql = "SELECT uq.user_id, uq.course_id, c.name, uq.status, uq.completion_date, uq.remarks FROM user_qualifications uq JOIN courses c ON uq.course_id = c.id";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				qualifications.add(mapResultSetToUserQualification(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching all qualifications.", e);
		}
		return qualifications;
	}

	public boolean updateQualificationStatus(int userId, int courseId, String status, LocalDate completionDate,
			String remarks) {
		if ("NICHT BESUCHT".equals(status)) {
			String deleteSql = "DELETE FROM user_qualifications WHERE user_id = ? AND course_id = ?";
			try (Connection conn = dbManager.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
				pstmt.setInt(1, userId);
				pstmt.setInt(2, courseId);
				pstmt.executeUpdate();
				return true;
			} catch (SQLException e) {
				logger.error("DAO Error deleting qualification for user {} course {}", userId, courseId, e);
				return false;
			}
		} else {
			String upsertSql = "INSERT INTO user_qualifications (user_id, course_id, status, completion_date, remarks) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = VALUES(status), completion_date = VALUES(completion_date), remarks = VALUES(remarks)";
			try (Connection conn = dbManager.getConnection();
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
				return pstmt.executeUpdate() >= 0;
			} catch (SQLException e) {
				logger.error("DAO Error upserting qualification for user {} course {}", userId, courseId, e);
				return false;
			}
		}
	}

	private UserQualification mapResultSetToUserQualification(ResultSet rs) throws SQLException {
		UserQualification uq = new UserQualification();
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