package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.FeedbackSubmission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class FeedbackSubmissionDAO {
	private static final Logger logger = LogManager.getLogger(FeedbackSubmissionDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public FeedbackSubmissionDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public boolean createSubmission(FeedbackSubmission submission) {
		String sql = "INSERT INTO feedback_submissions (user_id, subject, content) VALUES (?, ?, ?)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, submission.getUserId());
			pstmt.setString(2, submission.getSubject());
			pstmt.setString(3, submission.getContent());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error creating feedback submission for user {}", submission.getUserId(), e);
			return false;
		}
	}

	public List<FeedbackSubmission> getAllSubmissions() {
		List<FeedbackSubmission> submissions = new ArrayList<>();
		String sql = "SELECT fs.*, u.username FROM feedback_submissions fs JOIN users u ON fs.user_id = u.id ORDER BY FIELD(fs.status, 'NEW', 'VIEWED', 'PLANNED', 'COMPLETED', 'REJECTED'), fs.submitted_at DESC";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				submissions.add(mapResultSetToSubmission(rs));
			}
		} catch (SQLException e) {
			logger.error("Error fetching all feedback submissions", e);
		}
		return submissions;
	}

	public List<FeedbackSubmission> getSubmissionsByUserId(int userId) {
		List<FeedbackSubmission> submissions = new ArrayList<>();
		String sql = "SELECT fs.*, u.username FROM feedback_submissions fs JOIN users u ON fs.user_id = u.id WHERE fs.user_id = ? ORDER BY fs.submitted_at DESC";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					submissions.add(mapResultSetToSubmission(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching feedback submissions for user {}", userId, e);
		}
		return submissions;
	}

	public boolean updateStatus(int submissionId, String newStatus) {
		String sql = "UPDATE feedback_submissions SET status = ? WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newStatus);
			pstmt.setInt(2, submissionId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating status for submission {}", submissionId, e);
			return false;
		}
	}

	public boolean deleteSubmission(int submissionId) {
		String sql = "DELETE FROM feedback_submissions WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, submissionId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting submission {}", submissionId, e);
			return false;
		}
	}

	private FeedbackSubmission mapResultSetToSubmission(ResultSet rs) throws SQLException {
		FeedbackSubmission sub = new FeedbackSubmission();
		sub.setId(rs.getInt("id"));
		sub.setUserId(rs.getInt("user_id"));
		sub.setUsername(rs.getString("username"));
		sub.setSubject(rs.getString("subject"));
		sub.setContent(rs.getString("content"));
		sub.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
		sub.setStatus(rs.getString("status"));
		return sub;
	}
}