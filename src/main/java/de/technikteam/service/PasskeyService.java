package de.technikteam.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import de.technikteam.dao.PasskeyDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.PasskeyCredential;
import de.technikteam.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PasskeyService {

    private final RelyingParty relyingParty;
    private final PasskeyDAO passkeyDAO;
    private final UserDAO userDAO;

    private static final String REGISTRATION_REQUEST_KEY = "PASSKEY_REGISTRATION_REQUEST";
    private static final String AUTHENTICATION_REQUEST_KEY = "PASSKEY_AUTHENTICATION_REQUEST";

    @Autowired
    public PasskeyService(RelyingParty relyingParty, PasskeyDAO passkeyDAO, UserDAO userDAO) {
        this.relyingParty = relyingParty;
        this.passkeyDAO = passkeyDAO;
        this.userDAO = userDAO;
    }

    public PublicKeyCredentialCreationOptions startRegistration(User user, HttpSession session) throws JsonProcessingException {
        UserIdentity userIdentity = UserIdentity.builder()
                .name(user.getUsername())
                .displayName(user.getUsername())
                .id(new ByteArray(String.valueOf(user.getId()).getBytes()))
                .build();

        StartRegistrationOptions options = StartRegistrationOptions.builder()
                .user(userIdentity)
                .build();

        PublicKeyCredentialCreationOptions creationOptions = relyingParty.startRegistration(options);
        session.setAttribute(REGISTRATION_REQUEST_KEY, creationOptions.toJson());
        return creationOptions;
    }

    @Transactional
    public void finishRegistration(PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential, HttpSession session, User user, String deviceName, HttpServletRequest httpRequest) throws RegistrationFailedException, JsonProcessingException {
        String requestJson = (String) session.getAttribute(REGISTRATION_REQUEST_KEY);
        if (requestJson == null) {
            throw new IllegalStateException("No registration in progress.");
        }
        PublicKeyCredentialCreationOptions requestOptions = PublicKeyCredentialCreationOptions.fromJson(requestJson);

        RegistrationResult result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                .request(requestOptions)
                .response(credential)
                .build());

        PasskeyCredential newCredential = new PasskeyCredential();
        newCredential.setUserId(user.getId());
        newCredential.setDeviceName(deviceName != null ? deviceName : "New Device");
        newCredential.setCredentialId(result.getKeyId().getId());
        newCredential.setUserHandle(requestOptions.getUser().getId());
        newCredential.setPublicKeyCose(result.getPublicKeyCose());
        newCredential.setSignatureCount(result.getSignatureCount());
        newCredential.setUserAgent(httpRequest.getHeader("User-Agent"));
        passkeyDAO.saveCredential(newCredential);

        session.removeAttribute(REGISTRATION_REQUEST_KEY);
    }

    public PublicKeyCredentialRequestOptions startAuthentication(String username, HttpSession session) throws JsonProcessingException {
        StartAssertionOptions options = StartAssertionOptions.builder()
                .username(Optional.ofNullable(username).filter(s -> !s.isBlank()))
                .build();
        AssertionRequest assertionRequest = relyingParty.startAssertion(options);
        session.setAttribute(AUTHENTICATION_REQUEST_KEY, assertionRequest.toJson());
        return assertionRequest.getPublicKeyCredentialRequestOptions();
    }

    @Transactional
    public User finishAuthentication(PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential, HttpSession session) throws AssertionFailedException, JsonProcessingException {
        String requestJson = (String) session.getAttribute(AUTHENTICATION_REQUEST_KEY);
        if (requestJson == null) {
            throw new IllegalStateException("No authentication in progress.");
        }
        AssertionRequest request = AssertionRequest.fromJson(requestJson);

        AssertionResult result;
        try {
            result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(request)
                    .response(credential)
                    .build());
        } catch (AssertionFailedException e) {
            // Intercept specific technical errors and replace them with user-friendly messages.
            if (e.getMessage() != null && e.getMessage().contains("Unrequested credential ID")) {
                throw new AssertionFailedException("Verifizierung fehlgeschlagen: Dieser Passkey ist nicht mehr aktiv. Gründe hierfür können sein, dass du ihn gelöscht hast oder er zu einem anderen Benutzerkonto gehört.");
            }
            // Re-throw other assertion failures
            throw e;
        }

        if (result.isSuccess()) {
            passkeyDAO.updateSignatureCount(result.getCredential().getCredentialId(), result.getSignatureCount());
            passkeyDAO.updateLastUsedTimestamp(result.getCredential().getCredentialId());
            Optional<String> username = passkeyDAO.getUsernameForUserHandle(result.getCredential().getUserHandle());
            if (username.isPresent()) {
                return userDAO.getUserByUsername(username.get());
            }
        }
        return null;
    }
}