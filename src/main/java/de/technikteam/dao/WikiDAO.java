package de.technikteam.dao;

import de.technikteam.model.WikiEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class WikiDAO {
	private static final Logger logger = LogManager.getLogger(WikiDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public WikiDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<WikiEntry> wikiEntryRowMapper = (rs, rowNum) -> {
		WikiEntry entry = new WikiEntry();
		entry.setId(rs.getInt("id"));
		entry.setFilePath(rs.getString("file_path"));
		entry.setContent(rs.getString("content"));
		return entry;
	};

	public Optional<WikiEntry> getWikiEntryById(int id) {
		String sql = "SELECT * FROM wiki_documentation WHERE id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, wikiEntryRowMapper, id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		} catch (Exception e) {
			logger.error("Error fetching wiki entry by ID {}", id, e);
			return Optional.empty();
		}
	}

	public Optional<WikiEntry> findByFilePath(String filePath) {
		String sql = "SELECT * FROM wiki_documentation WHERE file_path = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, wikiEntryRowMapper, filePath));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		} catch (Exception e) {
			logger.error("Error fetching wiki entry by file_path {}", filePath, e);
			return Optional.empty();
		}
	}

	public List<WikiEntry> getAllWikiEntries() {
		String sql = "SELECT * FROM wiki_documentation ORDER BY file_path";
		try {
			return jdbcTemplate.query(sql, wikiEntryRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching all wiki entries", e);
			return List.of();
		}
	}

	public boolean updateWikiContent(int id, String content) {
		String sql = "UPDATE wiki_documentation SET content = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, content, id) > 0;
		} catch (Exception e) {
			logger.error("Error updating wiki content for ID {}", id, e);
			return false;
		}
	}

	public Optional<WikiEntry> createWikiEntry(WikiEntry entry) {
		String sql = "INSERT INTO wiki_documentation (file_path, content) VALUES (?, ?)";
		try {
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, entry.getFilePath());
				ps.setString(2, entry.getContent());
				return ps;
			}, keyHolder);
			entry.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
			return Optional.of(entry);
		} catch (Exception e) {
			logger.error("Error creating wiki entry for path {}", entry.getFilePath(), e);
			return Optional.empty();
		}
	}

	public boolean deleteWikiEntry(int id) {
		String sql = "DELETE FROM wiki_documentation WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, id) > 0;
		} catch (Exception e) {
			logger.error("Error deleting wiki entry with ID {}", id, e);
			return false;
		}
	}

	public List<WikiEntry> search(String query) {
		String sql = "SELECT * FROM wiki_documentation WHERE file_path LIKE ? OR content LIKE ? ORDER BY file_path ASC LIMIT 20";
		String searchTerm = "%" + query + "%";
		try {
			return jdbcTemplate.query(sql, wikiEntryRowMapper, searchTerm, searchTerm);
		} catch (Exception e) {
			logger.error("Error searching wiki entries for query '{}'", query, e);
			return List.of();
		}
	}
}