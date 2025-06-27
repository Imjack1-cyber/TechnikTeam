package de.technikteam.dao;

import de.technikteam.model.PasskeyCredential;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for managing Passkey (WebAuthn) credentials in the
 * `user_passkeys` table.
 */
public class PasskeyCredentialDAO {
	private static final Logger logger = LogManager.getLogger(PasskeyCredentialDAO.class);

	/**
	 * Adds a new passkey credential to the database for a user.
	 *
	 * @param credential The PasskeyCredential object to save.
	 */
	public void addCredential(PasskeyCredential credential) {
		String sql = "INSERT INTO user_passkeys (user_id, name, credential_id, public_key, signature_count, user_handle) VALUES (?, ?, ?, ?, ?, ?)";
		logger.debug("Adding passkey for user ID {}", credential.getUserId());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, credential.getUserId());
			pstmt.setString(2, credential.getName());
			pstmt.setString(3, credential.getCredentialId());
			pstmt.setString(4, credential.getPublicKey());
			pstmt.setLong(5, credential.getSignatureCount());
			pstmt.setString(6, credential.getUserHandle());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error adding passkey credential for user ID {}", credential.getUserId(), e);
		}
	}

	/**
	 * Retrieves all passkey credentials registered for a specific user.
	 *
	 * @param userId The ID of the user.
	 * @return A list of PasskeyCredential objects.
	 */
	public List<PasskeyCredential> getCredentialsForUser(int userId) {
		List<PasskeyCredential> credentials = new ArrayList<>();
		String sql = "SELECT * FROM user_passkeys WHERE user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					credentials.add(mapResultSetToCredential(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching passkeys for user ID {}", userId, e);
		}
		return credentials;
	}

	/**
	 * Retrieves a passkey credential by its unique credential ID.
	 *
	 * @param credentialId The base64url-encoded credential ID.
	 * @return An Optional containing the PasskeyCredential if found.
	 */
	public Optional<PasskeyCredential> getCredentialById(String credentialId) {
		String sql = "SELECT * FROM user_passkeys WHERE credential_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, credentialId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToCredential(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching passkey by credential ID {}", credentialId, e);
		}
		return Optional.empty();
	}

	/**
	 * Retrieves a passkey credential by its user handle.
	 *
	 * @param userHandle The base64url-encoded user handle.
	 * @return An Optional containing the PasskeyCredential if found.
	 */
	public Optional<PasskeyCredential> getCredentialByUserHandle(String userHandle) {
		String sql = "SELECT * FROM user_passkeys WHERE user_handle = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userHandle);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToCredential(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching passkey by user handle {}", userHandle, e);
		}
		return Optional.empty();
	}

	/**
	 * Updates the signature count for a given credential after a successful login.
	 *
	 * @param credentialId      The ID of the credential to update.
	 * @param newSignatureCount The new signature count.
	 */
	public void updateSignatureCount(String credentialId, long newSignatureCount) {
		String sql = "UPDATE user_passkeys SET signature_count = ? WHERE credential_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, newSignatureCount);
			pstmt.setString(2, credentialId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error updating signature count for credential ID {}", credentialId, e);
		}
	}

	/**
	 * Deletes a passkey credential from the database.
	 *
	 * @param credentialId The ID of the credential record to delete.
	 * @param userId       The ID of the user who owns the credential.
	 * @return true if the deletion was successful, false otherwise.
	 */
	public boolean deleteCredential(int credentialId, int userId) {
		String sql = "DELETE FROM user_passkeys WHERE id = ? AND user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, credentialId);
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting passkey ID {} for user ID {}", credentialId, userId, e);
			return false;
		}
	}

	private PasskeyCredential mapResultSetToCredential(ResultSet rs) throws SQLException {
		return new PasskeyCredential(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"),
				rs.getString("credential_id"), rs.getString("public_key"), rs.getLong("signature_count"),
				rs.getString("user_handle"), rs.getTimestamp("created_at").toLocalDateTime());
	}
}