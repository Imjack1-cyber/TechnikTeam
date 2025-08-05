package de.technikteam.api.v1;

import de.technikteam.model.Announcement;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/announcements")
@Tag(name = "Admin Announcements", description = "Endpoints for managing bulletin board announcements.")
public class AdminAnnouncementResource {

	private final AnnouncementService announcementService;

	@Autowired
	public AdminAnnouncementResource(AnnouncementService announcementService) {
		this.announcementService = announcementService;
	}

	@GetMapping
	@Operation(summary = "Get all announcements")
	public ResponseEntity<ApiResponse> getAllAnnouncements() {
		List<Announcement> announcements = announcementService.findAll();
		return ResponseEntity.ok(new ApiResponse(true, "Announcements retrieved successfully.", announcements));
	}

	@PostMapping
	@Operation(summary = "Create a new announcement")
	public ResponseEntity<ApiResponse> createAnnouncement(@Valid @RequestBody Announcement announcement,
			@AuthenticationPrincipal SecurityUser securityUser) {
		Announcement createdAnnouncement = announcementService.create(announcement, securityUser.getUser());
		return new ResponseEntity<>(new ApiResponse(true, "Announcement created successfully.", createdAnnouncement),
				HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update an announcement")
	public ResponseEntity<ApiResponse> updateAnnouncement(@PathVariable int id,
			@Valid @RequestBody Announcement announcement, @AuthenticationPrincipal SecurityUser securityUser) {
		announcement.setId(id);
		Announcement updatedAnnouncement = announcementService.update(announcement, securityUser.getUser());
		if (updatedAnnouncement != null) {
			return ResponseEntity.ok(new ApiResponse(true, "Announcement updated successfully.", updatedAnnouncement));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Announcement not found.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete an announcement")
	public ResponseEntity<ApiResponse> deleteAnnouncement(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (announcementService.delete(id, securityUser.getUser())) {
			return ResponseEntity.ok(new ApiResponse(true, "Announcement deleted successfully.", null));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Announcement not found.", null));
	}
}