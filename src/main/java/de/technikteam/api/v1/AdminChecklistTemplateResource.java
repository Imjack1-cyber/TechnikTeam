package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.ChecklistTemplateItemValidationDTO;
import de.technikteam.dao.ChecklistTemplateDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ChecklistTemplate;
import de.technikteam.service.ChecklistTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
	private final ChecklistTemplateService templateService;

	@Autowired
	public AdminChecklistTemplateResource(ChecklistTemplateDAO templateDAO, ChecklistTemplateService templateService) {
		this.templateDAO = templateDAO;
		this.templateService = templateService;
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
		try {
			ChecklistTemplate savedTemplate = templateDAO.save(template);
			return new ResponseEntity<>(new ApiResponse(true, "Vorlage erfolgreich erstellt.", savedTemplate),
					HttpStatus.CREATED);
		} catch (DuplicateKeyException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ApiResponse(false, "Eine Vorlage mit diesem Namen existiert bereits.", null));
		}
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a checklist template")
	public ResponseEntity<ApiResponse> updateTemplate(@PathVariable int id, @RequestBody ChecklistTemplate template) {
		template.setId(id);
		try {
			ChecklistTemplate updatedTemplate = templateDAO.save(template);
			return ResponseEntity.ok(new ApiResponse(true, "Vorlage erfolgreich aktualisiert.", updatedTemplate));
		} catch (DuplicateKeyException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ApiResponse(false, "Eine andere Vorlage mit diesem Namen existiert bereits.", null));
		}
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

	@GetMapping("/{id}/apply-preview")
	@Operation(summary = "Get template items with current availability for event planning")
	public ResponseEntity<ApiResponse> getTemplateForEventApplication(@PathVariable int id) {
		List<ChecklistTemplateItemValidationDTO> items = templateService.getTemplateForEventApplication(id);
		return ResponseEntity.ok(new ApiResponse(true, "Template preview retrieved.", items));
	}
}