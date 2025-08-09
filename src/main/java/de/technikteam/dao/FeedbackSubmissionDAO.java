package de.technikteam.dao;

import de.technikteam.model.FeedbackSubmission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FeedbackSubmissionDAO {
	private static final Logger logger = LogManager.getLogger(FeedbackSubmissionDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public FeedbackSubmissionDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<FeedbackSubmission> submissionRowMapper = (rs, rowNum) -> {
		FeedbackSubmission sub = new FeedbackSubmission();
		sub.setId(rs.getInt("id"));
		sub.setUserId(rs.getInt("user_id"));
		sub.setUsername(rs.getString("username"));
		sub.setSubject(rs.getString("subject"));
		sub.setDisplayTitle(rs.getString("display_title"));
		sub.setContent(rs.getString("content"));
		sub.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
		sub.setStatus(rs.getString("status"));
		sub.setDisplayOrder(rs.getInt("display_order"));
		return sub;
	};

	public boolean createSubmission(FeedbackSubmission submission) {
		String sql = "INSERT INTO feedback_submissions (user_id, subject, content) VALUES (?, ?, ?)";
		try {
			return jdbcTemplate.update(sql, submission.getUserId(), submission.getSubject(),
					submission.getContent()) > 0;
		} catch (Exception e) {
			logger.error("Error creating feedback submission for user {}", submission.getUserId(), e);
			return false;
		}
	}

	public List<FeedbackSubmission> getAllSubmissions() {
		String sql = "SELECT fs.*, u.username FROM feedback_submissions fs JOIN users u ON fs.user_id = u.id ORDER BY FIELD(fs.status, 'NEW', 'VIEWED', 'PLANNED', 'REJECTED', 'COMPLETED'), fs.display_order ASC";
		try {
			return jdbcTemplate.query(sql, submissionRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching all feedback submissions", e);
			return List.of();
		}
	}

	public FeedbackSubmission getSubmissionById(int submissionId) {
		String sql = "SELECT fs.*, u.username FROM feedback_submissions fs JOIN users u ON fs.user_id = u.id WHERE fs.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, submissionRowMapper, submissionId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching feedback submission by ID {}", submissionId, e);
			return null;
		}
	}

	public List<FeedbackSubmission> getSubmissionsByUserId(int userId) {
		String sql = "SELECT fs.*, u.username FROM feedback_submissions fs JOIN users u ON fs.user_id = u.id WHERE fs.user_id = ? ORDER BY fs.submitted_at DESC";
		try {
			return jdbcTemplate.query(sql, submissionRowMapper, userId);
		} catch (Exception e) {
			logger.error("Error fetching feedback submissions for user {}", userId, e);
			return List.of();
		}
	}

	public boolean updateStatusAndTitle(int submissionId, String newStatus, String displayTitle) {
		String sql = "UPDATE feedback_submissions SET status = ?, display_title = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, newStatus, displayTitle, submissionId) > 0;
		} catch (Exception e) {
			logger.error("Error updating status and title for submission {}", submissionId, e);
			return false;
		}
	}

	public boolean deleteSubmission(int submissionId) {
		String sql = "DELETE FROM feedback_submissions WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, submissionId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting submission {}", submissionId, e);
			return false;
		}
	}

}