package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.EventTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events/{eventId}/tasks")
@Tag(name = "Event Tasks", description = "Endpoints for managing tasks within an event.")
public class EventTaskResource {

	private static final Logger logger = LogManager.getLogger(EventTaskResource.class);
	private final EventTaskService eventTaskService;

	@Autowired
	public EventTaskResource(EventTaskService eventTaskService) {
		this.eventTaskService = eventTaskService;
	}

	@PostMapping
	@Operation(summary = "Create or update an event task")
	public ResponseEntity<ApiResponse> saveTask(@PathVariable int eventId, @RequestBody EventTask task,
			@AuthenticationPrincipal SecurityUser securityUser) {
		logger.debug("Received request to save task for event {}: {}", eventId, task.getDescription());
		try {
			task.setEventId(eventId);
			// Assuming userIds, itemIds etc. are part of the task DTO or handled
			// differently
			int taskId = eventTaskService.saveTaskAndHandleMentions(task,
					task.getAssignedUsers().stream().mapToInt(User::getId).toArray(), null, null, null, // Simplified
																										// for now,
																										// assuming
																										// these are not
																										// sent in this
																										// payload
					task.getDependsOn().stream().mapToInt(EventTask::getId).toArray(), securityUser.getUser());
			logger.info("Task {} for event {} saved successfully with ID: {}", task.getDescription(), eventId, taskId);
			return new ResponseEntity<>(new ApiResponse(true, "Task saved successfully.", Map.of("taskId", taskId)),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed to save task for event {}", eventId, e);
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Failed to save task: " + e.getMessage(), null));
		}
	}

	@PostMapping("/{taskId}/action")
	@Operation(summary = "Perform a user action on a task (status change, claim, unclaim)")
	public ResponseEntity<ApiResponse> performTaskAction(@PathVariable int eventId, @PathVariable int taskId,
			@RequestBody Map<String, String> payload, @AuthenticationPrincipal SecurityUser securityUser) {
		String action = payload.get("action");
		String status = payload.get("status");
		logger.debug("Received action '{}' for task {} in event {}", action, taskId, eventId);

		try {
			eventTaskService.performUserTaskAction(eventId, taskId, action, status, securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "Aktion erfolgreich ausgeführt.", null));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage(), null));
		} catch (Exception e) {
			logger.error("Error performing task action", e);
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Ein interner Fehler ist aufgetreten.", null));
		}
	}
}