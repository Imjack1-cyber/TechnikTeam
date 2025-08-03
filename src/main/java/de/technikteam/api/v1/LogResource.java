package de.technikteam.api.v1;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;
import de.technikteam.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@Tag(name = "Admin Logs", description = "Endpoints for viewing the admin action log.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('LOG_READ')")
public class LogResource {

	private final AdminLogDAO logDAO;

	@Autowired
	public LogResource(AdminLogDAO logDAO) {
		this.logDAO = logDAO;
	}

	@GetMapping
	@Operation(summary = "Get admin action logs", description = "Retrieves a list of all administrative actions. Can be limited.")
	public ResponseEntity<ApiResponse> getLogs(@RequestParam(required = false) Integer limit) {
		List<AdminLog> logs;
		if (limit != null) {
			logs = logDAO.getRecentLogs(limit);
		} else {
			logs = logDAO.getAllLogs();
		}
		return ResponseEntity.ok(new ApiResponse(true, "Protokolle erfolgreich abgerufen.", logs));
	}
}