package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Singleton
public class StatisticsDAO {
	private static final Logger logger = LogManager.getLogger(StatisticsDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public StatisticsDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public int getUserCount() {
		return getCount("SELECT COUNT(*) FROM users");
	}

	public int getActiveEventCount() {
		return getCount("SELECT COUNT(*) FROM events WHERE event_datetime >= NOW()");
	}

	private int getCount(String sql) {
		try (Connection conn = dbManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("SQL error executing count query: {}", sql, e);
		}
		return 0;
	}
}