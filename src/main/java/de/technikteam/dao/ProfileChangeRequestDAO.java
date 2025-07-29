package de.technikteam.dao;

import de.technikteam.model.ProfileChangeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProfileChangeRequestDAO {
	private static final Logger logger = LogManager.getLogger(ProfileChangeRequestDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ProfileChangeRequestDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<ProfileChangeRequest> requestRowMapper = (rs, rowNum) -> {
		ProfileChangeRequest request = new ProfileChangeRequest();
		request.setId(rs.getInt("id"));
		request.setUserId(rs.getInt("user_id"));
		request.setRequestedChanges(rs.getString("requested_changes"));
		request.setStatus(rs.getString("status"));
		request.setRequestedAt(rs.getTimestamp("requested_at").toLocalDateTime());
		request.setUsername(rs.getString("username"));
		if (rs.getObject("reviewed_by_admin_id") != null) {
			request.setReviewedByAdminId(rs.getInt("reviewed_by_admin_id"));
		}
		if (rs.getTimestamp("reviewed_at") != null) {
			request.setReviewedAt(rs.getTimestamp("reviewed_at").toLocalDateTime());
		}
		if (rs.getMetaData().getColumnCount() > 8) { // Simple check if admin_username is present
			request.setReviewedByAdminName(rs.getString("admin_username"));
		}
		return request;
	};

	public boolean createRequest(ProfileChangeRequest request) {
		String sql = "INSERT INTO profile_change_requests (user_id, requested_changes, status) VALUES (?, ?, 'PENDING')";
		try {
			return jdbcTemplate.update(sql, request.getUserId(), request.getRequestedChanges()) > 0;
		} catch (Exception e) {
			logger.error("Error creating profile change request for user {}", request.getUserId(), e);
			return false;
		}
	}

	public ProfileChangeRequest getRequestById(int id) {
		String sql = "SELECT pcr.*, u.username as username, a.username as admin_username FROM profile_change_requests pcr JOIN users u ON pcr.user_id = u.id LEFT JOIN users a ON pcr.reviewed_by_admin_id = a.id WHERE pcr.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, requestRowMapper, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching request by ID {}", id, e);
			return null;
		}
	}

	public List<ProfileChangeRequest> getPendingRequests() {
		String sql = "SELECT pcr.*, u.username as username FROM profile_change_requests pcr JOIN users u ON pcr.user_id = u.id WHERE pcr.status = 'PENDING' ORDER BY pcr.requested_at ASC";
		try {
			return jdbcTemplate.query(sql, requestRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching pending requests", e);
			return List.of();
		}
	}

	public boolean hasPendingRequest(int userId) {
		String sql = "SELECT COUNT(*) FROM profile_change_requests WHERE user_id = ? AND status = 'PENDING'";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("Error checking for pending request for user {}", userId, e);
			return false;
		}
	}

	public boolean updateRequestStatus(int requestId, String status, int adminId) {
		String sql = "UPDATE profile_change_requests SET status = ?, reviewed_by_admin_id = ?, reviewed_at = NOW() WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, status, adminId, requestId) > 0;
		} catch (Exception e) {
			logger.error("Error updating status for request {}", requestId, e);
			return false;
		}
	}
}