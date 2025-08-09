package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.model.Venue;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.FileService;
import de.technikteam.dao.VenueDAO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

	@Autowired
	public AdminVenueResource(VenueDAO venueDAO, FileService fileService, AdminLogService adminLogService) {
		this.venueDAO = venueDAO;
		this.fileService = fileService;
		this.adminLogService = adminLogService;
	}

	private User getSystemUser() {
		User user = new User();
		user.setId(0);
		user.setUsername("SYSTEM");
		return user;
	}

	@GetMapping
	@Operation(summary = "Get all venues")
	public ResponseEntity<ApiResponse> getAllVenues() {
		List<Venue> venues = venueDAO.findAll();
		return ResponseEntity.ok(new ApiResponse(true, "Venues retrieved successfully.", venues));
	}

	@PostMapping
	@Operation(summary = "Create a new venue")
	public ResponseEntity<ApiResponse> createVenue(@RequestPart("venue") Venue venue,
			@RequestPart(value = "mapImage", required = false) MultipartFile mapImage) throws IOException {
		if (mapImage != null && !mapImage.isEmpty()) {
			de.technikteam.model.File savedFile = fileService.storeFile(mapImage, null, "NUTZER", getSystemUser(),
					"venues");
			venue.setMapImagePath(savedFile.getFilepath());
		}
		Venue createdVenue = venueDAO.create(venue);
		adminLogService.log(getSystemUser().getUsername(), "VENUE_CREATE",
				"Venue '" + createdVenue.getName() + "' created.");
		return new ResponseEntity<>(new ApiResponse(true, "Venue created successfully.", createdVenue),
				HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a venue")
	public ResponseEntity<ApiResponse> updateVenue(@PathVariable int id, @RequestPart("venue") Venue venue,
			@RequestPart(value = "mapImage", required = false) MultipartFile mapImage) throws IOException {
		venue.setId(id);
		if (mapImage != null && !mapImage.isEmpty()) {
			de.technikteam.model.File savedFile = fileService.storeFile(mapImage, null, "NUTZER", getSystemUser(),
					"venues");
			venue.setMapImagePath(savedFile.getFilepath());
		} else if (venue.getMapImagePath() == null) {
			// Preserve existing image if a new one is not uploaded but path is not null in
			// DTO
			Optional<Venue> existingVenue = venueDAO.findById(id);
			existingVenue.ifPresent(v -> venue.setMapImagePath(v.getMapImagePath()));
		}

		if (venueDAO.update(venue)) {
			adminLogService.log(getSystemUser().getUsername(), "VENUE_UPDATE",
					"Venue '" + venue.getName() + "' updated.");
			return ResponseEntity.ok(new ApiResponse(true, "Venue updated successfully.", venue));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Venue not found.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a venue")
	public ResponseEntity<ApiResponse> deleteVenue(@PathVariable int id) {
		Optional<Venue> venue = venueDAO.findById(id);
		if (venue.isPresent() && venueDAO.delete(id)) {
			adminLogService.log(getSystemUser().getUsername(), "VENUE_DELETE",
					"Venue '" + venue.get().getName() + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Venue deleted successfully.", null));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Venue not found.", null));
	}
}