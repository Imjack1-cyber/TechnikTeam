package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.MeetingRequest;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import de.technikteam.security.CurrentUser;
import de.technikteam.service.AdminLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/meetings")
@Tag(name = "Admin Meetings", description = "Endpoints for managing specific training meetings.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('COURSE_READ')")
public class MeetingResource {

	private final MeetingDAO meetingDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public MeetingResource(MeetingDAO meetingDAO, AdminLogService adminLogService) {
		this.meetingDAO = meetingDAO;
		this.adminLogService = adminLogService;
	}

	@GetMapping
	@Operation(summary = "Get all meetings for a course")
	public ResponseEntity<ApiResponse> getMeetingsForCourse(@RequestParam int courseId) {
		List<Meeting> meetings = meetingDAO.getMeetingsForCourse(courseId);
		return ResponseEntity.ok(new ApiResponse(true, "Meetings retrieved successfully", meetings));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a single meeting by ID")
	public ResponseEntity<ApiResponse> getMeetingById(@PathVariable int id) {
		Meeting meeting = meetingDAO.getMeetingById(id);
		if (meeting != null) {
			return ResponseEntity.ok(new ApiResponse(true, "Meeting retrieved successfully", meeting));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Meeting not found", null));
	}

	@PostMapping
	@Operation(summary = "Create a new meeting")
	@PreAuthorize("hasAuthority('COURSE_CREATE')")
	public ResponseEntity<ApiResponse> createMeeting(@Valid @RequestBody MeetingRequest request,
			@CurrentUser User adminUser) {
		Meeting meeting = new Meeting();
		meeting.setCourseId(request.courseId());
		meeting.setName(request.name());
		meeting.setMeetingDateTime(request.meetingDateTime());
		meeting.setEndDateTime(request.endDateTime());
		meeting.setLeaderUserId(request.leaderUserId() != null ? request.leaderUserId() : 0);
		meeting.setDescription(request.description());
		meeting.setLocation(request.location());

		int newId = meetingDAO.createMeeting(meeting);
		if (newId > 0) {
			meeting.setId(newId);
			adminLogService.log(adminUser.getUsername(), "CREATE_MEETING_API",
					"Meeting '" + meeting.getName() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Meeting created successfully", meeting),
					HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Could not create meeting.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a meeting")
	@PreAuthorize("hasAuthority('COURSE_UPDATE')")
	public ResponseEntity<ApiResponse> updateMeeting(@PathVariable int id, @Valid @RequestBody MeetingRequest request,
			@CurrentUser User adminUser) {
		Meeting meeting = new Meeting();
		meeting.setId(id);
		meeting.setCourseId(request.courseId());
		meeting.setName(request.name());
		meeting.setMeetingDateTime(request.meetingDateTime());
		meeting.setEndDateTime(request.endDateTime());
		meeting.setLeaderUserId(request.leaderUserId() != null ? request.leaderUserId() : 0);
		meeting.setDescription(request.description());
		meeting.setLocation(request.location());

		if (meetingDAO.updateMeeting(meeting)) {
			adminLogService.log(adminUser.getUsername(), "UPDATE_MEETING_API",
					"Meeting '" + meeting.getName() + "' updated.");
			return ResponseEntity.ok(new ApiResponse(true, "Meeting updated successfully", meeting));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Meeting not found or update failed.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a meeting")
	@PreAuthorize("hasAuthority('COURSE_DELETE')")
	public ResponseEntity<ApiResponse> deleteMeeting(@PathVariable int id, @CurrentUser User adminUser) {
		Meeting meeting = meetingDAO.getMeetingById(id);
		if (meeting != null && meetingDAO.deleteMeeting(id)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_MEETING_API",
					"Meeting '" + meeting.getName() + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Meeting deleted successfully", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Meeting not found or delete failed.", null));
	}
}