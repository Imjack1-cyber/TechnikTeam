package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import de.technikteam.dao.CourseDAO;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 
 * Mapped to /WEB-INF/views/admin/admin_course_list.jsp, this servlet manages the parent course templates.
 * 
 * It handles listing all course templates, and processing the creation, update,
 * 
 * and deletion of these templates, which are now managed via modal dialogs on
 * 
 * the list page.
 */
@WebServlet("/admin/lehrgaenge")
public class AdminCourseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminCourseServlet.class);
	private CourseDAO courseDAO;
	private Gson gson = new Gson();

	@Override
	public void init() {
		courseDAO = new CourseDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		if ("getCourseData".equals(action)) {
			getCourseDataAsJson(req, resp);
			return;
		}

		logger.info("Listing all course templates for admin view.");
		List<Course> courseList = courseDAO.getAllCourses();
		req.setAttribute("courseList", courseList);
		req.getRequestDispatcher("/admin/lehrgaenge").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		logger.debug("AdminCourseServlet received POST request with action: {}", action);
		if ("delete".equals(action)) {
			handleDelete(req, resp);
		} else if ("create".equals(action) || "update".equals(action)) {
			handleCreateOrUpdate(req, resp);
		} else {
			logger.warn("Unknown POST action received: {}", action);
			resp.sendRedirect(req.getContextPath() + "/admin/lehrgaenge");
		}
	}

	private void getCourseDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int courseId = Integer.parseInt(req.getParameter("id"));
			Course course = courseDAO.getCourseById(courseId);
			if (course != null) {
				String courseJson = gson.toJson(course);
				resp.setContentType("application/json");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(courseJson);
			} else {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
			}
		} catch (NumberFormatException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		String idParam = request.getParameter("id");
		Course course = new Course();
		course.setName(request.getParameter("name"));
		course.setAbbreviation(request.getParameter("abbreviation"));
		course.setDescription(request.getParameter("description"));

		boolean success;
		if (idParam != null && !idParam.isEmpty()) { // UPDATE
			course.setId(Integer.parseInt(idParam));
			logger.info("Attempting to update course: {}", course.getName());
			Course originalCourse = courseDAO.getCourseById(course.getId());
			success = courseDAO.updateCourse(course);
			if (success) {
				StringBuilder changes = new StringBuilder();
				if (!Objects.equals(originalCourse.getName(), course.getName())) {
					changes.append(String.format("Name: '%s' -> '%s'. ", originalCourse.getName(), course.getName()));
				}
				if (!Objects.equals(originalCourse.getAbbreviation(), course.getAbbreviation())) {
					changes.append(String.format("Abk.: '%s' -> '%s'. ", originalCourse.getAbbreviation(),
							course.getAbbreviation()));
				}
				String logDetails = String.format("Lehrgangs-Vorlage '%s' (ID: %d) aktualisiert. %s",
						originalCourse.getName(), course.getId(), changes.toString());
				AdminLogService.log(adminUser.getUsername(), "UPDATE_COURSE", logDetails);
				request.getSession().setAttribute("successMessage", "Lehrgangs-Vorlage erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Fehler beim Aktualisieren der Vorlage.");
			}
		} else { // CREATE
			logger.info("Attempting to create new course: {}", course.getName());
			success = courseDAO.createCourse(course);
			if (success) {
				String logDetails = String.format("Lehrgangs-Vorlage '%s' (Abk.: %s) erstellt.", course.getName(),
						course.getAbbreviation());
				AdminLogService.log(adminUser.getUsername(), "CREATE_COURSE", logDetails);
				request.getSession().setAttribute("successMessage", "Neue Lehrgangs-Vorlage erfolgreich erstellt.");
			} else {
				request.getSession().setAttribute("errorMessage", "Fehler beim Erstellen der Vorlage.");
			}
		}
		response.sendRedirect(request.getContextPath() + "/admin/lehrgaenge");
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		try {
			int courseId = Integer.parseInt(req.getParameter("id"));
			logger.warn("Attempting to delete course with ID: {}", courseId);
			// Fetch course details before deleting for detailed logging
			Course courseToDelete = courseDAO.getCourseById(courseId);
			String courseName = (courseToDelete != null) ? courseToDelete.getName() : "N/A";

			if (courseDAO.deleteCourse(courseId)) {
				AdminLogService.log(adminUser.getUsername(), "DELETE_COURSE",
						"Lehrgangs-Vorlage '" + courseName + "' (ID: " + courseId
								+ ") und alle zugehörigen Meetings, Anhänge und Qualifikationen gelöscht.");
				req.getSession().setAttribute("successMessage", "Lehrgangs-Vorlage erfolgreich gelöscht.");
			} else {
				req.getSession().setAttribute("errorMessage", "Vorlage konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid course ID format for deletion.", e);
			req.getSession().setAttribute("errorMessage", "Ungültige ID für Löschvorgang.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/lehrgaenge");
	}
}