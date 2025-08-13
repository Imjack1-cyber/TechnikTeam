package de.technikteam.dao;

import de.technikteam.model.EventDebriefing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventDebriefingDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public EventDebriefingDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<EventDebriefing> debriefingRowMapper = (rs, rowNum) -> {
		EventDebriefing d = new EventDebriefing();
		d.setId(rs.getInt("id"));
		d.setEventId(rs.getInt("event_id"));
		d.setAuthorUserId(rs.getInt("author_user_id"));
		d.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
		d.setWhatWentWell(rs.getString("what_went_well"));
		d.setWhatToImprove(rs.getString("what_to_improve"));
		d.setEquipmentNotes(rs.getString("equipment_notes"));
		d.setStandoutCrewMembers(rs.getString("standout_crew_members"));
		d.setEventName(rs.getString("event_name"));
		d.setAuthorUsername(rs.getString("author_username"));
		return d;
	};

	public Optional<EventDebriefing> findByEventId(int eventId) {
		String sql = "SELECT ed.*, e.name as event_name, u.username as author_username " + "FROM event_debriefings ed "
				+ "JOIN events e ON ed.event_id = e.id " + "JOIN users u ON ed.author_user_id = u.id "
				+ "WHERE ed.event_id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, debriefingRowMapper, eventId));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public List<EventDebriefing> findAll() {
		String sql = "SELECT ed.*, e.name as event_name, u.username as author_username " + "FROM event_debriefings ed "
				+ "JOIN events e ON ed.event_id = e.id " + "JOIN users u ON ed.author_user_id = u.id "
				+ "ORDER BY ed.submitted_at DESC";
		return jdbcTemplate.query(sql, debriefingRowMapper);
	}

	public EventDebriefing save(EventDebriefing debriefing) {
		String sql = "INSERT INTO event_debriefings (event_id, author_user_id, what_went_well, what_to_improve, equipment_notes, standout_crew_members) "
				+ "VALUES (?, ?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE "
				+ "author_user_id = VALUES(author_user_id), " + "submitted_at = CURRENT_TIMESTAMP, "
				+ "what_went_well = VALUES(what_went_well), " + "what_to_improve = VALUES(what_to_improve), "
				+ "equipment_notes = VALUES(equipment_notes), "
				+ "standout_crew_members = VALUES(standout_crew_members)";

		jdbcTemplate.update(sql, debriefing.getEventId(), debriefing.getAuthorUserId(), debriefing.getWhatWentWell(),
				debriefing.getWhatToImprove(), debriefing.getEquipmentNotes(), debriefing.getStandoutCrewMembers());
		return findByEventId(debriefing.getEventId()).orElse(null);
	}
}