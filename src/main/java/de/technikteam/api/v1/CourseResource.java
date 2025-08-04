package de.technikteam.api.v1;

import de.technikteam.dao.CourseDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
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
@RequestMapping("/api/v1/courses")
@Tag(name = "Admin Courses", description = "Endpoints for managing course templates.")
@SecurityRequirement(name = "bearerAuth")
public class CourseResource {

	private final CourseDAO courseDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public CourseResource(CourseDAO courseDAO, AdminLogService adminLogService) {
		this.courseDAO = courseDAO;
		this.adminLogService = adminLogService;
	}

	@GetMapping
	@Operation(summary = "Get all course templates")
	public ResponseEntity<ApiResponse> getAllCourses() {
		List<Course> courses = courseDAO.getAllCourses();
		return ResponseEntity.ok(new ApiResponse(true, "Lehrgänge erfolgreich abgerufen.", courses));
	}

	@PostMapping
	@Operation(summary = "Create a new course template")
	public ResponseEntity<ApiResponse> createCourse(@RequestBody Course course,
			@AuthenticationPrincipal User adminUser) {
		if (courseDAO.createCourse(course)) {
			adminLogService.log(adminUser.getUsername(), "CREATE_COURSE_API",
					"Course '" + course.getName() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Lehrgang erfolgreich erstellt.", course),
					HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Lehrgang konnte nicht erstellt werden.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a course template")
	public ResponseEntity<ApiResponse> updateCourse(@PathVariable int id, @RequestBody Course course,
			@AuthenticationPrincipal User adminUser) {
		course.setId(id);
		if (courseDAO.updateCourse(course)) {
			adminLogService.log(adminUser.getUsername(), "UPDATE_COURSE_API",
					"Course '" + course.getName() + "' updated.");
			return ResponseEntity.ok(new ApiResponse(true, "Lehrgang erfolgreich aktualisiert.", course));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Lehrgang nicht gefunden oder Aktualisierung fehlgeschlagen.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a course template")
	public ResponseEntity<ApiResponse> deleteCourse(@PathVariable int id, @AuthenticationPrincipal User adminUser) {
		Course course = courseDAO.getCourseById(id);
		if (course != null && courseDAO.deleteCourse(id)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_COURSE_API",
					"Course '" + course.getName() + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Lehrgang erfolgreich gelöscht.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Lehrgang nicht gefunden oder Löschung fehlgeschlagen.", null));
	}
}