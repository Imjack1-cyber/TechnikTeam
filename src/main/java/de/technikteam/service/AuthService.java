// src/main/java/de/technikteam/service/AuthService.java
package de.technikteam.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class AuthService {
	private static final Logger logger = LogManager.getLogger(AuthService.class);
	private static final String JWT_ISSUER = "TechnikTeamApp";

	private final Algorithm algorithm;
	private final JWTVerifier verifier;
	private final UserDAO userDAO;

	@Autowired
	public AuthService(UserDAO userDAO, ConfigurationService configService) {
		this.userDAO = userDAO;
		String secret = configService.getProperty("jwt.secret");
		if (secret == null || secret.isBlank()) {
			logger.fatal("JWT secret is not configured in db.properties. Application cannot start securely.");
			throw new RuntimeException("JWT secret is not configured.");
		}
		this.algorithm = Algorithm.HMAC256(secret);
		this.verifier = JWT.require(algorithm).withIssuer(JWT_ISSUER).build();
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plus(8, ChronoUnit.HOURS);

		return JWT.create().withIssuer(JWT_ISSUER).withSubject(String.valueOf(user.getId()))
				.withClaim("username", user.getUsername()).withClaim("role", user.getRoleName())
				.withIssuedAt(Date.from(now)).withExpiresAt(Date.from(expiry)).sign(algorithm);
	}

	public User validateTokenAndGetUser(String token) {
		try {
			DecodedJWT decodedJWT = verifier.verify(token);
			int userId = Integer.parseInt(decodedJWT.getSubject());

			User user = userDAO.getUserById(userId);

			if (user == null) {
				logger.warn("JWT validation successful, but user with ID {} no longer exists.", userId);
				return null;
			}
			
			return user;
		} catch (JWTVerificationException e) {
			logger.warn("JWT verification failed: {}", e.getMessage());
			return null;
		} catch (NumberFormatException e) {
			logger.error("Invalid subject (user ID) in JWT.", e);
			return null;
		}
	}
}