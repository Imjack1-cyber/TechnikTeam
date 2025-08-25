package de.technikteam.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class JwtBlocklistDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JwtBlocklistDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isBlocklisted(String jti) {
        String sql = "SELECT COUNT(*) FROM jwt_blocklist WHERE jti = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, jti);
        return count != null && count > 0;
    }

    public void blocklist(String jti, LocalDateTime expiry) {
        String sql = "INSERT IGNORE INTO jwt_blocklist (jti, expiry) VALUES (?, ?)";
        jdbcTemplate.update(sql, jti, Timestamp.valueOf(expiry));
    }

    public void cleanExpired() {
        String sql = "DELETE FROM jwt_blocklist WHERE expiry < NOW()";
        jdbcTemplate.update(sql);
    }
}