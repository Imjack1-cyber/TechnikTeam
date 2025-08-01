package de.technikteam.service;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
		String secret = configService.getProperty("jwt.secret");
		if (secret == null || secret.isBlank() || secret.length() < 32) {
			logger.fatal(
					"JWT secret is not configured or is too short (must be at least 32 characters). Application cannot start securely.");
			throw new RuntimeException("JWT secret is not configured or is insecure.");
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
		Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
		cookie.setHttpOnly(true);
		cookie.setSecure(true); // Should be true in production
		cookie.setPath("/");
		cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
		// cookie.setSameSite("Strict"); // Enable for maximum security
		response.addCookie(cookie);
	}

	public void clearJwtCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie(AUTH_COOKIE_NAME, null);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0); // Expire immediately
		response.addCookie(cookie);
	}

	public UserDetails validateTokenAndGetUser(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

			int userId = Integer.parseInt(claims.getSubject());
			User user = userDAO.getUserById(userId);

			if (user == null) {
				logger.warn("JWT validation successful, but user with ID {} no longer exists.", userId);
				return null;
			}
			return new SecurityUser(user);
		} catch (Exception e) {
			logger.warn("JWT verification failed: {}", e.getMessage());
			return null;
		}
	}
}