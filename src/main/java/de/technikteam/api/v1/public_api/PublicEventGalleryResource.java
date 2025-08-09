package de.technikteam.api.v1.public_api;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.EventPhoto;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.EventGalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/events")
@Tag(name = "Public Events", description = "Endpoints for user interactions with events.")
public class PublicEventGalleryResource {

	private final EventGalleryService galleryService;

	@Autowired
	public PublicEventGalleryResource(EventGalleryService galleryService) {
		this.galleryService = galleryService;
	}

	@GetMapping("/{eventId}/gallery")
	@Operation(summary = "Get all photos for an event gallery")
	public ResponseEntity<ApiResponse> getGallery(@PathVariable int eventId) {
		List<EventPhoto> photos = galleryService.findPhotosByEventId(eventId);
		return ResponseEntity.ok(new ApiResponse(true, "Gallery photos retrieved.", photos));
	}

	@PostMapping("/{eventId}/gallery")
	@Operation(summary = "Upload a photo to an event gallery")
	public ResponseEntity<ApiResponse> uploadPhoto(@PathVariable int eventId, @RequestParam("file") MultipartFile file,
			@RequestParam("caption") String caption, @AuthenticationPrincipal SecurityUser securityUser) {
		try {
			EventPhoto photo = galleryService.addPhotoToGallery(eventId, file, caption, securityUser.getUser());
			return new ResponseEntity<>(new ApiResponse(true, "Photo uploaded successfully.", photo),
					HttpStatus.CREATED);
		} catch (IOException | IllegalArgumentException | SecurityException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		}
	}

	@DeleteMapping("/gallery/{photoId}")
	@Operation(summary = "Delete a photo from a gallery")
	public ResponseEntity<ApiResponse> deletePhoto(@PathVariable int photoId,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			galleryService.deletePhoto(photoId, securityUser.getUser());
			return ResponseEntity
					.ok(new ApiResponse(true, "Photo deleted successfully.", Map.of("deletedId", photoId)));
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage(), null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage(), null));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to delete photo file.", null));
		}
	}
}