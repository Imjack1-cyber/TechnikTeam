package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.Changelog;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.ChangelogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/changelogs")
@Tag(name = "Admin Changelogs", description = "Endpoints for managing application changelogs.")
public class AdminChangelogResource {

	private final ChangelogService changelogService;

	@Autowired
	public AdminChangelogResource(ChangelogService changelogService) {
		this.changelogService = changelogService;
	}

	@GetMapping
	@Operation(summary = "Get all changelogs")
	public ResponseEntity<ApiResponse> getAllChangelogs() {
		List<Changelog> changelogs = changelogService.findAll();
		return ResponseEntity.ok(new ApiResponse(true, "Changelogs retrieved successfully.", changelogs));
	}

	@PostMapping
	@Operation(summary = "Create a new changelog entry")
	public ResponseEntity<ApiResponse> createChangelog(@RequestBody Changelog changelog,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (changelogService.create(changelog, securityUser.getUser())) {
			return new ResponseEntity<>(new ApiResponse(true, "Changelog created successfully.", changelog),
					HttpStatus.CREATED);
		}
		return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to create changelog.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a changelog entry")
	public ResponseEntity<ApiResponse> updateChangelog(@PathVariable int id, @RequestBody Changelog changelog,
			@AuthenticationPrincipal SecurityUser securityUser) {
		changelog.setId(id);
		if (changelogService.update(changelog, securityUser.getUser())) {
			return ResponseEntity.ok(new ApiResponse(true, "Changelog updated successfully.", changelog));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Changelog not found.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a changelog entry")
	public ResponseEntity<ApiResponse> deleteChangelog(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (changelogService.delete(id, securityUser.getUser())) {
			return ResponseEntity.ok(new ApiResponse(true, "Changelog deleted successfully.", null));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Changelog not found.", null));
	}
}