package de.technikteam.security.passkey;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import de.technikteam.dao.PasskeyDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.PasskeyCredential;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PasskeyCredentialRepository implements CredentialRepository {

	private final PasskeyDAO passkeyDAO;
	private final UserDAO userDAO;

	@Autowired
	public PasskeyCredentialRepository(PasskeyDAO passkeyDAO, UserDAO userDAO) {
		this.passkeyDAO = passkeyDAO;
		this.userDAO = userDAO;
	}

	private ByteArray toByteArray(String base64urlString) {
		return new ByteArray(Base64.getUrlDecoder().decode(base64urlString));
	}

	@Override
	public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
		User user = userDAO.getUserByUsername(username);
		if (user == null) {
			return Set.of();
		}
		return passkeyDAO.getCredentialsByUserId(user.getId()).stream()
				.map(cred -> PublicKeyCredentialDescriptor.builder().id(toByteArray(cred.getCredentialId())).build())
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<ByteArray> getUserHandleForUsername(String username) {
		User user = userDAO.getUserByUsername(username);
		// We use username as the user handle for simplicity
		return Optional.ofNullable(user).map(u -> new ByteArray(u.getUsername().getBytes()));
	}

	@Override
	public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
		String username = new String(userHandle.getBytes());
		User user = userDAO.getUserByUsername(username);
		return Optional.ofNullable(user).map(User::getUsername);
	}

	@Override
	public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
		String credIdStr = Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId.getBytes());
		PasskeyCredential cred = passkeyDAO.getCredentialById(credIdStr);

		return Optional.ofNullable(cred)
				.map(c -> RegisteredCredential.builder().credentialId(toByteArray(c.getCredentialId()))
						.userHandle(toByteArray(c.getUserHandle())).publicKeyCose(toByteArray(c.getPublicKey()))
						.signatureCount(c.getSignatureCount()).build());
	}

	@Override
	public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
		String credIdStr = Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId.getBytes());
		PasskeyCredential cred = passkeyDAO.getCredentialById(credIdStr);

		if (cred == null) {
			return Set.of();
		}

		return Set.of(RegisteredCredential.builder().credentialId(toByteArray(cred.getCredentialId()))
				.userHandle(toByteArray(cred.getUserHandle())).publicKeyCose(toByteArray(cred.getPublicKey()))
				.signatureCount(cred.getSignatureCount()).build());
	}
}