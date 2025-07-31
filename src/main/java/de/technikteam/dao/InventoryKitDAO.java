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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
			return null; // Expected case when not found
		} catch (Exception e) {
			logger.error("Error fetching kit by ID {}", kitId, e);
			return null;
		}
	}

	public boolean deleteKit(int kitId) {
		String sql = "DELETE FROM inventory_kits WHERE id = ?";
		try {
			// First, delete dependencies in the linking table
			jdbcTemplate.update("DELETE FROM inventory_kit_items WHERE kit_id = ?", kitId);
			// Then, delete the kit itself
			return jdbcTemplate.update(sql, kitId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting inventory kit ID {}", kitId, e);
			return false;
		}
	}

	public List<InventoryKit> getAllKitsWithItems() {
		Map<Integer, InventoryKit> kitMap = new LinkedHashMap<>();
		String sql = "SELECT k.id, k.name, k.description, k.location, ki.item_id, ki.quantity, si.name as item_name FROM inventory_kits k LEFT JOIN inventory_kit_items ki ON k.id = ki.kit_id LEFT JOIN storage_items si ON ki.item_id = si.id ORDER BY k.name, si.name";

		jdbcTemplate.query(sql, (ResultSet rs) -> {
			int kitId = rs.getInt("id");
			InventoryKit kit = kitMap.computeIfAbsent(kitId, id -> {
				try {
					return kitRowMapper.mapRow(rs, 0);
				} catch (SQLException e) {
					// This is a safe way to handle checked exceptions within a lambda
					throw new RuntimeException("Failed to map ResultSet to InventoryKit", e);
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
}