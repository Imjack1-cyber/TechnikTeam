package de.technikteam.dao;

import de.technikteam.model.AvailabilityDayResponse;
import de.technikteam.model.AvailabilityPoll;
import de.technikteam.model.AvailabilityResponse;
import de.technikteam.model.SchedulingSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;

@Repository
public class AvailabilityDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AvailabilityDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AvailabilityPoll> pollRowMapper = (rs, rowNum) -> {
        AvailabilityPoll poll = new AvailabilityPoll();
        poll.setId(rs.getInt("id"));
        poll.setUuid(rs.getString("uuid"));
        poll.setType(rs.getString("type"));
        poll.setTitle(rs.getString("title"));
        poll.setDescription(rs.getString("description"));
        poll.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        poll.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        poll.setOptions(rs.getString("options"));
        poll.setVerificationCode(rs.getString("verification_code"));
        poll.setCreatedByUserId(rs.getInt("created_by_user_id"));
        poll.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        poll.setCreatedByUsername(rs.getString("created_by_username"));
        return poll;
    };

    private final RowMapper<AvailabilityResponse> responseRowMapper = (rs, rowNum) -> {
        AvailabilityResponse response = new AvailabilityResponse();
        response.setId(rs.getInt("id"));
        response.setPollId(rs.getInt("poll_id"));
        response.setUserId(rs.getObject("user_id", Integer.class));
        response.setGuestName(rs.getString("guest_name"));
        response.setStatus(rs.getString("status"));
        response.setNotes(rs.getString("notes"));
        response.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        response.setUsername(rs.getString("username"));
        return response;
    };

    private final RowMapper<AvailabilityDayResponse> dayResponseRowMapper = (rs, rowNum) -> {
        AvailabilityDayResponse vote = new AvailabilityDayResponse();
        vote.setId(rs.getInt("id"));
        vote.setResponseId(rs.getInt("response_id"));
        vote.setVoteDate(rs.getDate("vote_date").toLocalDate());
        vote.setStatus(rs.getString("status"));
        vote.setNotes(rs.getString("notes"));
        return vote;
    };


    public List<AvailabilityPoll> findAll() {
        String sql = "SELECT p.*, u.username as created_by_username FROM availability_polls p " +
                     "JOIN users u ON p.created_by_user_id = u.id ORDER BY p.start_time DESC";
        return jdbcTemplate.query(sql, pollRowMapper);
    }

    public Optional<AvailabilityPoll> findById(int id) {
        String sql = "SELECT p.*, u.username as created_by_username FROM availability_polls p " +
                     "JOIN users u ON p.created_by_user_id = u.id WHERE p.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, pollRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<AvailabilityPoll> findByUuid(String uuid) {
        String sql = "SELECT p.*, u.username as created_by_username FROM availability_polls p " +
                "JOIN users u ON p.created_by_user_id = u.id WHERE p.uuid = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, pollRowMapper, uuid));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public AvailabilityPoll create(AvailabilityPoll poll) {
        String sql = "INSERT INTO availability_polls (uuid, type, title, description, start_time, end_time, options, verification_code, created_by_user_id) VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, poll.getType());
            ps.setString(2, poll.getTitle());
            ps.setString(3, poll.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(poll.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(poll.getEndTime()));
            ps.setString(6, poll.getOptions());
            ps.setString(7, poll.getVerificationCode());
            ps.setInt(8, poll.getCreatedByUserId());
            return ps;
        }, keyHolder);
        poll.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return findById(poll.getId()).orElse(poll); // Re-fetch to get UUID and other defaults
    }

    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM availability_polls WHERE id = ?", id) > 0;
    }

    public List<AvailabilityResponse> findResponsesByPollId(int pollId) {
        String sql = "SELECT ar.*, u.username FROM availability_responses ar " +
                     "LEFT JOIN users u ON ar.user_id = u.id WHERE ar.poll_id = ?";
        return jdbcTemplate.query(sql, responseRowMapper, pollId);
    }

    public Optional<AvailabilityResponse> findResponse(int pollId, Integer userId, String guestName) {
        String sql;
        Object[] args;
        if (userId != null) {
            sql = "SELECT ar.*, u.username FROM availability_responses ar LEFT JOIN users u ON ar.user_id = u.id WHERE ar.poll_id = ? AND ar.user_id = ?";
            args = new Object[]{pollId, userId};
        } else {
            sql = "SELECT ar.*, NULL as username FROM availability_responses ar WHERE ar.poll_id = ? AND ar.guest_name = ?";
            args = new Object[]{pollId, guestName};
        }
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, responseRowMapper, args));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public AvailabilityResponse saveResponse(AvailabilityResponse response) {
        Optional<AvailabilityResponse> existingResponse = findResponse(response.getPollId(), response.getUserId(), response.getGuestName());

        if (existingResponse.isPresent()) {
            response.setId(existingResponse.get().getId());
            String updateSql = "UPDATE availability_responses SET status = ?, notes = ?, updated_at = NOW() WHERE id = ?";
            jdbcTemplate.update(updateSql, response.getStatus(), response.getNotes(), response.getId());
        } else {
            String insertSql = "INSERT INTO availability_responses (poll_id, user_id, guest_name, status, notes) VALUES (?, ?, ?, ?, ?)";
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, response.getPollId());
                if (response.getUserId() != null) ps.setInt(2, response.getUserId()); else ps.setNull(2, Types.INTEGER);
                ps.setString(3, response.getGuestName());
                ps.setString(4, response.getStatus());
                ps.setString(5, response.getNotes());
                return ps;
            }, keyHolder);
            response.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        }
        return response;
    }

    public void saveDayResponses(int responseId, List<AvailabilityDayResponse> votes) {
        jdbcTemplate.update("DELETE FROM availability_day_responses WHERE response_id = ?", responseId);
        if (votes != null && !votes.isEmpty()) {
            String sql = "INSERT INTO availability_day_responses (response_id, vote_date, status, notes) VALUES (?, ?, ?, ?)";
            jdbcTemplate.batchUpdate(sql, votes, 100, (ps, vote) -> {
                ps.setInt(1, responseId);
                ps.setDate(2, java.sql.Date.valueOf(vote.getVoteDate()));
                ps.setString(3, vote.getStatus());
                ps.setString(4, vote.getNotes());
            });
        }
    }

    public List<AvailabilityDayResponse> findDayResponsesByPollId(int pollId) {
        String sql = "SELECT adr.* FROM availability_day_responses adr " +
                     "JOIN availability_responses ar ON adr.response_id = ar.id " +
                     "WHERE ar.poll_id = ?";
        return jdbcTemplate.query(sql, dayResponseRowMapper, pollId);
    }
}