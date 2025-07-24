package de.technikteam.servlet;

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
import java.util.List;

@Singleton
public class MyFeedbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final FeedbackSubmissionDAO submissionDAO;

	@Inject
	public MyFeedbackServlet(FeedbackSubmissionDAO submissionDAO) {
		this.submissionDAO = submissionDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		List<FeedbackSubmission> mySubmissions = submissionDAO.getSubmissionsByUserId(user.getId());
		request.setAttribute("mySubmissions", mySubmissions);
		request.getRequestDispatcher("/views/public/my_feedback.jsp").forward(request, response);
	}
}