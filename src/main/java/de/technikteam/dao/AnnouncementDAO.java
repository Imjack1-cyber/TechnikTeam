package de.technikteam.dao;

import de.technikteam.model.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AnnouncementDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AnnouncementDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Announcement> rowMapper = (rs, rowNum) -> {
		Announcement announcement = new Announcement();
		announcement.setId(rs.getInt("id"));
		announcement.setTitle(rs.getString("title"));
		announcement.setContent(rs.getString("content"));
		announcement.setAuthorUserId(rs.getInt("author_user_id"));
		announcement.setAuthorUsername(rs.getString("author_username"));
		announcement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return announcement;
	};

	public List<Announcement> findAll() {
		String sql = "SELECT a.*, u.username as author_username FROM announcements a JOIN users u ON a.author_user_id = u.id ORDER BY a.created_at DESC";
		return jdbcTemplate.query(sql, rowMapper);
	}

	public Optional<Announcement> findById(int id) {
		String sql = "SELECT a.*, u.username as author_username FROM announcements a JOIN users u ON a.author_user_id = u.id WHERE a.id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Announcement create(Announcement announcement) {
		String sql = "INSERT INTO announcements (title, content, author_user_id) VALUES (?, ?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, announcement.getTitle());
			ps.setString(2, announcement.getContent());
			ps.setInt(3, announcement.getAuthorUserId());
			return ps;
		}, keyHolder);
		int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
		announcement.setId(newId);
		return announcement;
	}

	public Announcement update(Announcement announcement) {
		String sql = "UPDATE announcements SET title = ?, content = ? WHERE id = ?";
		jdbcTemplate.update(sql, announcement.getTitle(), announcement.getContent(), announcement.getId());
		return announcement;
	}

	public boolean delete(int id) {
		String sql = "DELETE FROM announcements WHERE id = ?";
		return jdbcTemplate.update(sql, id) > 0;
	}
}