package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.FileCategory;

/**
 * Data Access Object for managing file categories. Provides full CRUD (Create,
 * Read, Update, Delete) functionality.
 */
public class FileCategoryDAO {
	private static final Logger logger = LogManager.getLogger(FileCategoryDAO.class);

	/**
	 * Fetches all file categories from the database, ordered by name.
	 * 
	 * @return A list of FileCategory objects.
	 */
	public List<FileCategory> getAll() {
		logger.debug("Fetching all file categories.");
		List<FileCategory> categories = new ArrayList<>();
		String sql = "SELECT * FROM file_categories ORDER BY name";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				FileCategory category = new FileCategory();
				category.setId(rs.getInt("id"));
				category.setName(rs.getString("name"));
				categories.add(category);
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching file categories.", e);
		}
		return categories;
	}

	/**
	 * Creates a new file category in the database.
	 * 
	 * @param category The FileCategory object to create (ID is ignored).
	 * @return true if creation was successful, false otherwise.
	 */
	public boolean create(FileCategory category) {
		logger.info("Creating new file category: {}", category.getName());
		String sql = "INSERT INTO file_categories (name) VALUES (?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, category.getName());
			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			logger.error("SQL error creating file category: {}", category.getName(), e);
			return false;
		}
	}

	/**
	 * Updates an existing file category's name.
	 * 
	 * @param category The FileCategory object with the updated name and correct ID.
	 * @return true if update was successful, false otherwise.
	 */
	public boolean update(FileCategory category) {
		logger.info("Updating file category with ID: {}", category.getId());
		String sql = "UPDATE file_categories SET name = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, category.getName());
			pstmt.setInt(2, category.getId());
			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			logger.error("SQL error updating file category with ID: {}", category.getId(), e);
			return false;
		}
	}

	/**
	 * Deletes a file category from the database. Note: Due to the 'ON DELETE SET
	 * NULL' foreign key constraint in the 'files' table, files in this category
	 * will not be deleted but will have their category_id set to NULL.
	 * 
	 * @param categoryId The ID of the category to delete.
	 * @return true if deletion was successful, false otherwise.
	 */
	public boolean delete(int categoryId) {
		logger.warn("Deleting file category with ID: {}", categoryId);
		String sql = "DELETE FROM file_categories WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, categoryId);
			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			logger.error("SQL error deleting file category with ID: {}", categoryId, e);
			return false;
		}
	}
}