package de.technikteam.api.v1;

import de.technikteam.dao.ChecklistTemplateDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ChecklistTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/checklist-templates")
@Tag(name = "Admin Checklist Templates", description = "Endpoints for managing pre-flight checklist templates.")
@SecurityRequirement(name = "bearerAuth")
public class AdminChecklistTemplateResource {

	private final ChecklistTemplateDAO templateDAO;

	@Autowired
	public AdminChecklistTemplateResource(ChecklistTemplateDAO templateDAO) {
		this.templateDAO = templateDAO;
	}

	@GetMapping
	@Operation(summary = "Get all checklist templates")
	public ResponseEntity<ApiResponse> getAllTemplates() {
		List<ChecklistTemplate> templates = templateDAO.findAll();
		return ResponseEntity.ok(new ApiResponse(true, "Vorlagen erfolgreich abgerufen.", templates));
	}

	@PostMapping
	@Operation(summary = "Create a new checklist template")
	public ResponseEntity<ApiResponse> createTemplate(@RequestBody ChecklistTemplate template) {
		ChecklistTemplate savedTemplate = templateDAO.save(template);
		return new ResponseEntity<>(new ApiResponse(true, "Vorlage erfolgreich erstellt.", savedTemplate),
				HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a checklist template")
	public ResponseEntity<ApiResponse> updateTemplate(@PathVariable int id, @RequestBody ChecklistTemplate template) {
		template.setId(id);
		ChecklistTemplate updatedTemplate = templateDAO.save(template);
		return ResponseEntity.ok(new ApiResponse(true, "Vorlage erfolgreich aktualisiert.", updatedTemplate));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a checklist template")
	public ResponseEntity<ApiResponse> deleteTemplate(@PathVariable int id) {
		if (templateDAO.delete(id)) {
			return ResponseEntity.ok(new ApiResponse(true, "Vorlage erfolgreich gelöscht.", null));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Vorlage nicht gefunden.", null));
	}
}