package de.technikteam.api.v1.public_api;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.PageDocumentation;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.PageDocumentationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public/documentation")
@Tag(name = "Public Documentation", description = "Endpoints for viewing page documentation.")
public class PublicDocumentationResource {

	private final PageDocumentationService documentationService;

	@Autowired
	public PublicDocumentationResource(PageDocumentationService documentationService) {
		this.documentationService = documentationService;
	}

	@GetMapping
	@Operation(summary = "Get all accessible documentation pages")
	public ResponseEntity<ApiResponse> getAllDocs(@AuthenticationPrincipal SecurityUser securityUser) {
		boolean isAdmin = securityUser != null && securityUser.getUser().hasAdminAccess();
		List<PageDocumentation> docs = documentationService.findAll(isAdmin);
		return ResponseEntity.ok(new ApiResponse(true, "Documentation pages retrieved.", docs));
	}

	@GetMapping("/{pageKey}")
	@Operation(summary = "Get a single documentation page by key")
	public ResponseEntity<ApiResponse> getDocByKey(@PathVariable String pageKey,
			@AuthenticationPrincipal SecurityUser securityUser) {
		Optional<PageDocumentation> docOpt = documentationService.findByKey(pageKey);
		if (docOpt.isPresent()) {
			PageDocumentation doc = docOpt.get();
			boolean isAdmin = securityUser != null && securityUser.getUser().hasAdminAccess();
			if (doc.isAdminOnly() && !isAdmin) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Access denied.", null));
			}
			return ResponseEntity.ok(new ApiResponse(true, "Documentation retrieved.", doc));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Documentation not found.", null));
	}
}