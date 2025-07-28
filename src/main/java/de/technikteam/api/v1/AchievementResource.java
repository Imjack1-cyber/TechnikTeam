// src/main/java/de/technikteam/api/v1/AchievementResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.dao.AchievementDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Achievement;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A stateless, resource-oriented REST API endpoint for managing achievements.
 * Mapped to /api/v1/achievements/*
 */
@Singleton
public class AchievementResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AchievementResource.class);

	private final AchievementDAO achievementDAO;
	private final AdminLogService adminLogService;
	private final Gson gson;

	@Inject
	public AchievementResource(AchievementDAO achievementDAO, AdminLogService adminLogService, Gson gson) {
		this.achievementDAO = achievementDAO;
		this.adminLogService = adminLogService;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || (!adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)
				&& !adminUser.getPermissions().contains(Permissions.ACHIEVEMENT_VIEW))) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		try {
			if (pathInfo == null || pathInfo.equals("/")) {
				List<Achievement> achievements = achievementDAO.getAllAchievements();
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Achievements retrieved successfully", achievements));
			} else {
				Integer achievementId = parseIdFromPath(pathInfo);
				if (achievementId != null) {
					Achievement achievement = achievementDAO.getAchievementById(achievementId);
					if (achievement != null) {
						sendJsonResponse(resp, HttpServletResponse.SC_OK,
								new ApiResponse(true, "Achievement retrieved successfully", achievement));
					} else {
						sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Achievement not found");
					}
				} else {
					sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid achievement ID format");
				}
			}
		} catch (Exception e) {
			logger.error("Error processing GET request for achievements", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || (!adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)
				&& !adminUser.getPermissions().contains(Permissions.ACHIEVEMENT_CREATE))) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Achievement newAchievement = gson.fromJson(jsonPayload, Achievement.class);

			if (newAchievement.getAchievementKey() == null || newAchievement.getAchievementKey().isBlank()) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Achievement key cannot be empty.");
				return;
			}

			if (achievementDAO.createAchievement(newAchievement)) {
				adminLogService.log(adminUser.getUsername(), "CREATE_ACHIEVEMENT_API",
						"Achievement '" + newAchievement.getName() + "' created via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
						new ApiResponse(true, "Achievement created successfully", newAchievement));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
						"Could not create achievement (key may already exist).");
			}
		} catch (JsonSyntaxException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing POST request to create achievement", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || (!adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)
				&& !adminUser.getPermissions().contains(Permissions.ACHIEVEMENT_UPDATE))) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer achievementId = parseIdFromPath(req.getPathInfo());
		if (achievementId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid achievement ID in URL.");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Achievement updatedAchievement = gson.fromJson(jsonPayload, Achievement.class);
			updatedAchievement.setId(achievementId); // Ensure ID from URL is used

			if (achievementDAO.updateAchievement(updatedAchievement)) {
				adminLogService.log(adminUser.getUsername(), "UPDATE_ACHIEVEMENT_API", "Achievement '"
						+ updatedAchievement.getName() + "' (ID: " + achievementId + ") updated via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Achievement updated successfully", updatedAchievement));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Achievement not found or update failed.");
			}
		} catch (JsonSyntaxException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing PUT request to update achievement", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || (!adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)
				&& !adminUser.getPermissions().contains(Permissions.ACHIEVEMENT_DELETE))) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer achievementId = parseIdFromPath(req.getPathInfo());
		if (achievementId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid achievement ID in URL.");
			return;
		}

		Achievement achievementToDelete = achievementDAO.getAchievementById(achievementId);
		if (achievementToDelete == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Achievement to delete not found.");
			return;
		}

		if (achievementDAO.deleteAchievement(achievementId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_ACHIEVEMENT_API",
					"Achievement '" + achievementToDelete.getName() + "' (ID: " + achievementId + ") deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Achievement deleted successfully", Map.of("deletedId", achievementId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete achievement.");
		}
	}

	private Integer parseIdFromPath(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1)
			return null;
		try {
			return Integer.parseInt(pathInfo.substring(1));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}

	private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
		sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
	}
}