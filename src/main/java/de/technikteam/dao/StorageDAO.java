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

import de.technikteam.model.StorageItem;

public class StorageDAO {
	private static final Logger logger = LogManager.getLogger(StorageDAO.class);

	public Map<String, List<StorageItem>> getAllItemsGroupedByLocation() {
		logger.debug("Fetching all storage items.");
		List<StorageItem> items = new ArrayList<>();
		String sql = "SELECT * FROM storage_items ORDER BY location, cabinet, shelf, name";

		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				StorageItem item = new StorageItem();
				// ... (rest of the mapping code)
				item.setId(rs.getInt("id"));
				item.setName(rs.getString("name"));
				item.setLocation(rs.getString("location"));
				item.setCabinet(rs.getString("cabinet"));
				item.setShelf(rs.getString("shelf"));
				item.setCompartment(rs.getString("compartment"));
				item.setQuantity(rs.getInt("quantity"));
				item.setImagePath(rs.getString("image_path"));
				items.add(item);
			}
			logger.info("Successfully fetched {} storage items from database.", items.size());
		} catch (SQLException e) {
			logger.error("SQL error while fetching storage items.", e);
		}

		return items.stream().collect(Collectors.groupingBy(StorageItem::getLocation));
	}

	// Add these methods to src/main/java/de/technikteam/dao/StorageDAO.java

	private StorageItem mapResultSetToStorageItem(ResultSet rs) throws SQLException {
		StorageItem item = new StorageItem();
		item.setId(rs.getInt("id"));
		item.setName(rs.getString("name"));
		item.setLocation(rs.getString("location"));
		item.setCabinet(rs.getString("cabinet"));
		item.setShelf(rs.getString("shelf"));
		item.setCompartment(rs.getString("compartment"));
		item.setQuantity(rs.getInt("quantity"));
		item.setImagePath(rs.getString("image_path"));
		return item;
	}

	public StorageItem getItemById(int itemId) {
		String sql = "SELECT * FROM storage_items WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return mapResultSetToStorageItem(rs);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching storage item by ID: {}", itemId, e);
		}
		return null;
	}

	public boolean createItem(StorageItem item) {
		String sql = "INSERT INTO storage_items (name, location, cabinet, shelf, compartment, quantity, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, item.getName());
			pstmt.setString(2, item.getLocation());
			pstmt.setString(3, item.getCabinet());
			pstmt.setString(4, item.getShelf());
			pstmt.setString(5, item.getCompartment());
			pstmt.setInt(6, item.getQuantity());
			pstmt.setString(7, item.getImagePath());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating storage item: {}", item.getName(), e);
			return false;
		}
	}

	public boolean updateItem(StorageItem item) {
		logger.info("Updating storage item with ID: {}", item.getId());
		String sql = "UPDATE storage_items SET name=?, location=?, cabinet=?, shelf=?, compartment=?, quantity=?, image_path=? WHERE id=?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// --- DIES IST DER FIX: Alle Parameter müssen gesetzt werden ---
			pstmt.setString(1, item.getName());
			pstmt.setString(2, item.getLocation());
			pstmt.setString(3, item.getCabinet());
			pstmt.setString(4, item.getShelf());
			pstmt.setString(5, item.getCompartment());
			pstmt.setInt(6, item.getQuantity());
			pstmt.setString(7, item.getImagePath());
			pstmt.setInt(8, item.getId()); // Das WHERE-Kriterium

			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating storage item with ID: {}", item.getId(), e);
			return false;
		}
	}

	public boolean deleteItem(int itemId) {
		String sql = "DELETE FROM storage_items WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting storage item with ID: {}", itemId, e);
			return false;
		}
	}
}