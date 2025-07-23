package de.technikteam.service;

import de.technikteam.dao.PasskeyDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.UUID;

/**
 * SIMULATED Passkey Service. This class simulates the interaction with a real
 * WebAuthn library (like Yubico's). In a real-world scenario, this class would
 * contain complex cryptographic operations. For this exercise, it contains
 * placeholder logic that mimics the flow but does not perform actual
 * cryptographic validation.
 */
public class PasskeyService {
	private static final Logger logger = LogManager.getLogger(PasskeyService.class);
	private final PasskeyDAO passkeyDAO = new PasskeyDAO();
	private final UserDAO userDAO = new UserDAO();

	/**
	 * Simulates starting the registration process. Generates a unique challenge and
	 * user handle.
	 * 
	 * @param user The user who is registering a new passkey.
	 * @return A simulated JSON string representing the challenge.
	 */
	public String startRegistration(User user) {
		logger.info("Simulating passkey registration start for user '{}'", user.getUsername());
		String challenge = generateChallenge();
		String userHandle = UUID.randomUUID().toString();
		// In a real implementation, we would store this challenge in the session with a
		// timestamp.
		return String.format(
				"{\"challenge\": \"%s\", \"rp\": {\"name\": \"TechnikTeam\", \"id\": \"localhost\"}, \"user\": {\"id\": \"%s\", \"name\": \"%s\", \"displayName\": \"%s\"}, \"pubKeyCredParams\": [{\"type\": \"public-key\", \"alg\": -7}]}",
				challenge, userHandle, user.getUsername(), user.getUsername());
	}

	/**
	 * Simulates finishing the registration process. In a real implementation, this
	 * would verify the client's response against the stored challenge.
	 * 
	 * @param userId         The ID of the user.
	 * @param credentialData A JSON string from the client.
	 * @param deviceName     A friendly name for the new credential.
	 * @return true if the simulated verification is successful.
	 */
	public boolean finishRegistration(int userId, String credentialData, String deviceName) {
		logger.info("Simulating passkey registration finish for user ID {}. Device: '{}'", userId, deviceName);
		// SIMULATION: Assume validation is always successful.
		// A real implementation would parse 'credentialData', verify signatures, and
		// check the challenge.

		// SIMULATION: Create a placeholder credential to save.
		de.technikteam.model.PasskeyCredential credential = new de.technikteam.model.PasskeyCredential();
		credential.setUserId(userId);
		credential.setName(deviceName);
		credential.setUserHandle(UUID.randomUUID().toString()); // In real life, this comes from startRegistration
		credential.setCredentialId(generateChallenge()); // Unique ID for the key
		credential.setPublicKey("simulated-public-key-" + UUID.randomUUID());
		credential.setSignatureCount(0);

		return passkeyDAO.saveCredential(credential);
	}

	/**
	 * Simulates starting the authentication (login) process.
	 * 
	 * @param username The username attempting to log in.
	 * @return A simulated JSON string representing the authentication challenge.
	 */
	public String startAuthentication(String username) {
		logger.info("Simulating passkey authentication start for user '{}'", username);
		// A real implementation would fetch all known credential IDs for this user to
		// send to the client.
		String challenge = generateChallenge();
		return String.format("{\"challenge\": \"%s\", \"allowCredentials\": [], \"rpId\": \"localhost\"}", challenge);
	}

	/**
	 * Simulates finishing the authentication process. In a real implementation,
	 * this verifies the client's signature.
	 * 
	 * @param credentialData JSON data from the client.
	 * @return The User object if authentication is successful, otherwise null.
	 */
	public User finishAuthentication(String credentialData) {
		logger.info("Simulating passkey authentication finish.");
		// SIMULATION: Assume validation is always successful and find ANY user to log
		// in.
		// A real implementation would parse the credentialId from 'credentialData',
		// look it up in the DB,
		// retrieve the public key and signature count, and perform cryptographic
		// verification.

		// For this simulation, we'll just grab the first admin user to demonstrate the
		// login flow.
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