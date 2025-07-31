package de.technikteam.api.v1;

import de.technikteam.dao.AchievementDAO;
import de.technikteam.model.Achievement;
import de.technikteam.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/achievements")
@Tag(name = "Admin Achievements", description = "Endpoints for managing achievements.")
@SecurityRequirement(name = "bearerAuth")
public class AdminAchievementResource {

	private final AchievementDAO achievementDAO;

	@Autowired
	public AdminAchievementResource(AchievementDAO achievementDAO) {
		this.achievementDAO = achievementDAO;
	}

	@GetMapping
	@Operation(summary = "Get all achievements", description = "Retrieves a list of all available achievements.")
	@PreAuthorize("hasAuthority('ACHIEVEMENT_VIEW')")
	public ResponseEntity<ApiResponse> getAllAchievements() {
		List<Achievement> achievements = achievementDAO.getAllAchievements();
		return ResponseEntity.ok(new ApiResponse(true, "Achievements retrieved.", achievements));
	}

	@PostMapping
	@Operation(summary = "Create an achievement", description = "Creates a new achievement definition.")
	@PreAuthorize("hasAuthority('ACHIEVEMENT_CREATE')")
	public ResponseEntity<ApiResponse> createAchievement(@RequestBody Achievement achievement) {
		if (achievementDAO.createAchievement(achievement)) {
			return ResponseEntity.ok(new ApiResponse(true, "Achievement created.", null));
		}
		return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to create achievement.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update an achievement", description = "Updates an existing achievement's details.")
	@PreAuthorize("hasAuthority('ACHIEVEMENT_UPDATE')")
	public ResponseEntity<ApiResponse> updateAchievement(@PathVariable int id, @RequestBody Achievement achievement) {
		achievement.setId(id);
		if (achievementDAO.updateAchievement(achievement)) {
			return ResponseEntity.ok(new ApiResponse(true, "Achievement updated.", null));
		}
		return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to update achievement.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete an achievement", description = "Deletes an achievement definition.")
	@PreAuthorize("hasAuthority('ACHIEVEMENT_DELETE')")
	public ResponseEntity<ApiResponse> deleteAchievement(@PathVariable int id) {
		if (achievementDAO.deleteAchievement(id)) {
			return ResponseEntity.ok(new ApiResponse(true, "Achievement deleted.", null));
		}
		return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to delete achievement.", null));
	}
}