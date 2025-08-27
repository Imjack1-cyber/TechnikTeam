package de.technikteam.dao;

import de.technikteam.model.GeoIpRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GeoIpRuleDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GeoIpRuleDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GeoIpRule> rowMapper = (rs, rowNum) -> {
        GeoIpRule rule = new GeoIpRule();
        rule.setCountryCode(rs.getString("country_code"));
        rule.setRuleType(rs.getString("rule_type"));
        return rule;
    };

    public List<GeoIpRule> findAllRules() {
        String sql = "SELECT * FROM geoip_rules";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public boolean saveRule(GeoIpRule rule) {
        String sql = "INSERT INTO geoip_rules (country_code, rule_type) VALUES (?, ?) ON DUPLICATE KEY UPDATE rule_type = VALUES(rule_type)";
        return jdbcTemplate.update(sql, rule.getCountryCode(), rule.getRuleType()) > 0;
    }

    public boolean deleteRule(String countryCode) {
        String sql = "DELETE FROM geoip_rules WHERE country_code = ?";
        return jdbcTemplate.update(sql, countryCode) > 0;
    }
}