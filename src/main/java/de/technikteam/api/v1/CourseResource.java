// src/main/java/de/technikteam/api/v1/CourseResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.CourseDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map; // <-- This was the missing import
import java.util.stream.Collectors;

/**
 * A stateless, resource-oriented REST API endpoint for managing course
 * templates. Mapped to /api/v1/courses/*
 */
@Singleton
public class CourseResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CourseResource.class);

	private final CourseDAO courseDAO;
	private final AdminLogService adminLogService;
	private final Gson gson;

	@Inject
	public CourseResource(CourseDAO courseDAO, AdminLogService adminLogService, Gson gson) {
		this.courseDAO = courseDAO;
		this.adminLogService = adminLogService;
		this.gson = gson;
	}

	/**
	 * Handles GET requests. GET /api/v1/courses -> Returns a list of all course
	 * templates. GET /api/v1/courses/{id} -> Returns a single course template.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("COURSE_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();

		try {
			if (pathInfo == null || pathInfo.equals("/")) {
				List<Course> courses = courseDAO.getAllCourses();
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Courses retrieved successfully", courses));
			} else {
				Integer courseId = parseIdFromPath(pathInfo);
				if (courseId != null) {
					Course course = courseDAO.getCourseById(courseId);
					if (course != null) {
						sendJsonResponse(resp, HttpServletResponse.SC_OK,
								new ApiResponse(true, "Course retrieved successfully", course));
					} else {
						sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Course not found");
					}
				} else {
					sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID format");
				}
			}
		} catch (Exception e) {
			logger.error("Error processing GET request for courses", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	/**
	 * Handles POST requests. POST /api/v1/courses -> Creates a new course template.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("COURSE_CREATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Course newCourse = gson.fromJson(jsonPayload, Course.class);

			if (courseDAO.createCourse(newCourse)) {
				adminLogService.log(adminUser.getUsername(), "CREATE_COURSE_API",
						"Course template '" + newCourse.getName() + "' created via API.");
				// We don't know the new ID, so we return a generic success message.
				// A better approach would be for the DAO to return the created object.
				sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
						new ApiResponse(true, "Course created successfully", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
						"Course could not be created (name or abbreviation may already exist).");
			}
		} catch (JsonSyntaxException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing POST request to create course", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	/**
	 * Handles PUT requests. PUT /api/v1/courses/{id} -> Updates an existing course
	 * template.
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("COURSE_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer courseId = parseIdFromPath(req.getPathInfo());
		if (courseId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID in URL.");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Course updatedCourse = gson.fromJson(jsonPayload, Course.class);
			updatedCourse.setId(courseId);

			if (courseDAO.updateCourse(updatedCourse)) {
				adminLogService.log(adminUser.getUsername(), "UPDATE_COURSE_API",
						"Course template '" + updatedCourse.getName() + "' (ID: " + courseId + ") updated via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Course updated successfully", updatedCourse));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Course not found or update failed.");
			}
		} catch (JsonSyntaxException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing PUT request to update course", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	/**
	 * Handles DELETE requests. DELETE /api/v1/courses/{id} -> Deletes a course
	 * template.
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("COURSE_DELETE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer courseId = parseIdFromPath(req.getPathInfo());
		if (courseId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID in URL.");
			return;
		}

		Course courseToDelete = courseDAO.getCourseById(courseId);
		if (courseToDelete == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Course to delete not found.");
			return;
		}

		if (courseDAO.deleteCourse(courseId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_COURSE_API",
					"Course template '" + courseToDelete.getName() + "' (ID: " + courseId + ") deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Course deleted successfully", Map.of("deletedId", courseId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed to delete course. It may have dependent meetings or qualifications.");
		}
	}

	private Integer parseIdFromPath(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1)
			return null;
		try {
			return Integer.parseInt(pathInfo.substring(1));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}

	private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
		sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
	}
}