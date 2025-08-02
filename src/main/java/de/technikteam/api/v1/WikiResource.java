package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.WikiUpdateRequest;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.model.WikiEntry;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.WikiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/wiki")
@Tag(name = "Admin Wiki", description = "Endpoints for managing the technical documentation wiki.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ACCESS_ADMIN_PANEL')")
public class WikiResource {

	private final WikiService wikiService;
	private final WikiDAO wikiDAO;
	private final AdminLogService adminLogService;
	private final PolicyFactory richTextPolicy;

	@Autowired
	public WikiResource(WikiService wikiService, WikiDAO wikiDAO, AdminLogService adminLogService,
			@Qualifier("richTextPolicy") PolicyFactory richTextPolicy) {
		this.wikiService = wikiService;
		this.wikiDAO = wikiDAO;
		this.adminLogService = adminLogService;
		this.richTextPolicy = richTextPolicy;
	}

	@GetMapping
	@Operation(summary = "Get wiki navigation tree", description = "Retrieves the entire wiki page structure as a hierarchical tree.")
	public ResponseEntity<ApiResponse> getWikiTree() {
		Map<String, Object> treeData = wikiService.getWikiTreeAsData();
		return ResponseEntity.ok(new ApiResponse(true, "Wiki tree retrieved successfully", treeData));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a single wiki page", description = "Retrieves the content of a single wiki page by its ID.")
	public ResponseEntity<ApiResponse> getWikiEntryById(
			@Parameter(description = "ID of the wiki page to retrieve") @PathVariable int id) {
		Optional<WikiEntry> entryOptional = wikiDAO.getWikiEntryById(id);
		return entryOptional.map(entry -> ResponseEntity.ok(new ApiResponse(true, "Content loaded", entry)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponse(false, "Wiki entry not found", null)));
	}

	@PostMapping
	@Operation(summary = "Create a new wiki page", description = "Creates a new documentation page in the wiki.")
	public ResponseEntity<ApiResponse> createWikiEntry(@Valid @RequestBody WikiEntry newEntry,
			@AuthenticationPrincipal User adminUser) {
		if (newEntry.getFilePath() == null || newEntry.getFilePath().isBlank()) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "File path cannot be empty.", null));
		}
		if (wikiDAO.findByFilePath(newEntry.getFilePath()).isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ApiResponse(false, "An entry with this file path already exists.", null));
		}

		newEntry.setContent(richTextPolicy.sanitize(newEntry.getContent()));
		Optional<WikiEntry> createdEntryOptional = wikiDAO.createWikiEntry(newEntry);
		if (createdEntryOptional.isPresent()) {
			adminLogService.log(adminUser.getUsername(), "CREATE_WIKI_PAGE",
					"Created wiki page: " + createdEntryOptional.get().getFilePath());
			return new ResponseEntity<>(new ApiResponse(true, "Page created successfully", createdEntryOptional.get()),
					HttpStatus.CREATED);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to create page in database.", null));
		}
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a wiki page", description = "Updates the content of an existing wiki page.")
	public ResponseEntity<ApiResponse> updateWikiEntry(
			@Parameter(description = "ID of the wiki page to update") @PathVariable int id,
			@Valid @RequestBody WikiUpdateRequest updateRequest, @AuthenticationPrincipal User adminUser) {

		String sanitizedContent = richTextPolicy.sanitize(updateRequest.content());
		if (wikiDAO.updateWikiContent(id, sanitizedContent)) {
			adminLogService.log(adminUser.getUsername(), "UPDATE_WIKI_PAGE", "Updated wiki page ID: " + id);
			return ResponseEntity.ok(new ApiResponse(true, "Page updated successfully", null));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Failed to update page. It may not exist.", null));
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a wiki page", description = "Permanently deletes a wiki page.")
	public ResponseEntity<ApiResponse> deleteWikiEntry(
			@Parameter(description = "ID of the wiki page to delete") @PathVariable int id,
			@AuthenticationPrincipal User adminUser) {

		Optional<WikiEntry> entryToDelete = wikiDAO.getWikiEntryById(id);
		if (entryToDelete.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Wiki entry not found.", null));
		}

		if (wikiDAO.deleteWikiEntry(id)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_WIKI_PAGE",
					"Deleted wiki page: " + entryToDelete.get().getFilePath());
			return ResponseEntity.ok(new ApiResponse(true, "Page deleted successfully", Map.of("deletedId", id)));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to delete page.", null));
		}
	}
}