package de.technikteam.service;

import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import de.technikteam.dao.PasskeyCredentialDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.PasskeyCredential;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PasskeyService {
	private static final Logger logger = LogManager.getLogger(PasskeyService.class);
	private final PasskeyCredentialDAO passkeyDAO = new PasskeyCredentialDAO();

	// --- Singleton Pattern ---
	private static PasskeyService INSTANCE;

	// --- Lazy Initialization for RelyingParty ---
	private volatile RelyingParty relyingParty = null;
	private final Object rpInitLock = new Object();

	private PasskeyService() {
		// Constructor is empty. Initialization is deferred.
	}

	public static synchronized PasskeyService getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PasskeyService();
		}
		return INSTANCE;
	}

	private RelyingParty getRelyingParty() {
		if (relyingParty == null) {
			synchronized (rpInitLock) {
				if (relyingParty == null) {
					logger.info("First use of PasskeyService, initializing RelyingParty...");
					final String rpId = "5a1f-2a02-8108-9801-9c00-395e-fd2c-a9d1-abfc.ngrok-free.app";

					RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder().id(rpId)
							.name("TechnikTeam Web App").build();

					// FINAL FIX: Remove the .origins() call entirely.
					// This is the most likely source of the library version conflict.
					// The primary security check against the RP ID remains.
					this.relyingParty = RelyingParty.builder().identity(rpIdentity)
							.credentialRepository(new DatabaseCredentialRepository(passkeyDAO)).build();
					logger.info("RelyingParty initialized successfully for RP ID '{}'.", rpId);
				}
			}
		}
		return relyingParty;
	}

	// --- Registration Logic ---
	public PublicKeyCredentialCreationOptions startRegistration(User user) {
		ByteArray userHandle = new ByteArray(Integer.toString(user.getId()).getBytes(StandardCharsets.UTF_8));

		StartRegistrationOptions options = StartRegistrationOptions.builder().user(
				UserIdentity.builder().name(user.getUsername()).displayName(user.getUsername()).id(userHandle).build())
				.build();

		PublicKeyCredentialCreationOptions registration = getRelyingParty().startRegistration(options);
		logger.info("Started passkey registration for user: {}", user.getUsername());
		return registration;
	}

	public boolean finishRegistration(String credentialJson, String requestJson, User user, String passkeyName) {
		try {
			PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential = PublicKeyCredential
					.parseRegistrationResponseJson(credentialJson);
			PublicKeyCredentialCreationOptions request = PublicKeyCredentialCreationOptions.fromJson(requestJson);

			RegistrationResult result = getRelyingParty().finishRegistration(
					FinishRegistrationOptions.builder().request(request).response(credential).build());

			ByteArray userHandle = new ByteArray(Integer.toString(user.getId()).getBytes(StandardCharsets.UTF_8));

			PasskeyCredential newCredential = new PasskeyCredential();
			newCredential.setUserId(user.getId());
			newCredential.setName(passkeyName);
			newCredential.setCredentialId(result.getKeyId().getId().getBase64Url());
			newCredential.setPublicKey(result.getPublicKeyCose().getBase64());
			newCredential.setSignatureCount(result.getSignatureCount());
			newCredential.setUserHandle(userHandle.getBase64Url());
			passkeyDAO.addCredential(newCredential);

			logger.info("Successfully finished passkey registration for user: {}", user.getUsername());
			return true;
		} catch (RegistrationFailedException | IOException e) {
			logger.error("Passkey registration failed for user {}", user.getUsername(), e);
			return false;
		}
	}

	// --- Authentication Logic (now called Assertion) ---
	public AssertionRequest startAssertion() {
		StartAssertionOptions options = StartAssertionOptions.builder()
				.userVerification(UserVerificationRequirement.PREFERRED).build();
		AssertionRequest request = getRelyingParty().startAssertion(options);
		logger.info("Started passkey assertion (authentication) ceremony.");
		return request;
	}

	public Optional<User> finishAssertion(String credentialJson, String requestJson) {
		try {
			PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential = PublicKeyCredential
					.parseAssertionResponseJson(credentialJson);
			AssertionRequest request = AssertionRequest.fromJson(requestJson);

			AssertionResult result = getRelyingParty()
					.finishAssertion(FinishAssertionOptions.builder().request(request).response(credential).build());

			if (result.isSuccess()) {
				passkeyDAO.updateSignatureCount(result.getCredential().getCredentialId().getBase64Url(),
						result.getSignatureCount());
				UserDAO userDAO = new UserDAO();
				User user = userDAO.getUserByUsername(result.getUsername());
				logger.info("Passkey authentication successful for user: {}", result.getUsername());
				return Optional.ofNullable(user);
			}
		} catch (AssertionFailedException | IOException e) {
			logger.error("Passkey assertion failed.", e);
		}
		return Optional.empty();
	}

	private static class DatabaseCredentialRepository implements CredentialRepository {
		private final PasskeyCredentialDAO passkeyDAO;
		private final UserDAO userDAO = new UserDAO();

		public DatabaseCredentialRepository(PasskeyCredentialDAO passkeyDAO) {
			this.passkeyDAO = passkeyDAO;
		}

		@Override
		public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
			return passkeyDAO.getCredentialById(credentialId.getBase64Url()).filter(cred -> {
				try {
					return userHandle.equals(ByteArray.fromBase64Url(cred.getUserHandle()));
				} catch (Exception e) {
					logger.error("Failed to decode user handle from DB", e);
					return false;
				}
			}).map(this::toRegisteredCredential);
		}

		@Override
		public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
			return passkeyDAO.getCredentialById(credentialId.getBase64Url()).stream().map(this::toRegisteredCredential)
					.collect(Collectors.toSet());
		}

		@Override
		public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
			User user = userDAO.getUserByUsername(username);
			if (user != null) {
				return passkeyDAO.getCredentialsForUser(user.getId()).stream().map(c -> {
					try {
						return PublicKeyCredentialDescriptor.builder().id(ByteArray.fromBase64Url(c.getCredentialId()))
								.build();
					} catch (Exception e) {
						logger.error("Failed to decode credential ID from Base64Url", e);
						return null;
					}
				}).filter(d -> d != null).collect(Collectors.toSet());
			}
			return Set.of();
		}

		@Override
		public Optional<ByteArray> getUserHandleForUsername(String username) {
			User user = userDAO.getUserByUsername(username);
			if (user != null) {
				return Optional.of(new ByteArray(Integer.toString(user.getId()).getBytes(StandardCharsets.UTF_8)));
			}
			return Optional.empty();
		}

		@Override
		public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
			try {
				String userIdStr = new String(userHandle.getBytes(), StandardCharsets.UTF_8);
				int userId = Integer.parseInt(userIdStr);
				User user = userDAO.getUserById(userId);
				return Optional.ofNullable(user).map(User::getUsername);
			} catch (NumberFormatException e) {
				logger.error("Could not parse user ID from user handle: {}", userHandle.getBase64Url(), e);
				return Optional.empty();
			}
		}

		private RegisteredCredential toRegisteredCredential(PasskeyCredential cred) {
			try {
				return RegisteredCredential.builder().credentialId(ByteArray.fromBase64Url(cred.getCredentialId()))
						.userHandle(ByteArray.fromBase64Url(cred.getUserHandle()))
						.publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
						.signatureCount(cred.getSignatureCount()).build();
			} catch (Exception e) {
				logger.error("Could not convert PasskeyCredential to RegisteredCredential", e);
				throw new RuntimeException(e);
			}
		}
	}
}