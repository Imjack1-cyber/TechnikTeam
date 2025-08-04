package de.technikteam.api.v1;

import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import de.technikteam.service.ProfileRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "Admin Requests", description = "Endpoints for managing user-submitted requests.")
public class AdminRequestResource {

	private final ProfileChangeRequestDAO requestDAO;
	private final ProfileRequestService requestService;
	private final UserDAO userDAO;

	@Autowired
	public AdminRequestResource(ProfileChangeRequestDAO requestDAO, ProfileRequestService requestService,
			UserDAO userDAO) {
		this.requestDAO = requestDAO;
		this.requestService = requestService;
		this.userDAO = userDAO;
	}

	private User getSystemUser() {
		// Use the default admin user, which is guaranteed to exist by the
		// InitialAdminCreator
		User user = userDAO.getUserByUsername("admin");
		if (user == null) {
			// This is a critical state and should not happen in a properly initialized
			// system.
			// Throw a runtime exception to make the problem visible in the logs.
			throw new IllegalStateException("The default 'admin' user could not be found in the database.");
		}
		return user;
	}

	@GetMapping("/pending")
	@Operation(summary = "Get pending requests", description = "Retrieves a list of all profile change requests that are pending review.")
	public ResponseEntity<ApiResponse> getPendingRequests() {
		List<ProfileChangeRequest> requests = requestDAO.getPendingRequests();
		return ResponseEntity.ok(new ApiResponse(true, "Ausstehende Anträge erfolgreich abgerufen.", requests));
	}

	@PostMapping("/{id}/approve")
	@Operation(summary = "Approve a request", description = "Approves a profile change request and applies the changes to the user's profile.")
	public ResponseEntity<ApiResponse> approveRequest(
			@Parameter(description = "ID of the request to approve") @PathVariable int id) {
		try {
			if (requestService.approveRequest(id, getSystemUser())) {
				return ResponseEntity.ok(new ApiResponse(true, "Antrag genehmigt und Benutzer aktualisiert.", null));
			}
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Antrag konnte nicht genehmigt werden.", null));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Ein Dateisystemfehler ist aufgetreten: " + e.getMessage(), null));
		}
	}

	@PostMapping("/{id}/deny")
	@Operation(summary = "Deny a request", description = "Denies a profile change request.")
	public ResponseEntity<ApiResponse> denyRequest(
			@Parameter(description = "ID of the request to deny") @PathVariable int id) {
		try {
			if (requestService.denyRequest(id, getSystemUser())) {
				return ResponseEntity.ok(new ApiResponse(true, "Antrag abgelehnt.", null));
			}
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Antrag konnte nicht abgelehnt werden.", null));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Ein Dateisystemfehler ist aufgetreten: " + e.getMessage(), null));
		}
	}
}