package de.technikteam.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single Passkey (WebAuthn) credential record from the
 * `user_passkeys` table.
 */
public class PasskeyCredential {
	private int id;
	private int userId;
	private String name;
	private String credentialId;
	private String publicKey;
	private long signatureCount;
	private String userHandle;
	private LocalDateTime createdAt;

	public PasskeyCredential(int id, int userId, String name, String credentialId, String publicKey,
			long signatureCount, String userHandle, LocalDateTime createdAt) {
		this.id = id;
		this.userId = userId;
		this.name = name;
		this.credentialId = credentialId;
		this.publicKey = publicKey;
		this.signatureCount = signatureCount;
		this.userHandle = userHandle;
		this.createdAt = createdAt;
	}

	public PasskeyCredential() {
	}

	public String getFormattedCreatedAt() {
		return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "N/A";
	}

	// --- Getters and Setters ---
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

	public String getUserHandle() {
		return userHandle;
	}

	public void setUserHandle(String userHandle) {
		this.userHandle = userHandle;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}