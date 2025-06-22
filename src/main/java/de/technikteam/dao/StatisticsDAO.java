package de.technikteam.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A simple Data Access Object used to retrieve basic aggregate numbers from the
 * database, such as the total count of users and the number of active, upcoming
 * events. This is primarily used for the administrative dashboard.
 */

public class StatisticsDAO {
	private static final Logger logger = LogManager.getLogger(StatisticsDAO.class);

	/**
	 * Gets the total count of all users in the `users` table.
	 * 
	 * @return The total number of users.
	 */
	public int getUserCount() {
		logger.debug("Getting user count.");
		return getCount("SELECT COUNT(*) FROM users");
	}

	/**
	 * Gets the count of all events that are not yet in the past.
	 * 
	 * @return The number of active/upcoming events.
	 */
	public int getActiveEventCount() {
		logger.debug("Getting active event count.");
		return getCount("SELECT COUNT(*) FROM events WHERE event_datetime >= NOW()");
	}

	/**
	 * A generic helper method to execute a `SELECT COUNT(*)` query.
	 * 
	 * @param sql The SQL query to execute.
	 * @return The count, or 0 if an error occurs.
	 */
	private int getCount(String sql) {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				int count = rs.getInt(1);
				logger.info("Count query '{}' returned {}.", sql, count);
				return count;
			}
		} catch (SQLException e) {
			logger.error("SQL error executing count query: {}", sql, e);
		}
		return 0;
	}
}