package de.technikteam.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StatisticsDAO {
	private static final Logger logger = LogManager.getLogger(StatisticsDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public StatisticsDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public int getUserCount() {
		return getCount("SELECT COUNT(*) FROM users");
	}

	public int getActiveEventCount() {
		return getCount("SELECT COUNT(*) FROM events WHERE event_datetime >= NOW()");
	}

	private int getCount(String sql) {
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
			return count != null ? count : 0;
		} catch (Exception e) {
			logger.error("SQL error executing count query: {}", sql, e);
			return 0;
		}
	}
}