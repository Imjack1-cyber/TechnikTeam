package de.technikteam.api.v1.public_api;

import de.technikteam.api.v1.dto.GeneralFeedbackRequest;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventFeedbackDAO;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.*;
import de.technikteam.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	public ResponseEntity<ApiResponse> getMyFeedbackSubmissions(@CurrentUser User user) {
		List<FeedbackSubmission> submissions = submissionDAO.getSubmissionsByUserId(user.getId());
		return ResponseEntity.ok(new ApiResponse(true, "Submissions retrieved.", submissions));
	}

	@PostMapping("/general")
	@Operation(summary = "Submit general feedback", description = "Allows a user to submit a new general feedback entry.")
	public ResponseEntity<ApiResponse> submitGeneralFeedback(@Valid @RequestBody GeneralFeedbackRequest request,
			@CurrentUser User user) {
		FeedbackSubmission submission = new FeedbackSubmission();
		submission.setUserId(user.getId());
		submission.setSubject(request.subject());
		submission.setContent(request.content());

		if (submissionDAO.createSubmission(submission)) {
			return new ResponseEntity<>(new ApiResponse(true, "Feedback submitted successfully.", submission),
					HttpStatus.CREATED);
		} else {
			return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to save feedback.", null));
		}
	}

	@GetMapping("/forms")
	@Operation(summary = "Get feedback form for an event", description = "Retrieves the feedback form for a specific event and checks if the user has already submitted a response.")
	public ResponseEntity<ApiResponse> getEventFeedbackForm(
			@Parameter(description = "ID of the event") @RequestParam int eventId, @CurrentUser User user) {
		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Event not found.", null));
		}
		FeedbackForm form = eventFeedbackDAO.getFeedbackFormForEvent(eventId);
		if (form == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "No feedback form for this event.", null));
		}
		boolean alreadySubmitted = eventFeedbackDAO.hasUserSubmittedFeedback(form.getId(), user.getId());

		Map<String, Object> responseData = Map.of("event", event, "form", form, "alreadySubmitted", alreadySubmitted);
		return ResponseEntity.ok(new ApiResponse(true, "Form data retrieved.", responseData));
	}

	@PostMapping("/event")
	@Operation(summary = "Submit event feedback", description = "Submits a user's rating and comments for an event feedback form.")
	public ResponseEntity<ApiResponse> submitEventFeedback(@RequestBody FeedbackResponse response,
			@CurrentUser User user) {
		response.setUserId(user.getId());
		if (eventFeedbackDAO.saveFeedbackResponse(response)) {
			return ResponseEntity.ok(new ApiResponse(true, "Event feedback submitted.", null));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Failed to save event feedback.", null));
	}
}