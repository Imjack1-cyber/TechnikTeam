package de.technikteam.dao;

import de.technikteam.model.EventPhoto;
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
public class EventPhotoDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public EventPhotoDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<EventPhoto> rowMapper = (rs, rowNum) -> {
		EventPhoto photo = new EventPhoto();
		photo.setId(rs.getInt("id"));
		photo.setEventId(rs.getInt("event_id"));
		photo.setFileId(rs.getInt("file_id"));
		photo.setUploaderUserId(rs.getInt("uploader_user_id"));
		photo.setCaption(rs.getString("caption"));
		photo.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
		// Joined fields
		photo.setFilepath(rs.getString("filepath"));
		photo.setUploaderUsername(rs.getString("username"));
		return photo;
	};

	public List<EventPhoto> findByEventId(int eventId) {
		String sql = "SELECT ep.*, f.filepath, u.username FROM event_photos ep " + "JOIN files f ON ep.file_id = f.id "
				+ "JOIN users u ON ep.uploader_user_id = u.id " + "WHERE ep.event_id = ? ORDER BY ep.uploaded_at DESC";
		return jdbcTemplate.query(sql, rowMapper, eventId);
	}

	public Optional<EventPhoto> findById(int id) {
		String sql = "SELECT ep.*, f.filepath, u.username FROM event_photos ep " + "JOIN files f ON ep.file_id = f.id "
				+ "JOIN users u ON ep.uploader_user_id = u.id " + "WHERE ep.id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public EventPhoto create(EventPhoto photo) {
		String sql = "INSERT INTO event_photos (event_id, file_id, uploader_user_id, caption) VALUES (?, ?, ?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, photo.getEventId());
			ps.setInt(2, photo.getFileId());
			ps.setInt(3, photo.getUploaderUserId());
			ps.setString(4, photo.getCaption());
			return ps;
		}, keyHolder);
		photo.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
		return photo;
	}

	public boolean delete(int id) {
		return jdbcTemplate.update("DELETE FROM event_photos WHERE id = ?", id) > 0;
	}
}