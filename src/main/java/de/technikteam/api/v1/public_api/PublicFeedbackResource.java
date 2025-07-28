// src/main/java/de/technikteam/api/v1/public_api/PublicFeedbackResource.java
package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventFeedbackDAO;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class PublicFeedbackResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PublicFeedbackResource.class);

	private final FeedbackSubmissionDAO submissionDAO;
	private final EventFeedbackDAO eventFeedbackDAO;
	private final EventDAO eventDAO;
	private final Gson gson;

	@Inject
	public PublicFeedbackResource(FeedbackSubmissionDAO submissionDAO, EventFeedbackDAO eventFeedbackDAO,
			EventDAO eventDAO, Gson gson) {
		this.submissionDAO = submissionDAO;
		this.eventFeedbackDAO = eventFeedbackDAO;
		this.eventDAO = eventDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if ("/user".equals(pathInfo)) {
			List<FeedbackSubmission> submissions = submissionDAO.getSubmissionsByUserId(user.getId());
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Submissions retrieved.", submissions));
		} else if ("/forms".equals(pathInfo)) {
			handleGetEventFeedbackForm(req, resp, user);
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if ("/general".equals(pathInfo)) {
			handleGeneralFeedback(req, resp, user);
		} else if ("/event".equals(pathInfo)) {
			handleEventFeedback(req, resp, user);
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	private void handleGetEventFeedbackForm(HttpServletRequest req, HttpServletResponse resp, User user)
			throws IOException {
		try {
			int eventId = Integer.parseInt(req.getParameter("eventId"));
			Event event = eventDAO.getEventById(eventId);
			if (event == null) {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Event not found.");
				return;
			}

			FeedbackForm form = eventFeedbackDAO.getFeedbackFormForEvent(eventId);
			if (form == null) {
				form = new FeedbackForm();
				form.setEventId(eventId);
				form.setTitle("Feedback für Event: " + event.getName());
				int formId = eventFeedbackDAO.createFeedbackForm(form);
				form.setId(formId);
			}

			boolean alreadySubmitted = eventFeedbackDAO.hasUserSubmittedFeedback(form.getId(), user.getId());

			Map<String, Object> responseData = new HashMap<>();
			responseData.put("event", event);
			responseData.put("form", form);
			responseData.put("alreadySubmitted", alreadySubmitted);

			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Form data retrieved.", responseData));

		} catch (NumberFormatException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID.");
		}
	}

	private void handleGeneralFeedback(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Map<String, String> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
		}.getType());
		String subject = payload.get("subject");
		String content = payload.get("content");

		if (subject == null || subject.trim().isEmpty() || content == null || content.trim().isEmpty()) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Subject and content cannot be empty.");
			return;
		}

		FeedbackSubmission submission = new FeedbackSubmission();
		submission.setUserId(user.getId());
		submission.setSubject(subject);
		submission.setContent(content);

		if (submissionDAO.createSubmission(submission)) {
			sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
					new ApiResponse(true, "Feedback submitted successfully.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not submit feedback.");
		}
	}

	private void handleEventFeedback(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Map<String, Object> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, Object>>() {
		}.getType());

		int formId = ((Double) payload.get("formId")).intValue();
		int rating = ((Double) payload.get("rating")).intValue();
		String comments = (String) payload.get("comments");

		FeedbackResponse feedbackResponse = new FeedbackResponse();
		feedbackResponse.setFormId(formId);
		feedbackResponse.setUserId(user.getId());
		feedbackResponse.setRating(rating);
		feedbackResponse.setComments(comments);

		if (eventFeedbackDAO.saveFeedbackResponse(feedbackResponse)) {
			sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
					new ApiResponse(true, "Event feedback submitted successfully.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not submit event feedback.");
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