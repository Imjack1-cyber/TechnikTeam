package de.technikteam.service;

import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class LoginAttemptService {

	private static final Logger logger = LogManager.getLogger(LoginAttemptService.class);
	private static final int MAX_ATTEMPTS = 5;
	private static final long[] LOCKOUT_DURATIONS_MS = { TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(2),
			TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(10), TimeUnit.MINUTES.toMillis(30) };
	private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
	private final Map<String, Long> lockoutTimestamps = new ConcurrentHashMap<>();
	private final Map<String, Integer> lockoutLevel = new ConcurrentHashMap<>();

	public long getLockoutEndTime(String username) {
		return lockoutTimestamps.getOrDefault(username, 0L);
	}

	public int getLockoutLevel(String username) {
		return lockoutLevel.getOrDefault(username, 0);
	}

	public boolean isLockedOut(String username) {
		Long lockoutTime = lockoutTimestamps.get(username);
		if (lockoutTime == null)
			return false;

		int currentLevel = lockoutLevel.getOrDefault(username, 0);
		long duration = LOCKOUT_DURATIONS_MS[Math.min(currentLevel, LOCKOUT_DURATIONS_MS.length - 1)];

		return (System.currentTimeMillis() - lockoutTime) <= duration;
	}

	public void recordFailedLogin(String username) {
		int attempts = failedAttempts.compute(username, (k, v) -> (v == null) ? 1 : v + 1);
		if (attempts >= MAX_ATTEMPTS) {
			int currentLevel = lockoutLevel.compute(username, (k, v) -> (v == null) ? 0 : v + 1);
			long duration = LOCKOUT_DURATIONS_MS[Math.min(currentLevel, LOCKOUT_DURATIONS_MS.length - 1)];
			logger.warn("Locking out user {} for {} minutes due to {} failed login attempts.", username,
					TimeUnit.MILLISECONDS.toMinutes(duration), attempts);
			lockoutTimestamps.put(username, System.currentTimeMillis());
			failedAttempts.remove(username);
		}
	}

	public void clearLoginAttempts(String username) {
		failedAttempts.remove(username);
		lockoutTimestamps.remove(username);
		lockoutLevel.remove(username);
	}
}