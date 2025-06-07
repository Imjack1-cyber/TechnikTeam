package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.File; // Ensure this import points to your File model

public class FileDAO {
	private static final Logger logger = LogManager.getLogger(FileDAO.class);

	/**
	 * Helper method to map a ResultSet row to a File object.
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
		file.setCategory(rs.getString("category"));
		return file;
	}

	public Map<String, List<File>> getAllFilesGroupedByCategory() {
		// ... (existing implementation)
		logger.debug("Fetching all files grouped by category.");
		List<File> files = new ArrayList<>();
		String sql = "SELECT * FROM files ORDER BY category, filename";

		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				files.add(mapResultSetToFile(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching files.", e);
		}

		return files.stream().collect(Collectors.groupingBy(File::getCategory));
	}

	/**
	 * Retrieves a single file's metadata by its ID.
	 * 
	 * @param fileId The ID of the file to retrieve.
	 * @return A File object, or null if not found.
	 */
	public File getFileById(int fileId) {
		logger.debug("Fetching file by ID: {}", fileId);
		String sql = "SELECT * FROM files WHERE id = ?";
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
	 * Deletes a file record from the database. Does NOT delete the physical file.
	 * 
	 * @param fileId The ID of the file to delete.
	 * @return true if the database record was successfully deleted, false
	 *         otherwise.
	 */
	public boolean deleteFile(int fileId) {
		logger.warn("Deleting file record from database with ID: {}", fileId);
		String sql = "DELETE FROM files WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, fileId);
			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			logger.error("SQL error while deleting file record with ID: {}", fileId, e);
			return false;
		}
	}

	/**
	 * Creates a new file record in the database.
	 * 
	 * @param file The File object containing metadata to save.
	 * @return true if the creation was successful, false otherwise.
	 */
	public boolean createFile(File file) {
		logger.info("Creating new file record in DB: {}", file.getFilename());
		String sql = "INSERT INTO files (filename, filepath, category) VALUES (?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, file.getFilename());
			pstmt.setString(2, file.getFilepath());
			pstmt.setString(3, file.getCategory());

			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			// Check for duplicate entry error (UNIQUE constraint on filepath)
			if (e.getErrorCode() == 1062) { // MySQL error code for duplicate entry
				logger.error("SQL error: A file with the path '{}' already exists.", file.getFilepath(), e);
			} else {
				logger.error("SQL error while creating file record '{}'.", file.getFilename(), e);
			}
			return false;
		}
	}
}