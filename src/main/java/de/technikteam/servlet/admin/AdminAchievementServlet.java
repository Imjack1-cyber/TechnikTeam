package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.AchievementDAO;
import de.technikteam.dao.CourseDAO;
import de.technikteam.model.Achievement;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@WebServlet("/admin/achievements")
public class AdminAchievementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminAchievementServlet.class);
	private AchievementDAO achievementDAO;
	private CourseDAO courseDAO;
	private Gson gson;

	@Override
	public void init() {
		achievementDAO = new AchievementDAO();
		courseDAO = new CourseDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User currentUser = (User) request.getSession().getAttribute("user");
		Set<String> permissions = currentUser.getPermissions();

		if (!permissions.contains("ACHIEVEMENT_CREATE") && !permissions.contains("ACHIEVEMENT_UPDATE")
				&& !permissions.contains("ACHIEVEMENT_DELETE") && !permissions.contains("ACCESS_ADMIN_PANEL")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String action = request.getParameter("action");
		if ("getAchievementData".equals(action)) {
			getAchievementDataAsJson(request, response);
			return;
		}

		List<Achievement> achievements = achievementDAO.getAllAchievements();
		List<Course> allCourses = courseDAO.getAllCourses();
		request.setAttribute("achievements", achievements);
		request.setAttribute("allCourses", allCourses);
		request.getRequestDispatcher("/views/admin/admin_achievements.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		User adminUser = (User) request.getSession().getAttribute("user");

		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		Set<String> permissions = adminUser.getPermissions();

		String action = request.getParameter("action");
		switch (action) {
		case "create":
		case "update":
			handleCreateOrUpdate(request, response, adminUser);
			break;
		case "delete":
			handleDelete(request, response, adminUser);
			break;
		default:
			response.sendRedirect(request.getContextPath() + "/admin/achievements");
		}
	}

	private void getAchievementDataAsJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			int id = Integer.parseInt(request.getParameter("id"));
			Achievement achievement = achievementDAO.getAchievementById(id);
			if (achievement != null) {
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(gson.toJson(achievement));
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Achievement not found");
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		String idParam = request.getParameter("id");
		boolean isUpdate = idParam != null && !idParam.isEmpty();
		Set<String> permissions = adminUser.getPermissions();
		boolean hasMasterAccess = permissions.contains("ACCESS_ADMIN_PANEL");

		if ((isUpdate && !permissions.contains("ACHIEVEMENT_UPDATE") && !hasMasterAccess)
				|| (!isUpdate && !permissions.contains("ACHIEVEMENT_CREATE") && !hasMasterAccess)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Achievement achievement = new Achievement();
		achievement.setName(request.getParameter("name"));
		achievement.setDescription(request.getParameter("description"));
		achievement.setIconClass(request.getParameter("icon_class"));

		if (isUpdate) {
			achievement.setId(Integer.parseInt(idParam));
			if (achievementDAO.updateAchievement(achievement)) {
				AdminLogService.log(adminUser.getUsername(), "UPDATE_ACHIEVEMENT",
						"Erfolg '" + achievement.getName() + "' (ID: " + achievement.getId() + ") aktualisiert.");
				request.getSession().setAttribute("successMessage", "Erfolg erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Fehler beim Aktualisieren des Erfolgs.");
			}
		} else {
			// Read the generated key from the hidden input
			achievement.setAchievementKey(request.getParameter("achievement_key"));
			if (achievement.getAchievementKey() == null || achievement.getAchievementKey().trim().isEmpty()) {
				request.getSession().setAttribute("errorMessage",
						"Fehler: Der programmatische Key darf nicht leer sein.");
			} else if (achievementDAO.createAchievement(achievement)) {
				AdminLogService.log(adminUser.getUsername(), "CREATE_ACHIEVEMENT",
						"Erfolg '" + achievement.getName() + "' erstellt.");
				request.getSession().setAttribute("successMessage", "Neuer Erfolg erfolgreich erstellt.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"Fehler beim Erstellen des Erfolgs (Key bereits vorhanden?).");
			}
		}
		response.sendRedirect(request.getContextPath() + "/admin/achievements");
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		if (!adminUser.getPermissions().contains("ACHIEVEMENT_DELETE")
				&& !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			int id = Integer.parseInt(request.getParameter("id"));
			Achievement achievement = achievementDAO.getAchievementById(id);
			if (achievementDAO.deleteAchievement(id)) {
				AdminLogService.log(adminUser.getUsername(), "DELETE_ACHIEVEMENT", "Erfolg '"
						+ (achievement != null ? achievement.getName() : "N/A") + "' (ID: " + id + ") gelöscht.");
				request.getSession().setAttribute("successMessage", "Erfolg erfolgreich gelöscht.");
			} else {
				request.getSession().setAttribute("errorMessage", "Erfolg konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/achievements");
	}
}