package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.MeetingRequest;
import de.technikteam.dao.CourseDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
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
public class CourseResource {

	private final CourseDAO courseDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;

	@Autowired
	public CourseResource(CourseDAO courseDAO, AdminLogService adminLogService, NotificationService notificationService) {
		this.courseDAO = courseDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
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
			@AuthenticationPrincipal SecurityUser securityUser) {
		Course createdCourse = courseDAO.createCourse(course);
		if (createdCourse != null) {
			adminLogService.logCourseCreation(securityUser.getUser().getUsername(), createdCourse);
			notificationService.broadcastUIUpdate("COURSE", "CREATED", createdCourse);
			return new ResponseEntity<>(new ApiResponse(true, "Lehrgang erfolgreich erstellt.", createdCourse),
					HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Lehrgang konnte nicht erstellt werden.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a course template")
	public ResponseEntity<ApiResponse> updateCourse(@PathVariable int id, @RequestBody Course course,
			@AuthenticationPrincipal SecurityUser securityUser) {
		Course originalCourse = courseDAO.getCourseById(id);
		if (originalCourse == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Vorlage nicht gefunden.", null));
		}
		course.setId(id);
		if (courseDAO.updateCourse(course)) {
			adminLogService.logCourseUpdate(securityUser.getUser().getUsername(), originalCourse, course);
			notificationService.broadcastUIUpdate("COURSE", "UPDATED", course);
			return ResponseEntity.ok(new ApiResponse(true, "Lehrgang erfolgreich aktualisiert.", course));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Lehrgang nicht gefunden oder Aktualisierung fehlgeschlagen.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a course template")
	public ResponseEntity<ApiResponse> deleteCourse(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		Course course = courseDAO.getCourseById(id);
		if (course != null && courseDAO.deleteCourse(id)) {
			adminLogService.logCourseDeletion(securityUser.getUser().getUsername(), course);
			notificationService.broadcastUIUpdate("COURSE", "DELETED", Map.of("id", id));
			return ResponseEntity.ok(new ApiResponse(true, "Lehrgang erfolgreich gelöscht.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Lehrgang nicht gefunden oder Löschung fehlgeschlagen.", null));
	}
}