package de.technikteam.api.v1;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;
import de.technikteam.model.ApiResponse;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@Tag(name = "Admin Logs", description = "Endpoints for viewing and managing the admin action log.")
@SecurityRequirement(name = "bearerAuth")
public class LogResource {

	private final AdminLogDAO logDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public LogResource(AdminLogDAO logDAO, AdminLogService adminLogService) {
		this.logDAO = logDAO;
		this.adminLogService = adminLogService;
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

	@PostMapping("/{logId}/revoke")
	@Operation(summary = "Revoke an admin action", description = "Revokes a previously logged administrative action, if the action is reversible.")
	public ResponseEntity<ApiResponse> revokeLogAction(@PathVariable long logId,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			adminLogService.revokeAction(logId, securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "Aktion erfolgreich widerrufen.", null));
		} catch (UnsupportedOperationException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Ein unerwarteter Fehler ist aufgetreten.", null));
		}
	}
}