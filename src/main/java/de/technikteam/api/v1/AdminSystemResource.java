package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.MaintenanceStatusDTO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.SystemStatsDTO;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.SystemInfoService;
import de.technikteam.service.SystemSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/system")
@Tag(name = "Admin System", description = "Endpoints for retrieving system information and statistics.")
@SecurityRequirement(name = "bearerAuth")
public class AdminSystemResource {

	private final SystemInfoService systemInfoService;
	private final SystemSettingsService settingsService;
	private final AdminLogService adminLogService;

	@Autowired
	public AdminSystemResource(SystemInfoService systemInfoService, SystemSettingsService settingsService,
			AdminLogService adminLogService) {
		this.systemInfoService = systemInfoService;
		this.settingsService = settingsService;
		this.adminLogService = adminLogService;
	}

	@GetMapping("/stats")
	@Operation(summary = "Get system statistics", description = "Retrieves current system statistics like CPU load, memory usage, and disk space.")
	public ResponseEntity<ApiResponse> getSystemStats() {
		SystemStatsDTO stats = systemInfoService.getSystemStats();
		return ResponseEntity.ok(new ApiResponse(true, "Systemstatistiken erfolgreich abgerufen.", stats));
	}

	@GetMapping("/maintenance")
	@Operation(summary = "Get maintenance mode status")
	public ResponseEntity<ApiResponse> getMaintenanceMode() {
		MaintenanceStatusDTO status = settingsService.getMaintenanceStatus();
		return ResponseEntity.ok(new ApiResponse(true, "Wartungsmodus-Status abgerufen.", status));
	}

	@PostMapping("/maintenance")
	@Operation(summary = "Set maintenance mode status")
	public ResponseEntity<ApiResponse> setMaintenanceMode(@RequestBody MaintenanceStatusDTO status,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (status == null || status.mode() == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Payload must contain 'mode'.", null));
		}
		MaintenanceStatusDTO newStatus = settingsService.setMaintenanceMode(status);
		adminLogService.log(securityUser.getUser().getUsername(), "MAINTENANCE_MODE_UPDATE",
				"Maintenance mode set to " + status.mode() + ". Message: " + status.message());
		return ResponseEntity.ok(new ApiResponse(true, "Wartungsmodus erfolgreich aktualisiert.", newStatus));
	}
}