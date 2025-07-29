package de.technikteam.service;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final SecretKey secretKey;
	private final UserDAO userDAO;

	@Autowired
	public AuthService(UserDAO userDAO, ConfigurationService configService) {
		this.userDAO = userDAO;
		String secret = configService.getProperty("jwt.secret");
		if (secret == null || secret.isBlank()) {
			logger.fatal("JWT secret is not configured in application.properties. Application cannot start securely.");
			throw new RuntimeException("JWT secret is not configured.");
		}
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plus(8, ChronoUnit.HOURS);

		return Jwts.builder().issuer(JWT_ISSUER).subject(String.valueOf(user.getId()))
				.claim("username", user.getUsername()).claim("role", user.getRoleName()).issuedAt(Date.from(now))
				.expiration(Date.from(expiry)).signWith(secretKey).compact();
	}

	public User validateTokenAndGetUser(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

			int userId = Integer.parseInt(claims.getSubject());
			User user = userDAO.getUserById(userId);

			if (user == null) {
				logger.warn("JWT validation successful, but user with ID {} no longer exists.", userId);
				return null;
			}
			return user;
		} catch (Exception e) {
			logger.warn("JWT verification failed: {}", e.getMessage());
			return null;
		}
	}
}