package de.technikteam.dao;

import de.technikteam.model.FeedbackForm;
import de.technikteam.model.FeedbackResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class EventFeedbackDAO {
	private static final Logger logger = LogManager.getLogger(EventFeedbackDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public EventFeedbackDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public int createFeedbackForm(FeedbackForm form) {
		String sql = "INSERT INTO feedback_forms (event_id, title) VALUES (?, ?)";
		try {
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, form.getEventId());
				ps.setString(2, form.getTitle());
				return ps;
			}, keyHolder);
			return Objects.requireNonNull(keyHolder.getKey()).intValue();
		} catch (Exception e) {
			logger.error("Error creating feedback form for event {}", form.getEventId(), e);
			return 0;
		}
	}

	public boolean saveFeedbackResponse(FeedbackResponse response) {
		String sql = "INSERT INTO feedback_responses (form_id, user_id, rating, comments) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE rating = VALUES(rating), comments = VALUES(comments)";
		try {
			return jdbcTemplate.update(sql, response.getFormId(), response.getUserId(), response.getRating(),
					response.getComments()) > 0;
		} catch (Exception e) {
			logger.error("Error saving feedback response for form_id {}", response.getFormId(), e);
			return false;
		}
	}

	public FeedbackForm getFeedbackFormForEvent(int eventId) {
		String sql = "SELECT * FROM feedback_forms WHERE event_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				FeedbackForm form = new FeedbackForm();
				form.setId(rs.getInt("id"));
				form.setEventId(rs.getInt("event_id"));
				form.setTitle(rs.getString("title"));
				form.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
				return form;
			}, eventId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching feedback form for event ID {}", eventId, e);
			return null;
		}
	}

	public FeedbackForm getFormById(int formId) {
		String sql = "SELECT * FROM feedback_forms WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				FeedbackForm form = new FeedbackForm();
				form.setId(rs.getInt("id"));
				form.setEventId(rs.getInt("event_id"));
				form.setTitle(rs.getString("title"));
				form.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
				return form;
			}, formId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching feedback form for form ID {}", formId, e);
			return null;
		}
	}

	public List<FeedbackResponse> getResponsesForForm(int formId) {
		String sql = "SELECT fr.*, u.username FROM feedback_responses fr JOIN users u ON fr.user_id = u.id WHERE fr.form_id = ?";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				FeedbackResponse response = new FeedbackResponse();
				response.setId(rs.getInt("id"));
				response.setFormId(rs.getInt("form_id"));
				response.setUserId(rs.getInt("user_id"));
				response.setUsername(rs.getString("username"));
				response.setRating(rs.getInt("rating"));
				response.setComments(rs.getString("comments"));
				response.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
				return response;
			}, formId);
		} catch (Exception e) {
			logger.error("Error fetching responses for form ID {}", formId, e);
			return List.of();
		}
	}

	public boolean hasUserSubmittedFeedback(int formId, int userId) {
		String sql = "SELECT COUNT(*) FROM feedback_responses WHERE form_id = ? AND user_id = ?";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, formId, userId);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("Error checking user feedback submission status.", e);
			return false;
		}
	}
}