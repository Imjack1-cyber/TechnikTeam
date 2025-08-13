package de.technikteam.api.v1.public_api;

import de.technikteam.api.v1.dto.GeneralFeedbackRequest;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventFeedbackDAO;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.*;
import de.technikteam.security.SecurityUser;
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
	public ResponseEntity<ApiResponse> getMyFeedbackSubmissions(@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		List<FeedbackSubmission> submissions = submissionDAO.getSubmissionsByUserId(user.getId());
		return ResponseEntity.ok(new ApiResponse(true, "Einreichungen erfolgreich abgerufen.", submissions));
	}

	@PostMapping("/general")
	@Operation(summary = "Submit general feedback", description = "Allows a user to submit a new general feedback entry.")
	public ResponseEntity<ApiResponse> submitGeneralFeedback(@Valid @RequestBody GeneralFeedbackRequest request,
			@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		FeedbackSubmission submission = new FeedbackSubmission();
		submission.setUserId(user.getId());
		submission.setSubject(request.subject());
		submission.setContent(request.content());

		if (submissionDAO.createSubmission(submission)) {
			return new ResponseEntity<>(new ApiResponse(true, "Feedback erfolgreich übermittelt.", submission),
					HttpStatus.CREATED);
		} else {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Feedback konnte nicht gespeichert werden.", null));
		}
	}

	@GetMapping("/forms")
	@Operation(summary = "Get feedback form for an event", description = "Retrieves the feedback form for a specific event and checks if the user has already submitted a response.")
	public ResponseEntity<ApiResponse> getEventFeedbackForm(
			@Parameter(description = "ID of the event") @RequestParam int eventId,
			@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Veranstaltung nicht gefunden.", null));
		}
		FeedbackForm form = eventFeedbackDAO.getFeedbackFormForEvent(eventId);
		if (form == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Kein Feedback-Formular für diese Veranstaltung.", null));
		}
		boolean alreadySubmitted = eventFeedbackDAO.hasUserSubmittedFeedback(form.getId(), user.getId());

		Map<String, Object> responseData = Map.of("event", event, "form", form, "alreadySubmitted", alreadySubmitted);
		return ResponseEntity.ok(new ApiResponse(true, "Formulardaten erfolgreich abgerufen.", responseData));
	}

	@PostMapping("/event")
	@Operation(summary = "Submit event feedback", description = "Submits a user's rating and comments for an event feedback form.")
	public ResponseEntity<ApiResponse> submitEventFeedback(@RequestBody FeedbackResponse response,
			@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		response.setUserId(user.getId());

		FeedbackForm form = eventFeedbackDAO.getFormById(response.getFormId());
		if (form == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Feedback-Formular nicht gefunden.", null));
		}
		if (!eventDAO.isUserAssociatedWithEvent(form.getEventId(), user.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false,
					"Sie können nur Feedback für Veranstaltungen abgeben, an denen Sie teilgenommen haben.", null));
		}

		if (eventFeedbackDAO.saveFeedbackResponse(response)) {
			return ResponseEntity.ok(new ApiResponse(true, "Event-Feedback erfolgreich übermittelt.", null));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Event-Feedback konnte nicht gespeichert werden.", null));
	}
}