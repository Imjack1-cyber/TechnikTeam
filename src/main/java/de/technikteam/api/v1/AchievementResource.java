package de.technikteam.api.v1;

import de.technikteam.model.Achievement;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.dao.AchievementDAO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/achievements")
@Tag(name = "Achievements", description = "API for managing achievement definitions.")
@SecurityRequirement(name = "bearerAuth")
public class AchievementResource {

    private final AchievementDAO achievementDAO;
    private final AdminLogService adminLogService;

    @Autowired
    public AchievementResource(AchievementDAO achievementDAO, AdminLogService adminLogService) {
        this.achievementDAO = achievementDAO;
        this.adminLogService = adminLogService;
    }

    @GetMapping
    @Operation(summary = "Get all achievement definitions", description = "Retrieves a list of all available achievements.")
    public ResponseEntity<ApiResponse> getAllAchievements() {
        List<Achievement> achievements = achievementDAO.getAllAchievements();
        return ResponseEntity.ok(new ApiResponse(true, "Achievements retrieved successfully", achievements));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single achievement definition", description = "Retrieves an achievement by its ID.")
    public ResponseEntity<ApiResponse> getAchievementById(@PathVariable int id) {
        Achievement achievement = achievementDAO.getAchievementById(id);
        if (achievement != null) {
            return ResponseEntity.ok(new ApiResponse(true, "Achievement retrieved successfully", achievement));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Achievement not found", null));
        }
    }

    @PostMapping
    @Operation(summary = "Create a new achievement definition", description = "Adds a new achievement to the system.")
    public ResponseEntity<ApiResponse> createAchievement(@RequestBody Achievement newAchievement, @AuthenticationPrincipal User adminUser) {
        if (newAchievement.getAchievementKey() == null || newAchievement.getAchievementKey().isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Achievement key cannot be empty.", null));
        }
        if (achievementDAO.createAchievement(newAchievement)) {
            adminLogService.log(adminUser.getUsername(), "CREATE_ACHIEVEMENT_API", "Achievement '" + newAchievement.getName() + "' created via API.");
            return new ResponseEntity<>(new ApiResponse(true, "Achievement created successfully", newAchievement), HttpStatus.CREATED);
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Could not create achievement (key may already exist).", null));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an achievement definition", description = "Updates the details of an existing achievement.")
    public ResponseEntity<ApiResponse> updateAchievement(@PathVariable int id, @RequestBody Achievement updatedAchievement, @AuthenticationPrincipal User adminUser) {
        updatedAchievement.setId(id);
        if (achievementDAO.updateAchievement(updatedAchievement)) {
            adminLogService.log(adminUser.getUsername(), "UPDATE_ACHIEVEMENT_API", "Achievement '" + updatedAchievement.getName() + "' (ID: " + id + ") updated via API.");
            return ResponseEntity.ok(new ApiResponse(true, "Achievement updated successfully", updatedAchievement));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Achievement not found or update failed.", null));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an achievement definition", description = "Removes an achievement definition from the system.")
    public ResponseEntity<ApiResponse> deleteAchievement(@PathVariable int id, @AuthenticationPrincipal User adminUser) {
        Achievement achievementToDelete = achievementDAO.getAchievementById(id);
        if (achievementToDelete == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Achievement to delete not found.", null));
        }
        if (achievementDAO.deleteAchievement(id)) {
            adminLogService.log(adminUser.getUsername(), "DELETE_ACHIEVEMENT_API", "Achievement '" + achievementToDelete.getName() + "' (ID: " + id + ") deleted via API.");
            return ResponseEntity.ok(new ApiResponse(true, "Achievement deleted successfully", Map.of("deletedId", id)));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to delete achievement.", null));
        }
    }
}