package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.CourseDAO;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class AdminCourseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminCourseServlet.class);
	private final CourseDAO courseDAO;
	private final AdminLogService adminLogService;
	private final Gson gson = new Gson();

	@Inject
	public AdminCourseServlet(CourseDAO courseDAO, AdminLogService adminLogService) {
		this.courseDAO = courseDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		if ("getCourseData".equals(action)) {
			getCourseDataAsJson(req, resp);
			return;
		}
		List<Course> courseList = courseDAO.getAllCourses();
		req.setAttribute("courseList", courseList);
		req.getRequestDispatcher("/views/admin/admin_course_list.jsp").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		if (!CSRFUtil.isTokenValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}
		String action = req.getParameter("action");
		switch (action) {
		case "delete":
			handleDelete(req, resp);
			break;
		case "create":
		case "update":
			handleCreateOrUpdate(req, resp);
			break;
		default:
			resp.sendRedirect(req.getContextPath() + "/admin/lehrgaenge");
			break;
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

		if (idParam != null && !idParam.isEmpty()) {
			course.setId(Integer.parseInt(idParam));
			Course originalCourse = courseDAO.getCourseById(course.getId());
			if (courseDAO.updateCourse(course) && originalCourse != null) {
				String logDetails = String.format("Lehrgangs-Vorlage '%s' (ID: %d) aktualisiert. ",
						originalCourse.getName(), course.getId());
				adminLogService.log(adminUser.getUsername(), "UPDATE_COURSE", logDetails);
				request.getSession().setAttribute("successMessage", "Lehrgangs-Vorlage erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Fehler beim Aktualisieren der Vorlage.");
			}
		} else {
			if (courseDAO.createCourse(course)) {
				String logDetails = String.format("Lehrgangs-Vorlage '%s' (Abk.: %s) erstellt.", course.getName(),
						course.getAbbreviation());
				adminLogService.log(adminUser.getUsername(), "CREATE_COURSE", logDetails);
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
			Course courseToDelete = courseDAO.getCourseById(courseId);
			String courseName = (courseToDelete != null) ? courseToDelete.getName() : "N/A";
			if (courseDAO.deleteCourse(courseId)) {
				adminLogService.log(adminUser.getUsername(), "DELETE_COURSE",
						"Lehrgangs-Vorlage '" + courseName + "' (ID: " + courseId
								+ ") und alle zugehörigen Meetings, Anhänge und Qualifikationen gelöscht.");
				req.getSession().setAttribute("successMessage", "Lehrgangs-Vorlage erfolgreich gelöscht.");
			} else {
				req.getSession().setAttribute("errorMessage", "Vorlage konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			req.getSession().setAttribute("errorMessage", "Ungültige ID für Löschvorgang.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/lehrgaenge");
	}
}