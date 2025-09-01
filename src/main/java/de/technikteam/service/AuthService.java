package de.technikteam.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.technikteam.dao.JwtBlocklistDAO;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
	private static final Logger logger = LogManager.getLogger(AuthService.class);
	private static final String JWT_ISSUER = "TechnikTeamApp";
	public static final String AUTH_COOKIE_NAME = "TT_AUTH_TOKEN";
	private static final int COOKIE_MAX_AGE_SECONDS = 8 * 60 * 60; // 8 hours

	private final SecretKey secretKey;
	private final UserDAO userDAO;
	private final JwtBlocklistDAO jwtBlocklistDAO;
	private final LoadingCache<String, Boolean> revokedTokenCache;

	@Autowired
	public AuthService(UserDAO userDAO, ConfigurationService configService, JwtBlocklistDAO jwtBlocklistDAO) {
		this.userDAO = userDAO;
		this.jwtBlocklistDAO = jwtBlocklistDAO;
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
		this.revokedTokenCache = Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).maximumSize(10_000)
				.build(jti -> jwtBlocklistDAO.isBlocklisted(jti));
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plus(COOKIE_MAX_AGE_SECONDS, ChronoUnit.SECONDS);

		return Jwts.builder().issuer(JWT_ISSUER).subject(String.valueOf(user.getId())).id(UUID.randomUUID().toString())
				.issuedAt(Date.from(now)).expiration(Date.from(expiry)).signWith(secretKey).compact();
	}

	public String generatePreAuthToken(int userId) {
		Instant now = Instant.now();
		Instant expiry = now.plus(5, ChronoUnit.MINUTES); // Short-lived token for 2FA

		return Jwts.builder().issuer(JWT_ISSUER).subject(String.valueOf(userId)).claim("auth_level", "PRE_AUTH_2FA")
				.issuedAt(Date.from(now)).expiration(Date.from(expiry)).signWith(secretKey).compact();
	}

	public Claims parseTokenClaims(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	}

	public void addJwtCookie(User user, HttpServletResponse response) {
		String token = generateToken(user);
		String header = String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=Strict", AUTH_COOKIE_NAME,
				token, COOKIE_MAX_AGE_SECONDS);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	public void clearJwtCookie(HttpServletResponse response) {
		String header = String.format("%s=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=Strict", AUTH_COOKIE_NAME);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	public UserDetails validateTokenAndGetUser(String token) {
		try {
			Claims claims = parseTokenClaims(token);

			// Pre-auth tokens are not valid for general API access
			if ("PRE_AUTH_2FA".equals(claims.get("auth_level", String.class))) {
				logger.warn("Attempted to use a PRE_AUTH_2FA token for a general API request. Denying.");
				return null;
			}

			if (isTokenRevoked(claims.getId())) {
				logger.warn("JWT validation failed: Token with JTI {} has been revoked.", claims.getId());
				return null;
			}

			int userId = Integer.parseInt(claims.getSubject());
			User user = userDAO.getUserById(userId);

			if (user == null) {
				logger.warn("JWT-Validierung erfolgreich, aber Benutzer mit ID {} existiert nicht mehr.", userId);
				return null;
			}

			if (userDAO.isUserCurrentlySuspended(user)) {
				logger.warn("JWT validation failed: User {} is currently suspended.", user.getUsername());
				return null;
			}

			return new SecurityUser(user);
		} catch (Exception e) {
			logger.warn("JWT-Verifizierung fehlgeschlagen: {}", e.getMessage());
			return null;
		}
	}

	public User validatePreAuthTokenAndGetUser(String token) {
		try {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Pre-authentication token cannot be null or empty.");
            }
			Claims claims = parseTokenClaims(token);
			if (!"PRE_AUTH_2FA".equals(claims.get("auth_level", String.class))) {
				throw new SecurityException("Not a valid 2FA pre-authentication token.");
			}
			int userId = Integer.parseInt(claims.getSubject());
			return userDAO.getUserById(userId);
		} catch (Exception e) {
			logger.warn("Pre-auth JWT validation failed: {}", e.getMessage());
			return null;
		}
	}

	public void revokeToken(String jti) {
		// Since we don't have the full token, we can't get its original expiry.
		// We'll set a reasonable expiry in the future (e.g., the default session duration).
		LocalDateTime expiry = LocalDateTime.now().plus(COOKIE_MAX_AGE_SECONDS, ChronoUnit.SECONDS);
		jwtBlocklistDAO.blocklist(jti, expiry);
		revokedTokenCache.put(jti, true); // Eagerly update cache
	}

	public boolean isTokenRevoked(String jti) {
		return revokedTokenCache.get(jti);
	}

	@Scheduled(cron = "0 0 4 * * *") // Run daily at 4 AM
	public void cleanExpiredBlocklistEntries() {
		logger.info("Running scheduled cleanup of expired JWT blocklist entries.");
		jwtBlocklistDAO.cleanExpired();
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}
}