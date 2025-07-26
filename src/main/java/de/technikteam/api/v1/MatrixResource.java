// src/main/java/de/technikteam/api/v1/MatrixResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.*;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class MatrixResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MatrixResource.class);

	private final UserDAO userDAO;
	private final CourseDAO courseDAO;
	private final MeetingDAO meetingDAO;
	private final MeetingAttendanceDAO meetingAttendanceDAO;
	private final AdminLogService adminLogService;
	private final Gson gson;

	@Inject
	public MatrixResource(UserDAO userDAO, CourseDAO courseDAO, MeetingDAO meetingDAO,
			MeetingAttendanceDAO meetingAttendanceDAO, AdminLogService adminLogService, Gson gson) {
		this.userDAO = userDAO;
		this.courseDAO = courseDAO;
		this.meetingDAO = meetingDAO;
		this.meetingAttendanceDAO = meetingAttendanceDAO;
		this.adminLogService = adminLogService;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("QUALIFICATION_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			List<User> allUsers = userDAO.getAllUsers();
			List<Course> allCourses = courseDAO.getAllCourses();

			Map<Integer, List<Meeting>> meetingsByCourse = new HashMap<>();
			for (Course course : allCourses) {
				meetingsByCourse.put(course.getId(), meetingDAO.getMeetingsForCourse(course.getId()));
			}

			Map<String, MeetingAttendance> attendanceMap = meetingAttendanceDAO.getAllAttendance().stream()
					.collect(Collectors.toMap(a -> a.getUserId() + "-" + a.getMeetingId(), Function.identity()));

			Map<String, Object> responseData = new HashMap<>();
			responseData.put("users", allUsers);
			responseData.put("courses", allCourses);
			responseData.put("meetingsByCourse", meetingsByCourse);
			responseData.put("attendanceMap", attendanceMap);

			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Matrix data retrieved", responseData));

		} catch (Exception e) {
			logger.error("Error generating matrix data", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate matrix data.");
		}
	}

	/**
	 * Handles PUT requests to update a single attendance record. PUT
	 * /api/v1/matrix/attendance
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("QUALIFICATION_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> payload = gson.fromJson(jsonPayload, type);

			int userId = ((Double) payload.get("userId")).intValue();
			int meetingId = ((Double) payload.get("meetingId")).intValue();
			boolean attended = (Boolean) payload.get("attended");
			String remarks = (String) payload.get("remarks");

			if (meetingAttendanceDAO.setAttendance(userId, meetingId, attended, remarks)) {
				User targetUser = userDAO.getUserById(userId);
				Meeting meeting = meetingDAO.getMeetingById(meetingId);
				String status = attended ? "ATTENDED" : "ABSENT";
				String logDetails = String.format(
						"Attendance for user '%s' at meeting '%s' set to '%s'. Remarks: '%s'.",
						(targetUser != null ? targetUser.getUsername() : "N/A"),
						(meeting != null ? meeting.getName() : "N/A"), status, remarks);
				adminLogService.log(adminUser.getUsername(), "UPDATE_ATTENDANCE_API", logDetails);
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Attendance updated successfully", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Failed to update attendance record.");
			}
		} catch (JsonSyntaxException | NullPointerException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload format.");
		} catch (Exception e) {
			logger.error("Error processing PUT request to update attendance", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
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