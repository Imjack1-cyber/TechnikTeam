package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.model.Venue;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.FileService;
import de.technikteam.service.NotificationService;
import de.technikteam.dao.VenueDAO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.Gson;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/venues")
@Tag(name = "Admin Venues", description = "Endpoints for managing event venues and maps.")
public class AdminVenueResource {

	private final VenueDAO venueDAO;
	private final FileService fileService;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final Gson gson = new Gson();

	@Autowired
	public AdminVenueResource(VenueDAO venueDAO, FileService fileService, AdminLogService adminLogService, NotificationService notificationService) {
		this.venueDAO = venueDAO;
		this.fileService = fileService;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
	}

	@GetMapping
	@Operation(summary = "Get all venues")
	public ResponseEntity<ApiResponse> getAllVenues() {
		List<Venue> venues = venueDAO.findAll();
		return ResponseEntity.ok(new ApiResponse(true, "Venues retrieved successfully.", venues));
	}

	@PostMapping(consumes = {"multipart/form-data"})
	@Operation(summary = "Create a new venue")
	public ResponseEntity<ApiResponse> createVenue(@RequestPart("venue") String venueJson,
			@RequestPart(value = "mapImage", required = false) MultipartFile mapImage,
			@AuthenticationPrincipal SecurityUser securityUser) throws IOException {
        if (securityUser == null || securityUser.getUser() == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication is required.", null), HttpStatus.UNAUTHORIZED);
        }
        @Valid Venue venue = gson.fromJson(venueJson, Venue.class);
		if (mapImage != null && !mapImage.isEmpty()) {
			de.technikteam.model.File savedFile = fileService.storeFile(mapImage, null, "NUTZER",
					securityUser.getUser(), "venues");
			venue.setMapImagePath(savedFile.getFilepath());
		}
		Venue createdVenue = venueDAO.create(venue);
		adminLogService.log(securityUser.getUser().getUsername(), "VENUE_CREATE",
				"Venue '" + createdVenue.getName() + "' created.");
		notificationService.broadcastUIUpdate("VENUE", "CREATED", createdVenue);
		return new ResponseEntity<>(new ApiResponse(true, "Venue created successfully.", createdVenue),
				HttpStatus.CREATED);
	}

	@PostMapping(value = "/{id}", consumes = {"multipart/form-data"})
	@Operation(summary = "Update a venue")
	public ResponseEntity<ApiResponse> updateVenue(@PathVariable int id, @RequestPart("venue") String venueJson,
			@RequestPart(value = "mapImage", required = false) MultipartFile mapImage,
			@AuthenticationPrincipal SecurityUser securityUser) throws IOException {
        if (securityUser == null || securityUser.getUser() == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication is required.", null), HttpStatus.UNAUTHORIZED);
        }
        @Valid Venue venue = gson.fromJson(venueJson, Venue.class);
		venue.setId(id);
        
		if (mapImage != null && !mapImage.isEmpty()) {
			de.technikteam.model.File savedFile = fileService.storeFile(mapImage, null, "NUTZER",
					securityUser.getUser(), "venues");
			venue.setMapImagePath(savedFile.getFilepath());
		} else {
            // If no new image is sent, keep the old one. This handles the case where a user just updates text.
            venueDAO.findById(id).ifPresent(existingVenue -> {
                if (venue.getMapImagePath() == null) { // Retain old path if no new file is sent
                    venue.setMapImagePath(existingVenue.getMapImagePath());
                }
            });
		}

		if (venueDAO.update(venue)) {
			adminLogService.log(securityUser.getUser().getUsername(), "VENUE_UPDATE",
					"Venue '" + venue.getName() + "' updated.");
			notificationService.broadcastUIUpdate("VENUE", "UPDATED", venue);
			return ResponseEntity.ok(new ApiResponse(true, "Venue updated successfully.", venue));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Venue not found.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a venue")
	public ResponseEntity<ApiResponse> deleteVenue(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null || securityUser.getUser() == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Authentication is required.", null), HttpStatus.UNAUTHORIZED);
        }
		Optional<Venue> venue = venueDAO.findById(id);
		if (venue.isPresent() && venueDAO.delete(id)) {
			adminLogService.log(securityUser.getUser().getUsername(), "VENUE_DELETE",
					"Venue '" + venue.get().getName() + "' deleted.");
			notificationService.broadcastUIUpdate("VENUE", "DELETED", Map.of("id", id));
			return ResponseEntity.ok(new ApiResponse(true, "Venue deleted successfully.", null));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Venue not found.", null));
	}
}