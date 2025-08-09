package de.technikteam.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.technikteam.api.v1.dto.ProfileChangeRequestDTO;
import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ProfileRequestService {
	private static final Logger logger = LogManager.getLogger(ProfileRequestService.class);

	private final ProfileChangeRequestDAO requestDAO;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;

	@Autowired
	public ProfileRequestService(ProfileChangeRequestDAO requestDAO, UserDAO userDAO, AdminLogService adminLogService,
			NotificationService notificationService) {
		this.requestDAO = requestDAO;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
	}

	@Transactional
	public void createChangeRequest(User currentUser, ProfileChangeRequestDTO requestDTO) throws IOException {
		Map<String, String> changes = new HashMap<>();
		if (requestDTO.email() != null && !Objects.equals(currentUser.getEmail(), requestDTO.email())) {
			changes.put("email", requestDTO.email());
		}
		if (requestDTO.classYear() != null && currentUser.getClassYear() != requestDTO.classYear()) {
			changes.put("classYear", String.valueOf(requestDTO.classYear()));
		}
		if (requestDTO.className() != null && !Objects.equals(currentUser.getClassName(), requestDTO.className())) {
			changes.put("className", requestDTO.className());
		}
		if (requestDTO.profileIconClass() != null
				&& !Objects.equals(currentUser.getProfileIconClass(), requestDTO.profileIconClass())) {
			changes.put("profileIconClass", requestDTO.profileIconClass());
		}

		if (changes.isEmpty()) {
			return; // No changes to request
		}

		ProfileChangeRequest pcr = new ProfileChangeRequest();
		pcr.setUserId(currentUser.getId());
		pcr.setRequestedChanges(new Gson().toJson(changes));

		if (!requestDAO.createRequest(pcr)) {
			throw new IOException("Ihr Antrag konnte nicht in der Datenbank gespeichert werden.");
		}
	}

	@Transactional
	public boolean approveRequest(int requestId, User adminUser) throws IOException {
		ProfileChangeRequest pcr = requestDAO.getRequestById(requestId);
		if (pcr == null || !"PENDING".equals(pcr.getStatus())) {
			throw new IllegalStateException("Antrag nicht gefunden oder bereits bearbeitet.");
		}

		User userToUpdate = userDAO.getUserById(pcr.getUserId());
		if (userToUpdate == null) {
			requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId());
			throw new IllegalStateException(
					"Der zugehörige Benutzer existiert nicht mehr. Der Antrag wurde abgelehnt.");
		}

		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, String> changes = new Gson().fromJson(pcr.getRequestedChanges(), type);

		for (Map.Entry<String, String> entry : changes.entrySet()) {
			String field = entry.getKey();
			String value = entry.getValue();
			switch (field) {
			case "email":
				// Prevent unique constraint violation for empty strings
				userToUpdate.setEmail("".equals(value) ? null : value);
				break;
			case "classYear":
				userToUpdate.setClassYear(Integer.parseInt(value));
				break;
			case "className":
				userToUpdate.setClassName(value);
				break;
			case "profileIconClass":
				userToUpdate.setProfileIconClass(value);
				break;
			}
		}
		try {
			if (userDAO.updateUser(userToUpdate)
					&& requestDAO.updateRequestStatus(requestId, "APPROVED", adminUser.getId())) {
				adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_APPROVED_API", "Profile change for '"
						+ userToUpdate.getUsername() + "' (Request ID: " + requestId + ") approved via API.");

				String notificationTitle = "Profiländerung genehmigt";
				String notificationMessage = "Ihre beantragte Profiländerung wurde von einem Administrator genehmigt.";
				Map<String, Object> payload = Map.of("title", notificationTitle, "description", notificationMessage,
						"level", "Informational", "url", "/profil");
				notificationService.sendNotificationToUser(userToUpdate.getId(), payload);

				return true;
			}
		} catch (DuplicateKeyException e) {
			logger.warn("Failed to approve request {}: {}", requestId, e.getMessage());
			throw new IllegalStateException(
					"Die Genehmigung ist fehlgeschlagen, da die angeforderte E-Mail-Adresse bereits von einem anderen Konto verwendet wird.");
		}
		return false;
	}

	@Transactional
	public boolean denyRequest(int requestId, User adminUser) throws IOException {
		ProfileChangeRequest pcr = requestDAO.getRequestById(requestId);
		if (pcr == null || !"PENDING".equals(pcr.getStatus())) {
			throw new IllegalStateException("Antrag nicht gefunden oder bereits bearbeitet.");
		}

		if (requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId())) {
			adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_DENIED_API", "Profile change for user ID "
					+ pcr.getUserId() + " (Request ID: " + requestId + ") denied via API.");
			return true;
		}
		return false;
	}
}