package de.technikteam.api.v1.public_api;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/dashboard")
@Tag(name = "Public Dashboard", description = "Endpoints for the user-facing dashboard.")
@SecurityRequirement(name = "bearerAuth")
public class PublicDashboardResource {

	private final EventDAO eventDAO;
	private final EventTaskDAO eventTaskDAO;

	@Autowired
	public PublicDashboardResource(EventDAO eventDAO, EventTaskDAO eventTaskDAO) {
		this.eventDAO = eventDAO;
		this.eventTaskDAO = eventTaskDAO;
	}

	@GetMapping
	@Operation(summary = "Get dashboard data", description = "Retrieves all necessary data for the user's main dashboard view.")
	public ResponseEntity<ApiResponse> getDashboardData(@AuthenticationPrincipal User user) {
		List<Event> assignedEvents = eventDAO.getAssignedEventsForUser(user.getId(), 5);
		List<EventTask> openTasks = eventTaskDAO.getOpenTasksForUser(user.getId());
		List<Event> upcomingEvents = eventDAO.getAllActiveAndUpcomingEvents(); // Simplified for now

		Map<String, Object> dashboardData = new HashMap<>();
		dashboardData.put("assignedEvents", assignedEvents);
		dashboardData.put("openTasks", openTasks);
		dashboardData.put("upcomingEvents", upcomingEvents);

		return ResponseEntity.ok(new ApiResponse(true, "Dashboard data retrieved.", dashboardData));
	}
}