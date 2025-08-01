package de.technikteam.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import com.yubico.webauthn.exception.VerificationFailedException;
import de.technikteam.dao.PasskeyDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.PasskeyCredential;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class PasskeyService {
	private static final Logger logger = LogManager.getLogger(PasskeyService.class);
	private final PasskeyDAO passkeyDAO;
	private final UserDAO userDAO;
	private final RelyingParty relyingParty;
	private final ChallengeRepository challengeRepository;

	@Autowired
	public PasskeyService(PasskeyDAO passkeyDAO, UserDAO userDAO, ChallengeRepository challengeRepository) {
		this.passkeyDAO = passkeyDAO;
		this.userDAO = userDAO;
		this.challengeRepository = challengeRepository;

		RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder().id("localhost") // Must match your domain in
																							// production
				.name("TechnikTeam").build();

		this.relyingParty = RelyingParty.builder().identity(rpIdentity).credentialRepository(this.passkeyDAO)
				.allowOriginPort(true) // For localhost development
				.build();
	}

	public String startRegistration(User user) throws JsonProcessingException {
		StartRegistrationOptions options = StartRegistrationOptions.builder().user(UserIdentity.builder()
				.name(user.getUsername()).displayName(user.getUsername()).id(UserHandle.of(user.getId())).build())
				.build();

		PublicKeyCredentialCreationOptions credentialCreationOptions = relyingParty.startRegistration(options);
		challengeRepository.addRegistrationOptions(user.getUsername(), credentialCreationOptions);

		return credentialCreationOptions.toJson();
	}

	public boolean finishRegistration(int userId, String username, String deviceName, String credentialJson) {
		try {
			PublicKeyCredentialCreationOptions registrationOptions = challengeRepository
					.getRegistrationOptions(username)
					.orElseThrow(() -> new RegistrationFailedException("No registration ceremony found for user."));

			PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc = PublicKeyCredential
					.parseRegistrationResponseJson(credentialJson);

			RegistrationResult result = relyingParty.finishRegistration(
					FinishRegistrationOptions.builder().request(pkc).response(registrationOptions).build());

			PasskeyCredential credential = new PasskeyCredential();
			credential.setUserId(userId);
			credential.setName(deviceName);
			credential.setUserHandle(registrationOptions.getUser().getId().getBase64Url());
			credential.setCredentialId(result.getKeyId().getId().getBase64Url());
			credential.setPublicKey(result.getPublicKeyCose().getBase64Url());
			credential.setSignatureCount(result.getSignatureCount());

			return passkeyDAO.saveCredential(credential);

		} catch (RegistrationFailedException | IOException e) {
			logger.error("Passkey registration failed for user {}: {}", username, e.getMessage(), e);
			return false;
		} finally {
			challengeRepository.removeChallenge(username);
		}
	}

	public String startAuthentication(String username) throws JsonProcessingException {
		AssertionRequest assertionRequest = relyingParty
				.startAssertion(StartAssertionOptions.builder().username(Optional.of(username)).build());

		challengeRepository.addAssertionRequest(username, assertionRequest);

		return assertionRequest.toJson();
	}

	public User finishAuthentication(String credentialJson) {
		String username = null;
		try {
			PublicKeyCredential<AuthenticatorAssertionResponse, ClientAuthenticationExtensionOutputs> pkc = PublicKeyCredential
					.parseAssertionResponseJson(credentialJson);

			Optional<ByteArray> userHandle = pkc.getResponse().getUserHandle();
			if (userHandle.isEmpty()) {
				throw new VerificationFailedException("User handle is missing from assertion.");
			}
			username = passkeyDAO.getUsernameForUserHandle(userHandle.get())
					.orElseThrow(() -> new VerificationFailedException("Unknown user handle."));

			AssertionRequest assertionRequest = challengeRepository.getAssertionRequest(username)
					.orElseThrow(() -> new VerificationFailedException("No assertion request found for user."));

			AssertionResult result = relyingParty
					.finishAssertion(FinishAssertionOptions.builder().request(pkc).response(assertionRequest).build());

			if (result.isSuccess()) {
				passkeyDAO.updateSignatureCount(result.getCredential().getCredentialId(), result.getSignatureCount());
				return userDAO.getUserByUsername(result.getUsername());
			} else {
				return null;
			}
		} catch (VerificationFailedException | IOException e) {
			logger.error("Passkey authentication failed for user {}: {}", username, e.getMessage(), e);
			return null;
		} finally {
			if (username != null) {
				challengeRepository.removeChallenge(username);
			}
		}
	}

}