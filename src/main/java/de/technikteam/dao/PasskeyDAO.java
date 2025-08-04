package de.technikteam.dao;

import de.technikteam.model.PasskeyCredential;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PasskeyDAO {
	private static final Logger logger = LogManager.getLogger(PasskeyDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public PasskeyDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<PasskeyCredential> credentialRowMapper = (rs, rowNum) -> {
		PasskeyCredential cred = new PasskeyCredential();
		cred.setId(rs.getInt("id"));
		cred.setUserId(rs.getInt("user_id"));
		cred.setName(rs.getString("name"));
		cred.setUserHandle(rs.getString("user_handle"));
		cred.setCredentialId(rs.getString("credential_id"));
		cred.setPublicKey(rs.getString("public_key"));
		cred.setSignatureCount(rs.getLong("signature_count"));
		cred.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return cred;
	};

	public boolean saveCredential(PasskeyCredential credential) {
		String sql = "INSERT INTO user_passkeys (user_id, name, user_handle, credential_id, public_key, signature_count) VALUES (?, ?, ?, ?, ?, ?)";
		try {
			return jdbcTemplate.update(sql, credential.getUserId(), credential.getName(), credential.getUserHandle(),
					credential.getCredentialId(), credential.getPublicKey(), credential.getSignatureCount()) > 0;
		} catch (Exception e) {
			logger.error("Error saving passkey credential for user {}", credential.getUserId(), e);
			return false;
		}
	}

	public List<PasskeyCredential> getCredentialsByUserId(int userId) {
		String sql = "SELECT * FROM user_passkeys WHERE user_id = ? ORDER BY created_at DESC";
		try {
			return jdbcTemplate.query(sql, credentialRowMapper, userId);
		} catch (Exception e) {
			logger.error("Error fetching passkeys for user {}", userId, e);
			return List.of();
		}
	}

	public PasskeyCredential getCredentialById(String credentialId) {
		String sql = "SELECT * FROM user_passkeys WHERE credential_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, credentialRowMapper, credentialId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching passkey by credential ID", e);
			return null;
		}
	}

	public boolean deleteCredential(int credentialDbId, int userId) {
		String sql = "DELETE FROM user_passkeys WHERE id = ? AND user_id = ?";
		try {
			return jdbcTemplate.update(sql, credentialDbId, userId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting passkey ID {} for user {}", credentialDbId, userId, e);
			return false;
		}
	}

	public boolean updateSignatureCount(String credentialId, long newSignatureCount) {
		String sql = "UPDATE user_passkeys SET signature_count = ? WHERE credential_id = ?";
		try {
			return jdbcTemplate.update(sql, newSignatureCount, credentialId) > 0;
		} catch (Exception e) {
			logger.error("Error updating signature count for passkey", e);
			return false;
		}
	}
}