package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.FeedbackSubmission;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class GetFeedbackDetailsAction implements Action {

	private final FeedbackSubmissionDAO submissionDAO;

	@Inject
	public GetFeedbackDetailsAction(FeedbackSubmissionDAO submissionDAO) {
		this.submissionDAO = submissionDAO;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int submissionId = Integer.parseInt(request.getParameter("submissionId"));
			FeedbackSubmission submission = submissionDAO.getSubmissionById(submissionId);
			if (submission != null) {
				return new ApiResponse(true, "Details erfolgreich geladen.", submission);
			} else {
				return new ApiResponse(false, "Feedback-Eintrag nicht gefunden.", null);
			}
		} catch (NumberFormatException e) {
			return new ApiResponse(false, "Ungültige Feedback-ID.", null);
		}
	}
}