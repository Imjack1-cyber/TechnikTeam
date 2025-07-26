// src/main/java/de/technikteam/api/v1/ProfileRequestResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import de.technikteam.servlet.http.SessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * A stateless, resource-oriented REST API endpoint for managing user profile
 * change requests. Mapped to /api/v1/profile-requests/*
 */
@Singleton
public class ProfileRequestResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ProfileRequestResource.class);

	private final ProfileChangeRequestDAO requestDAO;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final Gson gson;

	@Inject
	public ProfileRequestResource(ProfileChangeRequestDAO requestDAO, UserDAO userDAO, AdminLogService adminLogService,
			NotificationService notificationService, Gson gson) {
		this.requestDAO = requestDAO;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
		this.gson = gson;
	}

	/**
	 * Handles GET requests. GET /api/v1/profile-requests?status=PENDING -> Returns
	 * a list of pending requests.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("USER_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String status = req.getParameter("status");
		if ("PENDING".equalsIgnoreCase(status)) {
			List<ProfileChangeRequest> pendingRequests = requestDAO.getPendingRequests();
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Pending requests retrieved.", pendingRequests));
		} else {
			// Extendable for other statuses if needed in the future
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
					"Invalid or missing status parameter. Use ?status=PENDING.");
		}
	}

	/**
	 * Handles POST requests for actions on requests. POST
	 * /api/v1/profile-requests/{id}/approve -> Approves a request. POST
	 * /api/v1/profile-requests/{id}/deny -> Denies a request.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("USER_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
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
					"Invalid URL format. Expected /api/v1/profile-requests/{id}/{action}.");
			return;
		}

		Integer requestId = parseId(pathParts[0]);
		String action = pathParts[1];

		if (requestId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request ID in URL.");
			return;
		}

		switch (action) {
		case "approve":
			handleApprove(req, resp, adminUser, requestId);
			break;
		case "deny":
			handleDeny(req, resp, adminUser, requestId);
			break;
		default:
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
		}
	}

	private void handleApprove(HttpServletRequest req, HttpServletResponse resp, User adminUser, int requestId)
			throws IOException {
		ProfileChangeRequest pcr = requestDAO.getRequestById(requestId);
		if (pcr == null || !"PENDING".equals(pcr.getStatus())) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Request not found or has already been processed.");
			return;
		}

		User userToUpdate = userDAO.getUserById(pcr.getUserId());
		if (userToUpdate == null) {
			requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId());
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND,
					"The associated user no longer exists. Request denied.");
			return;
		}

		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, String> changes = gson.fromJson(pcr.getRequestedChanges(), type);

		changes.forEach((field, value) -> {
			switch (field) {
			case "email":
				userToUpdate.setEmail(value);
				break;
			case "classYear":
				userToUpdate.setClassYear(Integer.parseInt(value));
				break;
			case "className":
				userToUpdate.setClassName(value);
				break;
			}
		});

		if (userDAO.updateUser(userToUpdate)
				&& requestDAO.updateRequestStatus(requestId, "APPROVED", adminUser.getId())) {
			adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_APPROVED_API", "Profile change for '"
					+ userToUpdate.getUsername() + "' (Request ID: " + requestId + ") approved via API.");

			String notificationMessage = "Your profile change has been approved. You will be logged out for security. Please log in again.";
			Map<String, Object> payload = Map.of("type", "logout_notification", "payload",
					Map.of("message", notificationMessage));
			notificationService.sendNotificationToUser(userToUpdate.getId(), payload);

			SessionManager.invalidateSessionsForUser(userToUpdate.getId());

			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Change request approved.", Map.of("requestId", requestId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed to apply changes or update the request.");
		}
	}

	private void handleDeny(HttpServletRequest req, HttpServletResponse resp, User adminUser, int requestId)
			throws IOException {
		ProfileChangeRequest pcr = requestDAO.getRequestById(requestId);
		if (pcr == null || !"PENDING".equals(pcr.getStatus())) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Request not found or has already been processed.");
			return;
		}

		if (requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId())) {
			adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_DENIED_API", "Profile change for user ID "
					+ pcr.getUserId() + " (Request ID: " + requestId + ") denied via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Change request denied.", Map.of("requestId", requestId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to deny the request.");
		}
	}

	private Integer parseId(String pathSegment) {
		try {
			return Integer.parseInt(pathSegment);
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