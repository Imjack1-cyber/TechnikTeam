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
import de.technikteam.service.TrainingHubService;
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
	private final TrainingHubService trainingHubService;

	@Autowired
	public PublicMeetingResource(MeetingDAO meetingDAO, MeetingAttendanceDAO attendanceDAO, AttachmentDAO attachmentDAO,
			MeetingSignupService signupService, TrainingHubService trainingHubService) {
		this.meetingDAO = meetingDAO;
		this.attendanceDAO = attendanceDAO;
		this.attachmentDAO = attachmentDAO;
		this.signupService = signupService;
		this.trainingHubService = trainingHubService;
	}

	@GetMapping
	@Operation(summary = "Get upcoming meetings grouped by course for a user", description = "Retrieves a list of courses, each containing its upcoming meetings and the user's status for both the course and each meeting.")
	public ResponseEntity<ApiResponse> getUpcomingMeetings(@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		return ResponseEntity.ok(new ApiResponse(true, "Kurse und Termine erfolgreich abgerufen.",
				trainingHubService.getCoursesWithMeetingsForUser(user)));
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

		try {
			if ("signup".equalsIgnoreCase(action)) {
				MeetingSignupService.SignupResult result = signupService.signupOrWaitlist(user.getId(), id, user.getId());
				Map<String, Object> data = new HashMap<>();
				data.put("status", result.status.name());

				if (result.status == MeetingSignupService.SignupStatus.ENROLLED
						|| result.status == MeetingSignupService.SignupStatus.WAITLISTED) {
					return ResponseEntity.ok(new ApiResponse(true, result.message, data));
				} else {
					// Covers ALREADY_ENROLLED and ERROR cases with a user-friendly message
					return ResponseEntity.badRequest().body(new ApiResponse(false, result.message, data));
				}
			} else if ("signoff".equalsIgnoreCase(action)) {
				if (signupService.signoffFromMeeting(user.getId(), id)) {
					return ResponseEntity.ok(new ApiResponse(true, "Abmeldung erfolgreich.", null));
				} else {
					// This case is unlikely but handled for completeness
					return ResponseEntity.badRequest()
							.body(new ApiResponse(false, "Abmeldung war nicht möglich.", null));
				}
			} else {
				return ResponseEntity.badRequest().body(new ApiResponse(false, "Unbekannte Aktion.", null));
			}
        } catch (Exception e) {
            // Catch any unexpected exceptions from the service layer
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Ein interner Serverfehler ist aufgetreten: " + e.getMessage(), null));
        }
	}
}