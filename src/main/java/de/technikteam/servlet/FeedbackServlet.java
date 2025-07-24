package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventFeedbackDAO;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.Event;
import de.technikteam.model.FeedbackForm;
import de.technikteam.model.FeedbackResponse;
import de.technikteam.model.FeedbackSubmission;
import de.technikteam.model.User;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class FeedbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final EventFeedbackDAO eventFeedbackDAO;
	private final FeedbackSubmissionDAO submissionDAO;
	private final EventDAO eventDAO;

	@Inject
	public FeedbackServlet(EventFeedbackDAO eventFeedbackDAO, FeedbackSubmissionDAO submissionDAO, EventDAO eventDAO) {
		this.eventFeedbackDAO = eventFeedbackDAO;
		this.submissionDAO = submissionDAO;
		this.eventDAO = eventDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");

		if ("submitEventFeedback".equals(action)) {
			showSubmitEventFeedbackForm(request, response, user);
			return;
		}

		request.getRequestDispatcher("/views/public/feedback.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String action = request.getParameter("action");
		if ("submitGeneralFeedback".equals(action)) {
			handleGeneralFeedback(request, response, user);
		} else if ("submitEventFeedbackResponse".equals(action)) {
			handleEventFeedbackResponse(request, response, user);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
		}
	}

	private void handleGeneralFeedback(HttpServletRequest request, HttpServletResponse response, User user)
			throws ServletException, IOException {
		String subject = request.getParameter("subject");
		String content = request.getParameter("content");

		if (subject == null || subject.trim().isEmpty() || content == null || content.trim().isEmpty()) {
			request.setAttribute("errorMessage", "Betreff und Inhalt dürfen nicht leer sein.");
			request.getRequestDispatcher("/views/public/feedback.jsp").forward(request, response);
			return;
		}

		FeedbackSubmission submission = new FeedbackSubmission();
		submission.setUserId(user.getId());
		submission.setSubject(subject);
		submission.setContent(content);

		if (submissionDAO.createSubmission(submission)) {
			request.getSession().setAttribute("successMessage",
					"Vielen Dank! Dein Feedback wurde erfolgreich übermittelt.");
			response.sendRedirect(request.getContextPath() + "/my-feedback");
		} else {
			request.setAttribute("errorMessage",
					"Dein Feedback konnte nicht übermittelt werden. Bitte versuche es später erneut.");
			request.getRequestDispatcher("/views/public/feedback.jsp").forward(request, response);
		}
	}

	private void handleEventFeedbackResponse(HttpServletRequest request, HttpServletResponse response, User user)
			throws IOException {
		int formId = Integer.parseInt(request.getParameter("formId"));
		int rating = Integer.parseInt(request.getParameter("rating"));
		String comments = request.getParameter("comments");

		FeedbackResponse feedbackResponse = new FeedbackResponse();
		feedbackResponse.setFormId(formId);
		feedbackResponse.setUserId(user.getId());
		feedbackResponse.setRating(rating);
		feedbackResponse.setComments(comments);

		eventFeedbackDAO.saveFeedbackResponse(feedbackResponse);
		request.getSession().setAttribute("successMessage", "Vielen Dank für dein Feedback!");
		response.sendRedirect(request.getContextPath() + "/profil");
	}

	private void showSubmitEventFeedbackForm(HttpServletRequest request, HttpServletResponse response, User user)
			throws ServletException, IOException {
		int eventId = Integer.parseInt(request.getParameter("eventId"));
		Event event = eventDAO.getEventById(eventId);
		FeedbackForm form = eventFeedbackDAO.getFeedbackFormForEvent(eventId);

		if (form == null) {
			form = new FeedbackForm();
			form.setEventId(eventId);
			form.setTitle("Feedback für Event: " + (event != null ? event.getName() : "Unbekannt"));
			int formId = eventFeedbackDAO.createFeedbackForm(form);
			form.setId(formId);
		}

		if (eventFeedbackDAO.hasUserSubmittedFeedback(form.getId(), user.getId())) {
			request.getSession().setAttribute("infoMessage", "Du hast bereits Feedback für dieses Event abgegeben.");
			response.sendRedirect(request.getContextPath() + "/profil");
			return;
		}

		request.setAttribute("event", event);
		request.setAttribute("form", form);
		request.getRequestDispatcher("/views/public/feedback_form.jsp").forward(request, response);
	}
}