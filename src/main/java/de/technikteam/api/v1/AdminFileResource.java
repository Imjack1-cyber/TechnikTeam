package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.CategoryRequest;
import de.technikteam.api.v1.dto.FileContentUpdateRequest;
import de.technikteam.api.v1.dto.FileRenameRequest;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.FileCategory;
import de.technikteam.model.FileSharingLink;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	@PostMapping
	@Operation(summary = "Upload a new file")
	public ResponseEntity<ApiResponse> uploadFile(@RequestPart("file") MultipartFile file,
			@RequestParam(required = false) Integer categoryId, 
            @RequestParam(required = false) String newCategoryName,
            @RequestParam String requiredRole,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
            Integer finalCategoryId = categoryId;
            if (newCategoryName != null && !newCategoryName.isBlank()) {
                // This is a simplified approach. A real app might have a dedicated CategoryService.
                if (fileDAO.createCategory(newCategoryName)) {
                    // This is inefficient but acceptable for this context. A better DAO would return the new ID.
                    finalCategoryId = fileDAO.getAllCategories().stream()
                        .filter(c -> newCategoryName.equals(c.getName()))
                        .map(FileCategory::getId)
                        .findFirst()
                        .orElse(categoryId);
                }
            }
			de.technikteam.model.File savedFile = fileService.storeFile(file, finalCategoryId, requiredRole,
					securityUser.getUser(), "docs");
			return new ResponseEntity<>(new ApiResponse(true, "Datei erfolgreich hochgeladen.", savedFile),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Datei konnte nicht hochgeladen werden: " + e.getMessage(), null));
		}
	}

	@PostMapping("/replace/{id}")
	@Operation(summary = "Replace an existing file")
	public ResponseEntity<ApiResponse> replaceFile(@PathVariable int id, @RequestPart("file") MultipartFile file,
			@RequestParam(required = false) Integer categoryId, @RequestParam String requiredRole,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			de.technikteam.model.File updatedFile = fileService.replaceFile(id, file, categoryId, requiredRole,
					securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "Datei erfolgreich ersetzt.", updatedFile));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Datei konnte nicht ersetzt werden: " + e.getMessage(), null));
		}
	}

	@PutMapping("/{id}/rename")
	@Operation(summary = "Rename a file")
	public ResponseEntity<ApiResponse> renameFile(@PathVariable int id, @Valid @RequestBody FileRenameRequest request,
			@AuthenticationPrincipal SecurityUser securityUser) {
		de.technikteam.model.File file = fileDAO.getFileById(id);
		if (file == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Datei nicht gefunden.", null));
		}
		if (fileDAO.renameFile(id, request.newName())) {
			adminLogService.log(securityUser.getUser().getUsername(), "FILE_RENAME",
					"File '" + file.getFilename() + "' (ID: " + id + ") renamed to '" + request.newName() + "'.");
			return ResponseEntity.ok(new ApiResponse(true, "Datei erfolgreich umbenannt.", null));
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse(false, "Datei konnte nicht umbenannt werden.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a file")
	public ResponseEntity<ApiResponse> deleteFile(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			if (fileService.deleteFile(id, securityUser.getUser())) {
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
	public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CategoryRequest request,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (fileDAO.createCategory(request.name())) {
			adminLogService.log(securityUser.getUser().getUsername(), "CREATE_FILE_CATEGORY_API",
					"Category '" + request.name() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Kategorie erfolgreich erstellt.", null),
					HttpStatus.CREATED);
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false,
				"Kategorie konnte nicht erstellt werden. Der Name existiert möglicherweise bereits.", null));
	}

	@PutMapping("/categories/{id}")
	@Operation(summary = "Rename a file category")
	public ResponseEntity<ApiResponse> renameCategory(@PathVariable int id, @Valid @RequestBody CategoryRequest request,
			@AuthenticationPrincipal SecurityUser securityUser) {
		String oldName = fileDAO.getCategoryNameById(id);
		if (oldName == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Kategorie nicht gefunden.", null));
		}
		if (fileDAO.renameCategory(id, request.name())) {
			adminLogService.log(securityUser.getUser().getUsername(), "RENAME_FILE_CATEGORY_API",
					"Category '" + oldName + "' renamed to '" + request.name() + "'.");
			return ResponseEntity.ok(new ApiResponse(true, "Kategorie erfolgreich umbenannt.", null));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false,
				"Kategorie konnte nicht umbenannt werden. Der neue Name existiert möglicherweise bereits.", null));
	}

	@DeleteMapping("/categories/{id}")
	@Operation(summary = "Delete a file category")
	public ResponseEntity<ApiResponse> deleteCategory(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		String categoryName = fileDAO.getCategoryNameById(id);
		if (categoryName != null && fileDAO.deleteCategory(id)) {
			adminLogService.log(securityUser.getUser().getUsername(), "DELETE_FILE_CATEGORY_API",
					"Category '" + categoryName + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Kategorie erfolgreich gelöscht.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Kategorie nicht gefunden oder konnte nicht gelöscht werden.", null));
	}

	@GetMapping
	@Operation(summary = "Get all files grouped by category (Admin View)")
	public ResponseEntity<ApiResponse> getAllFiles(@AuthenticationPrincipal SecurityUser securityUser) {
		Map<String, List<de.technikteam.model.File>> groupedFiles = fileDAO
				.getAllFilesGroupedByCategory(securityUser.getUser());
		List<de.technikteam.model.File> rawFiles = fileDAO.getAllFilesForAdmin();

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("grouped", groupedFiles);
		responseData.put("raw", rawFiles);

		return ResponseEntity.ok(new ApiResponse(true, "Dateien erfolgreich abgerufen.", responseData));
	}

	@GetMapping("/categories")
	@Operation(summary = "Get all file categories")
	public ResponseEntity<ApiResponse> getAllCategories() {
		List<FileCategory> categories = fileDAO.getAllCategories();
		return ResponseEntity.ok(new ApiResponse(true, "Kategorien erfolgreich abgerufen.", categories));
	}

    @PostMapping("/{fileId}/share")
    @Operation(summary = "Create a sharing link for a file")
    public ResponseEntity<ApiResponse> createShareLink(@PathVariable int fileId, @RequestBody FileSharingLink linkDetails, @AuthenticationPrincipal SecurityUser securityUser) {
        FileSharingLink createdLink = fileService.createSharingLink(fileId, linkDetails.getAccessLevel(), linkDetails.getExpiresAt(), securityUser.getUser());
        return new ResponseEntity<>(new ApiResponse(true, "Sharing link created.", createdLink), HttpStatus.CREATED);
    }

    @GetMapping("/{fileId}/share")
    @Operation(summary = "Get all sharing links for a file")
    public ResponseEntity<ApiResponse> getShareLinks(@PathVariable int fileId) {
        List<FileSharingLink> links = fileService.getSharingLinksForFile(fileId);
        return ResponseEntity.ok(new ApiResponse(true, "Sharing links retrieved.", links));
    }

    @DeleteMapping("/share/{linkId}")
    @Operation(summary = "Delete a sharing link")
    public ResponseEntity<ApiResponse> deleteShareLink(@PathVariable int linkId, @AuthenticationPrincipal SecurityUser securityUser) {
        fileService.deleteSharingLink(linkId, securityUser.getUser());
        return ResponseEntity.ok(new ApiResponse(true, "Sharing link deleted.", null));
    }
}