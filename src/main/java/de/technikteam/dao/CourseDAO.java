// In: src/main/java/de/technikteam/dao/CourseDAO.java
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
 * Data Access Object for managing the parent 'courses' table. This DAO only
 * handles the course templates (e.g., "Grundlehrgang"), not the schedulable
 * meetings.
 */
public class CourseDAO {
	private static final Logger logger = LogManager.getLogger(CourseDAO.class);

	/**
	 * Creates a new parent course template in the database.
	 * 
	 * @param course The Course object to create (only name, abbreviation,
	 *               description).
	 * @return true if creation was successful, false otherwise.
	 */
	public boolean createCourse(Course course) {
		// FIX: SQL statement now matches the new 'courses' table schema.
		String sql = "INSERT INTO courses (name, abbreviation, description) VALUES (?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getAbbreviation());
			pstmt.setString(3, course.getDescription());

			logger.info("Creating parent course: {}", course.getName());
			return pstmt.executeUpdate() > 0;

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
	 * Fetches all parent courses from the database, sorted by name.
	 * 
	 * @return A list of all Course objects.
	 */
	public List<Course> getAllCourses() {
		List<Course> courses = new ArrayList<>();
		// FIX: The query is now simple and orders by name, not a non-existent date
		// column.
		String sql = "SELECT * FROM courses ORDER BY name ASC";

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
	
	// In: src/main/java/de/technikteam/dao/CourseDAO.java

	// ... (keep createCourse, getCourseById, getAllCourses, and mapResultSetToCourse as they are)

	    /**
	     * Updates an existing parent course's name, abbreviation, and description.
	     * @param course The Course object with the updated data.
	     * @return true if the update was successful, false otherwise.
	     */
	    public boolean updateCourse(Course course) {
	        String sql = "UPDATE courses SET name = ?, abbreviation = ?, description = ? WHERE id = ?";
	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            
	            pstmt.setString(1, course.getName());
	            pstmt.setString(2, course.getAbbreviation());
	            pstmt.setString(3, course.getDescription());
	            pstmt.setInt(4, course.getId());
	            
	            logger.info("Updating parent course: {}", course.getName());
	            return pstmt.executeUpdate() > 0;
	            
	        } catch (SQLException e) {
	            logger.error("SQL error updating course: {}", course.getName(), e);
	            return false;
	        }
	    }

	    /**
	     * Deletes a parent course from the database.
	     * NOTE: ON DELETE CASCADE in the database should also delete all associated meetings.
	     * @param courseId The ID of the course to delete.
	     * @return true if the deletion was successful, false otherwise.
	     */
	    public boolean deleteCourse(int courseId) {
	        String sql = "DELETE FROM courses WHERE id = ?";
	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            
	            pstmt.setInt(1, courseId);
	            
	            logger.warn("Deleting parent course with ID: {}", courseId);
	            return pstmt.executeUpdate() > 0;
	            
	        } catch (SQLException e) {
	            logger.error("SQL error deleting course with ID: {}", courseId, e);
	            return false;
	        }
	    }
}