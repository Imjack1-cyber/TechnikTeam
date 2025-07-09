package de.technikteam.dao;

import de.technikteam.model.FeedbackForm;
import de.technikteam.model.FeedbackResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
	private static final Logger logger = LogManager.getLogger(FeedbackDAO.class);

	public int createFeedbackForm(FeedbackForm form) {
		String sql = "INSERT INTO feedback_forms (event_id, title) VALUES (?, ?)";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS)) {
			preparedStatement.setInt(1, form.getEventId());
			preparedStatement.setString(2, form.getTitle());
			int affectedRows = preparedStatement.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
					if (resultSet.next()) {
						return resultSet.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error creating feedback form for event {}", form.getEventId(), e);
		}
		return 0;
	}

	public boolean saveFeedbackResponse(FeedbackResponse response) {
		String sql = "INSERT INTO feedback_responses (form_id, user_id, rating, comments) VALUES (?, ?, ?, ?) "
				+ "ON DUPLICATE KEY UPDATE rating = VALUES(rating), comments = VALUES(comments)";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, response.getFormId());
			preparedStatement.setInt(2, response.getUserId());
			preparedStatement.setInt(3, response.getRating());
			preparedStatement.setString(4, response.getComments());
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error saving feedback response for form_id {}", response.getFormId(), e);
		}
		return false;
	}

	public FeedbackForm getFeedbackFormForEvent(int eventId) {
		String sql = "SELECT * FROM feedback_forms WHERE event_id = ?";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					FeedbackForm form = new FeedbackForm();
					form.setId(resultSet.getInt("id"));
					form.setEventId(resultSet.getInt("event_id"));
					form.setTitle(resultSet.getString("title"));
					form.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
					return form;
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching feedback form for event ID {}", eventId, e);
		}
		return null;
	}

	public List<FeedbackResponse> getResponsesForForm(int formId) {
		List<FeedbackResponse> responses = new ArrayList<>();
		String sql = "SELECT fr.*, u.username FROM feedback_responses fr JOIN users u ON fr.user_id = u.id WHERE fr.form_id = ?";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, formId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					FeedbackResponse response = new FeedbackResponse();
					response.setId(resultSet.getInt("id"));
					response.setFormId(resultSet.getInt("form_id"));
					response.setUserId(resultSet.getInt("user_id"));
					response.setUsername(resultSet.getString("username")); 
					response.setRating(resultSet.getInt("rating"));
					response.setComments(resultSet.getString("comments"));
					response.setSubmittedAt(resultSet.getTimestamp("submitted_at").toLocalDateTime());
					responses.add(response);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching responses for form ID {}", formId, e);
		}
		return responses;
	}

	public boolean hasUserSubmittedFeedback(int formId, int userId) {
		String sql = "SELECT 1 FROM feedback_responses WHERE form_id = ? AND user_id = ?";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, formId);
			preparedStatement.setInt(2, userId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		} catch (SQLException e) {
			logger.error("Error checking user feedback submission status.", e);
		}
		return false;
	}
}