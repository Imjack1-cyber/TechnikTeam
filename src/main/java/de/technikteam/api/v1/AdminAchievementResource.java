package de.technikteam.api.v1;

import de.technikteam.dao.AchievementDAO;
import de.technikteam.model.Achievement;
import de.technikteam.model.ApiResponse;
import de.technikteam.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/achievements")
@Tag(name = "Admin Achievements", description = "Endpoints for managing achievements.")
@SecurityRequirement(name = "bearerAuth")
public class AdminAchievementResource {

	private final AchievementDAO achievementDAO;
	private final NotificationService notificationService;

	@Autowired
	public AdminAchievementResource(AchievementDAO achievementDAO, NotificationService notificationService) {
		this.achievementDAO = achievementDAO;
		this.notificationService = notificationService;
	}

	@GetMapping
	@Operation(summary = "Get all achievements", description = "Retrieves a list of all available achievements.")
	public ResponseEntity<ApiResponse> getAllAchievements() {
		List<Achievement> achievements = achievementDAO.getAllAchievements();
		return ResponseEntity.ok(new ApiResponse(true, "Abzeichen erfolgreich abgerufen.", achievements));
	}

	@PostMapping
	@Operation(summary = "Create an achievement", description = "Creates a new achievement definition.")
	public ResponseEntity<ApiResponse> createAchievement(@RequestBody Achievement achievement) {
		if (achievementDAO.createAchievement(achievement)) {
			notificationService.broadcastUIUpdate("ACHIEVEMENT", "CREATED", achievement);
			return ResponseEntity.ok(new ApiResponse(true, "Abzeichen erstellt.", null));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Fehler beim Erstellen des Abzeichens.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update an achievement", description = "Updates an existing achievement's details.")
	public ResponseEntity<ApiResponse> updateAchievement(@PathVariable int id, @RequestBody Achievement achievement) {
		achievement.setId(id);
		if (achievementDAO.updateAchievement(achievement)) {
			notificationService.broadcastUIUpdate("ACHIEVEMENT", "UPDATED", achievement);
			return ResponseEntity.ok(new ApiResponse(true, "Abzeichen aktualisiert.", null));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Fehler beim Aktualisieren des Abzeichens.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete an achievement", description = "Deletes an achievement definition.")
	public ResponseEntity<ApiResponse> deleteAchievement(@PathVariable int id) {
		if (achievementDAO.deleteAchievement(id)) {
			notificationService.broadcastUIUpdate("ACHIEVEMENT", "DELETED", Map.of("id", id));
			return ResponseEntity.ok(new ApiResponse(true, "Abzeichen gelöscht.", null));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Fehler beim Löschen des Abzeichens.", null));
	}
}