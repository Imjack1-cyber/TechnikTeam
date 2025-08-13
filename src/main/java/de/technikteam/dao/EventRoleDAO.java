package de.technikteam.dao;

import de.technikteam.model.EventRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventRoleDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public EventRoleDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<EventRole> rowMapper = (rs, rowNum) -> {
		EventRole role = new EventRole();
		role.setId(rs.getInt("id"));
		role.setName(rs.getString("name"));
		role.setDescription(rs.getString("description"));
		role.setIconClass(rs.getString("icon_class"));
		role.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return role;
	};

	public List<EventRole> findAll() {
		String sql = "SELECT * FROM event_roles ORDER BY name ASC";
		return jdbcTemplate.query(sql, rowMapper);
	}

	public boolean create(EventRole role) {
		String sql = "INSERT INTO event_roles (name, description, icon_class) VALUES (?, ?, ?)";
		return jdbcTemplate.update(sql, role.getName(), role.getDescription(), role.getIconClass()) > 0;
	}

	public boolean update(EventRole role) {
		String sql = "UPDATE event_roles SET name = ?, description = ?, icon_class = ? WHERE id = ?";
		return jdbcTemplate.update(sql, role.getName(), role.getDescription(), role.getIconClass(), role.getId()) > 0;
	}

	public boolean delete(int id) {
		String sql = "DELETE FROM event_roles WHERE id = ?";
		return jdbcTemplate.update(sql, id) > 0;
	}
}