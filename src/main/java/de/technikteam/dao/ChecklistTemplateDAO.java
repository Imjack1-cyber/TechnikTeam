package de.technikteam.dao;

import de.technikteam.model.ChecklistTemplate;
import de.technikteam.model.ChecklistTemplateItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Repository
public class ChecklistTemplateDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ChecklistTemplateDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public ChecklistTemplate save(ChecklistTemplate template) {
		if (template.getId() > 0) {
			updateTemplate(template);
		} else {
			int newId = createTemplate(template);
			template.setId(newId);
		}

		// Sync items
		jdbcTemplate.update("DELETE FROM preflight_checklist_items WHERE template_id = ?", template.getId());
		if (template.getItems() != null && !template.getItems().isEmpty()) {
			String itemSql = "INSERT INTO preflight_checklist_items (template_id, item_text, display_order) VALUES (?, ?, ?)";
			jdbcTemplate.batchUpdate(itemSql, template.getItems(), 100, (ps, item) -> {
				ps.setInt(1, template.getId());
				ps.setString(2, item.getItemText());
				ps.setInt(3, template.getItems().indexOf(item));
			});
		}
		return template;
	}

	private int createTemplate(ChecklistTemplate template) {
		String sql = "INSERT INTO preflight_checklist_templates (name, description) VALUES (?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, template.getName());
			ps.setString(2, template.getDescription());
			return ps;
		}, keyHolder);
		return Objects.requireNonNull(keyHolder.getKey()).intValue();
	}

	private void updateTemplate(ChecklistTemplate template) {
		String sql = "UPDATE preflight_checklist_templates SET name = ?, description = ? WHERE id = ?";
		jdbcTemplate.update(sql, template.getName(), template.getDescription(), template.getId());
	}

	public List<ChecklistTemplate> findAll() {
		String sql = "SELECT t.id as template_id, t.name, t.description, i.id as item_id, i.item_text, i.display_order "
				+ "FROM preflight_checklist_templates t "
				+ "LEFT JOIN preflight_checklist_items i ON t.id = i.template_id " + "ORDER BY t.name, i.display_order";

		Map<Integer, ChecklistTemplate> templateMap = new LinkedHashMap<>();
		jdbcTemplate.query(sql, (ResultSet rs) -> {
			int templateId = rs.getInt("template_id");
			ChecklistTemplate template = templateMap.computeIfAbsent(templateId, id -> {
				try {
					ChecklistTemplate newTemplate = new ChecklistTemplate();
					newTemplate.setId(id);
					newTemplate.setName(rs.getString("name"));
					newTemplate.setDescription(rs.getString("description"));
					newTemplate.setItems(new ArrayList<>());
					return newTemplate;
				} catch (SQLException e) {
					throw new RuntimeException("Failed to map template", e);
				}
			});

			if (rs.getObject("item_id") != null) {
				ChecklistTemplateItem item = new ChecklistTemplateItem();
				item.setId(rs.getInt("item_id"));
				item.setTemplateId(templateId);
				item.setItemText(rs.getString("item_text"));
				item.setDisplayOrder(rs.getInt("display_order"));
				template.getItems().add(item);
			}
		});
		return new ArrayList<>(templateMap.values());
	}

	public boolean delete(int id) {
		return jdbcTemplate.update("DELETE FROM preflight_checklist_templates WHERE id = ?", id) > 0;
	}
}