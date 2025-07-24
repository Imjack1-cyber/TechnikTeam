package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.Course;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class CourseDAO {
	private static final Logger logger = LogManager.getLogger(CourseDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public CourseDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public boolean createCourse(Course course) {
		String sql = "INSERT INTO courses (name, abbreviation, description) VALUES (?, ?, ?)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getAbbreviation());
			pstmt.setString(3, course.getDescription());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating course: {}", course.getName(), e);
			return false;
		}
	}

	public Course getCourseById(int courseId) {
		String sql = "SELECT * FROM courses WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
		List<Course> courses = new ArrayList<>();
		String sql = "SELECT * FROM courses ORDER BY name ASC";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				courses.add(mapResultSetToCourse(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching all courses.", e);
		}
		return courses;
	}

	private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
		Course course = new Course();
		course.setId(rs.getInt("id"));
		course.setName(rs.getString("name"));
		course.setAbbreviation(rs.getString("abbreviation"));
		course.setDescription(rs.getString("description"));
		return course;
	}

	public boolean updateCourse(Course course) {
		String sql = "UPDATE courses SET name = ?, abbreviation = ?, description = ? WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, course.getName());
			pstmt.setString(2, course.getAbbreviation());
			pstmt.setString(3, course.getDescription());
			pstmt.setInt(4, course.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating course: {}", course.getName(), e);
			return false;
		}
	}

	public boolean deleteCourse(int courseId) {
		String sql = "DELETE FROM courses WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, courseId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting course with ID: {}", courseId, e);
			return false;
		}
	}
}