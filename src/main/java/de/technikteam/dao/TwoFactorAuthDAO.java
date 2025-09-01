package de.technikteam.dao;

import de.technikteam.model.UserBackupCode;
import de.technikteam.util.IpUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TwoFactorAuthDAO {

    private static final Logger logger = LogManager.getLogger(TwoFactorAuthDAO.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TwoFactorAuthDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isIpKnownForUser(int userId, String ipAddress) {
        logger.debug("Checking if IP '{}' is known for user ID {}.", ipAddress, userId);
        String sql = "SELECT ip_address FROM user_known_ips WHERE user_id = ?";
        List<String> knownIps = jdbcTemplate.queryForList(sql, String.class, userId);
        
        if (knownIps.isEmpty()) {
            logger.debug("No known IPs found for user {}. Current IP is unknown.", userId);
            return false;
        }
        logger.debug("Found known IPs for user {}: {}", userId, knownIps);

        for (String knownIp : knownIps) {
            boolean isMatch = IpUtils.isSameSubnet(ipAddress, knownIp);
            logger.debug("Comparing {} (current) with {} (known). Same subnet: {}", ipAddress, knownIp, isMatch);
            if (isMatch) {
                logger.info("Found matching subnet for user {}. IP '{}' is considered known.", userId, ipAddress);
                return true;
            }
        }

        logger.info("No matching subnet found for user {}. IP '{}' is considered new.", userId, ipAddress);
        return false;
    }

    public void addKnownIpForUser(int userId, String ipAddress) {
        String sql = "INSERT INTO user_known_ips (user_id, ip_address, last_seen) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE last_seen = VALUES(last_seen)";
        jdbcTemplate.update(sql, userId, ipAddress, Timestamp.valueOf(LocalDateTime.now()));
    }

    public void nameKnownIp(int userId, String ipAddress, String deviceName) {
        // We might need to add an IP that isn't there yet if the user renames it before a second login
        String sql = "INSERT INTO user_known_ips (user_id, ip_address, device_name, last_seen) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE device_name = VALUES(device_name), last_seen = VALUES(last_seen)";
        jdbcTemplate.update(sql, userId, ipAddress, deviceName, Timestamp.valueOf(LocalDateTime.now()));
    }

    public boolean forgetIp(int userId, String ipAddress) {
        String sql = "DELETE FROM user_known_ips WHERE user_id = ? AND ip_address = ?";
        return jdbcTemplate.update(sql, userId, ipAddress) > 0;
    }

    @Transactional
    public void setTotpSecretForUser(int userId, String encryptedSecret) {
        jdbcTemplate.update("UPDATE users SET totp_secret = ? WHERE id = ?", encryptedSecret, userId);
    }

    @Transactional
    public void enableTotpForUser(int userId, String encryptedSecret) {
        jdbcTemplate.update("UPDATE users SET is_totp_enabled = TRUE, totp_secret = ? WHERE id = ?", encryptedSecret, userId);
    }

    @Transactional
    public void disableTotpForUser(int userId) {
        jdbcTemplate.update("UPDATE users SET is_totp_enabled = FALSE, totp_secret = NULL WHERE id = ?", userId);
        jdbcTemplate.update("DELETE FROM user_backup_codes WHERE user_id = ?", userId);
    }

    @Transactional
    public void storeBackupCodeHashes(int userId, List<String> hashedCodes) {
        // Clear old codes first
        jdbcTemplate.update("DELETE FROM user_backup_codes WHERE user_id = ?", userId);
        // Insert new ones
        String sql = "INSERT INTO user_backup_codes (user_id, code_hash) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, hashedCodes, 100, (ps, hashedCode) -> {
            ps.setInt(1, userId);
            ps.setString(2, hashedCode);
        });
    }

    public List<UserBackupCode> getUnusedBackupCodesForUser(int userId) {
        String sql = "SELECT * FROM user_backup_codes WHERE user_id = ? AND is_used = FALSE";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserBackupCode code = new UserBackupCode();
            code.setId(rs.getLong("id"));
            code.setUserId(rs.getInt("user_id"));
            code.setCodeHash(rs.getString("code_hash"));
            code.setUsed(rs.getBoolean("is_used"));
            return code;
        }, userId);
    }

    public boolean markBackupCodeAsUsed(long codeId) {
        return jdbcTemplate.update("UPDATE user_backup_codes SET is_used = TRUE WHERE id = ?", codeId) > 0;
    }

    public String getTotpSecretForUser(int userId) {
        try {
            return jdbcTemplate.queryForObject("SELECT totp_secret FROM users WHERE id = ?", String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void clearKnownIpsForUser(int userId) {
        jdbcTemplate.update("DELETE FROM user_known_ips WHERE user_id = ?", userId);
    }
}