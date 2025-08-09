package de.technikteam.service;

// TODO: SECURITY-AUDIT-FIX (CWE-307)
// The current lockout mechanism is based solely on username, which makes it vulnerable
// to targeted Denial of Service attacks where an attacker can lock out any known user.
// REMEDIATION: This service should be refactored to be primarily IP-based.
// 1. A database migration is required to change the login_attempts table to track failures by IP address.
// 2. The logic in isLockedOut() and recordFailedLogin() must be updated to use the client's IP address
//    as the key for tracking and enforcing lockouts.

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LoginAttemptService {
	private static final Logger logger = LogManager.getLogger(LoginAttemptService.class);
	private static final int MAX_ATTEMPTS = 5;
	private static final int LOCKOUT_MINUTES = 30;

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public LoginAttemptService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean isLockedOut(String username, String ipAddress) {
		return isUserLocked(username);
	}

	public boolean isUserLocked(String username) {
		String sql = "SELECT last_attempt, attempts FROM login_attempts WHERE username = ?";
		try {
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				LocalDateTime lastAttempt = rs.getTimestamp("last_attempt").toLocalDateTime();
				int attempts = rs.getInt("attempts");
				if (attempts >= MAX_ATTEMPTS
						&& lastAttempt.isAfter(LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES))) {
					return true; // User is actively locked out.
				}
				// If we reach here, the user is not actively locked out.
				// If the lockout period has expired, we should clear the record.
				if (attempts >= MAX_ATTEMPTS) {
					clearLoginAttempts(username);
				}
				return false;
			}, username);
		} catch (Exception e) {
			return false; // No record means not locked out
		}
	}

	public long getRemainingLockoutSeconds(String username) {
		String sql = "SELECT last_attempt FROM login_attempts WHERE username = ?";
		try {
			LocalDateTime lastAttempt = jdbcTemplate.queryForObject(sql, LocalDateTime.class, username);
			if (lastAttempt != null) {
				LocalDateTime lockoutExpiry = lastAttempt.plusMinutes(LOCKOUT_MINUTES);
				Duration remaining = Duration.between(LocalDateTime.now(), lockoutExpiry);
				return remaining.isNegative() ? 0 : remaining.getSeconds();
			}
		} catch (Exception e) {
			// No record or other error means not locked out
		}
		return 0;
	}

	public void recordFailedLogin(String username, String ipAddress) {
		String sql = "INSERT INTO login_attempts (username, attempts, last_attempt) VALUES (?, 1, ?) "
				+ "ON DUPLICATE KEY UPDATE attempts = attempts + 1, last_attempt = ?";
		Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		jdbcTemplate.update(sql, username, now, now);

		String checkSql = "SELECT attempts FROM login_attempts WHERE username = ?";
		try {
			Integer currentAttempts = jdbcTemplate.queryForObject(checkSql, Integer.class, username);
			if (currentAttempts != null && currentAttempts >= MAX_ATTEMPTS) {
				logger.warn("Locking out user '{}' (from IP {}) due to {} failed login attempts.", username, ipAddress,
						currentAttempts);
			}
		} catch (Exception e) {
			logger.error("Could not check login attempts for user '{}' after failed login.", username, e);
		}
	}

	public void clearLoginAttempts(String username) {
		String sql = "DELETE FROM login_attempts WHERE username = ?";
		jdbcTemplate.update(sql, username);
	}
}