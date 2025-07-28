// src/main/java/de/technikteam/api/v1/public_api/PublicMeetingResource.java
package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Singleton
public class PublicMeetingResource extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final MeetingDAO meetingDAO;
	private final MeetingAttendanceDAO attendanceDAO;
	private final Gson gson;

	@Inject
	public PublicMeetingResource(MeetingDAO meetingDAO, MeetingAttendanceDAO attendanceDAO, Gson gson) {
		this.meetingDAO = meetingDAO;
		this.attendanceDAO = attendanceDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		List<Meeting> meetings = meetingDAO.getUpcomingMeetingsForUser(user);
		sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Meetings retrieved.", meetings));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");
		if (pathParts.length != 2) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
					"Invalid URL format. Expected /api/v1/public/meetings/{id}/{action}.");
			return;
		}

		try {
			int meetingId = Integer.parseInt(pathParts[0]);
			String action = pathParts[1];

			boolean success = false;
			if ("signup".equals(action)) {
				success = attendanceDAO.setAttendance(user.getId(), meetingId, true, "");
			} else if ("signoff".equals(action)) {
				success = attendanceDAO.setAttendance(user.getId(), meetingId, false, "");
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
				return;
			}

			if (success) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Action completed successfully.", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to process action.");
			}
		} catch (NumberFormatException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid meeting ID format.");
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