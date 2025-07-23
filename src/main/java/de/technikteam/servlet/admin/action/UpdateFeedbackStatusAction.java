package de.technikteam.servlet.admin.action;

import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class UpdateFeedbackStatusAction implements Action {
	private final FeedbackSubmissionDAO submissionDAO = new FeedbackSubmissionDAO();

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (!adminUser.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		try {
			int submissionId = Integer.parseInt(request.getParameter("submissionId"));
			String newStatus = request.getParameter("status");

			if (submissionDAO.updateStatus(submissionId, newStatus)) {
				AdminLogService.log(adminUser.getUsername(), "UPDATE_FEEDBACK_STATUS",
						"Status für Feedback ID " + submissionId + " auf '" + newStatus + "' gesetzt.");

				// Broadcast the UI update to all connected admins
				NotificationService.getInstance().broadcastUIUpdate("feedback_status_updated",
						Map.of("submissionId", submissionId, "newStatus", newStatus));

				return ApiResponse.success("Status erfolgreich aktualisiert.");
			} else {
				return ApiResponse.error("Fehler beim Aktualisieren des Status.");
			}
		} catch (NumberFormatException e) {
			return ApiResponse.error("Ungültige Feedback-ID.");
		}
	}
}