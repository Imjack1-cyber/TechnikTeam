package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.MeetingSignupService;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Meeting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/meetings")
@Tag(name = "Admin Meetings", description = "Admin endpoints for managing meetings, waitlists and repeats.")
@SecurityRequirement(name = "bearerAuth")
public class AdminMeetingManagementResource {

	private final MeetingSignupService signupService;
	private final MeetingDAO meetingDAO;

	@Autowired
	public AdminMeetingManagementResource(MeetingSignupService signupService, MeetingDAO meetingDAO) {
		this.signupService = signupService;
		this.meetingDAO = meetingDAO;
	}

	@Operation(summary = "Get enrolled users for a meeting (admin)")
	@GetMapping("/{meetingId}/participants")
	public ResponseEntity<ApiResponse> getParticipants(@PathVariable int meetingId) {
		List<User> participants = meetingDAO.getEnrolledUsersForMeeting(meetingId);
		return ResponseEntity.ok(new ApiResponse(true, "Teilnehmerliste abgerufen.", participants));
	}

	@Operation(summary = "Get waitlist for a meeting (admin)")
	@GetMapping("/{meetingId}/waitlist")
	public ResponseEntity<ApiResponse> getWaitlist(@PathVariable int meetingId) {
		List<User> waitlisted = signupService.getWaitlist(meetingId);
		Map<String, Object> data = new HashMap<>();
		data.put("waitlist", waitlisted);
		return ResponseEntity.ok(new ApiResponse(true, "Waitlist abgerufen.", data));
	}

	@Operation(summary = "Promote a user from a meeting's waitlist to enrolled status (admin)")
	@PostMapping("/{meetingId}/promote")
	public ResponseEntity<ApiResponse> promoteUser(@PathVariable int meetingId,
			@RequestBody Map<String, Integer> payload, @AuthenticationPrincipal SecurityUser securityUser) {
		Integer userId = payload.get("userId");
		if (userId == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "userId ist erforderlich.", null));
		}
		if (securityUser == null || securityUser.getUser() == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Nicht autorisiert.", null));
		}
		int adminId = securityUser.getUser().getId();
		boolean ok = signupService.promoteUserFromWaitlist(meetingId, userId, adminId);
		if (ok) {
			return ResponseEntity.ok(new ApiResponse(true, "Nutzer erfolgreich befördert.", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Beförderung fehlgeschlagen.", null));
		}
	}

	@Operation(summary = "Create a repeat meeting for an existing meeting (admin). This links the new meeting via parent_meeting_id.")
	@PostMapping("/{meetingId}/repeat")
	public ResponseEntity<ApiResponse> createRepeatMeeting(@PathVariable int meetingId,
			@RequestBody Map<String, String> payload, @AuthenticationPrincipal SecurityUser securityUser) {
		Meeting original = meetingDAO.getMeetingById(meetingId);
		if (original == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Original-Meeting nicht gefunden.", null));
		}

		try {
			Meeting newMeeting = new Meeting();
			newMeeting.setCourseId(original.getCourseId());
			newMeeting.setParentMeetingId(original.getId());
			newMeeting.setName(payload.getOrDefault("name", original.getName()));
			if (payload.containsKey("meetingDateTime")) {
				newMeeting.setMeetingDateTime(LocalDateTime.parse(payload.get("meetingDateTime")));
			} else {
				return ResponseEntity.badRequest().body(new ApiResponse(false, "meetingDateTime fehlt.", null));
			}
			if (payload.get("endDateTime") != null && !payload.get("endDateTime").isBlank()) {
				newMeeting.setEndDateTime(LocalDateTime.parse(payload.get("endDateTime")));
			}
			if (payload.containsKey("leaderUserId")) {
				newMeeting.setLeaderUserId(Integer.parseInt(payload.get("leaderUserId")));
			} else {
				newMeeting.setLeaderUserId(original.getLeaderUserId());
			}
			newMeeting.setDescription(payload.getOrDefault("description", original.getDescription()));
			newMeeting.setLocation(payload.getOrDefault("location", original.getLocation()));
            newMeeting.setMaxParticipants(original.getMaxParticipants());
            newMeeting.setSignupDeadline(original.getSignupDeadline());

			int newId = meetingDAO.createMeeting(newMeeting);
			if (newId > 0) {
				Map<String, Object> data = new HashMap<>();
				data.put("id", newId);
				return ResponseEntity.ok(new ApiResponse(true, "Repeat-Meeting erstellt.", data));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new ApiResponse(false, "Konnte Meeting nicht erstellen.", null));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Fehler: " + e.getMessage(), null));
		}
	}
}