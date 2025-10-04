package de.technikteam.dao;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import de.technikteam.model.PasskeyCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PasskeyDAO implements CredentialRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PasskeyDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PasskeyCredential> rowMapper = (rs, rowNum) -> {
        PasskeyCredential cred = new PasskeyCredential();
        cred.setId(rs.getLong("id"));
        cred.setUserId(rs.getInt("user_id"));
        cred.setDeviceName(rs.getString("device_name"));
        cred.setUserHandle(new ByteArray(rs.getBytes("user_handle")));
        cred.setCredentialId(new ByteArray(rs.getBytes("credential_id")));
        cred.setPublicKeyCose(new ByteArray(rs.getBytes("public_key")));
        cred.setSignatureCount(rs.getLong("signature_count"));
        cred.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return cred;
    };

    public boolean saveCredential(PasskeyCredential credential) {
        String sql = "INSERT INTO user_passkeys (user_id, device_name, credential_id, public_key, signature_count, user_handle) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                credential.getUserId(),
                credential.getDeviceName(),
                credential.getCredentialId().getBytes(),
                credential.getPublicKeyCose().getBytes(),
                credential.getSignatureCount(),
                credential.getUserHandle().getBytes()
        ) > 0;
    }
    
    public List<PasskeyCredential> getCredentialsByUserId(int userId) {
        String sql = "SELECT * FROM user_passkeys WHERE user_id = ?";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public boolean deleteCredential(long id, int userId) {
        String sql = "DELETE FROM user_passkeys WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, id, userId) > 0;
    }

    public boolean updateSignatureCount(ByteArray credentialId, long newSignatureCount) {
        String sql = "UPDATE user_passkeys SET signature_count = ? WHERE credential_id = ?";
        return jdbcTemplate.update(sql, newSignatureCount, credentialId.getBytes()) > 0;
    }
    
    public Set<RegisteredCredential> getCredentialsByUserHandle(ByteArray userHandle) {
        String sql = "SELECT * FROM user_passkeys WHERE user_handle = ?";
        List<PasskeyCredential> credentials = jdbcTemplate.query(sql, rowMapper, userHandle.getBytes());
        return credentials.stream()
                .map(this::toRegisteredCredential)
                .collect(Collectors.toSet());
    }

    public Set<RegisteredCredential> getCredentialsByUsername(String username) {
        String sql = "SELECT p.* FROM user_passkeys p JOIN users u ON p.user_id = u.id WHERE u.username = ?";
        List<PasskeyCredential> credentials = jdbcTemplate.query(sql, rowMapper, username);
        return credentials.stream()
                .map(this::toRegisteredCredential)
                .collect(Collectors.toSet());
    }

    public Optional<RegisteredCredential> getCredentialById(ByteArray credentialId) {
        String sql = "SELECT * FROM user_passkeys WHERE credential_id = ?";
        try {
            PasskeyCredential credential = jdbcTemplate.queryForObject(sql, rowMapper, credentialId.getBytes());
            return Optional.ofNullable(toRegisteredCredential(credential));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        String sql = "SELECT u.username FROM users u JOIN user_passkeys p ON u.id = p.user_id WHERE p.user_handle = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, String.class, userHandle.getBytes()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    private RegisteredCredential toRegisteredCredential(PasskeyCredential cred) {
        if (cred == null) return null;
        return RegisteredCredential.builder()
                .credentialId(cred.getCredentialId())
                .userHandle(cred.getUserHandle())
                .publicKeyCose(cred.getPublicKeyCose())
                .signatureCount(cred.getSignatureCount())
                .build();
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        String sql = "SELECT p.user_handle FROM user_passkeys p JOIN users u ON p.user_id = u.id WHERE u.username = ? LIMIT 1";
        try {
            byte[] userHandle = jdbcTemplate.queryForObject(sql, byte[].class, username);
            return Optional.ofNullable(userHandle).map(ByteArray::new);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        String sql = "SELECT * FROM user_passkeys WHERE credential_id = ? AND user_handle = ?";
        try {
            PasskeyCredential credential = jdbcTemplate.queryForObject(sql, rowMapper, credentialId.getBytes(), userHandle.getBytes());
            return Optional.ofNullable(toRegisteredCredential(credential));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        String sql = "SELECT * FROM user_passkeys WHERE credential_id = ?";
        List<PasskeyCredential> credentials = jdbcTemplate.query(sql, rowMapper, credentialId.getBytes());
        return credentials.stream().map(this::toRegisteredCredential).collect(Collectors.toSet());
    }
    
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        String sql = "SELECT p.credential_id FROM user_passkeys p JOIN users u ON p.user_id = u.id WHERE u.username = ?";
        List<byte[]> credentialIds = jdbcTemplate.queryForList(sql, byte[].class, username);
        return credentialIds.stream()
                .map(ByteArray::new)
                .map(id -> PublicKeyCredentialDescriptor.builder()
                        .id(id)
                        .type(PublicKeyCredentialType.PUBLIC_KEY)
                        .build())
                .collect(Collectors.toSet());
    }
}