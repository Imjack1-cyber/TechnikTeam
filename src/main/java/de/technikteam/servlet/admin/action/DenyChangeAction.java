package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@Singleton
public class DenyChangeAction implements Action {
	private final ProfileChangeRequestDAO requestDAO;
	private final AdminLogService adminLogService;

	@Inject
	public DenyChangeAction(ProfileChangeRequestDAO requestDAO, AdminLogService adminLogService) {
		this.requestDAO = requestDAO;
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

			if (requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId())) {
				adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_DENIED", "Profiländerung für Benutzer-ID "
						+ req.getUserId() + " (Request ID: " + requestId + ") abgelehnt.");
				return ApiResponse.success("Änderungsanfrage abgelehnt.", Map.of("requestId", requestId));
			} else {
				return ApiResponse.error("Fehler beim Ablehnen der Anfrage.");
			}
		} catch (NumberFormatException e) {
			return ApiResponse.error("Ungültige Anfrage-ID.");
		}
	}
}