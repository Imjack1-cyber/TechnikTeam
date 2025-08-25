package de.technikteam.dao;

import de.technikteam.model.AuthenticationLog;
import de.technikteam.util.DaoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AuthenticationLogDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuthenticationLogDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AuthenticationLog> rowMapper = (rs, rowNum) -> {
        AuthenticationLog log = new AuthenticationLog();
        log.setId(rs.getLong("id"));
        log.setUserId(rs.getObject("user_id", Integer.class));
        log.setUsername(rs.getString("username"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setEventType(rs.getString("event_type"));
        log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        log.setJti(rs.getString("jti"));
        if (rs.getTimestamp("token_expiry") != null) {
            log.setTokenExpiry(rs.getTimestamp("token_expiry").toLocalDateTime());
        }
        if (DaoUtils.hasColumn(rs, "is_revoked")) {
            log.setRevoked(rs.getBoolean("is_revoked"));
        }
        return log;
    };

    public void createLog(AuthenticationLog log) {
        String sql = "INSERT INTO authentication_logs (user_id, username, ip_address, event_type, jti, token_expiry) VALUES (?, ?, ?, ?, ?, ?)";
        Object[] args = {log.getUserId(), log.getUsername(), log.getIpAddress(), log.getEventType(), log.getJti(), log.getTokenExpiry() != null ? Timestamp.valueOf(log.getTokenExpiry()) : null};
        int[] types = {Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP};
        jdbcTemplate.update(sql, args, types);
    }

    public List<AuthenticationLog> getLogs(int limit) {
        String sql = "SELECT l.*, (b.jti IS NOT NULL) AS is_revoked " +
                     "FROM authentication_logs l " +
                     "LEFT JOIN jwt_blocklist b ON l.jti = b.jti " +
                     "WHERE l.event_type IN ('LOGIN_SUCCESS', 'LOGOUT') " +
                     "ORDER BY l.timestamp DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public AuthenticationLog getPreviousLoginInfo(int userId) {
        String sql = "SELECT * FROM authentication_logs WHERE user_id = ? AND event_type = 'LOGIN_SUCCESS' ORDER BY timestamp DESC LIMIT 1 OFFSET 1";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, userId);
        } catch (EmptyResultDataAccessException e) {
            return null; // This is expected for the first-ever login
        }
    }

    public LocalDateTime getTimestampOfLastLogin(int userId) {
        String sql = "SELECT timestamp FROM authentication_logs WHERE user_id = ? AND event_type = 'LOGIN_SUCCESS' ORDER BY timestamp DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, LocalDateTime.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public AuthenticationLog getLogByJti(String jti) {
        String sql = "SELECT * FROM authentication_logs WHERE jti = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, jti);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}