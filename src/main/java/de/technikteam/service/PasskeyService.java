package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.PasskeyDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.UUID;

@Singleton
public class PasskeyService {
	private static final Logger logger = LogManager.getLogger(PasskeyService.class);
	private final PasskeyDAO passkeyDAO;
	private final UserDAO userDAO;

	@Inject
	public PasskeyService(PasskeyDAO passkeyDAO, UserDAO userDAO) {
		this.passkeyDAO = passkeyDAO;
		this.userDAO = userDAO;
	}

	public String startRegistration(User user) {
		logger.info("Simulating passkey registration start for user '{}'", user.getUsername());
		String challenge = generateChallenge();
		String userHandle = UUID.randomUUID().toString();
		return String.format(
				"{\"challenge\": \"%s\", \"rp\": {\"name\": \"TechnikTeam\", \"id\": \"localhost\"}, \"user\": {\"id\": \"%s\", \"name\": \"%s\", \"displayName\": \"%s\"}, \"pubKeyCredParams\": [{\"type\": \"public-key\", \"alg\": -7}]}",
				challenge, userHandle, user.getUsername(), user.getUsername());
	}

	public boolean finishRegistration(int userId, String credentialData, String deviceName) {
		logger.info("Simulating passkey registration finish for user ID {}. Device: '{}'", userId, deviceName);
		de.technikteam.model.PasskeyCredential credential = new de.technikteam.model.PasskeyCredential();
		credential.setUserId(userId);
		credential.setName(deviceName);
		credential.setUserHandle(UUID.randomUUID().toString());
		credential.setCredentialId(generateChallenge());
		credential.setPublicKey("simulated-public-key-" + UUID.randomUUID());
		credential.setSignatureCount(0);
		return passkeyDAO.saveCredential(credential);
	}

	public String startAuthentication(String username) {
		logger.info("Simulating passkey authentication start for user '{}'", username);
		String challenge = generateChallenge();
		return String.format("{\"challenge\": \"%s\", \"allowCredentials\": [], \"rpId\": \"localhost\"}", challenge);
	}

	public User finishAuthentication(String credentialData) {
		logger.info("Simulating passkey authentication finish.");
		User user = userDAO.getUserById(1); 
		if (user != null) {
			user.setPermissions(userDAO.getPermissionsForUser(user.getId()));
			logger.info("Simulated passkey login successful for user '{}'", user.getUsername());
		}
		return user;
	}

	private String generateChallenge() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}