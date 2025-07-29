package de.technikteam.api.v1.public_api;

import de.technikteam.api.v1.dto.GeneralFeedbackRequest;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventFeedbackDAO;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/feedback")
@Tag(name = "Public Feedback", description = "Endpoints for users to submit and view feedback.")
@SecurityRequirement(name = "bearerAuth")
public class PublicFeedbackResource {

	private final FeedbackSubmissionDAO submissionDAO;
	private final EventFeedbackDAO eventFeedbackDAO;
	private final EventDAO eventDAO;

	@Autowired
	public PublicFeedbackResource(FeedbackSubmissionDAO submissionDAO, EventFeedbackDAO eventFeedbackDAO,
			EventDAO eventDAO) {
		this.submissionDAO = submissionDAO;
		this.eventFeedbackDAO = eventFeedbackDAO;
		this.eventDAO = eventDAO;
	}

	@GetMapping("/user")
	@Operation(summary = "Get user's feedback submissions", description = "Retrieves a list of all general feedback submissions made by the current user.")
	public ResponseEntity<ApiResponse> getMySubmissions(@AuthenticationPrincipal User user) {
		List<FeedbackSubmission> submissions = submissionDAO.getSubmissionsByUserId(user.getId());
		return ResponseEntity.ok(new ApiResponse(true, "Submissions retrieved.", submissions));
	}

	@GetMapping("/forms")
	@Operation(summary = "Get event feedback form data", description = "Retrieves the feedback form for a specific event, and indicates if the user has already submitted a response.")
	public ResponseEntity<ApiResponse> getEventFeedbackForm(
			@Parameter(description = "ID of the event to get the feedback form for") @RequestParam int eventId,
			@AuthenticationPrincipal User user) {

		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Event not found.", null));
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

		return ResponseEntity.ok(new ApiResponse(true, "Form data retrieved.", responseData));
	}

	@PostMapping("/general")
	@Operation(summary = "Submit general feedback", description = "Submits a new piece of general feedback or a feature request.")
	public ResponseEntity<ApiResponse> submitGeneralFeedback(@Valid @RequestBody GeneralFeedbackRequest request,
			@AuthenticationPrincipal User user) {
		FeedbackSubmission submission = new FeedbackSubmission();
		submission.setUserId(user.getId());
		submission.setSubject(request.subject());
		submission.setContent(request.content());

		if (submissionDAO.createSubmission(submission)) {
			return new ResponseEntity<>(new ApiResponse(true, "Feedback submitted successfully.", null),
					HttpStatus.CREATED);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Could not submit feedback.", null));
		}
	}
}