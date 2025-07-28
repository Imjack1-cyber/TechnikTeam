// src/main/java/de/technikteam/api/v1/public_api/PublicProfileResource.java
package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.*;
import de.technikteam.model.*;
import de.technikteam.util.PasswordPolicyValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class PublicProfileResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PublicProfileResource.class);

	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final UserQualificationsDAO qualificationsDAO;
	private final AchievementDAO achievementDAO;
	private final PasskeyDAO passkeyDAO;
	private final ProfileChangeRequestDAO requestDAO;
	private final Gson gson;

	@Inject
	public PublicProfileResource(UserDAO userDAO, EventDAO eventDAO, UserQualificationsDAO qualificationsDAO,
			AchievementDAO achievementDAO, PasskeyDAO passkeyDAO, ProfileChangeRequestDAO requestDAO, Gson gson) {
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.qualificationsDAO = qualificationsDAO;
		this.achievementDAO = achievementDAO;
		this.passkeyDAO = passkeyDAO;
		this.requestDAO = requestDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		try {
			Map<String, Object> profileData = new HashMap<>();
			profileData.put("user", user);
			profileData.put("eventHistory", eventDAO.getEventHistoryForUser(user.getId()));
			profileData.put("qualifications", qualificationsDAO.getQualificationsForUser(user.getId()));
			profileData.put("achievements", achievementDAO.getAchievementsForUser(user.getId()));
			profileData.put("passkeys", passkeyDAO.getCredentialsByUserId(user.getId()));
			profileData.put("hasPendingRequest", requestDAO.hasPendingRequest(user.getId()));

			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Profile data retrieved.", profileData));
		} catch (Exception e) {
			logger.error("Error fetching profile data for user {}", user.getUsername(), e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve profile data.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || !pathInfo.equals("/request-change")) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND,
					"Endpoint not found. Use POST /api/v1/public/profile/request-change for this action.");
			return;
		}

		handleProfileChangeRequest(req, resp, user);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
			return;
		}

		switch (pathInfo) {
		case "/chat-color":
			handleUpdateChatColor(req, resp, user);
			break;
		case "/password":
			handleUpdatePassword(req, resp, user);
			break;
		default:
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || !pathInfo.startsWith("/passkeys/")) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND,
					"Endpoint not found. Use DELETE /api/v1/public/profile/passkeys/{id}.");
			return;
		}

		try {
			int credentialId = Integer.parseInt(pathInfo.substring("/passkeys/".length()));
			handleDeletePasskey(resp, user, credentialId);
		} catch (NumberFormatException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid passkey credential ID.");
		}
	}

	private void handleProfileChangeRequest(HttpServletRequest req, HttpServletResponse resp, User currentUser)
			throws IOException {
		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Map<String, String> requestedData = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
			}.getType());

			Map<String, String> changes = new HashMap<>();
			String newEmail = requestedData.get("email");
			if (!Objects.equals(currentUser.getEmail(), newEmail)) {
				changes.put("email", newEmail);
			}

			int newClassYear = Integer.parseInt(requestedData.getOrDefault("classYear", "0"));
			if (currentUser.getClassYear() != newClassYear) {
				changes.put("classYear", String.valueOf(newClassYear));
			}

			String newClassName = requestedData.get("className");
			if (!Objects.equals(currentUser.getClassName(), newClassName)) {
				changes.put("className", newClassName);
			}

			if (changes.isEmpty()) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "No changes detected.", null));
				return;
			}

			ProfileChangeRequest pcr = new ProfileChangeRequest();
			pcr.setUserId(currentUser.getId());
			pcr.setRequestedChanges(gson.toJson(changes));

			if (requestDAO.createRequest(pcr)) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Change request submitted successfully.", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save your request.");
			}
		} catch (Exception e) {
			logger.error("Error processing profile change request for user {}", currentUser.getUsername(), e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
		}
	}

	private void handleUpdateChatColor(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Map<String, String> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
		}.getType());
		String chatColor = payload.get("chatColor");

		if (userDAO.updateUserChatColor(user.getId(), chatColor)) {
			sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Chat color updated.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save chat color.");
		}
	}

	private void handleUpdatePassword(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Map<String, String> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
			}.getType());
			String currentPassword = payload.get("currentPassword");
			String newPassword = payload.get("newPassword");
			String confirmPassword = payload.get("confirmPassword");

			if (userDAO.validateUser(user.getUsername(), currentPassword) == null) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Das aktuelle Passwort ist nicht korrekt.");
				return;
			}
			if (!newPassword.equals(confirmPassword)) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Die neuen Passwörter stimmen nicht überein.");
				return;
			}
			PasswordPolicyValidator.ValidationResult validationResult = PasswordPolicyValidator.validate(newPassword);
			if (!validationResult.isValid()) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, validationResult.getMessage());
				return;
			}
			if (userDAO.changePassword(user.getId(), newPassword)) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Ihr Passwort wurde erfolgreich geändert.", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Passwort konnte nicht geändert werden.");
			}
		} catch (Exception e) {
			logger.error("Error processing password change for user {}", user.getUsername(), e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ein interner Fehler ist aufgetreten.");
		}
	}

	private void handleDeletePasskey(HttpServletResponse resp, User user, int credentialId) throws IOException {
		if (passkeyDAO.deleteCredential(credentialId, user.getId())) {
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Passkey successfully removed.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not remove passkey.");
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