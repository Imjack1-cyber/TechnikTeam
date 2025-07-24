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
public class DeleteFeedbackAction implements Action {
	private final FeedbackSubmissionDAO submissionDAO;
	private final AdminLogService adminLogService;

	@Inject
	public DeleteFeedbackAction(FeedbackSubmissionDAO submissionDAO, AdminLogService adminLogService) {
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
			if (submissionDAO.deleteSubmission(submissionId)) {
				adminLogService.log(adminUser.getUsername(), "DELETE_FEEDBACK",
						"Feedback-Eintrag mit ID " + submissionId + " gelöscht.");
				NotificationService.getInstance().broadcastUIUpdate("feedback_deleted",
						Map.of("submissionId", submissionId));
				return ApiResponse.success("Feedback erfolgreich gelöscht.", Map.of("deletedId", submissionId));
			} else {
				return ApiResponse.error("Fehler beim Löschen des Feedbacks.");
			}
		} catch (NumberFormatException e) {
			return ApiResponse.error("Ungültige Feedback-ID.");
		}
	}
}