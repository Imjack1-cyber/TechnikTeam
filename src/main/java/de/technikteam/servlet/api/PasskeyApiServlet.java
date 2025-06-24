package de.technikteam.servlet.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.service.PasskeyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides the REST-like API endpoints for WebAuthn (Passkey) registration and
 * login.
 */
@WebServlet("/api/passkey/*")
public class PasskeyApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PasskeyApiServlet.class);
	private final PasskeyService passkeyService = PasskeyService.getInstance();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getPathInfo();
		if (action == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No action specified.");
			return;
		}

		switch (action) {
		case "/register/start":
			handleStartRegistration(req, resp);
			break;
		case "/register/finish":
			handleFinishRegistration(req, resp);
			break;
		case "/login/start":
			handleStartLogin(req, resp);
			break;
		case "/login/finish":
			handleFinishLogin(req, resp);
			break;
		default:
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown passkey action.");
			break;
		}
	}

	private void handleStartRegistration(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User user = (User) req.getSession().getAttribute("user");
		if (user == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		try {
			String options = passkeyService.startRegistration(user);
			req.getSession().setAttribute("passkeyRegistrationRequest", options);
			resp.setContentType("application/json");
			resp.getWriter().write(options);
		} catch (JsonProcessingException e) {
			logger.error("Error starting passkey registration", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void handleFinishRegistration(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User user = (User) req.getSession().getAttribute("user");
		String registrationRequest = (String) req.getSession().getAttribute("passkeyRegistrationRequest");
		if (user == null || registrationRequest == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No registration in progress.");
			return;
		}

		String credentialJson = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		String passkeyName = req.getParameter("name");
		if (passkeyName == null || passkeyName.isBlank()) {
			passkeyName = "Unbenannter Schlüssel";
		}

		boolean success = passkeyService.finishRegistration(credentialJson, registrationRequest, user, passkeyName);
		if (success) {
			resp.setStatus(HttpServletResponse.SC_OK);
		} else {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed.");
		}
		req.getSession().removeAttribute("passkeyRegistrationRequest");
	}

	private void handleStartLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			String options = passkeyService.startAuthentication();
			req.getSession().setAttribute("passkeyLoginRequest", options);
			resp.setContentType("application/json");
			resp.getWriter().write(options);
		} catch (JsonProcessingException e) {
			logger.error("Error starting passkey login", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void handleFinishLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String loginRequest = (String) req.getSession().getAttribute("passkeyLoginRequest");
		if (loginRequest == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No login in progress.");
			return;
		}

		String credentialJson = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		String userHandle = req.getParameter("userHandle");

		Optional<User> userOpt = passkeyService.finishAuthentication(credentialJson, loginRequest, userHandle);

		if (userOpt.isPresent()) {
			User user = userOpt.get();
			HttpSession session = req.getSession(true);
			session.setAttribute("user", user);
			session.setAttribute("username", user.getUsername());
			session.setAttribute("role", user.getRole());
			resp.setStatus(HttpServletResponse.SC_OK);
		} else {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Passkey authentication failed.");
		}
		req.getSession().removeAttribute("passkeyLoginRequest");
	}
}