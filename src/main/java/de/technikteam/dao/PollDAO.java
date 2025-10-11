package de.technikteam.dao;

import de.technikteam.model.Poll;
import de.technikteam.model.PollDayVote;
import de.technikteam.model.PollOption;
import de.technikteam.model.PollVote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

@Repository
public class PollDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PollDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Poll> pollRowMapper = (rs, rowNum) -> {
        Poll poll = new Poll();
        poll.setId(rs.getInt("id"));
        poll.setUuid(rs.getString("uuid"));
        poll.setType(rs.getString("type"));
        poll.setQuestion(rs.getString("question"));
        poll.setDescription(rs.getString("description"));
        if (rs.getTimestamp("start_time") != null) {
            poll.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        }
        if (rs.getTimestamp("end_time") != null) {
            poll.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        }
        poll.setCreatedByUserId(rs.getInt("created_by_user_id"));
        poll.setCreatedByUsername(rs.getString("username"));
        if (rs.getTimestamp("closes_at") != null) {
            poll.setClosesAt(rs.getTimestamp("closes_at").toLocalDateTime());
        }
        poll.setClosed(rs.getBoolean("is_closed"));
        poll.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        poll.setOptions(rs.getString("options"));
        poll.setVerificationCode(rs.getString("verification_code"));
        return poll;
    };
    
    private final RowMapper<PollDayVote> dayVoteRowMapper = (rs, rowNum) -> {
        PollDayVote vote = new PollDayVote();
        vote.setId(rs.getInt("id"));
        vote.setVoteId(rs.getLong("vote_id"));
        vote.setVoteDate(rs.getDate("vote_date").toLocalDate());
        vote.setStatus(rs.getString("status"));
        vote.setNotes(rs.getString("notes"));
        return vote;
    };
    
    private final RowMapper<PollVote> voteRowMapper = (rs, rowNum) -> {
        PollVote vote = new PollVote();
        vote.setId(rs.getLong("id"));
        vote.setPollId(rs.getInt("poll_id"));
        vote.setUserId(rs.getObject("user_id", Integer.class));
        vote.setGuestName(rs.getString("guest_name"));
        vote.setPollOptionId(rs.getObject("poll_option_id", Integer.class));
        vote.setNotes(rs.getString("notes"));
        vote.setVotedAt(rs.getTimestamp("voted_at").toLocalDateTime());
        return vote;
    };


    public List<Poll> findAll(int userId) {
        String sql = "SELECT p.*, u.username, (SELECT COUNT(*) FROM poll_votes pv WHERE pv.poll_id = p.id AND pv.user_id = ?) > 0 as has_voted FROM polls p JOIN users u ON p.created_by_user_id = u.id ORDER BY p.created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Poll poll = pollRowMapper.mapRow(rs, rowNum);
            poll.setHasVoted(rs.getBoolean("has_voted"));
            return poll;
        }, userId);
    }

    public Optional<Poll> findById(int pollId, int userId) {
        String sql = "SELECT p.*, u.username, (SELECT COUNT(*) FROM poll_votes pv WHERE pv.poll_id = p.id AND pv.user_id = ?) > 0 as has_voted FROM polls p JOIN users u ON p.created_by_user_id = u.id WHERE p.id = ?";
        try {
            Poll poll = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Poll p = pollRowMapper.mapRow(rs, rowNum);
                p.setHasVoted(rs.getBoolean("has_voted"));
                return p;
            }, userId, pollId);

            if (poll != null) {
                enrichPoll(poll, userId, null);
            }
            return Optional.ofNullable(poll);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Poll> findByUuid(String uuid, Integer userId, String guestName) {
        String sql = "SELECT p.*, u.username FROM polls p JOIN users u ON p.created_by_user_id = u.id WHERE p.uuid = ?";
        try {
            Poll poll = jdbcTemplate.queryForObject(sql, new Object[] { uuid }, new int[] { Types.VARCHAR },
                    pollRowMapper);
            if (poll != null) {
                enrichPoll(poll, userId, guestName);
            }
            return Optional.ofNullable(poll);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    private void enrichPoll(Poll poll, Integer userId, String guestName) {
        if (poll == null) return;
        poll.setHasVoted(hasVoted(poll.getId(), userId, guestName));
        if ("WORD_CLOUD".equals(poll.getType())) {
            poll.setWordCloudResults(getWordCloudResults(poll.getId()));
        } else {
            poll.setPollOptions(findOptionsByPollId(poll.getId()));
        }
    }


    public List<PollOption> findOptionsByPollId(int pollId) {
        String sql = "SELECT po.*, COUNT(pv.poll_option_id) as vote_count FROM poll_options po LEFT JOIN poll_votes pv ON po.id = pv.poll_option_id WHERE po.poll_id = ? GROUP BY po.id ORDER BY po.id";

        List<PollOption> options = jdbcTemplate.query(sql, (rs, rowNum) -> {
            PollOption option = new PollOption();
            option.setId(rs.getInt("id"));
            option.setPollId(rs.getInt("poll_id"));
            option.setOptionText(rs.getString("option_text"));
            option.setVoteCount(rs.getInt("vote_count"));
            return option;
        }, pollId);

        int totalVotes = options.stream().mapToInt(PollOption::getVoteCount).sum();
        if (totalVotes > 0) {
            options.forEach(opt -> opt.setVotePercentage(((double) opt.getVoteCount() / totalVotes) * 100));
        }
        return options;
    }

    public List<Map<String, Object>> getWordCloudResults(int pollId) {
        String sql = "SELECT word as text, COUNT(*) as value FROM poll_word_cloud_entries WHERE poll_id = ? GROUP BY word ORDER BY value DESC";
        return jdbcTemplate.queryForList(sql, pollId);
    }

    @Transactional
    public Poll create(Poll poll, List<String> optionTexts) {
        String sql = "INSERT INTO polls (uuid, question, description, type, start_time, end_time, created_by_user_id, closes_at, is_closed, options, verification_code) VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, poll.getQuestion());
            ps.setString(2, poll.getDescription());
            ps.setString(3, poll.getType());
            ps.setObject(4, poll.getStartTime() != null ? Timestamp.valueOf(poll.getStartTime()) : null, Types.TIMESTAMP);
            ps.setObject(5, poll.getEndTime() != null ? Timestamp.valueOf(poll.getEndTime()) : null, Types.TIMESTAMP);
            ps.setInt(6, poll.getCreatedByUserId());
            ps.setObject(7, poll.getClosesAt() != null ? Timestamp.valueOf(poll.getClosesAt()) : null, Types.TIMESTAMP);
            ps.setBoolean(8, poll.isClosed());
            ps.setString(9, poll.getOptions());
            ps.setString(10, poll.getVerificationCode());
            return ps;
        }, keyHolder);

        int newPollId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        poll.setId(newPollId);

        if (optionTexts != null && !optionTexts.isEmpty()) {
            String optionSql = "INSERT INTO poll_options (poll_id, option_text) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(optionSql, optionTexts, 100, (ps, optionText) -> {
                ps.setInt(1, newPollId);
                ps.setString(2, optionText);
            });
        }

        return findById(newPollId, poll.getCreatedByUserId()).orElse(poll);
    }

    public boolean update(Poll poll) {
        String sql = "UPDATE polls SET question = ?, description = ?, closes_at = ?, is_closed = ?, options = ? WHERE id = ?";
        return jdbcTemplate.update(sql, poll.getQuestion(), poll.getDescription(), poll.getClosesAt(), poll.isClosed(), poll.getOptions(),
                poll.getId()) > 0;
    }

    public boolean delete(int pollId) {
        return jdbcTemplate.update("DELETE FROM polls WHERE id = ?", pollId) > 0;
    }

    public boolean addOrUpdateVote(int pollId, int userId, int optionId) {
        String sql = "INSERT INTO poll_votes (poll_id, user_id, poll_option_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE poll_option_id = VALUES(poll_option_id)";
        return jdbcTemplate.update(sql, pollId, userId, optionId) > 0;
    }

    public long addOrUpdateSchedulingVote(int pollId, Integer userId, String guestName, String notes) {
        String findSql = "SELECT id FROM poll_votes WHERE poll_id = ? AND " + (userId != null ? "user_id = ?" : "guest_name = ?");
        Object[] findArgs = userId != null ? new Object[]{pollId, userId} : new Object[]{pollId, guestName};

        try {
            long existingVoteId = jdbcTemplate.queryForObject(findSql, Long.class, findArgs);
            // The 'notes' column for general remarks is on the poll_votes table now.
            String updateSql = "UPDATE poll_votes SET notes = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, notes, existingVoteId);
            return existingVoteId;
        } catch (EmptyResultDataAccessException e) {
            String insertSql = "INSERT INTO poll_votes (poll_id, user_id, guest_name, notes) VALUES (?, ?, ?, ?)";
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, pollId);
                if (userId != null) ps.setInt(2, userId); else ps.setNull(2, Types.INTEGER);
                ps.setString(3, guestName);
                ps.setString(4, notes);
                return ps;
            }, keyHolder);
            return Objects.requireNonNull(keyHolder.getKey()).longValue();
        }
    }

    public void saveDayVotes(long voteId, List<PollDayVote> dayVotes) {
        jdbcTemplate.update("DELETE FROM poll_day_votes WHERE vote_id = ?", voteId);
        if (dayVotes != null && !dayVotes.isEmpty()) {
            String sql = "INSERT INTO poll_day_votes (vote_id, vote_date, status, notes) VALUES (?, ?, ?, ?)";
            jdbcTemplate.batchUpdate(sql, dayVotes, 100, (ps, vote) -> {
                ps.setLong(1, voteId);
                ps.setDate(2, java.sql.Date.valueOf(vote.getVoteDate()));
                ps.setString(3, vote.getStatus());
                ps.setString(4, vote.getNotes());
            });
        }
    }

    public List<PollDayVote> findDayVotesForPoll(int pollId) {
        String sql = "SELECT pdv.* FROM poll_day_votes pdv JOIN poll_votes pv ON pdv.vote_id = pv.id WHERE pv.poll_id = ?";
        return jdbcTemplate.query(sql, dayVoteRowMapper, pollId);
    }


    public boolean addGuestVote(int pollId, String guestName, int optionId) {
        String sql = "INSERT INTO poll_votes (poll_id, guest_name, poll_option_id) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, pollId, guestName, optionId) > 0;
    }

    public boolean addWordCloudEntry(int pollId, Integer userId, String guestName, String word) {
        String sql = "INSERT INTO poll_word_cloud_entries (poll_id, user_id, guest_name, word) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, pollId, userId, guestName, word) > 0;
    }

    public boolean hasVoted(int pollId, Integer userId, String guestName) {
        if (userId != null) {
            String sql = "SELECT COUNT(*) FROM poll_votes WHERE poll_id = ? AND user_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, pollId, userId);
            return count != null && count > 0;
        }
        if (guestName != null && !guestName.isBlank()) {
            String sql = "SELECT COUNT(*) FROM poll_votes WHERE poll_id = ? AND guest_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, pollId, guestName);
            return count != null && count > 0;
        }
        return false;
    }

    public Optional<PollVote> findVote(int pollId, Integer userId, String guestName) {
        String sql = "SELECT * FROM poll_votes WHERE poll_id = ?";
        List<Object> args = new ArrayList<>();
        args.add(pollId);
        if (userId != null) {
            sql += " AND user_id = ?";
            args.add(userId);
        } else if (guestName != null && !guestName.isBlank()) {
            sql += " AND guest_name = ?";
            args.add(guestName);
        } else {
            return Optional.empty(); // Not enough info to find a unique vote
        }

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, voteRowMapper, args.toArray()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}