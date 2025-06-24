package de.technikteam.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import com.yubico.webauthn.exception.VerificationFailedException;
import de.technikteam.dao.PasskeyCredentialDAO;
import de.technikteam.model.PasskeyCredential;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A service to handle the business logic of WebAuthn (Passkey) authentication,
 * using the Yubico library.
 */
public class PasskeyService {
	private static final Logger logger = LogManager.getLogger(PasskeyService.class);
	private final RelyingParty relyingParty;
	private final PasskeyCredentialDAO passkeyDAO = new PasskeyCredentialDAO();

	// --- Singleton Pattern ---
	private static PasskeyService INSTANCE;

	private PasskeyService() {
		// This should be configured from a properties file in a real application
		RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder().id("localhost") // The domain where the app is
																							// running
				.name("TechnikTeam Web App").build();

		this.relyingParty = RelyingParty.builder().identity(rpIdentity)
				.credentialRepository(new InMemoryPasskeyRepository(passkeyDAO)).allowOriginPort(true) // Allow
																										// development
																										// on
																										// localhost:8080
				.allowOriginSubdomain(false).build();
	}

	public static synchronized PasskeyService getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PasskeyService();
		}
		return INSTANCE;
	}

	// --- Registration Logic ---
	public String startRegistration(User user) throws JsonProcessingException {
		List<PasskeyCredential> existingCredentials = passkeyDAO.getCredentialsForUser(user.getId());
		List<PublicKeyCredentialDescriptor> excludeCredentials = existingCredentials.stream()
				.map(cred -> PublicKeyCredentialDescriptor.builder().id(ByteArray.fromBase64Url(cred.getCredentialId()))
						.build())
				.collect(Collectors.toList());

		StartRegistrationOptions options = StartRegistrationOptions.builder()
				.user(UserIdentity.builder().name(user.getUsername()).displayName(user.getUsername())
						.id(new ByteArray(user.getUsername().getBytes())).build())
				.authenticatorSelection(AuthenticatorSelectionCriteria.builder()
						.authenticatorAttachment(AuthenticatorAttachment.PLATFORM)
						.residentKey(ResidentKeyRequirement.PREFERRED)
						.userVerification(UserVerificationRequirement.PREFERRED).build())
				.excludeCredentials(excludeCredentials).build();

		PublicKeyCredentialCreationOptions registration = relyingParty.startRegistration(options);
		logger.info("Started passkey registration for user: {}", user.getUsername());
		return registration.toJson();
	}

	public boolean finishRegistration(String credentialJson, String requestJson, User user, String passkeyName) {
		try {
			PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential = PublicKeyCredential
					.parseRegistrationResponseJson(credentialJson);
			FinishRegistrationOptions options = FinishRegistrationOptions.builder()
					.request(PublicKeyCredentialCreationOptions.fromJson(requestJson)).response(credential).build();
			RegistrationResult result = relyingParty.finishRegistration(options);

			PasskeyCredential newCredential = new PasskeyCredential();
			newCredential.setUserId(user.getId());
			newCredential.setName(passkeyName);
			newCredential.setCredentialId(result.getKeyId().getId().getBase64Url());
			newCredential.setPublicKey(result.getPublicKeyCose().getBase64());
			newCredential.setSignatureCount(result.getSignatureCount());
			newCredential.setUserHandle(credential.getResponse().getUserHandle().get().getBase64Url());
			passkeyDAO.addCredential(newCredential);

			logger.info("Successfully finished passkey registration for user: {}", user.getUsername());
			return true;
		} catch (RegistrationFailedException | IOException e) {
			logger.error("Passkey registration failed for user {}", user.getUsername(), e);
			return false;
		}
	}

	// --- Authentication Logic ---
	public String startAuthentication() throws JsonProcessingException {
		StartAuthenticationOptions options = StartAuthenticationOptions.builder()
				.userVerification(UserVerificationRequirement.PREFERRED).build();
		PublicKeyCredentialRequestOptions requestOptions = relyingParty.startAuthentication(options);
		logger.info("Started passkey authentication ceremony.");
		return requestOptions.toJson();
	}

	public Optional<User> finishAuthentication(String credentialJson, String requestJson, String userHandle) {
		try {
			PublicKeyCredential<AuthenticatorAssertionResponse, ClientAuthenticationExtensionOutputs> credential = PublicKeyCredential
					.parseAssertionResponseJson(credentialJson);
			FinishAuthenticationOptions options = FinishAuthenticationOptions.builder()
					.request(PublicKeyCredentialRequestOptions.fromJson(requestJson)).response(credential).build();
			AuthenticationResult result = relyingParty.finishAuthentication(options);

			if (result.isSuccess()) {
				passkeyDAO.updateSignatureCount(result.getCredential().getCredentialId().getBase64Url(),
						result.getSignatureCount());
				UserDAO userDAO = new UserDAO();
				User user = userDAO.getUserByUsername(result.getUsername());
				logger.info("Passkey authentication successful for user: {}", result.getUsername());
				return Optional.ofNullable(user);
			}
		} catch (VerificationFailedException | IOException e) {
			logger.error("Passkey authentication failed for user handle: {}", userHandle, e);
		}
		return Optional.empty();
	}

	// --- Yubico Library Helper Class ---
	private static class InMemoryPasskeyRepository implements CredentialRepository {
		private final PasskeyCredentialDAO passkeyDAO;

		public InMemoryPasskeyRepository(PasskeyCredentialDAO passkeyDAO) {
			this.passkeyDAO = passkeyDAO;
		}

		@Override
		public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
			User user = new UserDAO().getUserByUsername(username);
			if (user != null) {
				return passkeyDAO.getCredentialsForUser(user.getId()).stream().map(c -> PublicKeyCredentialDescriptor
						.builder().id(ByteArray.fromBase64Url(c.getCredentialId())).build())
						.collect(Collectors.toSet());
			}
			return Set.of();
		}

		@Override
		public Optional<ByteArray> getUserHandleForUsername(String username) {
			User user = new UserDAO().getUserByUsername(username);
			if (user != null) {
				List<PasskeyCredential> creds = passkeyDAO.getCredentialsForUser(user.getId());
				if (!creds.isEmpty()) {
					return Optional.of(ByteArray.fromBase64Url(creds.get(0).getUserHandle()));
				}
			}
			return Optional.empty();
		}

		@Override
		public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
			return passkeyDAO.getCredentialsForUser(0).stream() // Hacky way to check all
					.filter(c -> c.getUserHandle().equals(userHandle.getBase64Url()))
					.map(c -> new UserDAO().getUserById(c.getUserId()).getUsername()).findFirst();
		}

		@Override
		public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
			return passkeyDAO.getCredentialById(credentialId.getBase64Url())
					.map(c -> RegisteredCredential.builder().credentialId(ByteArray.fromBase64Url(c.getCredentialId()))
							.userHandle(ByteArray.fromBase64Url(c.getUserHandle()))
							.publicKeyCose(ByteArray.fromBase64(c.getPublicKey())).signatureCount(c.getSignatureCount())
							.build());
		}

		@Override
		public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
			return passkeyDAO.getCredentialById(credentialId.getBase64Url()).stream()
					.map(c -> RegisteredCredential.builder().credentialId(ByteArray.fromBase64Url(c.getCredentialId()))
							.userHandle(ByteArray.fromBase64Url(c.getUserHandle()))
							.publicKeyCose(ByteArray.fromBase64(c.getPublicKey())).signatureCount(c.getSignatureCount())
							.build())
					.collect(Collectors.toSet());
		}
	}
}