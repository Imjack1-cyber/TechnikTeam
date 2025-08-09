package de.technikteam.dao;

import de.technikteam.model.StorageItem;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class StorageDAO {
	private static final Logger logger = LogManager.getLogger(StorageDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public StorageDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<StorageItem> storageItemRowMapper = (rs, rowNum) -> {
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
		item.setCategory(rs.getString("category"));
		item.setStatus(rs.getString("status"));
		item.setCurrentHolderUserId(rs.getInt("current_holder_user_id"));
		item.setAssignedEventId(rs.getInt("assigned_event_id"));
		if (DaoUtils.hasColumn(rs, "holder_username")) {
			item.setCurrentHolderUsername(rs.getString("holder_username"));
		}
		return item;
	};

	public Map<String, List<StorageItem>> getAllItemsGroupedByLocation() {
		return getAllItems().stream()
				.collect(Collectors.groupingBy(
						item -> item.getLocation() != null && !item.getLocation().isBlank() ? item.getLocation().trim()
								: "Unbekannt"));
	}

	public List<StorageItem> getAllItems() {
		String sql = "SELECT si.*, u.username as holder_username FROM storage_items si LEFT JOIN users u ON si.current_holder_user_id = u.id ORDER BY si.location, si.name";
		try {
			return jdbcTemplate.query(sql, storageItemRowMapper);
		} catch (Exception e) {
			logger.error("Error while fetching all storage items.", e);
			return List.of();
		}
	}

	public List<StorageItem> getDefectiveItems() {
		String sql = "SELECT * FROM storage_items WHERE defective_quantity > 0 ORDER BY location, name";
		try {
			return jdbcTemplate.query(sql, storageItemRowMapper);
		} catch (Exception e) {
			logger.error("Error while fetching defective items.", e);
			return List.of();
		}
	}

	public StorageItem getItemById(int itemId) {
		String sql = "SELECT si.*, u.username as holder_username FROM storage_items si LEFT JOIN users u ON si.current_holder_user_id = u.id WHERE si.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, storageItemRowMapper, itemId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching storage item by ID: {}", itemId, e);
			return null;
		}
	}

	public boolean createItem(StorageItem item) {
		String sql = "INSERT INTO storage_items (name, location, cabinet, compartment, quantity, max_quantity, weight_kg, price_eur, image_path, category, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'IN_STORAGE')";
		try {
			return jdbcTemplate.update(sql, item.getName(), item.getLocation(), item.getCabinet(),
					item.getCompartment(), item.getQuantity(), item.getMaxQuantity(), item.getWeightKg(),
					item.getPriceEur(), item.getImagePath(), item.getCategory()) > 0;
		} catch (Exception e) {
			logger.error("Error creating storage item: {}", item.getName(), e);
			return false;
		}
	}

	public boolean updateItem(StorageItem item) {
		String sql = "UPDATE storage_items SET name=?, location=?, cabinet=?, compartment=?, quantity=?, max_quantity=?, defective_quantity=?, defect_reason=?, weight_kg=?, price_eur=?, image_path=?, category=?, status=?, current_holder_user_id=?, assigned_event_id=? WHERE id=?";
		try {
			Object holderId = item.getCurrentHolderUserId() > 0 ? item.getCurrentHolderUserId() : null;
			Object eventId = item.getAssignedEventId() > 0 ? item.getAssignedEventId() : null;
			return jdbcTemplate.update(sql, item.getName(), item.getLocation(), item.getCabinet(),
					item.getCompartment(), item.getQuantity(), item.getMaxQuantity(), item.getDefectiveQuantity(),
					item.getDefectReason(), item.getWeightKg(), item.getPriceEur(), item.getImagePath(),
					item.getCategory(), item.getStatus(), holderId, eventId, item.getId()) > 0;
		} catch (Exception e) {
			logger.error("Error updating storage item with ID: {}", item.getId(), e);
			return false;
		}
	}

	public boolean deleteItem(int itemId) {
		String sql = "DELETE FROM storage_items WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, itemId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting storage item with ID: {}", itemId, e);
			return false;
		}
	}

	public List<StorageItem> getLowStockItems(int limit) {
		String sql = "SELECT * FROM storage_items WHERE (quantity - defective_quantity) < (max_quantity * 0.25) AND max_quantity > 0 ORDER BY (quantity - defective_quantity) / max_quantity ASC LIMIT ?";
		try {
			return jdbcTemplate.query(sql, storageItemRowMapper, limit);
		} catch (Exception e) {
			logger.error("Error while fetching low stock items.", e);
			return List.of();
		}
	}

	public List<Map<String, Object>> getFutureReservationsForItem(int itemId) {
		String sql = "SELECT e.name as event_name, e.event_datetime, e.end_datetime "
				+ "FROM event_storage_reservations esr " + "JOIN events e ON esr.event_id = e.id "
				+ "WHERE esr.item_id = ? AND e.status IN ('GEPLANT', 'LAUFEND') AND e.event_datetime >= NOW() "
				+ "ORDER BY e.event_datetime ASC";
		try {
			return jdbcTemplate.queryForList(sql, itemId);
		} catch (Exception e) {
			logger.error("Error fetching future reservations for item {}", itemId, e);
			return List.of();
		}
	}

	public List<StorageItem> search(String query) {
		String sql = "SELECT * FROM storage_items WHERE name LIKE ? OR location LIKE ? OR cabinet LIKE ? OR compartment LIKE ? ORDER BY name ASC LIMIT 20";
		String searchTerm = "%" + query + "%";
		try {
			return jdbcTemplate.query(sql, storageItemRowMapper, searchTerm, searchTerm, searchTerm, searchTerm);
		} catch (Exception e) {
			logger.error("Error searching storage items for query '{}'", query, e);
			return List.of();
		}
	}
}