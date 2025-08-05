package de.technikteam.dao;

import de.technikteam.model.PageDocumentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PageDocumentationDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public PageDocumentationDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<PageDocumentation> rowMapper = (rs, rowNum) -> {
		PageDocumentation doc = new PageDocumentation();
		doc.setId(rs.getInt("id"));
		doc.setPageKey(rs.getString("page_key"));
		doc.setTitle(rs.getString("title"));
		doc.setPagePath(rs.getString("page_path"));
		doc.setFeatures(rs.getString("features"));
		doc.setRelatedPages(rs.getString("related_pages"));
		doc.setAdminOnly(rs.getBoolean("admin_only"));
		doc.setWikiEntryId(rs.getObject("wiki_entry_id", Integer.class));
		doc.setCategory(rs.getString("category"));
		doc.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		doc.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
		return doc;
	};

	public List<PageDocumentation> findAll(boolean isAdmin) {
		String sql = "SELECT * FROM page_documentation";
		if (!isAdmin) {
			sql += " WHERE admin_only = FALSE";
		}
		sql += " ORDER BY category, title ASC";
		return jdbcTemplate.query(sql, rowMapper);
	}

	public Optional<PageDocumentation> findById(int id) {
		String sql = "SELECT * FROM page_documentation WHERE id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Optional<PageDocumentation> findByKey(String pageKey) {
		String sql = "SELECT * FROM page_documentation WHERE page_key = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, pageKey));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public PageDocumentation create(PageDocumentation doc) {
		String sql = "INSERT INTO page_documentation (page_key, title, page_path, features, related_pages, admin_only, wiki_entry_id, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, doc.getPageKey());
			ps.setString(2, doc.getTitle());
			ps.setString(3, doc.getPagePath());
			ps.setString(4, doc.getFeatures());
			ps.setString(5, doc.getRelatedPages());
			ps.setBoolean(6, doc.isAdminOnly());
			if (doc.getWikiEntryId() != null) {
				ps.setInt(7, doc.getWikiEntryId());
			} else {
				ps.setNull(7, Types.INTEGER);
			}
			ps.setString(8, doc.getCategory());
			return ps;
		}, keyHolder);
		doc.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
		return doc;
	}

	public PageDocumentation update(PageDocumentation doc) {
		String sql = "UPDATE page_documentation SET title = ?, page_path = ?, features = ?, related_pages = ?, admin_only = ?, wiki_entry_id = ?, category = ? WHERE id = ?";
		jdbcTemplate.update(sql, doc.getTitle(), doc.getPagePath(), doc.getFeatures(), doc.getRelatedPages(),
				doc.isAdminOnly(), doc.getWikiEntryId(), doc.getCategory(), doc.getId());
		return doc;
	}

	public boolean delete(int id) {
		return jdbcTemplate.update("DELETE FROM page_documentation WHERE id = ?", id) > 0;
	}
}