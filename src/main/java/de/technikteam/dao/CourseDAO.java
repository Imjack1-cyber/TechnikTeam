package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.Course;
import de.technikteam.model.User;

/**
 * Data Access Object for all Course-related database operations. Handles
 * creating, reading, updating, and deleting courses, as well as managing user
 * attendance for courses.
 */
public class CourseDAO {
	private static final Logger logger = LogManager.getLogger(CourseDAO.class);

	// --- Methods for Public (User-Facing) Views ---

	/**
	 * Fetches all upcoming courses, enriched with the attendance status for a
	 * specific user. Used for the main /lehrgaenge page.
	 * 
	 * @param user The currently logged-in user.
	 * @return A list of upcoming Course objects with user-specific status.
	 */
	public List<Course> getUpcomingCoursesForUser(User user) {
		logger.debug("Fetching upcoming courses for user ID: {}", user.getId());
		List<Course> courses = new ArrayList<>();
		String sql = "SELECT c.*, ca.signup_status FROM courses c "
				+ "LEFT JOIN course_attendance ca ON c.id = ca.course_id AND ca.user_id = ? "
				+ "WHERE c.course_datetime >= NOW() ORDER BY c.course_datetime ASC";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, user.getId());
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Course course = mapResultSetToCourse(rs);
					course.setUserAttendanceStatus(rs.getString("signup_status"));
					courses.add(course);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching upcoming courses for user {}", user.getId(), e);
		}
		return courses;
	}

	// --- Methods for Admin CRUD Operations ---

	/**
	 * Fetches a single course by its ID. Used for the admin edit form.
	 * 
	 * @param courseId The ID of the course.
	 * @return A Course object, or null if not found.
	 */
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

	/**
	 * Creates a new course in the database.
	 * 
	 * @param course The Course object to create.
	 * @return true if creation was successful, false otherwise.
	 */
	public boolean createCourse(Course course) {
		String sql = "INSERT INTO courses (name, type, abbreviation, course_datetime, leader, description) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getType());
			pstmt.setString(3, course.getAbbreviation()); // <-- NEU
			pstmt.setTimestamp(4, Timestamp.valueOf(course.getCourseDateTime()));
			pstmt.setString(5, course.getLeader());
			pstmt.setString(6, course.getDescription());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating course: {}", course.getName(), e);
			return false;
		}
	}

	/**
	 * Updates an existing course in the database.
	 * 
	 * @param course The Course object with updated data.
	 * @return true if update was successful, false otherwise.
	 */
	public boolean updateCourse(Course course) {
		String sql = "UPDATE courses SET name=?, type=?, abbreviation=?, course_datetime=?, leader=?, description=? WHERE id=?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getType());
			pstmt.setString(3, course.getAbbreviation()); // <-- NEU
			pstmt.setTimestamp(4, Timestamp.valueOf(course.getCourseDateTime()));
			pstmt.setString(5, course.getLeader());
			pstmt.setString(6, course.getDescription());
			pstmt.setInt(7, course.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating course with ID: {}", course.getId(), e);
			return false;
		}
	}

	/**
	 * Deletes a course from the database.
	 * 
	 * @param courseId The ID of the course to delete.
	 * @return true if deletion was successful, false otherwise.
	 */
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

	// --- Methods for User Actions ---

	// Fügen Sie diese neue Methode zu Ihrer CourseDAO.java hinzu.

	/**
	 * Fetches a limited number of upcoming courses. Used for the home page
	 * dashboard view.
	 *
	 * @param limit The maximum number of courses to return.
	 * @return A list of upcoming Course objects.
	 */
	public List<Course> getUpcomingCourses(int limit) {
		logger.debug("Fetching upcoming courses with limit: {}", limit);
		List<Course> courses = new ArrayList<>();
		// Using LIMIT clause to restrict the number of results
		String sql = "SELECT * FROM courses WHERE course_datetime >= NOW() ORDER BY course_datetime ASC LIMIT ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, limit);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					// We reuse our existing helper method
					courses.add(mapResultSetToCourse(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching upcoming courses with limit.", e);
		}
		return courses;
	}

	// Stellen Sie sicher, dass diese Methode in Ihrem CourseDAO existiert und
	// vollständig ist.

	/**
	 * Signs a user off from a course by updating their status in the attendance
	 * table.
	 * 
	 * @param userId   The ID of the user.
	 * @param courseId The ID of the course.
	 */
	public void signOffFromCourse(int userId, int courseId) {
		logger.info("User {} signing off from course {}", userId, courseId);
		// We update the status. If no record exists, nothing happens, which is correct.
		String sql = "UPDATE course_attendance SET signup_status = 'ABGEMELDET' WHERE user_id = ? AND course_id = ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, userId);
			pstmt.setInt(2, courseId);

			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Successfully signed off user {} from course {}", userId, courseId);
			} else {
				logger.warn(
						"Attempted to sign off user {} from course {}, but no matching attendance record was found.",
						userId, courseId);
			}

		} catch (SQLException e) {
			logger.error("SQL error during course sign-off for user {} and course {}", userId, courseId, e);
		}
	}

	// Stellen Sie sicher, dass die signUpForCourse-Methode ebenfalls vorhanden ist.
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

	// Fügen Sie diese neue Methode zu Ihrer CourseDAO-Klasse hinzu.

	/**
	 * Fetches a list of all users who have signed up for a specific course.
	 * 
	 * @param courseId The ID of the course.
	 * @return A list of User objects.
	 */
	public List<User> getSignedUpUsersForCourse(int courseId) {
		logger.debug("Fetching all signed-up users for course ID: {}", courseId);
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, u.role FROM users u " + "JOIN course_attendance ca ON u.id = ca.user_id "
				+ "WHERE ca.course_id = ? AND ca.signup_status = 'ANGEMELDET'";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, courseId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role")));
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching signed-up users for course ID: {}", courseId, e);
		}
		return users;
	}

	/**
	 * Helper method to map a row from a ResultSet to a Course object. It safely
	 * checks for the existence of optional columns like 'abbreviation'.
	 *
	 * @param rs The ResultSet to map from.
	 * @return A populated Course object.
	 * @throws SQLException If a database access error occurs.
	 */
	private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
		Course course = new Course();
		course.setId(rs.getInt("id"));
		course.setName(rs.getString("name"));
		course.setType(rs.getString("type"));
		course.setCourseDateTime(rs.getTimestamp("course_datetime").toLocalDateTime());
		course.setLeader(rs.getString("leader"));
		course.setDescription(rs.getString("description"));

		// Safely check if the 'abbreviation' column was part of the SELECT query
		if (hasColumn(rs, "abbreviation")) {
			course.setAbbreviation(rs.getString("abbreviation"));
		}

		return course;
	}

	/**
	 * Fetches all courses from the database, sorted by date. This is typically used
	 * for the admin list view. The "SELECT *" ensures all columns, including the
	 * new 'abbreviation', are fetched.
	 *
	 * @return A list of all Course objects.
	 */
	public List<Course> getAllCourses() {
		logger.debug("Fetching all courses from database.");
		List<Course> courses = new ArrayList<>();
		// Using "SELECT *" is a simple way to ensure all columns are retrieved.
		String sql = "SELECT * FROM courses ORDER BY course_datetime DESC";

		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				courses.add(mapResultSetToCourse(rs));
			}
			logger.info("Fetched {} total courses.", courses.size());
		} catch (SQLException e) {
			logger.error("SQL error while fetching all courses.", e);
		}
		return courses;
	}

	/**
	 * Helper method to check if a ResultSet contains a certain column name. This
	 * prevents errors if a query doesn't select all columns.
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