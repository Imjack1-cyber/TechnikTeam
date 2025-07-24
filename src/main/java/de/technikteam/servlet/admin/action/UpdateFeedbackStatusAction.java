package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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

@Singleton
public class UpdateFeedbackStatusAction implements Action {
	private final FeedbackSubmissionDAO submissionDAO;
	private final AdminLogService adminLogService;

	@Inject
	public UpdateFeedbackStatusAction(FeedbackSubmissionDAO submissionDAO, AdminLogService adminLogService) {
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

		try {
			int submissionId = Integer.parseInt(request.getParameter("submissionId"));
			String newStatus = request.getParameter("status");
			String displayTitle = request.getParameter("displayTitle");

			if (submissionDAO.updateStatusAndTitle(submissionId, newStatus, displayTitle)) {
				adminLogService.log(adminUser.getUsername(), "UPDATE_FEEDBACK_STATUS",
						"Status für Feedback ID " + submissionId + " auf '" + newStatus + "' gesetzt.");

				NotificationService.getInstance().broadcastUIUpdate("feedback_status_updated",
						Map.of("submissionId", submissionId, "newStatus", newStatus, "displayTitle", displayTitle));

				return ApiResponse.success("Status erfolgreich aktualisiert.");
			} else {
				return ApiResponse.error("Fehler beim Aktualisieren des Status.");
			}
		} catch (NumberFormatException e) {
			return ApiResponse.error("Ungültige Feedback-ID.");
		}
	}
}