package de.technikteam.service;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class AuthService {
	private static final Logger logger = LogManager.getLogger(AuthService.class);
	private static final String JWT_ISSUER = "TechnikTeamApp";
	public static final String AUTH_COOKIE_NAME = "TT_AUTH_TOKEN";
	private static final int COOKIE_MAX_AGE_SECONDS = 8 * 60 * 60; // 8 hours

	private final SecretKey secretKey;
	private final UserDAO userDAO;

	@Autowired
	public AuthService(UserDAO userDAO, ConfigurationService configService) {
		this.userDAO = userDAO;
		// REMEDIATION: Load the JWT secret from an environment variable.
		String secret = System.getenv("JWT_SECRET");
		if (secret == null || secret.isBlank()) {
			// Fallback to properties file only for local development if env var is not
			// set.
			logger.warn(
					"JWT_SECRET environment variable not found. Falling back to application.properties. This is insecure and should not be used in production.");
			secret = configService.getProperty("jwt.secret");
		}
		if (secret == null || secret.isBlank() || secret.length() < 32) {
			logger.fatal(
					"JWT-Secret ist nicht konfiguriert oder zu kurz (muss mindestens 32 Zeichen lang sein). Die Anwendung kann nicht sicher gestartet werden.");
			throw new RuntimeException("JWT-Secret ist nicht konfiguriert oder unsicher.");
		}
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plus(COOKIE_MAX_AGE_SECONDS, ChronoUnit.SECONDS);

		return Jwts.builder().issuer(JWT_ISSUER).subject(String.valueOf(user.getId())).issuedAt(Date.from(now))
				.expiration(Date.from(expiry)).signWith(secretKey).compact();
	}

	public void addJwtCookie(User user, HttpServletResponse response) {
		String token = generateToken(user);
		String header = String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=Strict", AUTH_COOKIE_NAME,
				token, COOKIE_MAX_AGE_SECONDS);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	public void clearJwtCookie(HttpServletResponse response) {
		// Construct a Set-Cookie header that expires the cookie immediately.
		// It's crucial to include the same attributes (Path, HttpOnly, Secure,
		// SameSite) as
		// the original cookie to ensure the browser overwrites it correctly.
		String header = String.format("%s=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=Strict", AUTH_COOKIE_NAME);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	/**
	 * Validate the JWT, load the user and return a UserDetails. If the user does
	 * not exist or is currently suspended, returns null. This ensures that
	 * previously-issued tokens are rejected immediately once a user is suspended.
	 */
	public UserDetails validateTokenAndGetUser(String token) {
		try {
			// Parse and validate token
			Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

			int userId = Integer.parseInt(claims.getSubject());
			User user = userDAO.getUserById(userId);

			if (user == null) {
				logger.warn("JWT-Validierung erfolgreich, aber Benutzer mit ID {} existiert nicht mehr.", userId);
				return null;
			}

			// Enforce suspension check
			if (userDAO.isUserCurrentlySuspended(user)) {
				logger.warn("JWT validation failed: User {} is currently suspended.", user.getUsername());
				return null;
			}

			// Build security user
			return new SecurityUser(user);
		} catch (Exception e) {
			logger.warn("JWT-Verifizierung fehlgeschlagen: {}", e.getMessage());
			return null;
		}
	}
}