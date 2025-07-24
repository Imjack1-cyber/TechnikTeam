package de.technikteam.servlet.admin.action;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

@Singleton
public class ApproveChangeAction implements Action {
	private final ProfileChangeRequestDAO requestDAO;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final Gson gson = new Gson();

	@Inject
	public ApproveChangeAction(ProfileChangeRequestDAO requestDAO, UserDAO userDAO, AdminLogService adminLogService) {
		this.requestDAO = requestDAO;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
	}

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

			Type type = new TypeToken<Map<String, String>>() {
			}.getType();
			Map<String, String> changes = gson.fromJson(req.getRequestedChanges(), type);

			changes.forEach((field, value) -> {
				switch (field) {
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

			// CORRECTED: Call the version of updateUser that does not require a Connection
			// object.
			if (userDAO.updateUser(userToUpdate)
					&& requestDAO.updateRequestStatus(requestId, "APPROVED", adminUser.getId())) {
				adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_APPROVED", "Profiländerung für '"
						+ userToUpdate.getUsername() + "' (Request ID: " + requestId + ") genehmigt.");

				String notificationMessage = "Ihre Profiländerung wurde genehmigt. Sie werden zur Sicherheit abgemeldet. Bitte loggen Sie sich erneut ein.";
				Map<String, Object> payload = Map.of("type", "logout_notification", "payload",
						Map.of("message", notificationMessage));
				NotificationService.getInstance().sendNotificationToUser(userToUpdate.getId(), payload);

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