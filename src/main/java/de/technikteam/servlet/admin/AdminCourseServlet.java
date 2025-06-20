// Pfad: src/main/java/de/technikteam/servlet/admin/AdminCourseServlet.java
package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.CourseDAO;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/courses")
public class AdminCourseServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminCourseServlet.class);
	private CourseDAO courseDAO;

	@Override
	public void init() {
		courseDAO = new CourseDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
		switch (action) {
		case "edit":
		case "new":
			showForm(req, resp);
			break;
		default:
			listCourses(req, resp);
			break;
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		if ("delete".equals(action)) {
			handleDelete(req, resp);
		} else {
			handleCreateOrUpdate(req, resp);
		}
	}

	private void listCourses(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<Course> courseList = courseDAO.getAllCourses();
		req.setAttribute("courseList", courseList);
		req.getRequestDispatcher("/admin/admin_course_list.jsp").forward(req, resp);
	}

	private void showForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if ("edit".equals(req.getParameter("action"))) {
			int courseId = Integer.parseInt(req.getParameter("id"));
			Course course = courseDAO.getCourseById(courseId);
			req.setAttribute("course", course);
		}
		req.getRequestDispatcher("/admin/admin_course_form.jsp").forward(req, resp);
	}

	/**
	 * Handles both creation and update of a course from the admin form.
	 * Differentiates based on the presence of a course ID.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws IOException If an I/O error occurs.
	 */
	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String idParam = request.getParameter("id");
		String name = request.getParameter("name");

		try {
			Course course = new Course();
			course.setName(name);
			course.setAbbreviation(request.getParameter("abbreviation")); // Assuming you add this field
			course.setType(request.getParameter("type"));
			course.setLeader(request.getParameter("leader"));
			course.setDescription(request.getParameter("description"));
			course.setCourseDateTime(LocalDateTime.parse(request.getParameter("courseDateTime")));

			boolean success = false;
			User adminUser = (User) request.getSession().getAttribute("user");

			if (idParam != null && !idParam.isEmpty()) { // Update
				if (courseDAO.updateCourse(course)) {
					AdminLogService.log(adminUser.getUsername(), "UPDATE_COURSE",
							"Lehrgang '" + course.getName() + "' (ID: " + course.getId() + ") aktualisiert.");
				}
			} else { // Create
				if (courseDAO.createCourse(course)) {
					AdminLogService.log(adminUser.getUsername(), "CREATE_COURSE",
							"Lehrgang '" + course.getName() + "' erstellt.");
				}
			}

			if (!success) {
				request.getSession().setAttribute("errorMessage",
						"Lehrgang konnte nicht gespeichert werden. Name oder Abkürzung existiert möglicherweise bereits.");
			}
		} catch (DateTimeParseException e) {
			logger.error("Invalid date format submitted for course.", e);
			request.getSession().setAttribute("errorMessage", "Ungültiges Datumsformat.");
		} catch (Exception e) {
			logger.error("Error during course creation/update.", e);
			request.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
		}

		response.sendRedirect(request.getContextPath() + "/admin/courses");
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int courseId = Integer.parseInt(req.getParameter("id"));
		Course course = courseDAO.getCourseById(courseId); // Namen holen
		if (courseDAO.deleteCourse(courseId)) {
			User adminUser = (User) req.getSession().getAttribute("user");
			AdminLogService.log(adminUser.getUsername(), "DELETE_COURSE",
					"Lehrgang '" + course.getName() + "' (ID: " + courseId + ") gelöscht.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/courses");
	}
}