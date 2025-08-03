package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.SystemStatsDTO;
import de.technikteam.service.SystemInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "Admin System", description = "Endpoints for retrieving system information and statistics.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('SYSTEM_READ')")
public class SystemResource {

	private final SystemInfoService systemInfoService;

	@Autowired
	public SystemResource(SystemInfoService systemInfoService) {
		this.systemInfoService = systemInfoService;
	}

	@GetMapping("/stats")
	@Operation(summary = "Get system statistics", description = "Retrieves current system statistics like CPU load, memory usage, and disk space.")
	public ResponseEntity<ApiResponse> getSystemStats() {
		SystemStatsDTO stats = systemInfoService.getSystemStats();
		return ResponseEntity.ok(new ApiResponse(true, "Systemstatistiken erfolgreich abgerufen.", stats));
	}
}