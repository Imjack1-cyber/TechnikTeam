package de.technikteam.servlet.admin.action;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import de.technikteam.servlet.http.SessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class ApproveChangeAction implements Action {
	private final ProfileChangeRequestDAO requestDAO = new ProfileChangeRequestDAO();
	private final UserDAO userDAO = new UserDAO();
	private final Gson gson = new Gson();

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (!adminUser.getPermissions().contains("USER_UPDATE") && !adminUser.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		try {
			int requestId = Integer.parseInt(request.getParameter("requestId"));
			ProfileChangeRequest req = requestDAO.getRequestById(requestId);
			if (req == null || !"PENDING".equals(req.getStatus())) {
				return ApiResponse.error("Anfrage nicht gefunden oder bereits bearbeitet.");
			}

			User userToUpdate = userDAO.getUserById(req.getUserId());
			if (userToUpdate == null) {
				requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId());
				return ApiResponse.error("Der zugehörige Benutzer existiert nicht mehr. Anfrage abgelehnt.");
			}

			// Apply changes from JSON
			Type type = new TypeToken<Map<String, String>>() {
			}.getType();
			Map<String, String> changes = gson.fromJson(req.getRequestedChanges(), type);

			changes.forEach((field, value) -> {
				switch (field) {
				case "username":
					userToUpdate.setUsername(value);
					break;
				case "email":
					userToUpdate.setEmail(value);
					break;
				case "classYear":
					userToUpdate.setClassYear(Integer.parseInt(value));
					break;
				case "className":
					userToUpdate.setClassName(value);
					break;
				}
			});

			if (userDAO.updateUser(userToUpdate)
					&& requestDAO.updateRequestStatus(requestId, "APPROVED", adminUser.getId())) {
				AdminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_APPROVED", "Profiländerung für '"
						+ userToUpdate.getUsername() + "' (Request ID: " + requestId + ") genehmigt.");

				// Notify the user BEFORE invalidating their session.
				String notificationMessage = "Ihre Profiländerung wurde genehmigt. Sie werden zur Sicherheit abgemeldet. Bitte loggen Sie sich erneut ein.";
				Map<String, Object> payload = Map.of("type", "logout_notification", "payload",
						Map.of("message", notificationMessage));
				NotificationService.getInstance().sendNotificationToUser(userToUpdate.getId(), payload);

				// Invalidate the user's session to force them to log in again and see the
				// changes.
				SessionManager.invalidateSessionsForUser(userToUpdate.getId());

				return ApiResponse.success("Änderungsanfrage genehmigt.", Map.of("requestId", requestId));
			} else {
				return ApiResponse.error("Fehler beim Anwenden der Änderungen oder beim Aktualisieren der Anfrage.");
			}

		} catch (Exception e) {
			return ApiResponse.error("Ein interner Fehler ist aufgetreten: " + e.getMessage());
		}
	}
}