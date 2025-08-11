package de.technikteam.api.v1;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/matrix")
@Tag(name = "Admin Matrix", description = "Endpoints for the qualification matrix.")
@SecurityRequirement(name = "bearerAuth")
public class MatrixResource {

	private final UserDAO userDAO;
	private final CourseDAO courseDAO;
	private final MeetingDAO meetingDAO;
	private final MeetingAttendanceDAO meetingAttendanceDAO;
	private final UserQualificationsDAO qualificationsDAO;

	@Autowired
	public MatrixResource(UserDAO userDAO, CourseDAO courseDAO, MeetingDAO meetingDAO,
			MeetingAttendanceDAO meetingAttendanceDAO, UserQualificationsDAO qualificationsDAO) {
		this.userDAO = userDAO;
		this.courseDAO = courseDAO;
		this.meetingDAO = meetingDAO;
		this.meetingAttendanceDAO = meetingAttendanceDAO;
		this.qualificationsDAO = qualificationsDAO;
	}

	@GetMapping
	@Operation(summary = "Get qualification matrix data")
	public ResponseEntity<ApiResponse> getMatrixData() {
		List<User> allUsers = userDAO.getAllUsers();
		List<Course> allCourses = courseDAO.getAllCourses();

		Map<Integer, List<Meeting>> meetingsByCourse = new HashMap<>();
		for (Course course : allCourses) {
			meetingsByCourse.put(course.getId(), meetingDAO.getMeetingsForCourse(course.getId()));
		}

		Map<String, MeetingAttendance> attendanceMap = meetingAttendanceDAO.getAllAttendance().stream()
				.collect(Collectors.toMap(a -> a.getUserId() + "-" + a.getMeetingId(), Function.identity()));

		Map<String, Boolean> completionMap = new HashMap<>();
		for (User user : allUsers) {
			for (Course course : allCourses) {
				boolean hasCompleted = qualificationsDAO.hasUserCompletedCourse(user.getId(), course.getId());
				completionMap.put(user.getId() + "-" + course.getId(), hasCompleted);
			}
		}

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("users", allUsers);
		responseData.put("courses", allCourses);
		responseData.put("meetingsByCourse", meetingsByCourse);
		responseData.put("attendanceMap", attendanceMap);
		responseData.put("completionMap", completionMap);

		return ResponseEntity.ok(new ApiResponse(true, "Matrixdaten erfolgreich abgerufen.", responseData));
	}

	@PutMapping("/attendance")
	@Operation(summary = "Update meeting attendance")
	public ResponseEntity<ApiResponse> updateAttendance(@RequestBody MeetingAttendance attendance) {
		boolean success = meetingAttendanceDAO.setAttendance(attendance.getUserId(), attendance.getMeetingId(),
				attendance.getAttended(), attendance.getRemarks());
		if (success) {
			return ResponseEntity.ok(new ApiResponse(true, "Teilnahme aktualisiert.", null));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Fehler beim Aktualisieren der Teilnahme.", null));
	}

	@PutMapping("/qualification")
	@Operation(summary = "Update a user's course qualification status")
	public ResponseEntity<ApiResponse> updateQualification(@RequestBody UserQualification qualification) {
		boolean success = qualificationsDAO.updateQualificationStatus(qualification.getUserId(),
				qualification.getCourseId(), qualification.getStatus(), qualification.getCompletionDate(),
				qualification.getRemarks());
		if (success) {
			return ResponseEntity.ok(new ApiResponse(true, "Qualifikationsstatus aktualisiert.", null));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Fehler beim Aktualisieren des Qualifikationsstatus.", null));
	}
}