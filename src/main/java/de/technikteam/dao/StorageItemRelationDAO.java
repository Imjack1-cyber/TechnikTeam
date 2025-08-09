package de.technikteam.dao;

import de.technikteam.model.StorageItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StorageItemRelationDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public StorageItemRelationDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<StorageItem> itemMapper = (rs, rowNum) -> {
		StorageItem item = new StorageItem();
		item.setId(rs.getInt("id"));
		item.setName(rs.getString("name"));
		item.setLocation(rs.getString("location"));
		item.setQuantity(rs.getInt("quantity"));
		item.setDefectiveQuantity(rs.getInt("defective_quantity"));
		item.setMaxQuantity(rs.getInt("max_quantity"));
		return item;
	};

	public List<StorageItem> findRelatedItems(int itemId) {
		String sql = "SELECT si.* FROM storage_items si JOIN storage_item_relations sir ON si.id = sir.related_item_id WHERE sir.item_id = ?";
		return jdbcTemplate.query(sql, itemMapper, itemId);
	}

	public void updateRelations(int itemId, List<Integer> relatedItemIds) {
		jdbcTemplate.update("DELETE FROM storage_item_relations WHERE item_id = ?", itemId);

		if (relatedItemIds != null && !relatedItemIds.isEmpty()) {
			String sql = "INSERT INTO storage_item_relations (item_id, related_item_id) VALUES (?, ?)";
			jdbcTemplate.batchUpdate(sql, relatedItemIds, 100, (ps, relatedId) -> {
				ps.setInt(1, itemId);
				ps.setInt(2, relatedId);
			});
		}
	}
}