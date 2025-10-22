package de.technikteam.dao;

import de.technikteam.model.IdentityVerificationRequest;
import de.technikteam.util.DaoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class IdentityVerificationDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public IdentityVerificationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<IdentityVerificationRequest> rowMapper = (rs, rowNum) -> {
        IdentityVerificationRequest req = new IdentityVerificationRequest();
        req.setId(rs.getLong("id"));
        req.setUserId(rs.getInt("user_id"));
        req.setChallengeToken(rs.getString("challenge_token"));
        req.setRequestType(rs.getString("request_type"));
        req.setStatus(rs.getString("status"));
        req.setContext(rs.getString("context"));
        req.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        req.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (DaoUtils.hasColumn(rs, "username")) {
            req.setUsername(rs.getString("username"));
        }
        return req;
    };

    public IdentityVerificationRequest create(IdentityVerificationRequest request) {
        String sql = "INSERT INTO identity_verification_requests (user_id, challenge_token, request_type, context, expires_at) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, request.getUserId());
            ps.setString(2, request.getChallengeToken());
            ps.setString(3, request.getRequestType());
            ps.setString(4, request.getContext());
            ps.setTimestamp(5, Timestamp.valueOf(request.getExpiresAt()));
            return ps;
        }, keyHolder);
        request.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return request;
    }

    public Optional<IdentityVerificationRequest> findByToken(String token) {
        String sql = "SELECT ivr.*, u.username FROM identity_verification_requests ivr JOIN users u ON ivr.user_id = u.id WHERE ivr.challenge_token = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, token));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<IdentityVerificationRequest> findPendingPasswordResets() {
        String sql = "SELECT ivr.*, u.username FROM identity_verification_requests ivr JOIN users u ON ivr.user_id = u.id WHERE ivr.request_type = 'PASSWORD_RESET' AND ivr.status = 'PENDING' ORDER BY ivr.created_at ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public boolean updateStatus(String token, String status) {
        String sql = "UPDATE identity_verification_requests SET status = ? WHERE challenge_token = ?";
        return jdbcTemplate.update(sql, status, token) > 0;
    }

    public int deleteExpired() {
        String sql = "DELETE FROM identity_verification_requests WHERE expires_at < NOW() AND status = 'PENDING'";
        return jdbcTemplate.update(sql);
    }
}