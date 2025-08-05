package de.technikteam.dao;

import de.technikteam.model.Venue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class VenueDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public VenueDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Venue> venueRowMapper = (rs, rowNum) -> {
		Venue venue = new Venue();
		venue.setId(rs.getInt("id"));
		venue.setName(rs.getString("name"));
		venue.setAddress(rs.getString("address"));
		venue.setNotes(rs.getString("notes"));
		venue.setMapImagePath(rs.getString("map_image_path"));
		venue.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return venue;
	};

	public List<Venue> findAll() {
		String sql = "SELECT * FROM venues ORDER BY name ASC";
		return jdbcTemplate.query(sql, venueRowMapper);
	}

	public Optional<Venue> findById(int id) {
		String sql = "SELECT * FROM venues WHERE id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, venueRowMapper, id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Venue create(Venue venue) {
		String sql = "INSERT INTO venues (name, address, notes, map_image_path) VALUES (?, ?, ?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, venue.getName());
			ps.setString(2, venue.getAddress());
			ps.setString(3, venue.getNotes());
			ps.setString(4, venue.getMapImagePath());
			return ps;
		}, keyHolder);
		int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
		venue.setId(newId);
		return venue;
	}

	public boolean update(Venue venue) {
		String sql = "UPDATE venues SET name = ?, address = ?, notes = ?, map_image_path = ? WHERE id = ?";
		return jdbcTemplate.update(sql, venue.getName(), venue.getAddress(), venue.getNotes(), venue.getMapImagePath(),
				venue.getId()) > 0;
	}

	public boolean delete(int id) {
		String sql = "DELETE FROM venues WHERE id = ?";
		return jdbcTemplate.update(sql, id) > 0;
	}
}