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

import de.technikteam.model.Course;

public class CourseDAO {
	private static final Logger logger = LogManager.getLogger(CourseDAO.class);

	private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
		Course course = new Course();
		course.setId(rs.getInt("id"));
		course.setName(rs.getString("name"));
		course.setType(rs.getString("type"));
		course.setCourseDateTime(rs.getTimestamp("course_datetime").toLocalDateTime());
		course.setLeader(rs.getString("leader"));
		course.setDescription(rs.getString("description")); // This line will now work
		return course;
	}

	public List<Course> getUpcomingCourses(int limit) {
		logger.debug("Fetching upcoming courses with limit: {}", limit);
		List<Course> courses = new ArrayList<>();
		String sql = "SELECT * FROM courses WHERE course_datetime >= NOW() ORDER BY course_datetime ASC"
				+ (limit > 0 ? " LIMIT ?" : "");

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			if (limit > 0) {
				pstmt.setInt(1, limit);
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				courses.add(mapResultSetToCourse(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching upcoming courses with limit.", e);
		}
		return courses;
	}

	public void signUpForCourse(int userId, int courseId) {
		logger.info("User {} signing up for course {}", userId, courseId);
		String sql = "INSERT INTO course_attendance (user_id, course_id, signup_status) VALUES (?, ?, 'ANGEMELDET') "
				+ "ON DUPLICATE KEY UPDATE signup_status = 'ANGEMELDET'";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, courseId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL error during course sign-up for user {} and course {}", userId, courseId, e);
		}
	}

	public void signOffFromCourse(int userId, int courseId) {
		logger.info("User {} signing off from course {}", userId, courseId);
		String sql = "UPDATE course_attendance SET signup_status = 'ABGEMELDET' WHERE user_id = ? AND course_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, courseId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL error during course sign-off for user {} and course {}", userId, courseId, e);
		}
	}

	// Placeholder for a future feature: Get all courses attended by a user.
	public List<Course> getAttendedCoursesByUser(int userId) {
		// This would require a JOIN with course_attendance table.
		return new ArrayList<>();
	}

	/**
	 * Fetches all upcoming courses without a limit.
	 * 
	 * @return A list of all upcoming Course objects.
	 */
	public List<Course> getAllUpcomingCourses() {
		logger.debug("Fetching all upcoming courses.");
		List<Course> courses = new ArrayList<>();
		String sql = "SELECT * FROM courses WHERE course_datetime >= NOW() ORDER BY course_datetime ASC";

		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				courses.add(mapResultSetToCourse(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching all upcoming courses.", e);
		}
		return courses;
	}

	// Add these CRUD methods to the existing CourseDAO class.

	public Course getCourseById(int courseId) {
		logger.debug("Fetching course by ID: {}", courseId);
		String sql = "SELECT * FROM courses WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, courseId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToCourse(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching course by ID: {}", courseId, e);
		}
		return null;
	}

	public List<Course> getAllCourses() {
		logger.debug("Fetching all courses from database.");
		List<Course> courses = new ArrayList<>();
		String sql = "SELECT * FROM courses ORDER BY course_datetime DESC";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				courses.add(mapResultSetToCourse(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching all courses.", e);
		}
		return courses;
	}

	public boolean createCourse(Course course) {
		logger.info("Creating new course: {}", course.getName());
		String sql = "INSERT INTO courses (name, type, course_datetime, leader, description) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getType());
			// This line now compiles because Timestamp is imported
			pstmt.setTimestamp(3, Timestamp.valueOf(course.getCourseDateTime()));
			pstmt.setString(4, course.getLeader());
			pstmt.setString(5, course.getDescription());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating course: {}", course.getName(), e);
			return false;
		}
	}

	public boolean updateCourse(Course course) {
		logger.info("Updating course with ID: {}", course.getId());
		String sql = "UPDATE courses SET name=?, type=?, course_datetime=?, leader=?, description=? WHERE id=?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getType());
			// This line now compiles because Timestamp is imported
			pstmt.setTimestamp(3, Timestamp.valueOf(course.getCourseDateTime()));
			pstmt.setString(4, course.getLeader());
			pstmt.setString(5, course.getDescription());
			pstmt.setInt(6, course.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating course with ID: {}", course.getId(), e);
			return false;
		}
	}

	public boolean deleteCourse(int courseId) {
		logger.warn("Deleting course with ID: {}", courseId);
		String sql = "DELETE FROM courses WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, courseId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting course with ID: {}", courseId, e);
			return false;
		}
	}
}