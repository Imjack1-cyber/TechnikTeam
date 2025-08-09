package de.technikteam.api.v1.public_api;

import de.technikteam.dao.ChangelogDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Changelog;
import de.technikteam.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public/changelog")
@Tag(name = "Public Changelog", description = "Endpoints for user-facing changelog features.")
public class PublicChangelogResource {

	private final ChangelogDAO changelogDAO;

	@Autowired
	public PublicChangelogResource(ChangelogDAO changelogDAO) {
		this.changelogDAO = changelogDAO;
	}

	@GetMapping
	@Operation(summary = "Get all changelogs")
	public ResponseEntity<ApiResponse> getAllChangelogs() {
		List<Changelog> changelogs = changelogDAO.findAll();
		return ResponseEntity.ok(new ApiResponse(true, "Changelogs retrieved.", changelogs));
	}

	@GetMapping("/latest-unseen")
	@Operation(summary = "Get the latest unseen changelog for the current user")
	public ResponseEntity<ApiResponse> getLatestUnseenChangelog(@AuthenticationPrincipal SecurityUser securityUser) {
		if (securityUser == null) {
			return ResponseEntity.ok(new ApiResponse(true, "User not authenticated.", null));
		}
		Optional<Changelog> changelog = changelogDAO.findLatestUnseen(securityUser.getUser().getId());
		return ResponseEntity.ok(new ApiResponse(true, "Latest unseen changelog retrieved.", changelog.orElse(null)));
	}

	@PostMapping("/{id}/mark-seen")
	@Operation(summary = "Mark a changelog as seen by the current user")
	public ResponseEntity<ApiResponse> markAsSeen(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (securityUser == null) {
			return ResponseEntity.status(401).body(new ApiResponse(false, "User not authenticated.", null));
		}
		if (changelogDAO.markAsSeen(id, securityUser.getUser().getId())) {
			return ResponseEntity.ok(new ApiResponse(true, "Changelog marked as seen.", null));
		}
		return ResponseEntity.internalServerError().body(new ApiResponse(false, "Could not mark as seen.", null));
	}
}