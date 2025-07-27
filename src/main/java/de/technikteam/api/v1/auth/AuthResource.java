// src/main/java/de/technikteam/api/v1/auth/AuthResource.java
package de.technikteam.api.v1.auth;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AuthService;
import de.technikteam.service.LoginAttemptService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class AuthResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AuthResource.class);

	private final UserDAO userDAO;
	private final AuthService authService;
	private final LoginAttemptService loginAttemptService;
	private final Gson gson;

	@Inject
	public AuthResource(UserDAO userDAO, AuthService authService, LoginAttemptService loginAttemptService, Gson gson) {
		this.userDAO = userDAO;
		this.authService = authService;
		this.loginAttemptService = loginAttemptService;
		this.gson = gson;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, String> credentials = gson.fromJson(jsonPayload, type);

		String username = credentials.get("username");
		String password = credentials.get("password");

		if (username == null || password == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
			return;
		}

		if (loginAttemptService.isLockedOut(username)) {
			logger.warn("Blocked login attempt for locked-out user '{}'", username);
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Account is temporarily locked.");
			return;
		}

		User user = userDAO.validateUser(username, password);
		if (user != null) {
			loginAttemptService.clearLoginAttempts(username);
			String token = authService.generateToken(user);
			logger.info("JWT generated successfully for user '{}'", username);
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Login successful", Map.of("token", token)));
		} else {
			loginAttemptService.recordFailedLogin(username);
			logger.warn("Failed API login attempt for user '{}'", username);
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials.");
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