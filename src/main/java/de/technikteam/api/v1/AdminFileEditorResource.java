package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.FileContentUpdateRequest;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.File;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/admin/files/content")
@Tag(name = "Admin Files", description = "Endpoints for managing files and categories.")
public class AdminFileEditorResource {

    private final FileService fileService;
    private final FileDAO fileDAO;

    @Autowired
    public AdminFileEditorResource(FileService fileService, FileDAO fileDAO) {
        this.fileService = fileService;
        this.fileDAO = fileDAO;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a file's text content for editing")
    public ResponseEntity<ApiResponse> getFileContent(@PathVariable int id, @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            File file = fileDAO.getFileById(id);
            if (file == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Datei nicht gefunden.", null));
            }
            // All files are editable by admin
            String content = fileService.getFileContent(id);
            file.setContent(content);
            return ResponseEntity.ok(new ApiResponse(true, "File content retrieved.", file));
        } catch (IOException | SecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a file's text content")
    public ResponseEntity<ApiResponse> updateFileContent(@PathVariable int id,
                                                         @Valid @RequestBody FileContentUpdateRequest request, @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            File file = fileDAO.getFileById(id);
            if (file == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Datei nicht gefunden.", null));
            }
            // All files are editable by admin
            if (fileService.updateFileContent(id, request.content(), securityUser.getUser())) {
                return ResponseEntity.ok(new ApiResponse(true, "Datei-Inhalt erfolgreich gespeichert.", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Speichern des Datei-Inhalts fehlgeschlagen.", null));
            }
        } catch (IOException | SecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}