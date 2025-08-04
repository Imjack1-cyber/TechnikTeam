package de.technikteam.api.v1.public_api;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/search")
@Tag(name = "Public Search", description = "Endpoints for site-wide content search.")
@SecurityRequirement(name = "bearerAuth")
public class PublicSearchResource {

	private final SearchService searchService;

	@Autowired
	public PublicSearchResource(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping
	@Operation(summary = "Perform a site-wide search", description = "Searches across events, inventory, and documentation for a given query.")
	public ResponseEntity<ApiResponse> search(@Parameter(description = "The search term.") @RequestParam String query,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (query == null || query.trim().length() < 3) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Der Suchbegriff muss mindestens 3 Zeichen lang sein.", null));
		}

		User user = securityUser.getUser();
		return ResponseEntity.ok(new ApiResponse(true, "Suchergebnisse erfolgreich abgerufen.",
				searchService.performSearch(query, user)));
	}
}