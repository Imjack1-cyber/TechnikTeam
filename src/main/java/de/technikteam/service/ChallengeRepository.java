package de.technikteam.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class ChallengeRepository {

	private final Cache<String, PublicKeyCredentialCreationOptions> registrationCache = Caffeine.newBuilder()
			.maximumSize(100).expireAfterWrite(5, TimeUnit.MINUTES).build();

	private final Cache<String, AssertionRequest> assertionCache = Caffeine.newBuilder().maximumSize(100)
			.expireAfterWrite(5, TimeUnit.MINUTES).build();

	private final Cache<String, String> deviceNameCache = Caffeine.newBuilder().maximumSize(100)
			.expireAfterWrite(5, TimeUnit.MINUTES).build();

	public void addRegistrationOptions(String username, PublicKeyCredentialCreationOptions options) {
		registrationCache.put(username, options);
	}

	public Optional<PublicKeyCredentialCreationOptions> getRegistrationOptions(String username) {
		return Optional.ofNullable(registrationCache.getIfPresent(username));
	}

	public void addAssertionRequest(String username, AssertionRequest request) {
		assertionCache.put(username, request);
	}

	public Optional<AssertionRequest> getAssertionRequest(String username) {
		return Optional.ofNullable(assertionCache.getIfPresent(username));
	}

	public void addDeviceName(String username, String deviceName) {
		deviceNameCache.put(username, deviceName);
	}

	public Optional<String> getDeviceName(String username) {
		return Optional.ofNullable(deviceNameCache.getIfPresent(username));
	}

	public void removeDeviceName(String username) {
		deviceNameCache.invalidate(username);
	}

	public void removeChallenge(String username) {
		registrationCache.invalidate(username);
		assertionCache.invalidate(username);
	}
}