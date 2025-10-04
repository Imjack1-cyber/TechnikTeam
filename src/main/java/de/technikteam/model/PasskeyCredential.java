package de.technikteam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Represents a WebAuthn/Passkey credential stored in the `user_passkeys` table.
 */
public class PasskeyCredential {
	private long id;
	private int userId;
	private String deviceName;
	private ByteArray userHandle;
	private ByteArray credentialId;
	private ByteArray publicKeyCose;
	private long signatureCount;
	private LocalDateTime createdAt;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public ByteArray getUserHandle() {
		return userHandle;
	}

	public void setUserHandle(ByteArray userHandle) {
		this.userHandle = userHandle;
	}

	public ByteArray getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(ByteArray credentialId) {
		this.credentialId = credentialId;
	}

	public ByteArray getPublicKeyCose() {
		return publicKeyCose;
	}

	public void setPublicKeyCose(ByteArray publicKeyCose) {
		this.publicKeyCose = publicKeyCose;
	}

	public long getSignatureCount() {
		return signatureCount;
	}

	public void setSignatureCount(long signatureCount) {
		this.signatureCount = signatureCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}