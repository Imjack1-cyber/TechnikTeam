package de.technikteam.api.v1.public_api;

import de.technikteam.dao.*;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Attachment;
import de.technikteam.model.FileSharingLink;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/public/files")
@Tag(name = "Public Files", description = "Endpoints for downloading files and viewing images.")
public class PublicFileStreamResource {
	private static final Logger logger = LogManager.getLogger(PublicFileStreamResource.class);

	private final FileDAO fileDAO;
	private final AttachmentDAO attachmentDAO;
    private final FileSharingDAO fileSharingDAO;
	private final Path fileStorageLocation;

	@Autowired
	public PublicFileStreamResource(FileDAO fileDAO, AttachmentDAO attachmentDAO,
			FileSharingDAO fileSharingDAO, ConfigurationService configService) {
		this.fileDAO = fileDAO;
		this.attachmentDAO = attachmentDAO;
        this.fileSharingDAO = fileSharingDAO;
		this.fileStorageLocation = Paths.get(configService.getProperty("upload.directory")).toAbsolutePath()
				.normalize();
	}

	@GetMapping("/download/{id}")
	@Operation(summary = "Download a file", description = "Downloads a file (general or attachment) by its database ID after checking permissions.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File content", content = @Content(mediaType = "application/octet-stream"))
	public ResponseEntity<Resource> downloadFile(
			@Parameter(description = "ID of the file or attachment record") @PathVariable int id) {
		String filePathFromDb = null;
		String filenameForDownload = null;

		Attachment attachment = attachmentDAO.getAttachmentById(id);
		if (attachment != null) {
			filePathFromDb = attachment.getFilepath();
			filenameForDownload = attachment.getFilename();
		} else {
			de.technikteam.model.File dbFile = fileDAO.getFileById(id);
			if (dbFile != null) {
				filePathFromDb = dbFile.getFilepath();
				filenameForDownload = dbFile.getFilename();
			}
		}

		if (filePathFromDb == null)
			return ResponseEntity.notFound().build();

		return serveFile(filePathFromDb, filenameForDownload, false);
	}

    @GetMapping("/share/{token}/meta")
    @Operation(summary = "Get metadata for a shared file", description = "Retrieves metadata like filename for a file shared via a secure token.")
    public ResponseEntity<ApiResponse> getSharedFileMetadata(@PathVariable String token, @AuthenticationPrincipal SecurityUser securityUser) {
        FileSharingLink link = fileSharingDAO.findByToken(token)
                .orElse(null);

        if (link == null || (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now()))) {
            return ResponseEntity.status(HttpStatus.GONE).body(new ApiResponse(false, "Link is expired or invalid.", null));
        }

        User currentUser = (securityUser != null) ? securityUser.getUser() : null;

        // Authorization check
        switch (link.getAccessLevel()) {
            case "LOGGED_IN":
                if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "You must be logged in to access this file.", null));
                break;
            case "ADMIN":
                if (currentUser == null || !currentUser.hasAdminAccess()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "You do not have permission to access this file.", null));
                }
                break;
            case "PUBLIC":
                // No check needed
                break;
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Invalid link configuration.", null));
        }

        de.technikteam.model.File fileToServe = fileDAO.getFileById(link.getFileId());
        if (fileToServe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "File not found.", null));
        }

        // Don't send sensitive info like filepath
        fileToServe.setFilepath(null);

        return ResponseEntity.ok(new ApiResponse(true, "File metadata retrieved.", fileToServe));
    }


    @GetMapping("/share/{token}")
    @Operation(summary = "Download a shared file", description = "Downloads a file using a secure sharing token.")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String token, @AuthenticationPrincipal SecurityUser securityUser) {
        FileSharingLink link = fileSharingDAO.findByToken(token)
                .orElse(null);

        if (link == null || (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now()))) {
            return ResponseEntity.status(410).build(); // Gone
        }

        User currentUser = (securityUser != null) ? securityUser.getUser() : null;

        // Authorization check
        switch (link.getAccessLevel()) {
            case "LOGGED_IN":
                if (currentUser == null) return ResponseEntity.status(401).build();
                break;
            case "ADMIN":
                if (currentUser == null || !currentUser.hasAdminAccess()) {
                    return ResponseEntity.status(403).build();
                }
                break;
            case "PUBLIC":
                // No check needed
                break;
            default:
                return ResponseEntity.status(500).build();
        }

        de.technikteam.model.File fileToServe = fileDAO.getFileById(link.getFileId());
        if (fileToServe == null) {
            return ResponseEntity.notFound().build();
        }

        return serveFile(fileToServe.getFilepath(), fileToServe.getFilename(), false);
    }


	@GetMapping("/images/{filename:.+}")
	@Operation(summary = "Get an inventory image", description = "Retrieves an inventory image for display. The filename usually corresponds to a storage item's image path.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Image content", content = @Content(mediaType = "image/*"))
	public ResponseEntity<Resource> getImage(
			@Parameter(description = "The filename of the image") @PathVariable String filename) {
		return serveFile("images/" + filename, filename, true);
	}

	private ResponseEntity<Resource> serveFile(String relativePath, String originalFilename, boolean inline) {
		try {
			Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
			if (!filePath.startsWith(this.fileStorageLocation)) {
				logger.warn("Path Traversal Attack attempt detected for path '{}'", filePath);
				return ResponseEntity.status(403).build();
			}

			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists() || resource.isReadable()) {
				String contentType = "application/octet-stream"; 
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