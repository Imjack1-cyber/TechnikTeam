package de.technikteam.dao;

import de.technikteam.model.TrainingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class TrainingRequestDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public TrainingRequestDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<TrainingRequest> rowMapper = (rs, rowNum) -> {
		TrainingRequest request = new TrainingRequest();
		request.setId(rs.getInt("id"));
		request.setTopic(rs.getString("topic"));
		request.setRequesterUserId(rs.getInt("requester_user_id"));
		request.setRequesterUsername(rs.getString("requester_username"));
		request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		if (rs.getMetaData().getColumnCount() > 5) { // Check if interest_count is present
			request.setInterestCount(rs.getInt("interest_count"));
		}
		return request;
	};

	public Optional<TrainingRequest> findById(int id) {
		String sql = "SELECT tr.*, u.username as requester_username, "
				+ "(SELECT COUNT(*) FROM training_request_interest WHERE request_id = tr.id) as interest_count "
				+ "FROM training_requests tr JOIN users u ON tr.requester_user_id = u.id WHERE tr.id = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public List<TrainingRequest> findAllWithInterestCount() {
		String sql = "SELECT tr.*, u.username as requester_username, "
				+ "(SELECT COUNT(*) FROM training_request_interest WHERE request_id = tr.id) as interest_count "
				+ "FROM training_requests tr JOIN users u ON tr.requester_user_id = u.id ORDER BY interest_count DESC, tr.created_at DESC";
		return jdbcTemplate.query(sql, rowMapper);
	}

	public TrainingRequest create(String topic, int requesterUserId) {
		String sql = "INSERT INTO training_requests (topic, requester_user_id) VALUES (?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, topic);
			ps.setInt(2, requesterUserId);
			return ps;
		}, keyHolder);
		int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
		return findById(newId).orElse(null);
	}

	public boolean delete(int id) {
		return jdbcTemplate.update("DELETE FROM training_requests WHERE id = ?", id) > 0;
	}

	public boolean addInterest(int requestId, int userId) {
		try {
			return jdbcTemplate.update("INSERT INTO training_request_interest (request_id, user_id) VALUES (?, ?)",
					requestId, userId) > 0;
		} catch (DuplicateKeyException e) {
			// User has already registered interest, which is not an error state.
			return true;
		}
	}
}