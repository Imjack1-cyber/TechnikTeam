package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.ProfileChangeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ProfileChangeRequestDAO {
	private static final Logger logger = LogManager.getLogger(ProfileChangeRequestDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public ProfileChangeRequestDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public boolean createRequest(ProfileChangeRequest request) {
		String sql = "INSERT INTO profile_change_requests (user_id, requested_changes, status) VALUES (?, ?, 'PENDING')";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, request.getUserId());
			pstmt.setString(2, request.getRequestedChanges());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error creating profile change request for user {}", request.getUserId(), e);
			return false;
		}
	}

	public ProfileChangeRequest getRequestById(int id) {
		String sql = "SELECT pcr.*, u.username as username, a.username as admin_username FROM profile_change_requests pcr JOIN users u ON pcr.user_id = u.id LEFT JOIN users a ON pcr.reviewed_by_admin_id = a.id WHERE pcr.id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToRequest(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching request by ID {}", id, e);
		}
		return null;
	}

	public List<ProfileChangeRequest> getPendingRequests() {
		List<ProfileChangeRequest> requests = new ArrayList<>();
		String sql = "SELECT pcr.*, u.username as username FROM profile_change_requests pcr JOIN users u ON pcr.user_id = u.id WHERE pcr.status = 'PENDING' ORDER BY pcr.requested_at ASC";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				requests.add(mapResultSetToRequest(rs));
			}
		} catch (SQLException e) {
			logger.error("Error fetching pending requests", e);
		}
		return requests;
	}

	public boolean hasPendingRequest(int userId) {
		String sql = "SELECT 1 FROM profile_change_requests WHERE user_id = ? AND status = 'PENDING'";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			logger.error("Error checking for pending request for user {}", userId, e);
			return false;
		}
	}

	public boolean updateRequestStatus(int requestId, String status, int adminId) {
		String sql = "UPDATE profile_change_requests SET status = ?, reviewed_by_admin_id = ?, reviewed_at = NOW() WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setInt(2, adminId);
			pstmt.setInt(3, requestId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating status for request {}", requestId, e);
			return false;
		}
	}

	private ProfileChangeRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
		ProfileChangeRequest request = new ProfileChangeRequest();
		request.setId(rs.getInt("id"));
		request.setUserId(rs.getInt("user_id"));
		request.setRequestedChanges(rs.getString("requested_changes"));
		request.setStatus(rs.getString("status"));
		request.setRequestedAt(rs.getTimestamp("requested_at").toLocalDateTime());
		if (rs.getObject("reviewed_by_admin_id") != null) {
			request.setReviewedByAdminId(rs.getInt("reviewed_by_admin_id"));
		}
		if (rs.getTimestamp("reviewed_at") != null) {
			request.setReviewedAt(rs.getTimestamp("reviewed_at").toLocalDateTime());
		}
		if (rs.getMetaData().getColumnCount() > 7) {
			request.setUsername(rs.getString("username"));
			if (rs.getMetaData().getColumnCount() > 8) {
				request.setReviewedByAdminName(rs.getString("admin_username"));
			}
		}
		return request;
	}
}