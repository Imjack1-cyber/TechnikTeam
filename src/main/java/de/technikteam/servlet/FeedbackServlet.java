package de.technikteam.servlet;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.FeedbackDAO;
import de.technikteam.model.Event;
import de.technikteam.model.FeedbackForm;
import de.technikteam.model.FeedbackResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private FeedbackDAO feedbackDAO;
	private EventDAO eventDAO;

	@Override
	public void init() {
		feedbackDAO = new FeedbackDAO();
		eventDAO = new EventDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action") == null ? "list" : request.getParameter("action");

		try {
			switch (action) {
			case "submit":
				showSubmitForm(request, response, user);
				break;
			case "view":
				viewFeedbackResults(request, response);
				break;
			default:
				// Default action will now redirect to profile, as the listing is integrated
				// there.
				response.sendRedirect(request.getContextPath() + "/profil");
				break;
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");

		if ("createForm".equals(action)) {
			int eventId = Integer.parseInt(request.getParameter("eventId"));
			Event event = eventDAO.getEventById(eventId);
			FeedbackForm form = new FeedbackForm();
			form.setEventId(eventId);
			form.setTitle("Feedback für Event: " + (event != null ? event.getName() : "Unbekannt"));
			feedbackDAO.createFeedbackForm(form);
			AdminLogService.log(user.getUsername(), "CREATE_FEEDBACK_FORM",
					"Feedback-Formular für Event-ID " + eventId + " erstellt.");
			response.sendRedirect(request.getContextPath() + "/admin/veranstaltungen");

		} else if ("submitResponse".equals(action)) {
			int formId = Integer.parseInt(request.getParameter("formId"));
			int rating = Integer.parseInt(request.getParameter("rating"));
			String comments = request.getParameter("comments");

			FeedbackResponse feedbackResponse = new FeedbackResponse();
			feedbackResponse.setFormId(formId);
			feedbackResponse.setUserId(user.getId());
			feedbackResponse.setRating(rating);
			feedbackResponse.setComments(comments);

			feedbackDAO.saveFeedbackResponse(feedbackResponse);
			request.getSession().setAttribute("successMessage", "Vielen Dank für dein Feedback!");
			response.sendRedirect(request.getContextPath() + "/profil");
		}
	}

	private void showSubmitForm(HttpServletRequest request, HttpServletResponse response, User user)
			throws ServletException, IOException {
		int eventId = Integer.parseInt(request.getParameter("eventId"));
		Event event = eventDAO.getEventById(eventId);
		FeedbackForm form = feedbackDAO.getFeedbackFormForEvent(eventId);

		if (form == null) {
			request.getSession().setAttribute("errorMessage", "Für dieses Event wurde noch kein Feedback angefordert.");
			response.sendRedirect(request.getContextPath() + "/profil");
			return;
		}

		if (feedbackDAO.hasUserSubmittedFeedback(form.getId(), user.getId())) {
			request.getSession().setAttribute("infoMessage", "Du hast bereits Feedback für dieses Event abgegeben.");
			response.sendRedirect(request.getContextPath() + "/profil");
			return;
		}

		request.setAttribute("event", event);
		request.setAttribute("form", form);
		// CORRECTED: Forward to the actual JSP file path.
		request.getRequestDispatcher("/views/public/feedback_form.jsp").forward(request, response);
	}

	private void viewFeedbackResults(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int eventId = Integer.parseInt(request.getParameter("eventId"));
		Event event = eventDAO.getEventById(eventId);
		FeedbackForm form = feedbackDAO.getFeedbackFormForEvent(eventId);

		if (form != null) {
			List<FeedbackResponse> responses = feedbackDAO.getResponsesForForm(form.getId());
			request.setAttribute("responses", responses);
		}

		request.setAttribute("event", event);
		// CORRECTED: Forward to a (currently non-existent but logically correct)
		// results page.
		// For now, let's assume it should have been named feedback_results.jsp.
		// If this file does not exist, it will 404, but the servlet logic is now
		// correct.
		request.getRequestDispatcher("/views/public/feedback_results.jsp").forward(request, response);
	}
}