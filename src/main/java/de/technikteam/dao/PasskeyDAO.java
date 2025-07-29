package de.technikteam.dao;

import de.technikteam.model.PasskeyCredential;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class PasskeyDAO {
	private static final Logger logger = LogManager.getLogger(PasskeyDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public PasskeyDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<PasskeyCredential> credentialRowMapper = (rs, rowNum) -> new PasskeyCredential(
			rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"), rs.getString("user_handle"),
			rs.getString("credential_id"), rs.getString("public_key"), rs.getLong("signature_count"),
			rs.getTimestamp("created_at").toLocalDateTime());

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
		String sql = "SELECT * FROM user_passkeys WHERE user_id = ?";
		try {
			return jdbcTemplate.query(sql, credentialRowMapper, userId);
		} catch (Exception e) {
			logger.error("Error retrieving passkey credentials for user {}", userId, e);
			return List.of();
		}
	}

	public Optional<PasskeyCredential> getCredentialById(String credentialId) {
		String sql = "SELECT * FROM user_passkeys WHERE credential_id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, credentialRowMapper, credentialId));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		} catch (Exception e) {
			logger.error("Error retrieving credential by ID", e);
			return Optional.empty();
		}
	}

	public boolean deleteCredential(int credentialDbId, int userId) {
		String sql = "DELETE FROM user_passkeys WHERE id = ? AND user_id = ?";
		try {
			return jdbcTemplate.update(sql, credentialDbId, userId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting credential ID {} for user {}", credentialDbId, userId, e);
			return false;
		}
	}

	public boolean updateSignatureCount(String credentialId, long newSignatureCount) {
		String sql = "UPDATE user_passkeys SET signature_count = ? WHERE credential_id = ?";
		try {
			return jdbcTemplate.update(sql, newSignatureCount, credentialId) > 0;
		} catch (Exception e) {
			logger.error("Error updating signature count for credential", e);
			return false;
		}
	}
}