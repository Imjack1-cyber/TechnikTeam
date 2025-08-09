package de.technikteam.api.v1.public_api;

import de.technikteam.model.Announcement;
import de.technikteam.model.ApiResponse;
import de.technikteam.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/announcements")
@Tag(name = "Public Announcements", description = "Endpoints for viewing bulletin board announcements.")
@SecurityRequirement(name = "bearerAuth")
public class PublicAnnouncementResource {

	private final AnnouncementService announcementService;

	@Autowired
	public PublicAnnouncementResource(AnnouncementService announcementService) {
		this.announcementService = announcementService;
	}

	@GetMapping
	@Operation(summary = "Get all announcements")
	public ResponseEntity<ApiResponse> getAllAnnouncements() {
		List<Announcement> announcements = announcementService.findAll();
		return ResponseEntity.ok(new ApiResponse(true, "Announcements retrieved successfully.", announcements));
	}
}