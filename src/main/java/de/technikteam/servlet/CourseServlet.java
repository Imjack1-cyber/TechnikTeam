package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.CourseDAO;
import de.technikteam.model.Course;
import de.technikteam.model.User;

@WebServlet("/lehrgaenge")
public class CourseServlet extends HttpServlet {
	private CourseDAO courseDAO;

	@Override
	public void init() {
		courseDAO = new CourseDAO();
	}

	// Modify doGet in src/main/java/de/technikteam/servlet/CourseServlet.java
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		List<Course> courses = courseDAO.getAllUpcomingCourses(); // Assuming this is already there
		List<Course> attendedCourses = courseDAO.getAttendedCoursesByUser(user.getId());

		request.setAttribute("courses", courses);
		request.setAttribute("attendedCourseIds",
				attendedCourses.stream().map(Course::getId).collect(Collectors.toSet()));

		request.getRequestDispatcher("/lehrgaenge.jsp").forward(request, response);
	}
}