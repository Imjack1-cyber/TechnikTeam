package de.technikteam.dao;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import de.technikteam.model.PasskeyCredential;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PasskeyDAO implements CredentialRepository {
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

	public Optional<PasskeyCredential> getCredentialByCredentialId(ByteArray credentialId) {
		String sql = "SELECT * FROM user_passkeys WHERE credential_id = ?";
		try {
			return Optional
					.ofNullable(jdbcTemplate.queryForObject(sql, credentialRowMapper, credentialId.getBase64Url()));
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

	public boolean updateSignatureCount(ByteArray credentialId, long newSignatureCount) {
		String sql = "UPDATE user_passkeys SET signature_count = ? WHERE credential_id = ?";
		try {
			return jdbcTemplate.update(sql, newSignatureCount, credentialId.getBase64Url()) > 0;
		} catch (Exception e) {
			logger.error("Error updating signature count for credential", e);
			return false;
		}
	}

	@Override
	public Set<RegisteredCredential> getCredentialsByUserHandle(ByteArray userHandle) {
		String sql = "SELECT * FROM user_passkeys WHERE user_handle = ?";
		List<PasskeyCredential> creds = jdbcTemplate.query(sql, credentialRowMapper, userHandle.getBase64Url());
		return creds.stream()
				.map(dbCred -> RegisteredCredential.builder()
						.credentialId(ByteArray.fromBase64Url(dbCred.getCredentialId()))
						.userHandle(ByteArray.fromBase64Url(dbCred.getUserHandle()))
						.publicKeyCose(ByteArray.fromBase64Url(dbCred.getPublicKey()))
						.signatureCount(dbCred.getSignatureCount()).build())
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<RegisteredCredential> getCredentialById(ByteArray credentialId) {
		return getCredentialByCredentialId(credentialId).map(
				dbCred -> RegisteredCredential.builder().credentialId(ByteArray.fromBase64Url(dbCred.getCredentialId()))
						.userHandle(ByteArray.fromBase64Url(dbCred.getUserHandle()))
						.publicKeyCose(ByteArray.fromBase64Url(dbCred.getPublicKey()))
						.signatureCount(dbCred.getSignatureCount()).build());
	}

	@Override
	public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
		String sql = "SELECT u.username FROM users u JOIN user_passkeys up ON u.id = up.user_id WHERE up.user_handle = ? LIMIT 1";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, String.class, userHandle.getBase64Url()));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<UserIdentity> getUserIdentityByUserHandle(ByteArray userHandle) {
		String sql = "SELECT u.username FROM users u JOIN user_passkeys up ON u.id = up.user_id WHERE up.user_handle = ? LIMIT 1";
		try {
			return Optional
					.ofNullable(jdbcTemplate.queryForObject(sql,
							(rs, rowNum) -> UserIdentity.builder().name(rs.getString("username"))
									.displayName(rs.getString("username")).id(userHandle).build(),
							userHandle.getBase64Url()));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public Set<RegisteredCredential> getCredentialsByUsername(String username) {
		String sql = "SELECT up.* FROM user_passkeys up JOIN users u ON up.user_id = u.id WHERE u.username = ?";
		List<PasskeyCredential> dbCreds = jdbcTemplate.query(sql, credentialRowMapper, username);
		return dbCreds.stream()
				.map(dbCred -> RegisteredCredential.builder()
						.credentialId(ByteArray.fromBase64Url(dbCred.getCredentialId()))
						.userHandle(ByteArray.fromBase64Url(dbCred.getUserHandle()))
						.publicKeyCose(ByteArray.fromBase64Url(dbCred.getPublicKey()))
						.signatureCount(dbCred.getSignatureCount()).build())
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<ByteArray> getUserHandleForUsername(String username) {
		String sql = "SELECT up.user_handle FROM user_passkeys up JOIN users u ON up.user_id = u.id WHERE u.username = ? LIMIT 1";
		try {
			String handle = jdbcTemplate.queryForObject(sql, String.class, username);
			return Optional.ofNullable(handle).map(ByteArray::fromBase64Url);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public Set<ByteArray> getCredentialIdsForUsername(String username) {
		String sql = "SELECT up.credential_id FROM user_passkeys up JOIN users u ON up.user_id = u.id WHERE u.username = ?";
		return jdbcTemplate.queryForList(sql, String.class, username).stream().map(ByteArray::fromBase64Url)
				.collect(Collectors.toSet());
	}
}