package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.PageDocumentation;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.PageDocumentationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/documentation")
@Tag(name = "Admin Documentation", description = "Endpoints for managing page documentation.")
public class AdminDocumentationResource {

	private final PageDocumentationService documentationService;

	@Autowired
	public AdminDocumentationResource(PageDocumentationService documentationService) {
		this.documentationService = documentationService;
	}

	@GetMapping
	@Operation(summary = "Get all documentation pages")
	public ResponseEntity<ApiResponse> getAllDocs() {
		List<PageDocumentation> docs = documentationService.findAll(true);
		return ResponseEntity.ok(new ApiResponse(true, "Documentation pages retrieved.", docs));
	}

	@GetMapping("/{pageKey}")
	@Operation(summary = "Get a single documentation page by key")
	public ResponseEntity<ApiResponse> getDocByKey(@PathVariable String pageKey) {
		Optional<PageDocumentation> doc = documentationService.findByKey(pageKey);
		return doc.map(d -> ResponseEntity.ok(new ApiResponse(true, "Documentation retrieved.", d)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponse(false, "Documentation not found.", null)));
	}

	@PostMapping
	@Operation(summary = "Create a new documentation page")
	public ResponseEntity<ApiResponse> createDoc(@Valid @RequestBody PageDocumentation doc,
			@AuthenticationPrincipal SecurityUser securityUser) {
		PageDocumentation createdDoc = documentationService.create(doc, securityUser.getUser());
		return new ResponseEntity<>(new ApiResponse(true, "Documentation created successfully.", createdDoc),
				HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a documentation page")
	public ResponseEntity<ApiResponse> updateDoc(@PathVariable int id, @Valid @RequestBody PageDocumentation doc,
			@AuthenticationPrincipal SecurityUser securityUser) {
		doc.setId(id);
		PageDocumentation updatedDoc = documentationService.update(doc, securityUser.getUser());
		if (updatedDoc != null) {
			return ResponseEntity.ok(new ApiResponse(true, "Documentation updated successfully.", updatedDoc));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Documentation not found.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a documentation page")
	public ResponseEntity<ApiResponse> deleteDoc(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (documentationService.delete(id, securityUser.getUser())) {
			return ResponseEntity.ok(new ApiResponse(true, "Documentation deleted successfully.", null));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Documentation not found.", null));
	}
}