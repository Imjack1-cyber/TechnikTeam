package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.CategoryRequest;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.FileCategory;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/files")
@Tag(name = "Admin Files", description = "Endpoints for managing files and categories.")
public class AdminFileResource {

	private final FileDAO fileDAO;
	private final FileService fileService;
	private final AdminLogService adminLogService;

	@Autowired
	public AdminFileResource(FileDAO fileDAO, FileService fileService, AdminLogService adminLogService) {
		this.fileDAO = fileDAO;
		this.fileService = fileService;
		this.adminLogService = adminLogService;
	}

	private User getSystemUser() {
		User user = new User();
		user.setId(0);
		user.setUsername("SYSTEM");
		return user;
	}

	@PostMapping
	@Operation(summary = "Upload a new file")
	public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) Integer categoryId, @RequestParam String requiredRole) {
		try {
			de.technikteam.model.File savedFile = fileService.storeFile(file, categoryId, requiredRole,
					getSystemUser());
			return new ResponseEntity<>(new ApiResponse(true, "Datei erfolgreich hochgeladen.", savedFile),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Datei konnte nicht hochgeladen werden: " + e.getMessage(), null));
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a file")
	public ResponseEntity<ApiResponse> deleteFile(@PathVariable int id) {
		try {
			if (fileService.deleteFile(id, getSystemUser())) {
				return ResponseEntity.ok(new ApiResponse(true, "Datei erfolgreich gelöscht.", Map.of("deletedId", id)));
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponse(false, "Datei nicht gefunden.", null));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					new ApiResponse(false, "Datei konnte aufgrund eines Serverfehlers nicht gelöscht werden.", null));
		}
	}

	@PostMapping("/categories")
	@Operation(summary = "Create a new file category")
	public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
		if (fileDAO.createCategory(request.name())) {
			adminLogService.log("SYSTEM", "CREATE_FILE_CATEGORY_API", "Category '" + request.name() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Kategorie erfolgreich erstellt.", null),
					HttpStatus.CREATED);
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false,
				"Kategorie konnte nicht erstellt werden. Der Name existiert möglicherweise bereits.", null));
	}

	@DeleteMapping("/categories/{id}")
	@Operation(summary = "Delete a file category")
	public ResponseEntity<ApiResponse> deleteCategory(@PathVariable int id) {
		String categoryName = fileDAO.getCategoryNameById(id);
		if (categoryName != null && fileDAO.deleteCategory(id)) {
			adminLogService.log("SYSTEM", "DELETE_FILE_CATEGORY_API", "Category '" + categoryName + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Kategorie erfolgreich gelöscht.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Kategorie nicht gefunden oder konnte nicht gelöscht werden.", null));
	}

	@GetMapping
	@Operation(summary = "Get all files grouped by category (Admin View)")
	public ResponseEntity<ApiResponse> getAllFiles() {
		Map<String, List<de.technikteam.model.File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory(null); // Pass
																												// null
																												// as
																												// user
																												// is
																												// not
																												// authenticated
		return ResponseEntity.ok(new ApiResponse(true, "Dateien erfolgreich abgerufen.", groupedFiles));
	}

	@GetMapping("/categories")
	@Operation(summary = "Get all file categories")
	public ResponseEntity<ApiResponse> getAllCategories() {
		List<FileCategory> categories = fileDAO.getAllCategories();
		return ResponseEntity.ok(new ApiResponse(true, "Kategorien erfolgreich abgerufen.", categories));
	}
}