package de.technikteam.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * A utility class for Cross-Site Request Forgery (CSRF) protection. It provides
 * methods to generate, store, and validate session-bound tokens.
 */
public final class CSRFUtil {

	private static final Logger logger = LogManager.getLogger(CSRFUtil.class);
	private static final String CSRF_TOKEN_SESSION_ATTR = "csrfToken";
	private static final String CSRF_TOKEN_PARAM_NAME = "csrfToken";

	private CSRFUtil() {
	}

	public static void storeToken(HttpSession session) {
		if (session != null) {
			String token = generateToken();
			session.setAttribute(CSRF_TOKEN_SESSION_ATTR, token);
			logger.trace("Stored new CSRF token in session {}", session.getId());
		}
	}

	private static String generateToken() {
		SecureRandom random = new SecureRandom();
		byte[] tokenBytes = new byte[32];
		random.nextBytes(tokenBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
	}

	/**
	 * Validates the CSRF token from a request parameter against the one stored in
	 * the session. This method is intended for standard
	 * 'application/x-www-form-urlencoded' requests. For multipart requests, the
	 * token must be extracted manually first.
	 *
	 * @param request The HttpServletRequest containing the token.
	 * @return true if the token is valid and matches the session token, false
	 *         otherwise.
	 */
	public static boolean isTokenValid(HttpServletRequest request) {
		String requestToken = request.getParameter(CSRF_TOKEN_PARAM_NAME);
		return isTokenValid(request.getSession(false), requestToken);
	}

	/**
	 * Validates a given request token against the one stored in the session. This
	 * is the primary validation method, useful for multipart requests where the
	 * token is extracted manually by the servlet.
	 *
	 * @param session      The current HttpSession.
	 * @param requestToken The token submitted with the request.
	 * @return true if the token is valid and matches the session token, false
	 *         otherwise.
	 */
	public static boolean isTokenValid(HttpSession session, String requestToken) {
		if (session == null) {
			logger.warn("CSRF check failed: No session exists.");
			return false;
		}

		String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);

		if (sessionToken == null || requestToken == null || requestToken.isEmpty()) {
			logger.warn(
					"CSRF check failed: Session or request token is missing. Session-Token-Present: {}, Request-Token-Present: {}",
					sessionToken != null, requestToken != null && !requestToken.isEmpty());
			return false;
		}

		boolean isValid = Objects.equals(sessionToken, requestToken);
		if (!isValid) {
			logger.warn("CSRF token mismatch! Session: [{}], Request: [{}]", sessionToken, requestToken);
		}

		return isValid;
	}

	public static String getCsrfInputField(HttpSession session) {
		if (session != null) {
			String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
			if (token != null) {
				return "<input type=\"hidden\" name=\"" + CSRF_TOKEN_PARAM_NAME + "\" value=\"" + token + "\">";
			}
		}
		return "";
	}
}