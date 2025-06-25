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
 * DAO for managing inventory in the `storage_items` table.
 */
public class StorageDAO {
	private static final Logger logger = LogManager.getLogger(StorageDAO.class.getName());

	public Map<String, List<StorageItem>> getAllItemsGroupedByLocation() {
		List<StorageItem> items = new ArrayList<>();
		String sql = "SELECT * FROM storage_items ORDER BY location, cabinet, shelf, name";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				items.add(mapResultSetToStorageItem(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching storage items.", e);
		}
		return items.stream().collect(Collectors.groupingBy(StorageItem::getLocation));
	}

	public List<StorageItem> getDefectiveItems() {
		List<StorageItem> items = new ArrayList<>();
		String sql = "SELECT * FROM storage_items WHERE defective_quantity > 0 ORDER BY location, name";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				items.add(mapResultSetToStorageItem(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching defective items.", e);
		}
		return items;
	}

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
		item.setDefectiveQuantity(rs.getInt("defective_quantity"));
		item.setDefectReason(rs.getString("defect_reason"));
		item.setWeightKg(rs.getDouble("weight_kg"));
		item.setPriceEur(rs.getDouble("price_eur"));
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
		String sql = "INSERT INTO storage_items (name, location, cabinet, shelf, compartment, quantity, max_quantity, weight_kg, price_eur, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, item.getName());
			pstmt.setString(2, item.getLocation());
			pstmt.setString(3, item.getCabinet());
			pstmt.setString(4, item.getShelf());
			pstmt.setString(5, item.getCompartment());
			pstmt.setInt(6, item.getQuantity());
			pstmt.setInt(7, item.getMaxQuantity());
			pstmt.setDouble(8, item.getWeightKg());
			pstmt.setDouble(9, item.getPriceEur());
			pstmt.setString(10, item.getImagePath());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating storage item: {}", item.getName(), e);
			return false;
		}
	}

	public boolean updateItem(StorageItem item) {
		String sql = "UPDATE storage_items SET name=?, location=?, cabinet=?, shelf=?, compartment=?, quantity=?, max_quantity=?, defective_quantity=?, defect_reason=?, weight_kg=?, price_eur=?, image_path=? WHERE id=?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, item.getName());
			pstmt.setString(2, item.getLocation());
			pstmt.setString(3, item.getCabinet());
			pstmt.setString(4, item.getShelf());
			pstmt.setString(5, item.getCompartment());
			pstmt.setInt(6, item.getQuantity());
			pstmt.setInt(7, item.getMaxQuantity());
			pstmt.setInt(8, item.getDefectiveQuantity());
			pstmt.setString(9, item.getDefectReason());
			pstmt.setDouble(10, item.getWeightKg());
			pstmt.setDouble(11, item.getPriceEur());
			pstmt.setString(12, item.getImagePath());
			pstmt.setInt(13, item.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating storage item with ID: {}", item.getId(), e);
			return false;
		}
	}

	public boolean updateItemQuantity(int itemId, int quantityChange) throws SQLException {
		String sql = "UPDATE storage_items SET quantity = quantity + ? WHERE id = ? AND quantity + ? >= defective_quantity";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, quantityChange);
			pstmt.setInt(2, itemId);
			pstmt.setInt(3, quantityChange);
			return pstmt.executeUpdate() > 0;
		}
	}

	public boolean updateDefectiveStatus(int itemId, int defectiveQty, String reason) throws SQLException {
		String sql = "UPDATE storage_items SET defective_quantity = ?, defect_reason = ? WHERE id = ? AND ? <= quantity";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, defectiveQty);
			pstmt.setString(2, reason);
			pstmt.setInt(3, itemId);
			pstmt.setInt(4, defectiveQty);
			return pstmt.executeUpdate() > 0;
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