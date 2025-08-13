package de.technikteam.dao;

import de.technikteam.model.Achievement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AchievementDAO {
	private static final Logger logger = LogManager.getLogger(AchievementDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AchievementDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Achievement> achievementRowMapper = (rs, rowNum) -> {
		Achievement ach = new Achievement();
		ach.setId(rs.getInt("id"));
		ach.setAchievementKey(rs.getString("achievement_key"));
		ach.setName(rs.getString("name"));
		ach.setDescription(rs.getString("description"));
		ach.setIconClass(rs.getString("icon_class"));
		return ach;
	};

	public List<Achievement> getAllAchievements() {
		String sql = "SELECT * FROM achievements ORDER BY name";
		try {
			return jdbcTemplate.query(sql, achievementRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching all achievements", e);
			return List.of();
		}
	}

	public Achievement getAchievementById(int id) {
		String sql = "SELECT * FROM achievements WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, achievementRowMapper, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching achievement by ID {}", id, e);
			return null;
		}
	}

	public boolean createAchievement(Achievement achievement) {
		String sql = "INSERT INTO achievements (achievement_key, name, description, icon_class) VALUES (?, ?, ?, ?)";
		try {
			return jdbcTemplate.update(sql, achievement.getAchievementKey(), achievement.getName(),
					achievement.getDescription(), achievement.getIconClass()) > 0;
		} catch (Exception e) {
			logger.error("Error creating achievement '{}'", achievement.getName(), e);
			return false;
		}
	}

	public boolean updateAchievement(Achievement achievement) {
		String sql = "UPDATE achievements SET name = ?, description = ?, icon_class = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, achievement.getName(), achievement.getDescription(),
					achievement.getIconClass(), achievement.getId()) > 0;
		} catch (Exception e) {
			logger.error("Error updating achievement ID {}", achievement.getId(), e);
			return false;
		}
	}

	public boolean deleteAchievement(int id) {
		String sql = "DELETE FROM achievements WHERE id = ?";
		try {
			jdbcTemplate.update("DELETE FROM user_achievements WHERE achievement_id = ?", id);
			return jdbcTemplate.update(sql, id) > 0;
		} catch (Exception e) {
			logger.error("Error deleting achievement ID {}", id, e);
			return false;
		}
	}

	public List<Achievement> getAchievementsForUser(int userId) {
		String sql = "SELECT a.*, ua.earned_at FROM achievements a JOIN user_achievements ua ON a.id = ua.achievement_id WHERE ua.user_id = ? ORDER BY ua.earned_at DESC";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				Achievement ach = achievementRowMapper.mapRow(rs, rowNum);
				ach.setEarnedAt(rs.getTimestamp("earned_at").toLocalDateTime());
				return ach;
			}, userId);
		} catch (Exception e) {
			logger.error("Error fetching achievements for user {}", userId, e);
			return List.of();
		}
	}

	public boolean grantAchievementToUser(int userId, String achievementKey) {
		if (hasAchievement(userId, achievementKey)) {
			return false; 
		}
		String sql = "INSERT INTO user_achievements (user_id, achievement_id) SELECT ?, id FROM achievements WHERE achievement_key = ?";
		try {
			return jdbcTemplate.update(sql, userId, achievementKey) > 0;
		} catch (Exception e) {
			logger.error("Error granting achievement '{}' to user {}", achievementKey, userId, e);
			return false;
		}
	}

	public boolean hasAchievement(int userId, String achievementKey) {
		String sql = "SELECT COUNT(*) FROM user_achievements ua JOIN achievements a ON ua.achievement_id = a.id WHERE ua.user_id = ? AND a.achievement_key = ?";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, achievementKey);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("Error checking for achievement '{}' for user {}", achievementKey, userId, e);
			return false;
		}
	}
}