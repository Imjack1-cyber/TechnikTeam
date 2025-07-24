package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.PasskeyCredential;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class PasskeyDAO {
	private static final Logger logger = LogManager.getLogger(PasskeyDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public PasskeyDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public boolean saveCredential(PasskeyCredential credential) {
		String sql = "INSERT INTO user_passkeys (user_id, name, user_handle, credential_id, public_key, signature_count) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, credential.getUserId());
			pstmt.setString(2, credential.getName());
			pstmt.setString(3, credential.getUserHandle());
			pstmt.setString(4, credential.getCredentialId());
			pstmt.setString(5, credential.getPublicKey());
			pstmt.setLong(6, credential.getSignatureCount());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error saving passkey credential for user {}", credential.getUserId(), e);
			return false;
		}
	}

	public List<PasskeyCredential> getCredentialsByUserId(int userId) {
		List<PasskeyCredential> credentials = new ArrayList<>();
		String sql = "SELECT * FROM user_passkeys WHERE user_id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					credentials.add(mapResultSetToCredential(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error retrieving passkey credentials for user {}", userId, e);
		}
		return credentials;
	}

	public Optional<PasskeyCredential> getCredentialById(String credentialId) {
		String sql = "SELECT * FROM user_passkeys WHERE credential_id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, credentialId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToCredential(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error retrieving credential by ID", e);
		}
		return Optional.empty();
	}

	public boolean deleteCredential(int credentialDbId, int userId) {
		String sql = "DELETE FROM user_passkeys WHERE id = ? AND user_id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, credentialDbId);
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting credential ID {} for user {}", credentialDbId, userId, e);
			return false;
		}
	}

	public boolean updateSignatureCount(String credentialId, long newSignatureCount) {
		String sql = "UPDATE user_passkeys SET signature_count = ? WHERE credential_id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, newSignatureCount);
			pstmt.setString(2, credentialId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating signature count for credential", e);
			return false;
		}
	}

	private PasskeyCredential mapResultSetToCredential(ResultSet rs) throws SQLException {
		return new PasskeyCredential(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"),
				rs.getString("user_handle"), rs.getString("credential_id"), rs.getString("public_key"),
				rs.getLong("signature_count"), rs.getTimestamp("created_at").toLocalDateTime());
	}
}