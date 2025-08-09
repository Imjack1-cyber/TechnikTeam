package de.technikteam.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

	private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

	public Bucket resolveBucket(String apiKey) {
		return cache.computeIfAbsent(apiKey, this::newBucket);
	}

	private Bucket newBucket(String apiKey) {
		// Example: 10 requests per minute
		Bandwidth limit = Bandwidth.simple(10, Duration.ofMinutes(1));
		return Bucket.builder().addLimit(limit).build();
	}
}