package de.technikteam.api.v1.public_api;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.File;
import de.technikteam.model.User;
import de.technikteam.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/files")
@Tag(name = "Public Files", description = "Endpoints for downloading files and viewing images.")
@SecurityRequirement(name = "bearerAuth")
public class PublicFilesResource {

	private final FileDAO fileDAO;

	@Autowired
	public PublicFilesResource(FileDAO fileDAO) {
		this.fileDAO = fileDAO;
	}

	@GetMapping
	@Operation(summary = "Get all accessible files grouped by category", description = "Retrieves files visible to the current user, grouped by their category.")
	public ResponseEntity<ApiResponse> getFiles(@CurrentUser User user) {
		Map<String, List<File>> files = fileDAO.getAllFilesGroupedByCategory(user);
		return ResponseEntity.ok(new ApiResponse(true, "Files retrieved successfully.", files));
	}
}