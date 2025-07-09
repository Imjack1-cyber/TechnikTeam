package de.technikteam.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A utility class for Cross-Site Request Forgery (CSRF) protection. It provides
 * methods to generate and validate session-bound tokens.
 */
public final class CSRFUtil {

	private static final Logger logger = LogManager.getLogger(CSRFUtil.class);
	private static final String CSRF_TOKEN_SESSION_ATTR = "csrfToken";

	/**
	 * Private constructor to prevent instantiation.
	 */
	private CSRFUtil() {
	}

	/**
	 * Generates a new, cryptographically secure random token and stores it in the
	 * session.
	 * 
	 * @param session The HttpSession to store the token in.
	 */
	public static void storeToken(HttpSession session) {
		if (session != null) {
			String token = generateToken();
			session.setAttribute(CSRF_TOKEN_SESSION_ATTR, token);
			logger.trace("Stored new CSRF token in session {}", session.getId());
		}
	}

	/**
	 * Generates a new, cryptographically secure random token.
	 * 
	 * @return A Base64-encoded random token string.
	 */
	private static String generateToken() {
		SecureRandom random = new SecureRandom();
		byte[] tokenBytes = new byte[32];
		random.nextBytes(tokenBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
	}

	/**
	 * Validates the CSRF token from a request against the one stored in the
	 * session. This method should be called at the beginning of any state-changing
	 * POST request handler.
	 * 
	 * @param request The HttpServletRequest containing the token.
	 * @return true if the token is valid, false otherwise.
	 */
	public static boolean isTokenValid(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		String requestToken = request.getParameter(CSRF_TOKEN_SESSION_ATTR);
		return isTokenValid(session, requestToken);
	}

	/**
	 * Validates a given request token against the one stored in the session. This
	 * is useful for multipart requests where the token is extracted manually.
	 * 
	 * @param session      The current HttpSession.
	 * @param requestToken The token submitted with the request.
	 * @return true if the token is valid, false otherwise.
	 */
	public static boolean isTokenValid(HttpSession session, String requestToken) {
		if (session == null) {
			logger.warn("CSRF check failed: No session exists.");
			return false;
		}

		String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);

		if (sessionToken == null || requestToken == null || requestToken.isEmpty()) {
			logger.warn("CSRF check failed: Session or request token is missing.");
			return false;
		}

		boolean isValid = sessionToken.equals(requestToken);
		if (!isValid) {
			logger.warn("CSRF token mismatch! Session: [{}], Request: [{}]", sessionToken, requestToken);
		}

		return isValid;
	}

	/**
	 * Returns the HTML hidden input field for the CSRF token. This can be used in
	 * JSPs to easily include the token in forms.
	 * 
	 * @param session The current HttpSession.
	 * @return An HTML string for the hidden input field.
	 */
	public static String getCsrfInputField(HttpSession session) {
		if (session != null) {
			String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
			if (token != null) {
				return "<input type=\"hidden\" name=\"" + CSRF_TOKEN_SESSION_ATTR + "\" value=\"" + token + "\">";
			}
		}
		return "";
	}
}