package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.Achievement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AchievementDAO {
	private static final Logger logger = LogManager.getLogger(AchievementDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public AchievementDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public List<Achievement> getAllAchievements() {
		List<Achievement> achievements = new ArrayList<>();
		String sql = "SELECT * FROM achievements ORDER BY name";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				achievements.add(mapResultSetToAchievement(rs));
			}
		} catch (SQLException e) {
			logger.error("Error fetching all achievements", e);
		}
		return achievements;
	}

	public Achievement getAchievementById(int id) {
		String sql = "SELECT * FROM achievements WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToAchievement(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching achievement by ID {}", id, e);
		}
		return null;
	}

	public boolean createAchievement(Achievement achievement) {
		String sql = "INSERT INTO achievements (achievement_key, name, description, icon_class) VALUES (?, ?, ?, ?)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, achievement.getAchievementKey());
			pstmt.setString(2, achievement.getName());
			pstmt.setString(3, achievement.getDescription());
			pstmt.setString(4, achievement.getIconClass());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error creating achievement '{}'", achievement.getName(), e);
			return false;
		}
	}

	public boolean updateAchievement(Achievement achievement) {
		String sql = "UPDATE achievements SET name = ?, description = ?, icon_class = ? WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, achievement.getName());
			pstmt.setString(2, achievement.getDescription());
			pstmt.setString(3, achievement.getIconClass());
			pstmt.setInt(4, achievement.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating achievement ID {}", achievement.getId(), e);
			return false;
		}
	}

	public boolean deleteAchievement(int id) {
		String sql = "DELETE FROM achievements WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting achievement ID {}", id, e);
			return false;
		}
	}

	public List<Achievement> getAchievementsForUser(int userId) {
		List<Achievement> achievements = new ArrayList<>();
		String sql = "SELECT a.id, a.achievement_key, a.name, a.description, a.icon_class, ua.earned_at "
				+ "FROM achievements a JOIN user_achievements ua ON a.id = ua.achievement_id "
				+ "WHERE ua.user_id = ? ORDER BY ua.earned_at DESC";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Achievement ach = mapResultSetToAchievement(rs);
					ach.setEarnedAt(rs.getTimestamp("earned_at").toLocalDateTime());
					achievements.add(ach);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching achievements for user {}", userId, e);
		}
		return achievements;
	}

	public boolean grantAchievementToUser(int userId, String achievementKey) {
		if (hasAchievement(userId, achievementKey)) {
			return false;
		}
		String sql = "INSERT INTO user_achievements (user_id, achievement_id) SELECT ?, id FROM achievements WHERE achievement_key = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, achievementKey);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error granting achievement '{}' to user {}", achievementKey, userId, e);
		}
		return false;
	}

	public boolean hasAchievement(int userId, String achievementKey) {
		String sql = "SELECT 1 FROM user_achievements ua JOIN achievements a ON ua.achievement_id = a.id WHERE ua.user_id = ? AND a.achievement_key = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, achievementKey);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			logger.error("Error checking for achievement '{}' for user {}", achievementKey, userId, e);
		}
		return false;
	}

	private Achievement mapResultSetToAchievement(ResultSet rs) throws SQLException {
		Achievement ach = new Achievement();
		ach.setId(rs.getInt("id"));
		ach.setAchievementKey(rs.getString("achievement_key"));
		ach.setName(rs.getString("name"));
		ach.setDescription(rs.getString("description"));
		ach.setIconClass(rs.getString("icon_class"));
		return ach;
	}
}