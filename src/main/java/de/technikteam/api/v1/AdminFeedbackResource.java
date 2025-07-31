package de.technikteam.api.v1;

import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.FeedbackSubmission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedback")
@Tag(name = "Admin Feedback", description = "Endpoints for managing user feedback.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN_DASHBOARD_ACCESS')") // General permission for feedback management
public class AdminFeedbackResource {

	private final FeedbackSubmissionDAO submissionDAO;

	@Autowired
	public AdminFeedbackResource(FeedbackSubmissionDAO submissionDAO) {
		this.submissionDAO = submissionDAO;
	}

	@GetMapping
	@Operation(summary = "Get all feedback submissions", description = "Retrieves all feedback submissions from all users, ordered by status.")
	public ResponseEntity<ApiResponse> getAllSubmissions() {
		List<FeedbackSubmission> submissions = submissionDAO.getAllSubmissions();
		return ResponseEntity.ok(new ApiResponse(true, "All submissions retrieved.", submissions));
	}

	@PutMapping("/{id}/status")
	@Operation(summary = "Update feedback status", description = "Updates the status of a specific feedback submission.")
	public ResponseEntity<ApiResponse> updateStatus(@PathVariable int id, @RequestBody Map<String, String> payload) {
		String newStatus = payload.get("status");
		FeedbackSubmission submission = submissionDAO.getSubmissionById(id);
		if (submission == null) {
			return ResponseEntity.notFound().build();
		}
		if (submissionDAO.updateStatusAndTitle(id, newStatus, submission.getDisplayTitle())) {
			return ResponseEntity.ok(new ApiResponse(true, "Status updated.", null));
		}
		return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to update status.", null));
	}
}