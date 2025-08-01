package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.CategoryRequest;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.FileCategory;
import de.technikteam.model.User;
import de.technikteam.security.CurrentUser;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/files")
@Tag(name = "Admin Files", description = "Endpoints for managing files and categories.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('FILE_MANAGE')")
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

	@PostMapping
	@Operation(summary = "Upload a new file")
	@PreAuthorize("hasAuthority('FILE_CREATE')")
	public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) Integer categoryId, @RequestParam String requiredRole,
			@CurrentUser User adminUser) {
		try {
			de.technikteam.model.File savedFile = fileService.storeFile(file, categoryId, requiredRole, adminUser);
			return new ResponseEntity<>(new ApiResponse(true, "File uploaded successfully.", savedFile),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Could not upload the file: " + e.getMessage(), null));
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a file")
	@PreAuthorize("hasAuthority('FILE_DELETE')")
	public ResponseEntity<ApiResponse> deleteFile(@PathVariable int id, @CurrentUser User adminUser) {
		try {
			if (fileService.deleteFile(id, adminUser)) {
				return ResponseEntity.ok(new ApiResponse(true, "File deleted successfully", Map.of("deletedId", id)));
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponse(false, "File not found.", null));
			}
		} catch (Exception e) {
			// This will catch the RuntimeException thrown by the service if physical file
			// deletion fails.
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Could not delete file due to a server error.", null));
		}
	}

	@PostMapping("/categories")
	@Operation(summary = "Create a new file category")
	public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CategoryRequest request,
			@CurrentUser User adminUser) {
		if (fileDAO.createCategory(request.name())) {
			adminLogService.log(adminUser.getUsername(), "CREATE_FILE_CATEGORY_API",
					"Category '" + request.name() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Category created.", null), HttpStatus.CREATED);
		}
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ApiResponse(false, "Category could not be created. Name might already exist.", null));
	}

	@DeleteMapping("/categories/{id}")
	@Operation(summary = "Delete a file category")
	public ResponseEntity<ApiResponse> deleteCategory(@PathVariable int id, @CurrentUser User adminUser) {
		String categoryName = fileDAO.getCategoryNameById(id);
		if (categoryName != null && fileDAO.deleteCategory(id)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_FILE_CATEGORY_API",
					"Category '" + categoryName + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Category deleted.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Category not found or could not be deleted.", null));
	}

	@GetMapping
	@Operation(summary = "Get all files grouped by category (Admin View)")
	public ResponseEntity<ApiResponse> getAllFiles(@CurrentUser User user) {
		Map<String, List<de.technikteam.model.File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory(user);
		return ResponseEntity.ok(new ApiResponse(true, "Files retrieved", groupedFiles));
	}

	@GetMapping("/categories")
	@Operation(summary = "Get all file categories")
	public ResponseEntity<ApiResponse> getAllCategories() {
		List<FileCategory> categories = fileDAO.getAllCategories();
		return ResponseEntity.ok(new ApiResponse(true, "Categories retrieved", categories));
	}
}