package de.technikteam.api.v1.public_api;

import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.FileDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Attachment;
import de.technikteam.model.User;
import de.technikteam.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/public/files")
@Tag(name = "Public Files", description = "Endpoints for downloading files and viewing images.")
@SecurityRequirement(name = "bearerAuth")
public class PublicFileStreamResource {
	private static final Logger logger = LogManager.getLogger(PublicFileStreamResource.class);

	private final FileDAO fileDAO;
	private final AttachmentDAO attachmentDAO;
	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;
	private final ConfigurationService configService;
	private final Path fileStorageLocation;

	@Autowired
	public PublicFileStreamResource(FileDAO fileDAO, AttachmentDAO attachmentDAO, EventDAO eventDAO,
			MeetingDAO meetingDAO, ConfigurationService configService) {
		this.fileDAO = fileDAO;
		this.attachmentDAO = attachmentDAO;
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
		this.configService = configService;
		this.fileStorageLocation = Paths.get(configService.getProperty("upload.directory")).toAbsolutePath()
				.normalize();
	}

	@GetMapping("/download/{id}")
	@Operation(summary = "Download a file", description = "Downloads a file (general or attachment) by its database ID after checking permissions.")
	@ApiResponse(responseCode = "200", description = "File content", content = @Content(mediaType = "application/octet-stream"))
	public ResponseEntity<Resource> downloadFile(
			@Parameter(description = "ID of the file or attachment record") @PathVariable int id,
			@AuthenticationPrincipal User user) {

		String filePathFromDb = null;
		String filenameForDownload = null;
		boolean isAuthorized = false;

		Attachment attachment = attachmentDAO.getAttachmentById(id);
		if (attachment != null) {
			filePathFromDb = attachment.getFilepath();
			filenameForDownload = attachment.getFilename();
			isAuthorized = isUserAuthorizedForAttachment(user, attachment);
		} else {
			de.technikteam.model.File dbFile = fileDAO.getFileById(id);
			if (dbFile != null) {
				filePathFromDb = dbFile.getFilepath();
				filenameForDownload = dbFile.getFilename();
				isAuthorized = user.hasAdminAccess() || "NUTZER".equalsIgnoreCase(dbFile.getRequiredRole());
			}
		}

		if (filePathFromDb == null)
			return ResponseEntity.notFound().build();
		if (!isAuthorized)
			return ResponseEntity.status(403).build();

		return serveFile(filePathFromDb, filenameForDownload, false);
	}

	@GetMapping("/images/{filename:.+}")
	@Operation(summary = "Get an image", description = "Retrieves an image for display. The filename usually corresponds to a storage item's image path.")
	@ApiResponse(responseCode = "200", description = "Image content", content = @Content(mediaType = "image/*"))
	public ResponseEntity<Resource> getImage(
			@Parameter(description = "The filename of the image") @PathVariable String filename) {
		return serveFile("images/" + filename, filename, true);
	}

	private boolean isUserAuthorizedForAttachment(User user, Attachment attachment) {
		if (user.hasAdminAccess())
			return true;
		if ("NUTZER".equalsIgnoreCase(attachment.getRequiredRole())) {
			if ("EVENT".equals(attachment.getParentType())) {
				return eventDAO.isUserAssociatedWithEvent(attachment.getParentId(), user.getId());
			} else if ("MEETING".equals(attachment.getParentType())) {
				return meetingDAO.isUserAssociatedWithMeeting(attachment.getParentId(), user.getId());
			}
		}
		return false;
	}

	private ResponseEntity<Resource> serveFile(String relativePath, String originalFilename, boolean inline) {
		try {
			Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
			if (!filePath.startsWith(this.fileStorageLocation)) {
				logger.fatal("CRITICAL: Path Traversal Attack Detected! Attempted to access '{}'", filePath);
				return ResponseEntity.status(403).build();
			}

			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists() || resource.isReadable()) {
				String contentType = "application/octet-stream"; // fallback
				try {
					contentType = Files.probeContentType(filePath);
				} catch (IOException e) {
					logger.warn("Could not determine content type for file {}", filePath);
				}

				String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+",
						"%20");
				String disposition = inline ? "inline" : "attachment";

				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename*=UTF-8''" + encodedFilename)
						.body(resource);
			} else {
				logger.error("File not found or not readable at path: {}", filePath);
				return ResponseEntity.notFound().build();
			}
		} catch (MalformedURLException ex) {
			logger.error("Malformed URL for file path: {}", relativePath, ex);
			return ResponseEntity.notFound().build();
		}
	}
}