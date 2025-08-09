package de.technikteam.dao;

import de.technikteam.model.ChecklistItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChecklistDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ChecklistDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<ChecklistItem> checklistItemRowMapper = (rs, rowNum) -> {
		ChecklistItem item = new ChecklistItem();
		item.setId(rs.getInt("id"));
		item.setEventId(rs.getInt("event_id"));
		item.setItemId(rs.getInt("item_id"));
		item.setItemName(rs.getString("item_name"));
		item.setQuantity(rs.getInt("quantity"));
		item.setStatus(rs.getString("status"));
		item.setLastUpdatedByUserId(rs.getObject("last_updated_by_user_id", Integer.class));
		item.setLastUpdatedByUsername(rs.getString("last_updated_by_username"));
		item.setLastUpdatedAt(rs.getTimestamp("last_updated_at").toLocalDateTime());
		return item;
	};

	public List<ChecklistItem> getChecklistForEvent(int eventId) {
		String sql = "SELECT ci.*, si.name as item_name, u.username as last_updated_by_username "
				+ "FROM event_inventory_checklist ci " + "JOIN storage_items si ON ci.item_id = si.id "
				+ "LEFT JOIN users u ON ci.last_updated_by_user_id = u.id " + "WHERE ci.event_id = ? ORDER BY si.name";
		return jdbcTemplate.query(sql, checklistItemRowMapper, eventId);
	}

	public int generateChecklistFromReservations(int eventId) {
		String sql = "INSERT INTO event_inventory_checklist (event_id, item_id, quantity, status) "
				+ "SELECT event_id, item_id, reserved_quantity, 'PENDING' FROM event_storage_reservations "
				+ "WHERE event_id = ? " + "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";
		return jdbcTemplate.update(sql, eventId);
	}

	public boolean updateChecklistItemStatus(int checklistItemId, String status, int userId) {
		String sql = "UPDATE event_inventory_checklist SET status = ?, last_updated_by_user_id = ? WHERE id = ?";
		return jdbcTemplate.update(sql, status, userId, checklistItemId) > 0;
	}

	public ChecklistItem getChecklistItemById(int checklistItemId) {
		String sql = "SELECT ci.*, si.name as item_name, u.username as last_updated_by_username "
				+ "FROM event_inventory_checklist ci " + "JOIN storage_items si ON ci.item_id = si.id "
				+ "LEFT JOIN users u ON ci.last_updated_by_user_id = u.id " + "WHERE ci.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, checklistItemRowMapper, checklistItemId);
		} catch (Exception e) {
			return null;
		}
	}
}