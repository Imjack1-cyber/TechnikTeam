package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "Endpoints for the administrative dashboard.")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardResource {

	private final AdminDashboardService dashboardService;

	@Autowired
	public AdminDashboardResource(AdminDashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping
	@Operation(summary = "Get all data for the admin dashboard")
	@PreAuthorize("hasAuthority('ADMIN_DASHBOARD_ACCESS')")
	public ResponseEntity<ApiResponse> getDashboardData(@AuthenticationPrincipal SecurityUser securityUser) {
		// The service method doesn't require the user, but this demonstrates the
		// pattern.
		return ResponseEntity
				.ok(new ApiResponse(true, "Dashboard data retrieved.", dashboardService.getDashboardData()));
	}
}