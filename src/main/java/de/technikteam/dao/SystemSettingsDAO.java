package de.technikteam.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SystemSettingsDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public SystemSettingsDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String getSetting(String key) {
		String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
		try {
			return jdbcTemplate.queryForObject(sql, String.class, key);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public boolean updateSetting(String key, String value) {
		String sql = "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?";
		return jdbcTemplate.update(sql, value, key) > 0;
	}
}