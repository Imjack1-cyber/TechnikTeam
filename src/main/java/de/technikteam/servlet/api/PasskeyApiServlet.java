package de.technikteam.servlet.api;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
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

@WebServlet("/api/passkey/*")
public class PasskeyApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PasskeyApiServlet.class);
	// REMOVED: Do not initialize the service here, as it can prevent the servlet
	// from loading.
	// private final PasskeyService passkeyService = PasskeyService.getInstance();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// FIX: The service is now instantiated inside the try-catch block.
		// This guarantees that if PasskeyService.getInstance() or its subsequent
		// initialization fails with a critical Error, we can catch it and log it.
		try {
			PasskeyService passkeyService = PasskeyService.getInstance();
			String action = req.getPathInfo();
			if (action == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No action specified.");
				return;
			}

			switch (action) {
			case "/register/start":
				handleStartRegistration(req, resp, passkeyService);
				break;
			case "/register/finish":
				handleFinishRegistration(req, resp, passkeyService);
				break;
			case "/login/start":
				handleStartLogin(req, resp, passkeyService);
				break;
			case "/login/finish":
				handleFinishLogin(req, resp, passkeyService);
				break;
			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown passkey action.");
				break;
			}
		} catch (Throwable t) {
			logger.error(
					"FATAL: A critical error occurred while initializing or using the PasskeyService. This is likely a dependency or library version conflict.",
					t);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A critical error occurred on the server.");
		}
	}

	private void handleStartRegistration(HttpServletRequest req, HttpServletResponse resp,
			PasskeyService passkeyService) throws IOException {
		User user = (User) req.getSession().getAttribute("user");
		if (user == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		PublicKeyCredentialCreationOptions options = passkeyService.startRegistration(user);
		String optionsJson = options.toJson();
		req.getSession().setAttribute("passkeyRegistrationRequest", optionsJson);
		resp.setContentType("application/json");
		resp.getWriter().write(optionsJson);
	}

	private void handleFinishRegistration(HttpServletRequest req, HttpServletResponse resp,
			PasskeyService passkeyService) throws IOException {
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

	private void handleStartLogin(HttpServletRequest req, HttpServletResponse resp, PasskeyService passkeyService)
			throws IOException {
		AssertionRequest assertionRequest = passkeyService.startAssertion();
		String optionsJson = assertionRequest.toJson();
		req.getSession().setAttribute("passkeyLoginRequest", optionsJson);
		resp.setContentType("application/json");
		resp.getWriter().write(optionsJson);
	}

	private void handleFinishLogin(HttpServletRequest req, HttpServletResponse resp, PasskeyService passkeyService)
			throws IOException {
		String loginRequest = (String) req.getSession().getAttribute("passkeyLoginRequest");
		if (loginRequest == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No login in progress.");
			return;
		}

		String credentialJson = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

		Optional<User> userOpt = passkeyService.finishAssertion(credentialJson, loginRequest);

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