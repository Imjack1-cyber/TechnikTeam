package de.technikteam.dao;

import de.technikteam.model.InventoryKit;
import de.technikteam.model.InventoryKitItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing inventory kits and their contents.
 */
public class InventoryKitDAO {
	private static final Logger logger = LogManager.getLogger(InventoryKitDAO.class);

	public int createKit(InventoryKit kit) {
		String sql = "INSERT INTO inventory_kits (name, description) VALUES (?, ?)";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, kit.getName());
			pstmt.setString(2, kit.getDescription());
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						return rs.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error creating inventory kit '{}'", kit.getName(), e);
		}
		return 0;
	}

	public boolean updateKit(InventoryKit kit) {
		String sql = "UPDATE inventory_kits SET name = ?, description = ?, location = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, kit.getName());
			pstmt.setString(2, kit.getDescription());
			pstmt.setString(3, kit.getLocation());
			pstmt.setInt(4, kit.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating inventory kit ID {}", kit.getId(), e);
			return false;
		}
	}

	private InventoryKit mapResultSetToKit(ResultSet rs) throws SQLException {
		InventoryKit kit = new InventoryKit();
		kit.setId(rs.getInt("id"));
		kit.setName(rs.getString("name"));
		kit.setDescription(rs.getString("description"));
		kit.setLocation(rs.getString("location")); // CHANGED
		return kit;
	}

	public InventoryKit getKitById(int kitId) {
		String sql = "SELECT * FROM inventory_kits WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, kitId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToKit(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching kit by ID {}", kitId, e);
		}
		return null;
	}

	public boolean deleteKit(int kitId) {
		String sql = "DELETE FROM inventory_kits WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, kitId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting inventory kit ID {}", kitId, e);
			return false;
		}
	}

	public List<InventoryKit> getAllKits() {
		List<InventoryKit> kits = new ArrayList<>();
		String sql = "SELECT * FROM inventory_kits ORDER BY name";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				kits.add(mapResultSetToKit(rs));
			}
		} catch (SQLException e) {
			logger.error("Error fetching all inventory kits", e);
		}
		return kits;
	}

	public List<InventoryKitItem> getItemsForKit(int kitId) {
		List<InventoryKitItem> items = new ArrayList<>();
		String sql = "SELECT iki.*, si.name as item_name FROM inventory_kit_items iki "
				+ "JOIN storage_items si ON iki.item_id = si.id " + "WHERE iki.kit_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, kitId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					InventoryKitItem item = new InventoryKitItem();
					item.setKitId(rs.getInt("kit_id"));
					item.setItemId(rs.getInt("item_id"));
					item.setQuantity(rs.getInt("quantity"));
					item.setItemName(rs.getString("item_name"));
					items.add(item);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching items for kit ID {}", kitId, e);
		}
		return items;
	}

	public boolean updateKitItems(int kitId, String[] itemIds, String[] quantities) {
		String deleteSql = "DELETE FROM inventory_kit_items WHERE kit_id = ?";
		String insertSql = "INSERT INTO inventory_kit_items (kit_id, item_id, quantity) VALUES (?, ?, ?)";
		Connection conn = null;

		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false); // Start transaction

			try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
				deleteStmt.setInt(1, kitId);
				deleteStmt.executeUpdate();
			}

			if (itemIds != null && quantities != null && itemIds.length == quantities.length) {
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
					for (int i = 0; i < itemIds.length; i++) {
						if (itemIds[i] == null || itemIds[i].isEmpty()) {
							continue;
						}
						int itemId = Integer.parseInt(itemIds[i]);
						int quantity = Integer.parseInt(quantities[i]);
						if (quantity > 0) {
							insertStmt.setInt(1, kitId);
							insertStmt.setInt(2, itemId);
							insertStmt.setInt(3, quantity);
							insertStmt.addBatch();
						}
					}
					insertStmt.executeBatch();
				}
			}

			conn.commit(); // Commit transaction
			logger.info("Successfully updated items for kit ID: {}", kitId);
			return true;
		} catch (SQLException | NumberFormatException e) {
			logger.error("Error during transaction for updating kit items for kit ID {}. Rolling back.", kitId, e);
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					logger.error("Failed to rollback transaction.", ex);
				}
			}
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException ex) {
					logger.error("Failed to close connection after kit item update.", ex);
				}
			}
		}
	}
}