package de.technikteam.api.v1;

import de.technikteam.dao.ReportDAO;
import de.technikteam.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Admin Reports", description = "Endpoints for generating reports and statistics.")
@SecurityRequirement(name = "bearerAuth")
public class ReportResource {

	private final ReportDAO reportDAO;

	@Autowired
	public ReportResource(ReportDAO reportDAO) {
		this.reportDAO = reportDAO;
	}

	@GetMapping("/dashboard")
	@Operation(summary = "Get dashboard report data", description = "Retrieves aggregated data for the admin dashboard, including event trends and user activity.")
	public ResponseEntity<ApiResponse> getDashboardReport() {
		Map<String, Object> dashboardData = new HashMap<>();
		dashboardData.put("eventTrend", reportDAO.getEventCountByMonth(12));
		dashboardData.put("userActivity", reportDAO.getUserParticipationStats(10));
		dashboardData.put("totalInventoryValue", reportDAO.getTotalInventoryValue());
		return ResponseEntity.ok(new ApiResponse(true, "Dashboard data retrieved", dashboardData));
	}
}