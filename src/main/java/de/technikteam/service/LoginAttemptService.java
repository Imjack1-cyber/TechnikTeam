package de.technikteam.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LoginAttemptService {
	private static final Logger logger = LogManager.getLogger(LoginAttemptService.class);
	private static final int MAX_IP_ATTEMPTS = 10;
	private static final int MAX_USERNAME_ATTEMPTS = 5;
	private static final int LOCKOUT_MINUTES = 15;

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public LoginAttemptService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public boolean isLockedOut(String username, String ipAddress) {
		String sql = "SELECT last_attempt, attempts FROM login_attempts WHERE ip_address = ? AND username = ?";
		try {
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				LocalDateTime lastAttempt = rs.getTimestamp("last_attempt").toLocalDateTime();
				int attempts = rs.getInt("attempts");

				// Check for IP-based lockout (stricter)
				int ipTotalAttempts = getTotalAttemptsForIp(ipAddress);
				if (ipTotalAttempts >= MAX_IP_ATTEMPTS
						&& lastAttempt.isAfter(LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES))) {
					logger.warn("IP address {} is locked out.", ipAddress);
					return true;
				}

				// Check for username-based lockout (less strict)
				if (attempts >= MAX_USERNAME_ATTEMPTS
						&& lastAttempt.isAfter(LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES))) {
					logger.warn("Username '{}' is locked out.", username);
					return true;
				}

				// If we reach here, the user is not actively locked out.
				// If the lockout period has expired, we should clear the record.
				if (attempts >= MAX_USERNAME_ATTEMPTS || ipTotalAttempts >= MAX_IP_ATTEMPTS) {
					clearLoginAttemptsForPair(username, ipAddress);
				}
				return false;
			}, ipAddress, username);
		} catch (Exception e) {
			return false; // No record means not locked out
		}
	}

	public boolean isUserLocked(String username) {
		// This method is now less critical but can be used for UI hints.
		// It checks if *any* IP has locked out this user.
		String sql = "SELECT EXISTS (SELECT 1 FROM login_attempts WHERE username = ? AND attempts >= ? AND last_attempt > ?)";
		Timestamp lockoutThreshold = Timestamp.valueOf(LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES));
		try {
			return jdbcTemplate.queryForObject(sql, Boolean.class, username, MAX_USERNAME_ATTEMPTS, lockoutThreshold);
		} catch (Exception e) {
			return false;
		}
	}

	public long getRemainingLockoutSeconds(String username) {
		// This simplified method still works as a hint for the user.
		String sql = "SELECT last_attempt FROM login_attempts WHERE username = ? ORDER BY last_attempt DESC LIMIT 1";
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

	@Transactional
	public void recordFailedLogin(String username, String ipAddress) {
		String sql = "INSERT INTO login_attempts (ip_address, username, attempts, last_attempt) VALUES (?, ?, 1, ?) "
				+ "ON DUPLICATE KEY UPDATE attempts = attempts + 1, last_attempt = ?";
		Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		jdbcTemplate.update(sql, ipAddress, username, now, now);

		// Log if a lockout has occurred
		if (isLockedOut(username, ipAddress)) {
			logger.warn("Lockout triggered for username '{}' from IP '{}'.", username, ipAddress);
		}
	}

	@Transactional
	public void clearLoginAttempts(String username) {
		// Clear all attempts for a given username upon successful login, regardless of
		// IP.
		String sql = "DELETE FROM login_attempts WHERE username = ?";
		jdbcTemplate.update(sql, username);
	}

	private void clearLoginAttemptsForPair(String username, String ipAddress) {
		String sql = "DELETE FROM login_attempts WHERE username = ? AND ip_address = ?";
		jdbcTemplate.update(sql, username, ipAddress);
	}

	private int getTotalAttemptsForIp(String ipAddress) {
		String sql = "SELECT SUM(attempts) FROM login_attempts WHERE ip_address = ?";
		try {
			Integer total = jdbcTemplate.queryForObject(sql, Integer.class, ipAddress);
			return total != null ? total : 0;
		} catch (Exception e) {
			return 0;
		}
	}
}