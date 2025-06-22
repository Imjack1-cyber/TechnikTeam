package de.technikteam.dao;

import de.technikteam.model.StorageItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data Access Object for managing inventory in the `storage_items` table. It
 * handles full CRUD operations for storage items and provides methods for
 * quantity adjustments and grouping items by location for display.
 */
public class StorageDAO {
	private static final Logger logger = LogManager.getLogger(StorageDAO.class.getName());

	/**
	 * Fetches all storage items and groups them by their 'location' field.
	 * 
	 * @return A Map where keys are location names and values are lists of items in
	 *         that location.
	 */
	public Map<String, List<StorageItem>> getAllItemsGroupedByLocation() {
		logger.debug("Fetching all storage items, grouped by location.");
		List<StorageItem> items = new ArrayList<>();
		String sql = "SELECT * FROM storage_items ORDER BY location, cabinet, shelf, name";

		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				items.add(mapResultSetToStorageItem(rs));
			}
			logger.info("Successfully fetched {} storage items from database.", items.size());
		} catch (SQLException e) {
			logger.error("SQL error while fetching storage items.", e);
		}

		// Group the flat list of items into a map using Java Streams
		return items.stream().collect(Collectors.groupingBy(StorageItem::getLocation));
	}

	/**
	 * Helper method to map a row from a ResultSet to a StorageItem object.
	 * 
	 * @param rs The ResultSet to map.
	 * @return A populated StorageItem object.
	 * @throws SQLException If a database error occurs.
	 */
	private StorageItem mapResultSetToStorageItem(ResultSet rs) throws SQLException {
		StorageItem item = new StorageItem();
		item.setId(rs.getInt("id"));
		item.setName(rs.getString("name"));
		item.setLocation(rs.getString("location"));
		item.setCabinet(rs.getString("cabinet"));
		item.setShelf(rs.getString("shelf"));
		item.setCompartment(rs.getString("compartment"));
		item.setQuantity(rs.getInt("quantity"));
		item.setMaxQuantity(rs.getInt("max_quantity"));
		item.setImagePath(rs.getString("image_path"));
		return item;
	}

	/**
	 * Fetches a single storage item by its unique ID.
	 * 
	 * @param itemId The ID of the item to fetch.
	 * @return A StorageItem object, or null if not found.
	 */
	public StorageItem getItemById(int itemId) {
		String sql = "SELECT * FROM storage_items WHERE id = ?";
		logger.debug("Fetching storage item by ID: {}", itemId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				logger.info("Found storage item '{}' with ID: {}", rs.getString("name"), itemId);
				return mapResultSetToStorageItem(rs);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching storage item by ID: {}", itemId, e);
		}
		logger.warn("No storage item found with ID: {}", itemId);
		return null;
	}

	/**
	 * Creates a new storage item in the database.
	 * 
	 * @param item The StorageItem object to persist.
	 * @return true if the creation was successful.
	 */
	public boolean createItem(StorageItem item) {
		String sql = "INSERT INTO storage_items (name, location, cabinet, shelf, compartment, quantity, max_quantity, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		logger.debug("Creating new storage item: {}", item.getName());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, item.getName());
			pstmt.setString(2, item.getLocation());
			pstmt.setString(3, item.getCabinet());
			pstmt.setString(4, item.getShelf());
			pstmt.setString(5, item.getCompartment());
			pstmt.setInt(6, item.getQuantity());
			pstmt.setInt(7, item.getMaxQuantity());
			pstmt.setString(8, item.getImagePath());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating storage item: {}", item.getName(), e);
			return false;
		}
	}

	/**
	 * Updates all fields of an existing storage item.
	 * 
	 * @param item The StorageItem object with the new data.
	 * @return true if the update was successful.
	 */
	public boolean updateItem(StorageItem item) {
		logger.debug("DAO: Preparing to update item ID: {}. Values -> Name: '{}', Quantity: {}, MaxQuantity: {}",
				item.getId(), item.getName(), item.getQuantity(), item.getMaxQuantity());
		String sql = "UPDATE storage_items SET name=?, location=?, cabinet=?, shelf=?, compartment=?, quantity=?, max_quantity=?, image_path=? WHERE id=?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, item.getName());
			pstmt.setString(2, item.getLocation());
			pstmt.setString(3, item.getCabinet());
			pstmt.setString(4, item.getShelf());
			pstmt.setString(5, item.getCompartment());
			pstmt.setInt(6, item.getQuantity());
			pstmt.setInt(7, item.getMaxQuantity());
			pstmt.setString(8, item.getImagePath());
			pstmt.setInt(9, item.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating storage item with ID: {}", item.getId(), e);
			return false;
		}
	}

	/**
	 * Atomically updates the quantity of an item by a given amount (can be
	 * negative). Ensures that the quantity never drops below zero.
	 * 
	 * @param itemId         The ID of the item to update.
	 * @param quantityChange The amount to add (positive) or remove (negative).
	 * @return true if the update was successful.
	 * @throws SQLException if a database error occurs.
	 */
	public boolean updateItemQuantity(int itemId, int quantityChange) throws SQLException {
		String sql = "UPDATE storage_items SET quantity = quantity + ? WHERE id = ? AND quantity + ? >= 0";
		logger.debug("Attempting to change quantity for item ID {} by {}", itemId, quantityChange);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, quantityChange);
			pstmt.setInt(2, itemId);
			pstmt.setInt(3, quantityChange);
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Successfully changed quantity for item {} by {}", itemId, quantityChange);
				return true;
			} else {
				logger.warn("Failed to update quantity for item {}. Not enough stock or item not found.", itemId);
				return false;
			}
		}
	}

	/**
	 * Deletes a storage item from the database.
	 * 
	 * @param itemId The ID of the item to delete.
	 * @return true if deletion was successful.
	 */
	public boolean deleteItem(int itemId) {
		String sql = "DELETE FROM storage_items WHERE id = ?";
		logger.warn("Attempting to delete storage item with ID: {}", itemId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting storage item with ID: {}", itemId, e);
			return false;
		}
	}
}