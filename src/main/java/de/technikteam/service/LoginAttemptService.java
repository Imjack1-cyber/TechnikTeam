package de.technikteam.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

	private static final Logger logger = LogManager.getLogger(LoginAttemptService.class);
	private static final int MAX_ATTEMPTS = 5;

	// Cache to store failed attempt counts, expires after 30 minutes of inactivity.
	private final Cache<String, Integer> attemptsCache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES)
			.build();

	// Cache to store lockout status, expires after 30 minutes.
	private final Cache<String, Boolean> lockoutCache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES)
			.build();

	public boolean isLockedOut(String username) {
		return lockoutCache.getIfPresent(username) != null;
	}

	public void recordFailedLogin(String username) {
		int attempts = attemptsCache.get(username, k -> 0);
		attempts++;
		attemptsCache.put(username, attempts);

		if (attempts >= MAX_ATTEMPTS) {
			logger.warn("Locking out user {} due to {} failed login attempts.", username, attempts);
			lockoutCache.put(username, true);
			attemptsCache.invalidate(username); // Reset attempts after lockout
		}
	}

	public void clearLoginAttempts(String username) {
		attemptsCache.invalidate(username);
		lockoutCache.invalidate(username);
	}
}