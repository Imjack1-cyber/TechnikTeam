package de.technikteam.dao;

import de.technikteam.model.File;
import de.technikteam.model.FileCategory;
import de.technikteam.model.User;
import de.technikteam.util.DaoUtils;
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
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class FileDAO {
	private static final Logger logger = LogManager.getLogger(FileDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public FileDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<File> fileRowMapper = (rs, rowNum) -> {
		File file = new File();
		file.setId(rs.getInt("id"));
		file.setFilename(rs.getString("filename"));
		file.setFilepath(rs.getString("filepath"));
		file.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
		file.setCategoryId(rs.getObject("category_id", Integer.class));
		if (DaoUtils.hasColumn(rs, "needs_warning")) {
			file.setNeedsWarning(rs.getBoolean("needs_warning"));
		}
		if (DaoUtils.hasColumn(rs, "required_role")) {
			file.setRequiredRole(rs.getString("required_role"));
		}
		if (DaoUtils.hasColumn(rs, "category_name")) {
			file.setCategoryName(
					rs.getString("category_name") == null ? "Ohne Kategorie" : rs.getString("category_name"));
		} else {
			file.setCategoryName("Ohne Kategorie");
		}
		logger.trace("Mapped file from ResultSet: ID={}, Name={}, CategoryID={}, CategoryName={}", file.getId(),
				file.getFilename(), file.getCategoryId(), file.getCategoryName());
		return file;
	};

	public Map<String, List<File>> getAllFilesGroupedByCategory(User user) {
		logger.debug("Grouping all files by category for user: {}", user != null ? user.getUsername() : "SYSTEM");
		List<File> files = getAllFiles(user);
		Map<String, List<File>> groupedFiles = files.stream()
				.filter(file -> file.getFilepath() == null
						|| (!file.getFilepath().startsWith("chat/") && !file.getFilepath().startsWith("eventchat/")))
				.collect(Collectors.groupingBy(File::getCategoryName));

		if (logger.isTraceEnabled()) {
			groupedFiles.forEach((category, fileList) -> logger.trace("Category '{}' contains {} files: {}", category,
					fileList.size(), fileList.stream().map(File::getFilename).collect(Collectors.joining(", "))));
		}
		return groupedFiles;
	}

	public List<File> getAllFiles(User user) {
		StringBuilder sql = new StringBuilder(
				"SELECT f.*, fc.name as category_name FROM files f LEFT JOIN file_categories fc ON f.category_id = fc.id ");

		if (user != null && !user.hasAdminAccess()) {
			sql.append("WHERE f.required_role = 'NUTZER' ");
		}

		sql.append("ORDER BY CASE WHEN fc.name IS NULL THEN 1 ELSE 0 END, fc.name, f.filename");
		logger.debug("Executing getAllFiles SQL for user '{}': {}", user != null ? user.getUsername() : "SYSTEM",
				sql.toString());

		try {
			List<File> files = jdbcTemplate.query(sql.toString(), fileRowMapper);
			logger.debug("Fetched {} total file records from database.", files.size());
			return files;
		} catch (Exception e) {
			logger.error("Error while fetching files.", e);
			return List.of();
		}
	}

	public int createFile(File file) {
		String sql = "INSERT INTO files (filename, filepath, category_id, required_role, needs_warning) VALUES (?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, file.getFilename());
				ps.setString(2, file.getFilepath());
				if (file.getCategoryId() != null && file.getCategoryId() > 0) {
					ps.setInt(3, file.getCategoryId());
				} else {
					ps.setNull(3, Types.INTEGER);
				}
				ps.setString(4, file.getRequiredRole());
				ps.setBoolean(5, file.isNeedsWarning());
				return ps;
			}, keyHolder);
			return Objects.requireNonNull(keyHolder.getKey()).intValue();
		} catch (Exception e) {
			logger.error("Error creating file record for '{}'", file.getFilename(), e);
			return 0;
		}
	}

	public boolean touchFileRecord(int fileId) {
		String sql = "UPDATE files SET uploaded_at = CURRENT_TIMESTAMP WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, fileId) > 0;
		} catch (Exception e) {
			logger.error("Error touching file record for ID {}", fileId, e);
			return false;
		}
	}

	public boolean reassignFileToCategory(int fileId, int categoryId) {
		String sql = "UPDATE files SET category_id = ? WHERE id = ?";
		try {
			Object newCategoryId = categoryId > 0 ? categoryId : null;
			return jdbcTemplate.update(sql, newCategoryId, fileId) > 0;
		} catch (Exception e) {
			logger.error("Error reassigning file {} to category {}", fileId, categoryId, e);
			return false;
		}
	}

	public List<FileCategory> getAllCategories() {
		String sql = "SELECT * FROM file_categories ORDER BY name";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				FileCategory cat = new FileCategory();
				cat.setId(rs.getInt("id"));
				cat.setName(rs.getString("name"));
				return cat;
			});
		} catch (Exception e) {
			logger.error("Error fetching file categories.", e);
			return List.of();
		}
	}

	public File getFileById(int fileId) {
		String sql = "SELECT f.*, fc.name as category_name FROM files f LEFT JOIN file_categories fc ON f.category_id = fc.id WHERE f.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, fileRowMapper, fileId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error while fetching file with ID: {}", fileId, e);
			return null;
		}
	}

	public boolean deleteFile(int fileId) {
		String sql = "DELETE FROM files WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, fileId) > 0;
		} catch (Exception e) {
			logger.error("Error while deleting file record with ID: {}", fileId, e);
			return false;
		}
	}

	public boolean createCategory(String categoryName) {
		String sql = "INSERT INTO file_categories (name) VALUES (?)";
		try {
			return jdbcTemplate.update(sql, categoryName) > 0;
		} catch (Exception e) {
			logger.error("Error creating file category '{}'", categoryName, e);
			return false;
		}
	}

	public boolean deleteCategory(int categoryId) {
		String sql = "DELETE FROM file_categories WHERE id = ?";
		try {
			// First, un-assign files from this category
			jdbcTemplate.update("UPDATE files SET category_id = NULL WHERE category_id = ?", categoryId);
			return jdbcTemplate.update(sql, categoryId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting category ID {}", categoryId, e);
			return false;
		}
	}

	public String getCategoryNameById(int categoryId) {
		String sql = "SELECT name FROM file_categories WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, String.class, categoryId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching category name for ID: {}", categoryId, e);
			return null;
		}
	}
}