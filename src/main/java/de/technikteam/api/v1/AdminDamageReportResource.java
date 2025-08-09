package de.technikteam.api.v1;

import de.technikteam.dao.DamageReportDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.DamageReport;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/damage-reports")
@Tag(name = "Admin Damage Reports", description = "Endpoints for managing user-submitted damage reports.")
@SecurityRequirement(name = "bearerAuth")
public class AdminDamageReportResource {

	private final DamageReportDAO damageReportDAO;
	private final StorageService storageService;

	@Autowired
	public AdminDamageReportResource(DamageReportDAO damageReportDAO, StorageService storageService) {
		this.damageReportDAO = damageReportDAO;
		this.storageService = storageService;
	}

	@GetMapping("/pending")
	@Operation(summary = "Get all pending damage reports")
	public ResponseEntity<ApiResponse> getPendingReports() {
		List<DamageReport> reports = damageReportDAO.getPendingReports();
		return ResponseEntity.ok(new ApiResponse(true, "Ausstehende Meldungen abgerufen.", reports));
	}

	@PostMapping("/{reportId}/confirm")
	@Operation(summary = "Confirm a damage report")
	public ResponseEntity<ApiResponse> confirmReport(@PathVariable int reportId,
			@RequestBody Map<String, Integer> payload, @AuthenticationPrincipal SecurityUser securityUser) {
		try {
			int quantity = payload.getOrDefault("quantity", 1);
			storageService.confirmDamageReport(reportId, quantity, securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "Schadensmeldung bestätigt.", null));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		}
	}

	@PostMapping("/{reportId}/reject")
	@Operation(summary = "Reject a damage report")
	public ResponseEntity<ApiResponse> rejectReport(@PathVariable int reportId,
			@RequestBody Map<String, String> payload, @AuthenticationPrincipal SecurityUser securityUser) {
		String adminNotes = payload.get("adminNotes");
		try {
			storageService.rejectDamageReport(reportId, adminNotes, securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "Schadensmeldung abgelehnt.", null));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		}
	}
}