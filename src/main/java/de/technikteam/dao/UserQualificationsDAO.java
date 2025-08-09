package de.technikteam.dao;

import de.technikteam.model.UserQualification;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class UserQualificationsDAO {
	private static final Logger logger = LogManager.getLogger(UserQualificationsDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public UserQualificationsDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private UserQualification mapResultSetToUserQualification(ResultSet rs, int rowNum) throws SQLException {
		UserQualification uq = new UserQualification();
		if (DaoUtils.hasColumn(rs, "user_id")) {
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

	public List<UserQualification> getQualificationsForUser(int userId) {
		String sql = "SELECT uq.course_id, c.name, uq.status, uq.completion_date, uq.remarks FROM user_qualifications uq JOIN courses c ON uq.course_id = c.id WHERE uq.user_id = ?";
		try {
			return jdbcTemplate.query(sql, this::mapResultSetToUserQualification, userId);
		} catch (Exception e) {
			logger.error("SQL error fetching qualifications for user ID: {}", userId, e);
			return List.of();
		}
	}

	public boolean hasUserCompletedCourse(int userId, int courseId) {
		String sql = "SELECT COUNT(*) FROM user_qualifications WHERE user_id = ? AND course_id = ? AND status = 'BESTANDEN'";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, courseId);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("Error checking if user {} completed course {}", userId, courseId, e);
			return false;
		}
	}

	public boolean updateQualificationStatus(int userId, int courseId, String status, LocalDate completionDate,
			String remarks) {
		if ("NICHT BESUCHT".equals(status)) {
			String deleteSql = "DELETE FROM user_qualifications WHERE user_id = ? AND course_id = ?";
			try {
				jdbcTemplate.update(deleteSql, userId, courseId);
				return true;
			} catch (Exception e) {
				logger.error("DAO Error deleting qualification for user {} course {}", userId, courseId, e);
				return false;
			}
		} else {
			String upsertSql = "INSERT INTO user_qualifications (user_id, course_id, status, completion_date, remarks) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = VALUES(status), completion_date = VALUES(completion_date), remarks = VALUES(remarks)";
			try {
				jdbcTemplate.update(upsertSql, userId, courseId, status, completionDate, remarks);
				return true;
			} catch (Exception e) {
				logger.error("DAO Error upserting qualification for user {} course {}", userId, courseId, e);
				return false;
			}
		}
	}
}