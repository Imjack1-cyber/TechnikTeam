package de.technikteam.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.technikteam.api.v1.dto.ProfileChangeRequestDTO;
import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import de.technikteam.util.FileSignatureValidator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProfileRequestService {

	private final ProfileChangeRequestDAO requestDAO;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final ConfigurationService configService;
	private final Gson gson;

	@Autowired
	public ProfileRequestService(ProfileChangeRequestDAO requestDAO, UserDAO userDAO, AdminLogService adminLogService,
			NotificationService notificationService, ConfigurationService configService, Gson gson) {
		this.requestDAO = requestDAO;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
		this.configService = configService;
		this.gson = gson;
	}

	@Transactional
	public void createChangeRequest(User currentUser, ProfileChangeRequestDTO requestDTO, MultipartFile profilePicture)
			throws IOException {
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

		if (profilePicture != null && !profilePicture.isEmpty()) {
			if (!FileSignatureValidator.isFileTypeAllowed(profilePicture)) {
				throw new IOException("Invalid profile picture file type. Only JPG and PNG are allowed.");
			}
			String tempPath = saveTemporaryProfilePicture(profilePicture);
			changes.put("profilePicturePath", tempPath);
		}

		if (changes.isEmpty()) {
			return; // No changes to request
		}

		ProfileChangeRequest pcr = new ProfileChangeRequest();
		pcr.setUserId(currentUser.getId());
		pcr.setRequestedChanges(gson.toJson(changes));

		if (!requestDAO.createRequest(pcr)) {
			throw new IOException("Could not save your request to the database.");
		}
	}

	@Transactional
	public boolean approveRequest(int requestId, User adminUser) throws IOException {
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

		for (Map.Entry<String, String> entry : changes.entrySet()) {
			String field = entry.getKey();
			String value = entry.getValue();
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
			case "profilePicturePath":
				String permanentPath = makeProfilePicturePermanent(value, userToUpdate.getProfilePicturePath());
				userToUpdate.setProfilePicturePath(permanentPath);
				break;
			}
		}

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
	public boolean denyRequest(int requestId, User adminUser) throws IOException {
		ProfileChangeRequest pcr = requestDAO.getRequestById(requestId);
		if (pcr == null || !"PENDING".equals(pcr.getStatus())) {
			throw new IllegalStateException("Request not found or has already been processed.");
		}

		// If the request included a temporary file, delete it
		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, String> changes = gson.fromJson(pcr.getRequestedChanges(), type);
		if (changes.containsKey("profilePicturePath")) {
			deleteTemporaryProfilePicture(changes.get("profilePicturePath"));
		}

		if (requestDAO.updateRequestStatus(requestId, "DENIED", adminUser.getId())) {
			adminLogService.log(adminUser.getUsername(), "PROFILE_CHANGE_DENIED_API", "Profile change for user ID "
					+ pcr.getUserId() + " (Request ID: " + requestId + ") denied via API.");
			return true;
		}
		return false;
	}

	private String saveTemporaryProfilePicture(MultipartFile file) throws IOException {
		Path uploadDir = Paths.get(configService.getProperty("upload.directory"));
		Path tempDir = uploadDir.resolve("temp_avatars");
		Files.createDirectories(tempDir);

		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		String newFilename = UUID.randomUUID().toString() + "." + extension;
		Path targetPath = tempDir.resolve(newFilename);
		Files.copy(file.getInputStream(), targetPath);
		return newFilename;
	}

	private String makeProfilePicturePermanent(String tempFilename, String oldPermanentFilename) throws IOException {
		Path uploadDir = Paths.get(configService.getProperty("upload.directory"));
		Path tempDir = uploadDir.resolve("temp_avatars");
		Path finalDir = uploadDir.resolve("avatars");
		Files.createDirectories(finalDir);

		Path sourcePath = tempDir.resolve(tempFilename);
		Path destinationPath = finalDir.resolve(tempFilename);

		if (Files.exists(sourcePath)) {
			Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

			// Delete the old profile picture if it exists
			if (oldPermanentFilename != null && !oldPermanentFilename.isBlank()) {
				Path oldPath = finalDir.resolve(oldPermanentFilename);
				Files.deleteIfExists(oldPath);
			}
			return tempFilename;
		}
		throw new IOException("Temporary profile picture not found: " + tempFilename);
	}

	private void deleteTemporaryProfilePicture(String tempFilename) throws IOException {
		Path uploadDir = Paths.get(configService.getProperty("upload.directory"));
		Path tempFile = uploadDir.resolve("temp_avatars").resolve(tempFilename);
		Files.deleteIfExists(tempFile);
	}
}