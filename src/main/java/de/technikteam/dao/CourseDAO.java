package de.technikteam.dao;

import de.technikteam.model.Course;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class CourseDAO {
	private static final Logger logger = LogManager.getLogger(CourseDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public CourseDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Course> courseRowMapper = (rs, rowNum) -> {
		Course course = new Course();
		course.setId(rs.getInt("id"));
		course.setName(rs.getString("name"));
		course.setAbbreviation(rs.getString("abbreviation"));
		course.setDescription(rs.getString("description"));
		return course;
	};

	public Course createCourse(Course course) {
		String sql = "INSERT INTO courses (name, abbreviation, description) VALUES (?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, course.getName());
				ps.setString(2, course.getAbbreviation());
				ps.setString(3, course.getDescription());
				return ps;
			}, keyHolder);
			int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
			course.setId(newId);
			return course;
		} catch (Exception e) {
			logger.error("Error creating course: {}", course.getName(), e);
			return null;
		}
	}

	public Course getCourseById(int courseId) {
		String sql = "SELECT * FROM courses WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, courseRowMapper, courseId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching course by ID: {}", courseId, e);
			return null;
		}
	}

	public List<Course> getAllCourses() {
		String sql = "SELECT * FROM courses ORDER BY name ASC";
		try {
			return jdbcTemplate.query(sql, courseRowMapper);
		} catch (Exception e) {
			logger.error("Error while fetching all courses.", e);
			return List.of();
		}
	}

	public boolean updateCourse(Course course) {
		String sql = "UPDATE courses SET name = ?, abbreviation = ?, description = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, course.getName(), course.getAbbreviation(), course.getDescription(),
					course.getId()) > 0;
		} catch (Exception e) {
			logger.error("Error updating course: {}", course.getName(), e);
			return false;
		}
	}

	public boolean deleteCourse(int courseId) {
		String sql = "DELETE FROM courses WHERE id = ?";
		try {
			// Manually delete related data due to foreign key constraints
			jdbcTemplate.update("DELETE FROM event_skill_requirements WHERE required_course_id = ?", courseId);
			jdbcTemplate.update("DELETE FROM user_qualifications WHERE course_id = ?", courseId);
			jdbcTemplate.update("DELETE FROM meetings WHERE course_id = ?", courseId);

			return jdbcTemplate.update(sql, courseId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting course with ID: {}", courseId, e);
			return false;
		}
	}
}