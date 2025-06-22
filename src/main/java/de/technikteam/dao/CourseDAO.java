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

import de.technikteam.model.Course;

/**
 * Data Access Object for managing `Course` templates in the `courses` table.
 * This DAO handles CRUD operations for the parent course definitions (e.g.,
 * "Grundlehrgang Tontechnik"), which serve as blueprints for individual,
 * schedulable `Meeting` instances.
 */
public class CourseDAO {
	private static final Logger logger = LogManager.getLogger(CourseDAO.class);

	/**
	 * Creates a new parent course template in the database.
	 * 
	 * @param course The Course object to create (containing name, abbreviation, and
	 *               description).
	 * @return true if creation was successful, false otherwise.
	 */
	public boolean createCourse(Course course) {
		String sql = "INSERT INTO courses (name, abbreviation, description) VALUES (?, ?, ?)";
		logger.debug("Attempting to create parent course: {}", course.getName());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getAbbreviation());
			pstmt.setString(3, course.getDescription());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				logger.info("Successfully created parent course: {}", course.getName());
				return true;
			}
			return false;

		} catch (SQLException e) {
			logger.error("SQL error creating course: {}", course.getName(), e);
			return false;
		}
	}

	/**
	 * Fetches a single parent course by its ID.
	 * 
	 * @param courseId The ID of the course.
	 * @return A Course object, or null if not found.
	 */
	public Course getCourseById(int courseId) {
		String sql = "SELECT * FROM courses WHERE id = ?";
		logger.debug("Attempting to fetch course by ID: {}", courseId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, courseId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("Found course with ID: {}", courseId);
					return mapResultSetToCourse(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching course by ID: {}", courseId, e);
		}
		logger.warn("No course found with ID: {}", courseId);
		return null;
	}

	/**
	 * Fetches all parent courses from the database, sorted alphabetically by name.
	 * 
	 * @return A list of all Course objects.
	 */
	public List<Course> getAllCourses() {
		List<Course> courses = new ArrayList<>();
		String sql = "SELECT * FROM courses ORDER BY name ASC";
		logger.debug("Attempting to fetch all parent courses.");
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				courses.add(mapResultSetToCourse(rs));
			}
			logger.info("Fetched {} total parent courses.", courses.size());
		} catch (SQLException e) {
			logger.error("SQL error while fetching all courses.", e);
		}
		return courses;
	}

	/**
	 * Helper method to map a ResultSet row to a Course object.
	 * 
	 * @param rs The ResultSet to map from.
	 * @return A populated Course object.
	 * @throws SQLException If a database access error occurs.
	 */
	private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
		Course course = new Course();
		course.setId(rs.getInt("id"));
		course.setName(rs.getString("name"));
		course.setAbbreviation(rs.getString("abbreviation"));
		course.setDescription(rs.getString("description"));
		return course;
	}

	/**
	 * Updates an existing parent course's name, abbreviation, and description.
	 * 
	 * @param course The Course object with the updated data.
	 * @return true if the update was successful, false otherwise.
	 */
	public boolean updateCourse(Course course) {
		String sql = "UPDATE courses SET name = ?, abbreviation = ?, description = ? WHERE id = ?";
		logger.debug("Attempting to update parent course: {}", course.getName());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getAbbreviation());
			pstmt.setString(3, course.getDescription());
			pstmt.setInt(4, course.getId());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				logger.info("Successfully updated parent course: {}", course.getName());
				return true;
			}
			return false;

		} catch (SQLException e) {
			logger.error("SQL error updating course: {}", course.getName(), e);
			return false;
		}
	}

	/**
	 * Deletes a parent course from the database. NOTE: This relies on `ON DELETE
	 * CASCADE` in the database schema to also delete all associated meetings and
	 * qualifications.
	 * 
	 * @param courseId The ID of the course to delete.
	 * @return true if the deletion was successful, false otherwise.
	 */
	public boolean deleteCourse(int courseId) {
		String sql = "DELETE FROM courses WHERE id = ?";
		logger.debug("Attempting to delete parent course with ID: {}", courseId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, courseId);

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				logger.warn("Successfully deleted parent course with ID: {}", courseId);
				return true;
			}
			return false;

		} catch (SQLException e) {
			logger.error("SQL error deleting course with ID: {}", courseId, e);
			return false;
		}
	}
}