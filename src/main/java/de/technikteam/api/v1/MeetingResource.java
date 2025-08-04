package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.MeetingRequest;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/meetings")
@Tag(name = "Admin Meetings", description = "Endpoints for managing specific training meetings.")
@SecurityRequirement(name = "bearerAuth")
public class MeetingResource {

	private final MeetingDAO meetingDAO;
	private final AdminLogService adminLogService;
	private final PolicyFactory richTextPolicy;

	@Autowired
	public MeetingResource(MeetingDAO meetingDAO, AdminLogService adminLogService,
			@Qualifier("richTextPolicy") PolicyFactory richTextPolicy) {
		this.meetingDAO = meetingDAO;
		this.adminLogService = adminLogService;
		this.richTextPolicy = richTextPolicy;
	}

	@GetMapping
	@Operation(summary = "Get all meetings for a course")
	public ResponseEntity<ApiResponse> getMeetingsForCourse(@RequestParam int courseId) {
		List<Meeting> meetings = meetingDAO.getMeetingsForCourse(courseId);
		return ResponseEntity.ok(new ApiResponse(true, "Termine erfolgreich abgerufen.", meetings));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a single meeting by ID")
	public ResponseEntity<ApiResponse> getMeetingById(@PathVariable int id) {
		Meeting meeting = meetingDAO.getMeetingById(id);
		if (meeting != null) {
			return ResponseEntity.ok(new ApiResponse(true, "Termin erfolgreich abgerufen.", meeting));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Termin nicht gefunden.", null));
	}

	@PostMapping
	@Operation(summary = "Create a new meeting")
	public ResponseEntity<ApiResponse> createMeeting(@Valid @RequestBody MeetingRequest request,
			@AuthenticationPrincipal User adminUser) {
		Meeting meeting = new Meeting();
		meeting.setCourseId(request.courseId());
		meeting.setName(request.name());
		meeting.setMeetingDateTime(request.meetingDateTime());
		meeting.setEndDateTime(request.endDateTime());
		meeting.setLeaderUserId(request.leaderUserId() != null ? request.leaderUserId() : 0);
		meeting.setDescription(richTextPolicy.sanitize(request.description()));
		meeting.setLocation(request.location());

		int newId = meetingDAO.createMeeting(meeting);
		if (newId > 0) {
			meeting.setId(newId);
			adminLogService.log(adminUser.getUsername(), "CREATE_MEETING_API",
					"Meeting '" + meeting.getName() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Termin erfolgreich erstellt.", meeting),
					HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Termin konnte nicht erstellt werden.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a meeting")
	public ResponseEntity<ApiResponse> updateMeeting(@PathVariable int id, @Valid @RequestBody MeetingRequest request,
			@AuthenticationPrincipal User adminUser) {
		Meeting meeting = new Meeting();
		meeting.setId(id);
		meeting.setCourseId(request.courseId());
		meeting.setName(request.name());
		meeting.setMeetingDateTime(request.meetingDateTime());
		meeting.setEndDateTime(request.endDateTime());
		meeting.setLeaderUserId(request.leaderUserId() != null ? request.leaderUserId() : 0);
		meeting.setDescription(richTextPolicy.sanitize(request.description()));
		meeting.setLocation(request.location());

		if (meetingDAO.updateMeeting(meeting)) {
			adminLogService.log(adminUser.getUsername(), "UPDATE_MEETING_API",
					"Meeting '" + meeting.getName() + "' updated.");
			return ResponseEntity.ok(new ApiResponse(true, "Termin erfolgreich aktualisiert.", meeting));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Termin nicht gefunden oder Aktualisierung fehlgeschlagen.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a meeting")
	public ResponseEntity<ApiResponse> deleteMeeting(@PathVariable int id, @AuthenticationPrincipal User adminUser) {
		Meeting meeting = meetingDAO.getMeetingById(id);
		if (meeting != null && meetingDAO.deleteMeeting(id)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_MEETING_API",
					"Meeting '" + meeting.getName() + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Termin erfolgreich gelöscht.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Termin nicht gefunden oder Löschung fehlgeschlagen.", null));
	}
}