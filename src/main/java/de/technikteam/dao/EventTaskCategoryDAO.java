package de.technikteam.dao;

import de.technikteam.model.EventTaskCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class EventTaskCategoryDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EventTaskCategoryDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<EventTaskCategory> rowMapper = (rs, rowNum) -> {
        EventTaskCategory category = new EventTaskCategory();
        category.setId(rs.getInt("id"));
        category.setEventId(rs.getInt("event_id"));
        category.setName(rs.getString("name"));
        category.setDisplayOrder(rs.getInt("display_order"));
        return category;
    };

    public List<EventTaskCategory> findByEventId(int eventId) {
        String sql = "SELECT * FROM event_task_categories WHERE event_id = ? ORDER BY display_order ASC";
        return jdbcTemplate.query(sql, rowMapper, eventId);
    }

    public EventTaskCategory create(EventTaskCategory category) {
        String sql = "INSERT INTO event_task_categories (event_id, name, display_order) " +
                     "SELECT ?, ?, COALESCE(MAX(display_order), -1) + 1 FROM event_task_categories WHERE event_id = ?";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, category.getEventId());
            ps.setString(2, category.getName());
            ps.setInt(3, category.getEventId());
            return ps;
        }, keyHolder);
        int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        category.setId(newId);
        return category;
    }

    public boolean update(EventTaskCategory category) {
        String sql = "UPDATE event_task_categories SET name = ? WHERE id = ? AND event_id = ?";
        return jdbcTemplate.update(sql, category.getName(), category.getId(), category.getEventId()) > 0;
    }

    public void updateOrder(int eventId, List<Integer> categoryIds) {
        String sql = "UPDATE event_task_categories SET display_order = ? WHERE id = ? AND event_id = ?";
        jdbcTemplate.batchUpdate(sql, categoryIds, 100, (ps, categoryId) -> {
            ps.setInt(1, categoryIds.indexOf(categoryId));
            ps.setInt(2, categoryId);
            ps.setInt(3, eventId);
        });
    }

    public boolean delete(int categoryId) {
        String sql = "DELETE FROM event_task_categories WHERE id = ?";
        return jdbcTemplate.update(sql, categoryId) > 0;
    }
}