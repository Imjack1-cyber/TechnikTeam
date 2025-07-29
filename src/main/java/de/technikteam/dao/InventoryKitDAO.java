package de.technikteam.dao;

import de.technikteam.model.InventoryKit;
import de.technikteam.model.InventoryKitItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class InventoryKitDAO {
	private static final Logger logger = LogManager.getLogger(InventoryKitDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public InventoryKitDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<InventoryKit> kitRowMapper = (rs, rowNum) -> {
		InventoryKit kit = new InventoryKit();
		kit.setId(rs.getInt("id"));
		kit.setName(rs.getString("name"));
		kit.setDescription(rs.getString("description"));
		kit.setLocation(rs.getString("location"));
		kit.setItems(new ArrayList<>());
		return kit;
	};

	public int createKit(InventoryKit kit) {
		String sql = "INSERT INTO inventory_kits (name, description, location) VALUES (?, ?, ?)";
		try {
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, kit.getName());
				ps.setString(2, kit.getDescription());
				ps.setString(3, kit.getLocation());
				return ps;
			}, keyHolder);
			return Objects.requireNonNull(keyHolder.getKey()).intValue();
		} catch (Exception e) {
			logger.error("Error creating inventory kit '{}'", kit.getName(), e);
			return 0;
		}
	}

	public boolean updateKit(InventoryKit kit) {
		String sql = "UPDATE inventory_kits SET name = ?, description = ?, location = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, kit.getName(), kit.getDescription(), kit.getLocation(), kit.getId()) > 0;
		} catch (Exception e) {
			logger.error("Error updating inventory kit ID {}", kit.getId(), e);
			return false;
		}
	}

	public InventoryKit getKitById(int kitId) {
		String sql = "SELECT * FROM inventory_kits WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, kitRowMapper, kitId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching kit by ID {}", kitId, e);
			return null;
		}
	}

	public boolean deleteKit(int kitId) {
		String sql = "DELETE FROM inventory_kits WHERE id = ?";
		try {
			jdbcTemplate.update("DELETE FROM inventory_kit_items WHERE kit_id = ?", kitId);
			return jdbcTemplate.update(sql, kitId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting inventory kit ID {}", kitId, e);
			return false;
		}
	}

	public List<InventoryKit> getAllKitsWithItems() {
		Map<Integer, InventoryKit> kitMap = new LinkedHashMap<>();
		String sql = "SELECT k.id, k.name, k.description, k.location, ki.item_id, ki.quantity, si.name as item_name FROM inventory_kits k LEFT JOIN inventory_kit_items ki ON k.id = ki.kit_id LEFT JOIN storage_items si ON ki.item_id = si.id ORDER BY k.name, si.name";

		jdbcTemplate.query(sql, rs -> {
			int kitId = rs.getInt("id");
			InventoryKit kit = kitMap.computeIfAbsent(kitId, id -> {
				try {
					return kitRowMapper.mapRow(rs, 0);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			});
			if (rs.getInt("item_id") > 0) {
				InventoryKitItem item = new InventoryKitItem();
				item.setKitId(kitId);
				item.setItemId(rs.getInt("item_id"));
				item.setQuantity(rs.getInt("quantity"));
				item.setItemName(rs.getString("item_name"));
				kit.getItems().add(item);
			}
		});
		return new ArrayList<>(kitMap.values());
	}

	public List<InventoryKitItem> getItemsForKit(int kitId) {
		String sql = "SELECT iki.*, si.name as item_name FROM inventory_kit_items iki JOIN storage_items si ON iki.item_id = si.id WHERE iki.kit_id = ?";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				InventoryKitItem item = new InventoryKitItem();
				item.setKitId(rs.getInt("kit_id"));
				item.setItemId(rs.getInt("item_id"));
				item.setQuantity(rs.getInt("quantity"));
				item.setItemName(rs.getString("item_name"));
				return item;
			}, kitId);
		} catch (Exception e) {
			logger.error("Error fetching items for kit ID {}", kitId, e);
			return List.of();
		}
	}

	public boolean updateKitItems(int kitId, String[] itemIds, String[] quantities) {
		try {
			jdbcTemplate.update("DELETE FROM inventory_kit_items WHERE kit_id = ?", kitId);
			if (itemIds != null && quantities != null && itemIds.length == quantities.length) {
				String insertSql = "INSERT INTO inventory_kit_items (kit_id, item_id, quantity) VALUES (?, ?, ?)";
				jdbcTemplate.batchUpdate(insertSql, List.of(itemIds), 100, (ps, itemIdStr) -> {
					if (itemIdStr != null && !itemIdStr.isEmpty()) {
						int index = List.of(itemIds).indexOf(itemIdStr);
						int quantity = Integer.parseInt(quantities[index]);
						if (quantity > 0) {
							ps.setInt(1, kitId);
							ps.setInt(2, Integer.parseInt(itemIdStr));
							ps.setInt(3, quantity);
						}
					}
				});
			}
			return true;
		} catch (Exception e) {
			logger.error("Error during transaction for updating kit items for kit ID {}", kitId, e);
			return false;
		}
	}

	public List<InventoryKit> getAllKits() {
		String sql = "SELECT * FROM inventory_kits ORDER BY name";
		try {
			return jdbcTemplate.query(sql, kitRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching all inventory kits", e);
			return List.of();
		}
	}
}