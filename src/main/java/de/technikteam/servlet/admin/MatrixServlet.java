package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.technikteam.config.LocalDateAdapter;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.model.UserQualification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/matrix")
public class MatrixServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO userDAO;
	private CourseDAO courseDAO;
	private UserQualificationsDAO userQualificationsDAO;
	private Gson gson; // It's slightly more efficient to create the Gson object once.

	@Override
	public void init() {
		userDAO = new UserDAO();
		courseDAO = new CourseDAO();
		userQualificationsDAO = new UserQualificationsDAO();

		// Build the custom Gson instance once when the servlet is initialized.
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
		this.gson = gsonBuilder.create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        
		List<User> allUsers = userDAO.getAllUsers();
		List<Course> allCourses = courseDAO.getAllCourses();
		List<UserQualification> allQualifications = userQualificationsDAO.getAllQualifications();

		// Create the Java Map.
		Map<String, UserQualification> qualificationMap = allQualifications.stream()
				.collect(Collectors.toMap(q -> q.getUserId() + "-" + q.getCourseId(), Function.identity()));
		
		// Create the JSON String from the map.
		String qualificationMapJson = this.gson.toJson(qualificationMap);
		
        // --- THE FIX IS HERE ---
        // Pass BOTH the Java Map (for JSP rendering) AND the JSON String (for JavaScript).
		request.setAttribute("allUsers", allUsers);
		request.setAttribute("allCourses", allCourses);
		request.setAttribute("qualificationMap", qualificationMap); // This line was missing.
		request.setAttribute("qualificationMapJson", qualificationMapJson);

		request.getRequestDispatcher("/admin/admin_matrix.jsp").forward(request, response);
	}
}