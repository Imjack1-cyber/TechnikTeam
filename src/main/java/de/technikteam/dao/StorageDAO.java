package de.technikteam.dao;

import de.technikteam.model.StorageItem;
import de.technikteam.util.DaoUtils;
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
		return getAllItems().stream().collect(Collectors.groupingBy(item -> item.getLocation().trim()));
	}

	public List<StorageItem> getAllItems() {
		List<StorageItem> items = new ArrayList<>();
		String sql = "SELECT si.*, u.username as holder_username " + "FROM storage_items si "
				+ "LEFT JOIN users u ON si.current_holder_user_id = u.id " + "ORDER BY si.name";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				items.add(mapResultSetToStorageItem(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching all storage items.", e);
		}
		return items;
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
		item.setCompartment(rs.getString("compartment"));
		item.setQuantity(rs.getInt("quantity"));
		item.setMaxQuantity(rs.getInt("max_quantity"));
		item.setDefectiveQuantity(rs.getInt("defective_quantity"));
		item.setDefectReason(rs.getString("defect_reason"));
		item.setWeightKg(rs.getDouble("weight_kg"));
		item.setPriceEur(rs.getDouble("price_eur"));
		item.setImagePath(rs.getString("image_path"));
		item.setStatus(rs.getString("status"));
		item.setCurrentHolderUserId(rs.getInt("current_holder_user_id"));
		item.setAssignedEventId(rs.getInt("assigned_event_id"));
		if (DaoUtils.hasColumn(rs, "holder_username")) {
			item.setCurrentHolderUsername(rs.getString("holder_username"));
		}
		return item;
	}

	public StorageItem getItemById(int itemId) {
		String sql = "SELECT si.*, u.username as holder_username " + "FROM storage_items si "
				+ "LEFT JOIN users u ON si.current_holder_user_id = u.id " + "WHERE si.id = ?";
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
		String sql = "INSERT INTO storage_items (name, location, cabinet, compartment, quantity, max_quantity, weight_kg, price_eur, image_path, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'IN_STORAGE')";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, item.getName());
			pstmt.setString(2, item.getLocation());
			pstmt.setString(3, item.getCabinet());
			pstmt.setString(4, item.getCompartment());
			pstmt.setInt(5, item.getQuantity());
			pstmt.setInt(6, item.getMaxQuantity());
			pstmt.setDouble(7, item.getWeightKg());
			pstmt.setDouble(8, item.getPriceEur());
			pstmt.setString(9, item.getImagePath());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating storage item: {}", item.getName(), e);
			return false;
		}
	}

	public boolean updateItem(StorageItem item) {
		String sql = "UPDATE storage_items SET name=?, location=?, cabinet=?, compartment=?, quantity=?, max_quantity=?, defective_quantity=?, defect_reason=?, weight_kg=?, price_eur=?, image_path=?, status=?, current_holder_user_id=?, assigned_event_id=? WHERE id=?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, item.getName());
			pstmt.setString(2, item.getLocation());
			pstmt.setString(3, item.getCabinet());
			pstmt.setString(4, item.getCompartment());
			pstmt.setInt(5, item.getQuantity());
			pstmt.setInt(6, item.getMaxQuantity());
			pstmt.setInt(7, item.getDefectiveQuantity());
			pstmt.setString(8, item.getDefectReason());
			pstmt.setDouble(9, item.getWeightKg());
			pstmt.setDouble(10, item.getPriceEur());
			pstmt.setString(11, item.getImagePath());
			pstmt.setString(12, item.getStatus());
			if (item.getCurrentHolderUserId() > 0)
				pstmt.setInt(13, item.getCurrentHolderUserId());
			else
				pstmt.setNull(13, Types.INTEGER);
			if (item.getAssignedEventId() > 0)
				pstmt.setInt(14, item.getAssignedEventId());
			else
				pstmt.setNull(14, Types.INTEGER);
			pstmt.setInt(15, item.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating storage item with ID: {}", item.getId(), e);
			return false;
		}
	}

	public boolean performCheckout(int itemId, int quantity, int userId, Integer eventId) throws SQLException {
		String sql = "UPDATE storage_items "
				+ "SET quantity = quantity - ?, status = 'CHECKED_OUT', current_holder_user_id = ?, assigned_event_id = ? "
				+ "WHERE id = ? AND (quantity - defective_quantity) >= ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, quantity);
			pstmt.setInt(2, userId);
			if (eventId != null) {
				pstmt.setInt(3, eventId);
			} else {
				pstmt.setNull(3, Types.INTEGER);
			}
			pstmt.setInt(4, itemId);
			pstmt.setInt(5, quantity);
			return pstmt.executeUpdate() > 0;
		}
	}

	public boolean performCheckin(int itemId, int quantity) throws SQLException {
		String sql = "UPDATE storage_items "
				+ "SET quantity = quantity + ?, status = 'IN_STORAGE', current_holder_user_id = NULL, assigned_event_id = NULL "
				+ "WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, quantity);
			pstmt.setInt(2, itemId);
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

	public boolean repairItems(int itemId, int repairedQuantity) throws SQLException {
		String sql = "UPDATE storage_items "
				+ "SET defective_quantity = defective_quantity - ?, status = CASE WHEN (defective_quantity - ?) <= 0 THEN 'IN_STORAGE' ELSE status END "
				+ "WHERE id = ? AND defective_quantity >= ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, repairedQuantity);
			pstmt.setInt(2, repairedQuantity);
			pstmt.setInt(3, itemId);
			pstmt.setInt(4, repairedQuantity);
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

	public boolean updateItemStatus(int itemId, String status) {
		String sql = "UPDATE storage_items SET status = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setInt(2, itemId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating status for item ID: {}", itemId, e);
			return false;
		}
	}
}