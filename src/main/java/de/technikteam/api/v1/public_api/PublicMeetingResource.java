package de.technikteam.api.v1.public_api;

import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/meetings")
@Tag(name = "Public Meetings", description = "Endpoints for user interactions with training meetings.")
@SecurityRequirement(name = "bearerAuth")
public class PublicMeetingResource {

	private final MeetingDAO meetingDAO;
	private final MeetingAttendanceDAO attendanceDAO;

	@Autowired
	public PublicMeetingResource(MeetingDAO meetingDAO, MeetingAttendanceDAO attendanceDAO) {
		this.meetingDAO = meetingDAO;
		this.attendanceDAO = attendanceDAO;
	}

	@GetMapping
	@Operation(summary = "Get upcoming meetings for user", description = "Retrieves a list of upcoming meetings, indicating the user's current attendance status for each.")
	public ResponseEntity<ApiResponse> getUpcomingMeetings(@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		List<Meeting> meetings = meetingDAO.getUpcomingMeetingsForUser(user);
		return ResponseEntity.ok(new ApiResponse(true, "Termine erfolgreich abgerufen.", meetings));
	}

	@PostMapping("/{id}/{action}")
	@Operation(summary = "Sign up or off from a meeting", description = "Allows the current user to sign up for or sign off from a specific meeting.")
	public ResponseEntity<ApiResponse> handleMeetingAction(
			@Parameter(description = "ID of the meeting") @PathVariable int id,
			@Parameter(description = "Action to perform. Must be 'signup' or 'signoff'.") @PathVariable String action,
			@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();

		boolean success;
		if ("signup".equalsIgnoreCase(action)) {
			success = attendanceDAO.setAttendance(user.getId(), id, true, "");
		} else if ("signoff".equalsIgnoreCase(action)) {
			success = attendanceDAO.setAttendance(user.getId(), id, false, "");
		} else {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Unbekannte Aktion.", null));
		}

		if (success) {
			return ResponseEntity.ok(new ApiResponse(true, "Aktion erfolgreich ausgeführt.", null));
		} else {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Aktion konnte nicht verarbeitet werden.", null));
		}
	}
}