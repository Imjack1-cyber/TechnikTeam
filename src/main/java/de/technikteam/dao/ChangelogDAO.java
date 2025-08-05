package de.technikteam.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.technikteam.model.Changelog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class ChangelogDAO {

	private final JdbcTemplate jdbcTemplate;
	private final Gson gson = new Gson();
	private final Type listType = new TypeToken<List<Integer>>() {
	}.getType();

	@Autowired
	public ChangelogDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Changelog> rowMapper = (rs, rowNum) -> {
		Changelog cl = new Changelog();
		cl.setId(rs.getInt("id"));
		cl.setVersion(rs.getString("version"));
		cl.setReleaseDate(rs.getDate("release_date").toLocalDate());
		cl.setTitle(rs.getString("title"));
		cl.setNotes(rs.getString("notes"));
		String seenByJson = rs.getString("seen_by_users");
		cl.setSeenByUserIds(gson.fromJson(seenByJson, listType));
		return cl;
	};

	public Optional<Changelog> findById(int id) {
		String sql = "SELECT * FROM changelogs WHERE id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public List<Changelog> findAll() {
		String sql = "SELECT * FROM changelogs ORDER BY release_date DESC, version DESC";
		return jdbcTemplate.query(sql, rowMapper);
	}

	public Optional<Changelog> findLatestUnseen(int userId) {
		// FIX: Correctly use JSON_CONTAINS by passing the value to check as a string.
		String sql = "SELECT * FROM changelogs WHERE NOT JSON_CONTAINS(seen_by_users, CAST(? AS CHAR), '$') ORDER BY release_date DESC, version DESC LIMIT 1";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, String.valueOf(userId)));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public boolean markAsSeen(int changelogId, int userId) {
		// FIX: Correctly use JSON_CONTAINS by passing the value to check as a string.
		String sql = "UPDATE changelogs SET seen_by_users = JSON_ARRAY_APPEND(seen_by_users, '$', ?) WHERE id = ? AND NOT JSON_CONTAINS(seen_by_users, CAST(? AS CHAR), '$')";
		return jdbcTemplate.update(sql, userId, changelogId, String.valueOf(userId)) > 0;
	}

	public boolean create(Changelog changelog) {
		String sql = "INSERT INTO changelogs (version, release_date, title, notes) VALUES (?, ?, ?, ?)";
		return jdbcTemplate.update(sql, changelog.getVersion(), Date.valueOf(changelog.getReleaseDate()),
				changelog.getTitle(), changelog.getNotes()) > 0;
	}

	public boolean update(Changelog changelog) {
		String sql = "UPDATE changelogs SET version = ?, release_date = ?, title = ?, notes = ? WHERE id = ?";
		return jdbcTemplate.update(sql, changelog.getVersion(), Date.valueOf(changelog.getReleaseDate()),
				changelog.getTitle(), changelog.getNotes(), changelog.getId()) > 0;
	}

	public boolean delete(int id) {
		String sql = "DELETE FROM changelogs WHERE id = ?";
		return jdbcTemplate.update(sql, id) > 0;
	}
}