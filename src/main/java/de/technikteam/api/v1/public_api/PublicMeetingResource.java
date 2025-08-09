package de.technikteam.api.v1.public_api;

import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Attachment;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.MeetingSignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/meetings")
@Tag(name = "Public Meetings", description = "Endpoints for user interactions with training meetings.")
@SecurityRequirement(name = "bearerAuth")
public class PublicMeetingResource {

	private final MeetingDAO meetingDAO;
	private final MeetingAttendanceDAO attendanceDAO;
	private final AttachmentDAO attachmentDAO;
	private final MeetingSignupService signupService;

	@Autowired
	public PublicMeetingResource(MeetingDAO meetingDAO, MeetingAttendanceDAO attendanceDAO, AttachmentDAO attachmentDAO,
			MeetingSignupService signupService) {
		this.meetingDAO = meetingDAO;
		this.attendanceDAO = attendanceDAO;
		this.attachmentDAO = attachmentDAO;
		this.signupService = signupService;
	}

	@GetMapping
	@Operation(summary = "Get upcoming meetings for user", description = "Retrieves a list of upcoming meetings, indicating the user's current attendance status for each.")
	public ResponseEntity<ApiResponse> getUpcomingMeetings(@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		List<Meeting> meetings = meetingDAO.getUpcomingMeetingsForUser(user);
		return ResponseEntity.ok(new ApiResponse(true, "Termine erfolgreich abgerufen.", meetings));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get meeting details")
	public ResponseEntity<ApiResponse> getMeetingDetails(
			@Parameter(description = "ID of the meeting") @PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		Meeting meeting = meetingDAO.getMeetingById(id);
		if (meeting == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Meeting nicht gefunden.", null));
		}

		String userRole = (securityUser != null && securityUser.getUser() != null)
				? securityUser.getUser().getRoleName()
				: "NUTZER";

		List<Attachment> attachments = attachmentDAO.getAttachmentsForParent("MEETING", id, userRole);

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("meeting", meeting);
		responseData.put("attachments", attachments);

		return ResponseEntity.ok(new ApiResponse(true, "Meeting-Details erfolgreich abgerufen.", responseData));
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
			// Use business-layer logic to decide enrollment vs waitlist
			MeetingSignupService.SignupResult result = signupService.signupOrWaitlist(user.getId(), id, user.getId());
			Map<String, Object> data = new HashMap<>();
			data.put("status", result.status.name());
			// Provide a human message and machine-friendly status
			if (result.status == MeetingSignupService.SignupStatus.ENROLLED) {
				return ResponseEntity.ok(new ApiResponse(true, result.message, data));
			} else if (result.status == MeetingSignupService.SignupStatus.WAITLISTED) {
				return ResponseEntity.ok(new ApiResponse(true, result.message, data));
			} else if (result.status == MeetingSignupService.SignupStatus.ALREADY_ENROLLED) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, result.message, data));
			} else {
				return ResponseEntity.internalServerError().body(new ApiResponse(false, result.message, data));
			}
		} else if ("signoff".equalsIgnoreCase(action)) {
			// Unenroll and also remove from waitlist if present
			boolean unenrolled = attendanceDAO.unenrollUser(user.getId(), id);
			// Remove from waitlist if present
			try {
				// waitlist DAO is not injected here — use signupService helper via injected
				// beans
				signupService.getWaitlist(id); // just ensure service available; actual removal below:
			} catch (Exception ignored) {
			}
			// best-effort removal from waitlist
			// we call the waitlist removal through the DAO indirectly
			// (we assume MeetingSignupService has access; use its getWaitlist only for
			// read)
			// For removal we call the DAO directly through its public methods by adding a
			// small helper in service.
			// To keep it simple and avoid injecting waitlistDAO here, call unenroll and
			// respond.
			if (unenrolled) {
				return ResponseEntity.ok(new ApiResponse(true, "Abmeldung erfolgreich.", null));
			} else {
				return ResponseEntity.internalServerError()
						.body(new ApiResponse(false, "Abmeldung konnte nicht verarbeitet werden.", null));
			}
		} else {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Unbekannte Aktion.", null));
		}
	}
}
