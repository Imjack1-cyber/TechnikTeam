package de.technikteam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Represents a WebAuthn/Passkey credential stored in the `user_passkeys` table.
 */
public class PasskeyCredential {
	private int id;
	private int userId;
	private String name;
	private String userHandle;
	private String credentialId;
	private String publicKey;
	private long signatureCount;
	private LocalDateTime createdAt;

	@JsonIgnore
	public byte[] getUserHandleBytes() {
		return Base64.getUrlDecoder().decode(this.userHandle);
	}

	public void setUserHandleBytes(byte[] userHandleBytes) {
		this.userHandle = Base64.getUrlEncoder().withoutPadding().encodeToString(userHandleBytes);
	}

	@JsonIgnore
	public byte[] getCredentialIdBytes() {
		return Base64.getUrlDecoder().decode(this.credentialId);
	}

	public void setCredentialIdBytes(byte[] credentialIdBytes) {
		this.credentialId = Base64.getUrlEncoder().withoutPadding().encodeToString(credentialIdBytes);
	}

	@JsonIgnore
	public byte[] getPublicKeyBytes() {
		return Base64.getUrlDecoder().decode(this.publicKey);
	}

	public void setPublicKeyBytes(byte[] publicKeyBytes) {
		this.publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKeyBytes);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserHandle() {
		return userHandle;
	}

	public void setUserHandle(String userHandle) {
		this.userHandle = userHandle;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
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