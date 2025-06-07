package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.ParticipationHistory;

public class HistoryDAO {
	private static final Logger logger = LogManager.getLogger(HistoryDAO.class);

	public List<ParticipationHistory> getFullParticipationHistory() {
		logger.debug("Fetching full participation history.");
		List<ParticipationHistory> historyList = new ArrayList<>();
		String sql = "SELECT u.username, e.name, e.event_datetime, ea.signup_status " + "FROM event_attendance ea "
				+ "JOIN users u ON ea.user_id = u.id " + "JOIN events e ON ea.event_id = e.id "
				+ "ORDER BY u.username, e.event_datetime DESC";

		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				ParticipationHistory history = new ParticipationHistory();
				history.setUsername(rs.getString("username"));
				history.setEventName(rs.getString("name"));
				history.setEventDate(rs.getTimestamp("event_datetime").toLocalDateTime());
				history.setStatus(rs.getString("signup_status"));
				historyList.add(history);
			}
			logger.info("Fetched {} history entries.", historyList.size());
		} catch (SQLException e) {
			logger.error("SQL error while fetching participation history.", e);
		}
		return historyList;
	}
}