package de.technikteam.dao;

import de.technikteam.model.File;
import de.technikteam.model.FileCategory;
import de.technikteam.model.User;
import de.technikteam.util.DaoUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This DAO manages metadata of uploaded files stored in the `files` and
 * `file_categories` tables. It handles creating, reading, and deleting file
 * records and categories. It includes role-based filtering to control file
 * visibility and logic to group files by category for display in the UI. It
 * also provides methods to manage a simple key-value content store in the
 * `shared_documents` table.
 */
public class FileDAO {
	private static final Logger logger = LogManager.getLogger(FileDAO.class);

	/**
	 * Helper method to map a row from a ResultSet to a File object.
	 * 
	 * @param rs The ResultSet to map.
	 * @return A populated File object.
	 * @throws SQLException If a database error occurs.
	 */
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

		String categoryName = rs.getString("category_name");
		file.setCategoryName(categoryName == null ? "Ohne Kategorie" : categoryName);

		return file;
	}

	/**
	 * Fetches all file records, applying role-based filtering, and groups them by
	 * category name.
	 * 
	 * @param user The current user, used to determine their role.
	 * @return A Map where keys are category names and values are lists of files.
	 */
	public Map<String, List<File>> getAllFilesGroupedByCategory(User user) {
		logger.debug("Fetching all files grouped by category for user role: {}", user.getRole());
		List<File> files = new ArrayList<>();

		String sql = "SELECT f.*, fc.name as category_name FROM files f "
				+ "LEFT JOIN file_categories fc ON f.category_id = fc.id ";

		if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
			sql += "WHERE f.required_role = 'NUTZER' ";
			logger.debug("Applying 'NUTZER' role filter for file query.");
		}

		sql += "ORDER BY fc.name, f.filename";

		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				files.add(mapResultSetToFile(rs));
			}
			logger.info("Fetched {} files visible to user role '{}'.", files.size(), user.getRole());
		} catch (SQLException e) {
			logger.error("SQL error while fetching files.", e);
		}

		return files.stream().collect(Collectors.groupingBy(File::getCategoryName));
	}

	/**
	 * Creates a new file metadata record in the database.
	 * 
	 * @param file The File object to persist.
	 * @return true if creation was successful, false otherwise.
	 */
	public boolean createFile(File file) {
		String sql = "INSERT INTO files (filename, filepath, category_id, required_role) VALUES (?, ?, ?, ?)";
		logger.debug("Creating file record for '{}' with role '{}'", file.getFilename(), file.getRequiredRole());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

	/**
	 * Fetches all file categories from the database, sorted by name.
	 * 
	 * @return A list of FileCategory objects.
	 */
	public List<FileCategory> getAllCategories() {
		List<FileCategory> categories = new ArrayList<>();
		String sql = "SELECT * FROM file_categories ORDER BY name";
		logger.debug("Fetching all file categories.");
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				FileCategory cat = new FileCategory();
				cat.setId(rs.getInt("id"));
				cat.setName(rs.getString("name"));
				categories.add(cat);
			}
			logger.info("Fetched {} file categories.", categories.size());
		} catch (SQLException e) {
			logger.error("SQL error fetching file categories.", e);
		}
		return categories;
	}

	/**
	 * Retrieves a single file's metadata by its ID.
	 *
	 * @param fileId The ID of the file to retrieve.
	 * @return A File object populated with data, or null if not found.
	 */
	public File getFileById(int fileId) {
		logger.debug("Fetching file by ID: {}", fileId);
		String sql = "SELECT f.*, fc.name as category_name " + "FROM files f "
				+ "LEFT JOIN file_categories fc ON f.category_id = fc.id " + "WHERE f.id = ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, fileId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("Found file with ID: {}", fileId);
					return mapResultSetToFile(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching file with ID: {}", fileId, e);
		}

		logger.warn("No file found with ID: {}", fileId);
		return null;
	}

	/**
	 * Deletes a file record from the 'files' table in the database. Note: This
	 * method ONLY deletes the database record. The physical file must be deleted
	 * separately by the calling servlet.
	 *
	 * @param fileId The ID of the file record to delete.
	 * @return true if the database record was successfully deleted, false
	 *         otherwise.
	 */
	public boolean deleteFile(int fileId) {
		logger.warn("Attempting to delete file record from database with ID: {}", fileId);
		String sql = "DELETE FROM files WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, fileId);
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Successfully deleted file record with ID: {}", fileId);
				return true;
			} else {
				logger.warn("Could not delete file record with ID: {}. It might not exist.", fileId);
				return false;
			}
		} catch (SQLException e) {
			logger.error("SQL error while deleting file record with ID: {}", fileId, e);
			return false;
		}
	}

	/**
	 * Creates a new file category.
	 * 
	 * @param categoryName The name of the new category.
	 * @return true if successful.
	 */
	public boolean createCategory(String categoryName) {
		logger.info("Creating new file category: {}", categoryName);
		String sql = "INSERT INTO file_categories (name) VALUES (?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, categoryName);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating file category '{}'", categoryName, e);
			return false;
		}
	}

	/**
	 * Updates the name of an existing file category.
	 * 
	 * @param categoryId The ID of the category to update.
	 * @param newName    The new name for the category.
	 * @return true if successful.
	 */
	public boolean updateCategory(int categoryId, String newName) {
		String sql = "UPDATE file_categories SET name = ? WHERE id = ?";
		logger.debug("Updating category ID {} to new name '{}'", categoryId, newName);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newName);
			pstmt.setInt(2, categoryId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating category ID {}", categoryId, e);
			return false;
		}
	}

	/**
	 * Deletes a file category. Due to "ON DELETE SET NULL" constraint in the DB,
	 * files in this category will have their category_id set to NULL.
	 * 
	 * @param categoryId The ID of the category to delete.
	 * @return true if successful.
	 */
	public boolean deleteCategory(int categoryId) {
		logger.warn("Attempting to delete category ID: {}", categoryId);
		String sql = "DELETE FROM file_categories WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, categoryId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting category ID {}", categoryId, e);
			return false;
		}
	}

	/**
	 * Retrieves the name of a category by its ID.
	 * 
	 * @param categoryId The ID of the category.
	 * @return The category name, or null if not found.
	 */
	public String getCategoryNameById(int categoryId) {
		String sql = "SELECT name FROM file_categories WHERE id = ?";
		logger.debug("Fetching category name for ID: {}", categoryId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

	/**
	 * Retrieves the content of a shared document (e.g., for the collaborative
	 * editor).
	 * 
	 * @param documentName The unique name/key of the document.
	 * @return The document's content as a string, or an empty string if not found.
	 */
	public String getDocumentContent(String documentName) {
		String sql = "SELECT content FROM shared_documents WHERE document_name = ?";
		logger.trace("Fetching document content for name: {}", documentName);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

	/**
	 * Updates the content of a shared document.
	 * 
	 * @param documentName The unique name/key of the document to update.
	 * @param content      The new content to save.
	 * @return true if the update was successful.
	 */
	public boolean updateDocumentContent(String documentName, String content) {
		String sql = "UPDATE shared_documents SET content = ? WHERE document_name = ?";
		logger.trace("Updating document content for name: {}", documentName);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, content);
			pstmt.setString(2, documentName);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating document content for name: {}", documentName, e);
			return false;
		}
	}
}