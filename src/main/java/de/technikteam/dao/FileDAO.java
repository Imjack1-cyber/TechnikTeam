package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.File;
import de.technikteam.model.FileCategory;
import de.technikteam.model.User;
import de.technikteam.service.ConfigurationService;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class FileDAO {
	private static final Logger logger = LogManager.getLogger(FileDAO.class);
	private final DatabaseManager dbManager;
	private final ConfigurationService configService;

	@Inject
	public FileDAO(DatabaseManager dbManager, ConfigurationService configService) {
		this.dbManager = dbManager;
		this.configService = configService;
	}

	private File mapResultSetToFile(ResultSet rs) throws SQLException {
		File file = new File();
		file.setId(rs.getInt("id"));
		file.setFilename(rs.getString("filename"));
		file.setFilepath(rs.getString("filepath"));
		file.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
		file.setCategoryId(rs.getInt("category_id"));

		if (DaoUtils.hasColumn(rs, "required_role")) {
			file.setRequiredRole(rs.getString("required_role"));
		}

		if (DaoUtils.hasColumn(rs, "category_name")) {
			String categoryName = rs.getString("category_name");
			file.setCategoryName(categoryName == null ? "Ohne Kategorie" : categoryName);
		} else {
			file.setCategoryName("Ohne Kategorie");
		}
		return file;
	}

	public Map<String, List<File>> getAllFilesGroupedByCategory(User user) {
		Map<Integer, String> categoryIdToNameMap = new HashMap<>();
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT id, name FROM file_categories")) {
			while (rs.next()) {
				categoryIdToNameMap.put(rs.getInt("id"), rs.getString("name"));
			}
		} catch (SQLException e) {
			logger.error("Could not fetch file categories for grouping.", e);
			return new HashMap<>();
		}

		List<File> files = new ArrayList<>();
		String sql = "SELECT * FROM files ";
		if (!user.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			sql += "WHERE required_role = 'NUTZER' ";
		}
		sql += "ORDER BY filename";

		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				File file = new File();
				file.setId(rs.getInt("id"));
				file.setFilename(rs.getString("filename"));
				file.setFilepath(rs.getString("filepath"));
				file.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
				file.setRequiredRole(rs.getString("required_role"));
				int categoryId = rs.getInt("category_id");
				file.setCategoryId(categoryId);
				String categoryName = categoryIdToNameMap.get(categoryId);
				file.setCategoryName(categoryName != null ? categoryName : "Ohne Kategorie");
				files.add(file);
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching files.", e);
		}
		return files.stream().collect(Collectors.groupingBy(File::getCategoryName));
	}

	public String getDocumentContentByPath(String filepath) {
		try {
			java.io.File physicalFile = new java.io.File(configService.getProperty("upload.directory"), filepath);
			return new String(Files.readAllBytes(physicalFile.toPath()), StandardCharsets.UTF_8);
		} catch (NoSuchFileException e) {
			logger.error("Physical file is missing at path: {}", filepath);
		} catch (IOException e) {
			logger.error("Could not read file content for path {}", filepath, e);
		}
		return "";
	}

	public boolean createFile(File file) {
		String sql = "INSERT INTO files (filename, filepath, category_id, required_role) VALUES (?, ?, ?, ?)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, file.getFilename());
			pstmt.setString(2, file.getFilepath());
			if (file.getCategoryId() > 0) {
				pstmt.setInt(3, file.getCategoryId());
			} else {
				pstmt.setNull(3, Types.INTEGER);
			}
			pstmt.setString(4, file.getRequiredRole());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating file record for '{}'", file.getFilename(), e);
			return false;
		}
	}

	public boolean updateFileContent(String filepath, String content) {
		try {
			java.io.File targetFile = new java.io.File(configService.getProperty("upload.directory"), filepath);
			Files.write(targetFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
			return true;
		} catch (IOException e) {
			logger.error("Failed to write updated content to file at path: {}", filepath, e);
			return false;
		}
	}

	public boolean touchFileRecord(int fileId) {
		String sql = "UPDATE files SET uploaded_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, fileId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error touching file record for ID {}", fileId, e);
			return false;
		}
	}

	public boolean reassignFileToCategory(int fileId, int categoryId) {
		String sql = "UPDATE files SET category_id = ? WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			if (categoryId > 0) {
				pstmt.setInt(1, categoryId);
			} else {
				pstmt.setNull(1, Types.INTEGER);
			}
			pstmt.setInt(2, fileId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error reassigning file {} to category {}", fileId, categoryId, e);
			return false;
		}
	}

	public List<FileCategory> getAllCategories() {
		List<FileCategory> categories = new ArrayList<>();
		String sql = "SELECT * FROM file_categories ORDER BY name";
		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				FileCategory cat = new FileCategory();
				cat.setId(rs.getInt("id"));
				cat.setName(rs.getString("name"));
				categories.add(cat);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching file categories.", e);
		}
		return categories;
	}

	public File getFileById(int fileId) {
		String sql = "SELECT f.*, fc.name as category_name FROM files f LEFT JOIN file_categories fc ON f.category_id = fc.id WHERE f.id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, fileId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToFile(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching file with ID: {}", fileId, e);
		}
		return null;
	}

	public boolean deleteFile(int fileId) {
		File dbFile = getFileById(fileId);
		if (dbFile == null) {
			return false;
		}
		String sql = "DELETE FROM files WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, fileId);
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				try {
					java.io.File physicalFile = new java.io.File(configService.getProperty("upload.directory"),
							dbFile.getFilepath());
					Files.deleteIfExists(physicalFile.toPath());
				} catch (IOException e) {
					logger.error("Error deleting physical file for ID: {}", fileId, e);
				}
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			logger.error("SQL error while deleting file record with ID: {}", fileId, e);
			return false;
		}
	}

	public boolean createCategory(String categoryName) {
		String sql = "INSERT INTO file_categories (name) VALUES (?)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, categoryName);
			return pstmt.executeUpdate() > 0;
		} catch (SQLIntegrityConstraintViolationException e) {
			logger.warn("Attempted to create a duplicate file category: '{}'", categoryName);
			return false;
		} catch (SQLException e) {
			logger.error("SQL error creating file category '{}'", categoryName, e);
			return false;
		}
	}

	public boolean updateCategory(int categoryId, String newName) {
		String sql = "UPDATE file_categories SET name = ? WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newName);
			pstmt.setInt(2, categoryId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating category ID {}", categoryId, e);
			return false;
		}
	}

	public boolean deleteCategory(int categoryId) {
		String sql = "DELETE FROM file_categories WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, categoryId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting category ID {}", categoryId, e);
			return false;
		}
	}

	public String getCategoryNameById(int categoryId) {
		String sql = "SELECT name FROM file_categories WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, categoryId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("name");
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching category name for ID: {}", categoryId, e);
		}
		return null;
	}

	public String getDocumentContent(String documentName) {
		String sql = "SELECT content FROM shared_documents WHERE document_name = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, documentName);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("content");
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching document content for name: {}", documentName, e);
		}
		return "";
	}

	public boolean updateDocumentContent(String documentName, String content) {
		String sql = "INSERT INTO shared_documents (document_name, content) VALUES (?, ?) ON DUPLICATE KEY UPDATE content = VALUES(content)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, documentName);
			pstmt.setString(2, content);
			return pstmt.executeUpdate() >= 0;
		} catch (SQLException e) {
			logger.error("Error upserting document content for name: {}", documentName, e);
			return false;
		}
	}
}