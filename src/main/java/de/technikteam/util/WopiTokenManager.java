package de.technikteam.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simple, in-memory, thread-safe manager for short-lived WOPI access tokens.
 * This is used to validate that requests from the Collabora server originate
 * from a legitimate user session in our application without relying on a shared
 * HttpSession.
 */
public class WopiTokenManager {

	private static final Logger logger = LogManager.getLogger(WopiTokenManager.class);
	private static final WopiTokenManager INSTANCE = new WopiTokenManager();
	private final Map<String, TokenData> tokenStore = new ConcurrentHashMap<>();
	private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

	// Inner class to hold token data including its creation time
	public static class TokenData {
		final String fileId;
		final String username;
		final Instant creationTime;

		TokenData(String fileId, String username) {
			this.fileId = fileId;
			this.username = username;
			this.creationTime = Instant.now();
		}

		public String getUsername() {
			return username;
		}
	}

	private WopiTokenManager() {
		// Schedule a task to run periodically to clean up expired tokens.
		// Tokens live for 5 minutes.
		cleanupScheduler.scheduleAtFixedRate(this::removeExpiredTokens, 5, 5, TimeUnit.MINUTES);
		logger.info("WopiTokenManager initialized with a 5-minute token expiry policy.");
	}

	public static WopiTokenManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Stores a new token with its associated file ID and username.
	 * 
	 * @param token    The generated access token.
	 * @param fileId   The ID of the file the token is for.
	 * @param username The user initiating the session.
	 */
	public void storeToken(String token, String fileId, String username) {
		tokenStore.put(token, new TokenData(fileId, username));
	}

	/**
	 * Validates a token and checks if it corresponds to the expected file ID.
	 * 
	 * @param token          The token from the request.
	 * @param expectedFileId The file ID from the URL.
	 * @return The TokenData object if the token is valid, null otherwise.
	 */
	public TokenData validateAndGetData(String token, String expectedFileId) {
		if (token == null || expectedFileId == null) {
			return null;
		}
		TokenData data = tokenStore.get(token);
		if (data == null) {
			logger.warn("Token validation failed: Token '{}' not found in store.", token);
			return null; // Token does not exist
		}

		if (data.creationTime.isBefore(Instant.now().minusSeconds(300))) {
			logger.warn("Token validation failed: Token for file {} has expired.", data.fileId);
			tokenStore.remove(token); // Remove expired token
			return null;
		}

		if (expectedFileId.equals(data.fileId)) {
			return data;
		} else {
			logger.warn("Token validation failed: Token is for file '{}', but request is for file '{}'.", data.fileId,
					expectedFileId);
			return null;
		}
	}

	/**
	 * Removes tokens that are older than 5 minutes to prevent them from
	 * accumulating in memory and being used for replay attacks.
	 */
	private void removeExpiredTokens() {
		Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
		long originalSize = tokenStore.size();
		tokenStore.entrySet().removeIf(entry -> entry.getValue().creationTime.isBefore(fiveMinutesAgo));
		long removedCount = originalSize - tokenStore.size();
		if (removedCount > 0) {
			logger.info("WOPI token cleanup: Removed {} expired tokens.", removedCount);
		}
	}

	/**
	 * Shuts down the background cleanup thread. This must be called when the
	 * application context is destroyed to prevent resource leaks.
	 */
	public void shutdown() {
		if (cleanupScheduler != null && !cleanupScheduler.isShutdown()) {
			cleanupScheduler.shutdownNow();
			logger.info("WopiTokenManager cleanup scheduler has been shut down.");
		}
	}
}