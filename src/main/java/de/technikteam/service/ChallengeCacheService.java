package de.technikteam.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A short-lived, in-memory cache to store WebAuthn challenges between the
 * "start" and "finish" steps of a registration or authentication ceremony. This
 * avoids reliance on HttpSession in a stateless JWT environment.
 */
@Service
public class ChallengeCacheService {

	private final Cache<String, Object> challengeCache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
			.maximumSize(1000).build();

	public void put(String key, Object value) {
		challengeCache.put(key, value);
	}

	public <T> Optional<T> get(String key, Class<T> type) {
		return Optional.ofNullable(challengeCache.getIfPresent(key)).filter(type::isInstance).map(type::cast);
	}

	public void remove(String key) {
		challengeCache.invalidate(key);
	}
}