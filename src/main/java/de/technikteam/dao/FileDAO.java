package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.File;
import de.technikteam.model.FileCategory;

public class FileDAO {
	private static final Logger logger = LogManager.getLogger(FileDAO.class);

	// Ersetzen Sie die bestehende mapResultSetToFile-Methode
	private File mapResultSetToFile(ResultSet rs) throws SQLException {
		File file = new File();
		file.setId(rs.getInt("id"));
		file.setFilename(rs.getString("filename"));
		file.setFilepath(rs.getString("filepath"));
		file.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
		file.setCategoryId(rs.getInt("category_id"));

		// FIX: Weise einen Standardwert zu, wenn der Kategoriename NULL ist.
		String categoryName = rs.getString("category_name");
		file.setCategoryName(categoryName == null ? "Ohne Kategorie" : categoryName);

		return file;
	}

	public Map<String, List<File>> getAllFilesGroupedByCategory() {
		logger.debug("Fetching all files grouped by category.");
		List<File> files = new ArrayList<>();
		// FIX: JOIN mit file_categories, um den Namen der Kategorie zu erhalten
		String sql = "SELECT f.*, fc.name as category_name FROM files f "
				+ "LEFT JOIN file_categories fc ON f.category_id = fc.id " + "ORDER BY fc.name, f.filename";

		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				files.add(mapResultSetToFile(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching files.", e);
		}

		// FIX: Gruppiere nach dem categoryName, nicht der alten getCategory-Methode
		return files.stream().collect(Collectors.groupingBy(File::getCategoryName));
	}

	public boolean createFile(File file) {
		String sql = "INSERT INTO files (filename, filepath, category_id) VALUES (?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, file.getFilename());
			pstmt.setString(2, file.getFilepath());
			// Prüfen, ob eine gültige Kategorie-ID gesetzt ist
			if (file.getCategoryId() > 0) {
				pstmt.setInt(3, file.getCategoryId());
			} else {
				pstmt.setNull(3, Types.INTEGER);
			}
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			/* ... Fehlerbehandlung ... */ return false;
		}
	}

	// Holt alle Kategorien für das Dropdown-Menü
	public List<FileCategory> getAllCategories() {
		List<FileCategory> categories = new ArrayList<>();
		String sql = "SELECT * FROM file_categories ORDER BY name";
		try (Connection conn = DatabaseManager.getConnection();
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

	// Hilfsmethode zur Überprüfung der Spaltenexistenz
	private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves a single file's metadata by its ID. This method is crucial for
	 * deletion, as it provides the filename needed to delete the physical file.
	 *
	 * @param fileId The ID of the file to retrieve.
	 * @return A File object populated with data, or null if not found.
	 */
	public File getFileById(int fileId) {
		logger.debug("Fetching file by ID: {}", fileId);
		// We select all necessary columns to build a complete File object.
		String sql = "SELECT f.id, f.filename, f.filepath, f.uploaded_at, f.category_id, fc.name as category_name "
				+ "FROM files f " + "LEFT JOIN file_categories fc ON f.category_id = fc.id " + "WHERE f.id = ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, fileId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("Found file with ID: {}", fileId);
					// Use the central mapping method to create the object
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
		logger.warn("Deleting file record from database with ID: {}", fileId);
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

	// Fügen Sie diese neue Methode hinzu
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

	// Fügen Sie diese Methoden zu FileDAO.java hinzu
	public boolean updateCategory(int categoryId, String newName) {
		String sql = "UPDATE file_categories SET name = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newName);
			pstmt.setInt(2, categoryId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			/* ... */ return false;
		}
	}

	public boolean deleteCategory(int categoryId) {
		// Hinweis: Wegen "ON DELETE SET NULL" werden Dateien in dieser Kategorie nicht
		// gelöscht,
		// sondern ihre category_id wird auf NULL gesetzt.
		String sql = "DELETE FROM file_categories WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, categoryId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			/* ... */ return false;
		}
	}
}