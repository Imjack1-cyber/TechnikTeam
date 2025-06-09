package de.technikteam.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.UserQualification;

public class UserQualificationsDAO {
	private static final Logger logger = LogManager.getLogger(UserQualificationsDAO.class);

	// Hilfsmethode, um den Code sauber zu halten
	private UserQualification mapResultSetToUserQualification(ResultSet rs) throws SQLException {
		UserQualification uq = new UserQualification();
		// userId wird nur von getAllQualifications benötigt, daher prüfen wir, ob die
		// Spalte existiert
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

	// Methode getQualificationsForUser anpassen, um neue Felder zu lesen
	public List<UserQualification> getQualificationsForUser(int userId) {
		List<UserQualification> qualifications = new ArrayList<>();
		// SQL-Abfrage um `remarks` erweitert
		String sql = "SELECT uq.course_id, c.name, uq.status, uq.completion_date, uq.remarks "
				+ "FROM user_qualifications uq JOIN courses c ON uq.course_id = c.id WHERE uq.user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

	// NEUE Methode, die für die Matrix-Seite benötigt wird
	public List<UserQualification> getAllQualifications() {
		logger.debug("Fetching all user qualifications.");
		List<UserQualification> qualifications = new ArrayList<>();
		String sql = "SELECT uq.user_id, uq.course_id, c.name, uq.status, uq.completion_date, uq.remarks "
				+ "FROM user_qualifications uq JOIN courses c ON uq.course_id = c.id";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				qualifications.add(mapResultSetToUserQualification(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching all qualifications.", e);
		}
		return qualifications;
	}

	// updateQualificationStatus anpassen, um remarks zu speichern
	public boolean updateQualificationStatus(int userId, int courseId, String status, LocalDate completionDate,
			String remarks) {
		String sql = "UPDATE user_qualifications SET status = ?, completion_date = ?, remarks = ? WHERE user_id = ? AND course_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setDate(2, completionDate != null ? Date.valueOf(completionDate) : null);
			pstmt.setString(3, remarks);
			pstmt.setInt(4, userId);
			pstmt.setInt(5, courseId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating qualification", e);
			return false;
		}
	}

	// Hilfsmethode, um zu prüfen, ob eine Spalte existiert (verhindert Fehler)
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

	public boolean qualificationExists(int userId, int courseId) {
		String sql = "SELECT 1 FROM user_qualifications WHERE user_id = ? AND course_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, courseId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next(); // true if a record was found
			}
		} catch (SQLException e) {
			logger.error("Error checking if qualification exists", e);
			return false;
		}
	}

	public boolean createQualification(int userId, int courseId, String status, LocalDate completionDate,
			String remarks) {
		String sql = "INSERT INTO user_qualifications (user_id, course_id, status, completion_date, remarks) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, courseId);
			pstmt.setString(3, status);
			pstmt.setDate(4, completionDate != null ? Date.valueOf(completionDate) : null);
			pstmt.setString(5, remarks);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error creating qualification", e);
			return false;
		}
	}

	public boolean deleteQualification(int userId, int courseId) {
		String sql = "DELETE FROM user_qualifications WHERE user_id = ? AND course_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, courseId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting qualification", e);
			return false;
		}
	}
}