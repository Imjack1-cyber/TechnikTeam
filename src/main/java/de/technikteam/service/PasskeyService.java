package de.technikteam.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import de.technikteam.dao.PasskeyDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.PasskeyCredential;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasskeyService {
	private static final Logger logger = LogManager.getLogger(PasskeyService.class);

	private final RelyingParty relyingParty;
	private final PasskeyDAO passkeyDAO;
	private final UserDAO userDAO;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	public PasskeyService(RelyingParty relyingParty, PasskeyDAO passkeyDAO, UserDAO userDAO) {
		this.relyingParty = relyingParty;
		this.passkeyDAO = passkeyDAO;
		this.userDAO = userDAO;
	}

	public PublicKeyCredentialCreationOptions startRegistration(User user) {
		StartRegistrationOptions options = StartRegistrationOptions.builder()
				.user(UserIdentity.builder().name(user.getUsername()).displayName(user.getUsername())
						.id(new ByteArray(user.getUsername().getBytes())).build())
				.build();
		return relyingParty.startRegistration(options);
	}

	@Transactional
	public boolean finishRegistration(String responseJson, String deviceName, User user,
			PublicKeyCredentialCreationOptions registrationRequest) throws RegistrationFailedException, IOException {
		PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc = PublicKeyCredential
				.parseRegistrationResponseJson(responseJson);

		RegistrationResult result = relyingParty.finishRegistration(
				FinishRegistrationOptions.builder().request(registrationRequest).response(pkc).build());

		PasskeyCredential credential = new PasskeyCredential();
		credential.setUserId(user.getId());
		credential.setName(deviceName);
		credential.setUserHandle(new String(registrationRequest.getUser().getId().getBytes()));
		credential.setCredentialId(
				Base64.getUrlEncoder().withoutPadding().encodeToString(result.getKeyId().getId().getBytes()));
		credential.setPublicKey(
				Base64.getUrlEncoder().withoutPadding().encodeToString(result.getPublicKeyCose().getBytes()));
		credential.setSignatureCount(result.getSignatureCount());

		passkeyDAO.saveCredential(credential);
		logger.info("Successfully registered passkey '{}' for user '{}'", deviceName, user.getUsername());
		return true;
	}

	public AssertionRequest startAuthentication(String username) {
		return relyingParty.startAssertion(StartAssertionOptions.builder().username(Optional.of(username)).build());
	}

	@Transactional
	public User finishAuthentication(String responseJson, AssertionRequest assertionRequest)
			throws AssertionFailedException, IOException {
		PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc = PublicKeyCredential
				.parseAssertionResponseJson(responseJson);

		AssertionResult result = relyingParty
				.finishAssertion(FinishAssertionOptions.builder().request(assertionRequest).response(pkc).build());

		if (result.isSuccess()) {
			String credentialId = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(result.getCredential().getCredentialId().getBytes());
			passkeyDAO.updateSignatureCount(credentialId, result.getSignatureCount());
			logger.info("Successfully authenticated user '{}' with passkey.", result.getUsername());
			return userDAO.getUserByUsername(result.getUsername());
		} else {
			return null;
		}
	}

	/**
	 * Extracts the username from the credential response before the ceremony is
	 * complete. This is a workaround for not having a session to store the
	 * username.
	 */
	public String getUsernameFromLoginCredential(String credentialJson) throws IOException {
		JsonNode root = objectMapper.readTree(credentialJson);
		JsonNode response = root.path("response");
		if (response.has("userHandle")) {
			String userHandleBase64 = response.get("userHandle").asText();
			if (userHandleBase64 == null || userHandleBase64.isEmpty()) {
				// Some authenticators might send an empty userHandle for resident keys.
				// In this case, we need to look up the user by credential ID.
				String credentialIdBase64 = root.path("id").asText();
				if (credentialIdBase64 != null && !credentialIdBase64.isEmpty()) {
					PasskeyCredential cred = passkeyDAO.getCredentialById(credentialIdBase64);
					if (cred != null) {
						return userDAO.getUserById(cred.getUserId()).getUsername();
					}
				}
				return null;
			}
			byte[] userHandleBytes = Base64.getUrlDecoder().decode(userHandleBase64);
			return new String(userHandleBytes);
		}
		return null;
	}
}