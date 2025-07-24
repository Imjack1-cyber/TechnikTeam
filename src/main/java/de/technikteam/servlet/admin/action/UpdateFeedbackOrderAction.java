package de.technikteam.servlet.admin.action;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

@Singleton
public class UpdateFeedbackOrderAction implements Action {
	private static final Logger logger = LogManager.getLogger(UpdateFeedbackOrderAction.class);
	private final FeedbackSubmissionDAO submissionDAO;
	private final AdminLogService adminLogService;
	private final Gson gson = new Gson();

	@Inject
	public UpdateFeedbackOrderAction(FeedbackSubmissionDAO submissionDAO, AdminLogService adminLogService) {
		this.submissionDAO = submissionDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (!adminUser.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		String jsonPayload = request.getParameter("reorderData");
		if (jsonPayload == null) {
			return ApiResponse.error("Missing reorder data.");
		}

		try {
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> data = gson.fromJson(jsonPayload, type);

			// FIX: Gson may parse numbers as Double or String. Safely convert to int.
			int submissionId = (int) Double.parseDouble(String.valueOf(data.get("submissionId")));
			String newStatus = (String) data.get("newStatus");

			// This action only updates the status. The title is updated via the modal.
			boolean success = submissionDAO.updateStatus(submissionId, newStatus);

			if (success) {
				adminLogService.log(adminUser.getUsername(), "UPDATE_FEEDBACK_STATUS",
						"Status für Feedback ID " + submissionId + " auf '" + newStatus + "' gesetzt (via Drag&Drop).");
				return ApiResponse.success("Feedback-Status aktualisiert.");
			} else {
				return ApiResponse.error("Fehler beim Aktualisieren des Feedback-Status.");
			}
		} catch (Exception e) {
			logger.error("Error processing feedback reorder request", e);
			return ApiResponse.error("Serverfehler beim Verarbeiten der Anfrage: " + e.getMessage());
		}
	}
}