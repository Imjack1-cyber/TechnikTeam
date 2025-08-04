package de.technikteam.api.v1;

import de.technikteam.dao.CourseDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Admin Courses", description = "Endpoints for managing course templates.")
public class CourseResource {

	private final CourseDAO courseDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public CourseResource(CourseDAO courseDAO, AdminLogService adminLogService) {
		this.courseDAO = courseDAO;
		this.adminLogService = adminLogService;
	}

	private User getSystemUser() {
		User user = new User();
		user.setId(0);
		user.setUsername("SYSTEM");
		return user;
	}

	@GetMapping
	@Operation(summary = "Get all course templates")
	public ResponseEntity<ApiResponse> getAllCourses() {
		List<Course> courses = courseDAO.getAllCourses();
		return ResponseEntity.ok(new ApiResponse(true, "Lehrgänge erfolgreich abgerufen.", courses));
	}

	@PostMapping
	@Operation(summary = "Create a new course template")
	public ResponseEntity<ApiResponse> createCourse(@RequestBody Course course) {
		if (courseDAO.createCourse(course)) {
			adminLogService.log(getSystemUser().getUsername(), "CREATE_COURSE_API",
					"Course '" + course.getName() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Lehrgang erfolgreich erstellt.", course),
					HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Lehrgang konnte nicht erstellt werden.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a course template")
	public ResponseEntity<ApiResponse> updateCourse(@PathVariable int id, @RequestBody Course course) {
		course.setId(id);
		if (courseDAO.updateCourse(course)) {
			adminLogService.log(getSystemUser().getUsername(), "UPDATE_COURSE_API",
					"Course '" + course.getName() + "' updated.");
			return ResponseEntity.ok(new ApiResponse(true, "Lehrgang erfolgreich aktualisiert.", course));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Lehrgang nicht gefunden oder Aktualisierung fehlgeschlagen.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a course template")
	public ResponseEntity<ApiResponse> deleteCourse(@PathVariable int id) {
		Course course = courseDAO.getCourseById(id);
		if (course != null && courseDAO.deleteCourse(id)) {
			adminLogService.log(getSystemUser().getUsername(), "DELETE_COURSE_API",
					"Course '" + course.getName() + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Lehrgang erfolgreich gelöscht.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Lehrgang nicht gefunden oder Löschung fehlgeschlagen.", null));
	}
}