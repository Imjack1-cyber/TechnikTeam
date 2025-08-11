package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.EventDebriefingDTO;
import de.technikteam.config.Permissions;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventDebriefingDAO;
import de.technikteam.dao.EventFeedbackDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.EventDebriefing;
import de.technikteam.model.FeedbackResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.EventDebriefingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/events")
@Tag(name = "Admin Event Debriefings", description = "Endpoints for managing post-event reports.")
@SecurityRequirement(name = "bearerAuth")
public class AdminEventDebriefingResource {

	private final EventDebriefingDAO debriefingDAO;
	private final EventDebriefingService debriefingService;
	private final EventDAO eventDAO;
	private final EventFeedbackDAO feedbackDAO;

	@Autowired
	public AdminEventDebriefingResource(EventDebriefingDAO debriefingDAO, EventDebriefingService debriefingService,
			EventDAO eventDAO, EventFeedbackDAO feedbackDAO) {
		this.debriefingDAO = debriefingDAO;
		this.debriefingService = debriefingService;
		this.eventDAO = eventDAO;
		this.feedbackDAO = feedbackDAO;
	}

	@GetMapping("/debriefings")
	@Operation(summary = "Get all debriefing reports")
	public ResponseEntity<ApiResponse> getAllDebriefings() {
		List<EventDebriefing> debriefings = debriefingDAO.findAll().stream().map(debriefingService::enrichDebriefing)
				.collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse(true, "Debriefings abgerufen.", debriefings));
	}

	@GetMapping("/{eventId}/debriefing")
	@Operation(summary = "Get a debriefing for a specific event")
	public ResponseEntity<ApiResponse> getDebriefingForEvent(@PathVariable int eventId) {
		Optional<EventDebriefing> debriefingOpt = debriefingDAO.findByEventId(eventId);
		if (debriefingOpt.isPresent()) {
			return ResponseEntity.ok(new ApiResponse(true, "Debriefing abgerufen.",
					debriefingService.enrichDebriefing(debriefingOpt.get())));
		} else {
			return ResponseEntity.ok(new ApiResponse(true, "Kein Debriefing für dieses Event vorhanden.", null));
		}
	}

	@PostMapping("/{eventId}/debriefing")
	@Operation(summary = "Create or update a debriefing for an event")
	public ResponseEntity<ApiResponse> saveDebriefing(@PathVariable int eventId,
			@Valid @RequestBody EventDebriefingDTO dto, @AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Event nicht gefunden.", null));
		}

		try {
			EventDebriefing savedDebriefing = debriefingService.saveDebriefing(eventId, dto, user);
			return new ResponseEntity<>(new ApiResponse(true, "Debriefing erfolgreich gespeichert.", savedDebriefing),
					HttpStatus.CREATED);
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		}
	}

	@GetMapping("/{eventId}/feedback-summary")
	@Operation(summary = "Get aggregated user feedback for an event")
	public ResponseEntity<ApiResponse> getFeedbackSummary(@PathVariable int eventId) {
		List<FeedbackResponse> responses = feedbackDAO.getResponsesForEvent(eventId);
		return ResponseEntity.ok(new ApiResponse(true, "User feedback summary retrieved.", responses));
	}
}