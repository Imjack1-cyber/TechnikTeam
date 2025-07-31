package de.technikteam.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.Map;

@Service
public class ProfileRequestService {

	private final ProfileChangeRequestDAO requestDAO;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final Gson gson;

	@Autowired
	public ProfileRequestService(ProfileChangeRequestDAO requestDAO, UserDAO userDAO, AdminLogService adminLogService,
			NotificationService notificationService, Gson gson) {
		this.requestDAO = requestDAO;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
		this.gson = gson;
	}

	@Transactional
	public boolean approveRequest(int requestId, User adminUser) {
		ProfileChangeRequest pcr = requestDAO.getRequestById(requestId);
		if (pcr == null || !"PENDING".equals(pcr.getStatus())) {
			throw new IllegalStateException("Request not found or has already been processed.");
		}

		User userToUpdate = userDAO.getUserById(pcr.getUserId());
		if (userToUpdate == null) {
			requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId());
			throw new IllegalStateException("The associated user no longer exists. Request has been denied.");
		}

		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, String> changes = gson.fromJson(pcr.getRequestedChanges(), type);

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

		if (userDAO.updateUser(userToUpdate)
				&& requestDAO.updateRequestStatus(requestId, "APPROVED", adminUser.getId())) {
			adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_APPROVED_API", "Profile change for '"
					+ userToUpdate.getUsername() + "' (Request ID: " + requestId + ") approved via API.");

			String notificationMessage = "Your profile change has been approved.";
			Map<String, Object> payload = Map.of("type", "alert", "payload", Map.of("message", notificationMessage));
			notificationService.sendNotificationToUser(userToUpdate.getId(), payload);

			return true;
		}
		return false;
	}

	@Transactional
	public boolean denyRequest(int requestId, User adminUser) {
		ProfileChangeRequest pcr = requestDAO.getRequestById(requestId);
		if (pcr == null || !"PENDING".equals(pcr.getStatus())) {
			throw new IllegalStateException("Request not found or has already been processed.");
		}

		if (requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId())) {
			adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_DENIED_API", "Profile change for user ID "
					+ pcr.getUserId() + " (Request ID: " + requestId + ") denied via API.");
			return true;
		}
		return false;
	}
}