package de.technikteam.api.v1;

import de.technikteam.dao.EventTaskCategoryDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.EventTaskCategory;
import de.technikteam.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/events/{eventId}/task-categories")
@Tag(name = "Admin Event Tasks", description = "Endpoints for managing event task categories.")
public class AdminEventTaskCategoryResource {

    private final EventTaskCategoryDAO categoryDAO;
    private final NotificationService notificationService;

    @Autowired
    public AdminEventTaskCategoryResource(EventTaskCategoryDAO categoryDAO, NotificationService notificationService) {
        this.categoryDAO = categoryDAO;
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get all task categories for an event")
    public ResponseEntity<ApiResponse> getCategories(@PathVariable int eventId) {
        List<EventTaskCategory> categories = categoryDAO.findByEventId(eventId);
        return ResponseEntity.ok(new ApiResponse(true, "Categories retrieved successfully.", categories));
    }

    @PostMapping
    @Operation(summary = "Create a new task category")
    public ResponseEntity<ApiResponse> createCategory(@PathVariable int eventId, @RequestBody EventTaskCategory category) {
        category.setEventId(eventId);
        EventTaskCategory createdCategory = categoryDAO.create(category);
        notificationService.broadcastUIUpdate("EVENT_TASK_CATEGORY", "CREATED", createdCategory);
        return new ResponseEntity<>(new ApiResponse(true, "Category created successfully.", createdCategory), HttpStatus.CREATED);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update a task category")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable int eventId, @PathVariable int categoryId, @RequestBody EventTaskCategory category) {
        category.setId(categoryId);
        category.setEventId(eventId);
        if (categoryDAO.update(category)) {
            notificationService.broadcastUIUpdate("EVENT_TASK_CATEGORY", "UPDATED", category);
            return ResponseEntity.ok(new ApiResponse(true, "Category updated successfully.", category));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete a task category")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable int eventId, @PathVariable int categoryId) {
        if (categoryDAO.delete(categoryId)) {
            notificationService.broadcastUIUpdate("EVENT_TASK_CATEGORY", "DELETED", Map.of("id", categoryId, "eventId", eventId));
            return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully.", null));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/reorder")
    @Operation(summary = "Update the display order of categories")
    public ResponseEntity<ApiResponse> reorderCategories(@PathVariable int eventId, @RequestBody Map<String, List<Integer>> payload) {
        List<Integer> categoryIds = payload.get("categoryIds");
        if (categoryIds == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Payload must contain 'categoryIds'.", null));
        }
        categoryDAO.updateOrder(eventId, categoryIds);
        notificationService.broadcastUIUpdate("EVENT", "UPDATED", Map.of("id", eventId));
        return ResponseEntity.ok(new ApiResponse(true, "Category order updated successfully.", null));
    }
}