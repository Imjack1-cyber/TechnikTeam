// src/main/java/de/technikteam/api/v1/FeedbackResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.DatabaseManager;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.FeedbackSubmission;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A stateless, resource-oriented REST API endpoint for managing feedback
 * submissions. Mapped to /api/v1/feedback/*
 */
@Singleton
public class FeedbackResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(FeedbackResource.class);
	private static final List<String> FEEDBACK_STATUS_ORDER = Arrays.asList("NEW", "VIEWED", "PLANNED", "REJECTED",
			"COMPLETED");

	private final FeedbackSubmissionDAO submissionDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final DatabaseManager dbManager;
	private final Gson gson;

	@Inject
	public FeedbackResource(FeedbackSubmissionDAO submissionDAO, AdminLogService adminLogService,
			NotificationService notificationService, DatabaseManager dbManager, Gson gson) {
		this.submissionDAO = submissionDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
		this.dbManager = dbManager;
		this.gson = gson;
	}

	/**
	 * Handles GET requests. GET /api/v1/feedback -> Returns the full state of the
	 * feedback board. GET /api/v1/feedback/{id} -> Returns a single feedback
	 * submission.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.hasAdminAccess()) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			handleGetAllFeedback(req, resp);
		} else {
			handleGetSingleFeedback(req, resp, pathInfo);
		}
	}

	/**
	 * Handles PATCH requests for partial updates. PATCH /api/v1/feedback/{id} ->
	 * Updates status and/or displayTitle.
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.hasAdminAccess()) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer submissionId = parseIdFromPath(req.getPathInfo());
		if (submissionId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid submission ID in URL.");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Map<String, String> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
			}.getType());

			String newStatus = payload.get("status");
			String displayTitle = payload.get("displayTitle");

			if (submissionDAO.updateStatusAndTitle(submissionId, newStatus, displayTitle)) {
				adminLogService.log(adminUser.getUsername(), "UPDATE_FEEDBACK_STATUS_API",
						"Status for Feedback ID " + submissionId + " set to '" + newStatus + "' via API.");
				notificationService.broadcastUIUpdate("feedback_status_updated",
						Map.of("submissionId", submissionId, "newStatus", newStatus, "displayTitle", displayTitle));
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Status successfully updated.", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Feedback entry not found or update failed.");
			}
		} catch (JsonSyntaxException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing PATCH for feedback {}", submissionId, e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred.");
		}
	}

	/**
	 * Handles POST requests for non-idempotent actions. POST
	 * /api/v1/feedback/reorder -> Updates the status and order of multiple items.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.hasAdminAccess()) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		if ("/reorder".equals(pathInfo)) {
			handleReorder(req, resp, adminUser);
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	/**
	 * Handles DELETE requests. DELETE /api/v1/feedback/{id} -> Deletes a feedback
	 * submission.
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.hasAdminAccess()) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer submissionId = parseIdFromPath(req.getPathInfo());
		if (submissionId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid submission ID in URL.");
			return;
		}

		if (submissionDAO.deleteSubmission(submissionId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_FEEDBACK_API",
					"Feedback entry with ID " + submissionId + " deleted via API.");
			notificationService.broadcastUIUpdate("feedback_deleted", Map.of("submissionId", submissionId));
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Feedback successfully deleted.", Map.of("deletedId", submissionId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND,
					"Failed to delete feedback. It may have already been deleted.");
		}
	}

	private void handleGetAllFeedback(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		List<FeedbackSubmission> submissions = submissionDAO.getAllSubmissions();
		Map<String, List<FeedbackSubmission>> groupedSubmissions = submissions.stream()
				.collect(Collectors.groupingBy(FeedbackSubmission::getStatus));

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("groupedSubmissions", groupedSubmissions);
		responseData.put("statusOrder", FEEDBACK_STATUS_ORDER);

		sendJsonResponse(resp, HttpServletResponse.SC_OK,
				new ApiResponse(true, "Feedback board state retrieved.", responseData));
	}

	private void handleGetSingleFeedback(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
			throws IOException {
		Integer submissionId = parseIdFromPath(pathInfo);
		if (submissionId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid submission ID format.");
			return;
		}

		FeedbackSubmission submission = submissionDAO.getSubmissionById(submissionId);
		if (submission != null) {
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Details loaded successfully.", submission));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Feedback entry not found.");
		}
	}

	private void handleReorder(HttpServletRequest req, HttpServletResponse resp, User adminUser) throws IOException {
		try (Connection conn = dbManager.getConnection()) {
			conn.setAutoCommit(false);
			try {
				String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
				Type type = new TypeToken<Map<String, Object>>() {
				}.getType();
				Map<String, Object> data = gson.fromJson(jsonPayload, type);

				int submissionId = ((Double) data.get("submissionId")).intValue();
				String newStatus = (String) data.get("newStatus");
				List<Double> orderedIdsDouble = (List<Double>) data.get("orderedIds");
				if (orderedIdsDouble == null) {
					throw new IllegalArgumentException("orderedIds list is missing in the payload.");
				}
				List<Integer> orderedIds = orderedIdsDouble.stream().map(Double::intValue).collect(Collectors.toList());

				submissionDAO.updateStatus(submissionId, newStatus, conn);
				submissionDAO.updateOrderForStatus(orderedIds, conn);

				conn.commit();

				adminLogService.log(adminUser.getUsername(), "UPDATE_FEEDBACK_ORDER_API", "Feedback ID " + submissionId
						+ " status set to '" + newStatus + "' and list reordered via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Feedback status and order updated.", null));

			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} catch (Exception e) {
			logger.error("Error processing feedback reorder request", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Server error processing the request: " + e.getMessage());
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