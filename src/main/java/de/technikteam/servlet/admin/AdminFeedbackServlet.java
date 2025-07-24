package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.FeedbackSubmission;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class AdminFeedbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final FeedbackSubmissionDAO submissionDAO;

	// Define the order of statuses for the Kanban board
	private static final List<String> FEEDBACK_STATUS_ORDER = Arrays.asList("NEW", "VIEWED", "PLANNED", "REJECTED",
			"COMPLETED");

	@Inject
	public AdminFeedbackServlet(FeedbackSubmissionDAO submissionDAO) {
		this.submissionDAO = submissionDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null || !user.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		List<FeedbackSubmission> submissions = submissionDAO.getAllSubmissions();

		// Group submissions by status for the Kanban view
		Map<String, List<FeedbackSubmission>> groupedSubmissions = submissions.stream()
				.collect(Collectors.groupingBy(FeedbackSubmission::getStatus));

		request.setAttribute("groupedSubmissions", groupedSubmissions);
		request.setAttribute("feedbackStatusOrder", FEEDBACK_STATUS_ORDER); // Pass the order to the JSP

		request.getRequestDispatcher("/views/admin/admin_feedback.jsp").forward(request, response);
	}
}