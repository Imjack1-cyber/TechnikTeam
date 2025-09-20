package de.technikteam.dao;

import de.technikteam.model.FileSharingLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class FileSharingDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FileSharingDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FileSharingLink> rowMapper = (rs, rowNum) -> {
        FileSharingLink link = new FileSharingLink();
        link.setId(rs.getInt("id"));
        link.setFileId(rs.getInt("file_id"));
        link.setToken(rs.getString("token"));
        link.setAccessLevel(rs.getString("access_level"));
        Timestamp expiry = rs.getTimestamp("expires_at");
        if (expiry != null) {
            link.setExpiresAt(expiry.toLocalDateTime());
        }
        link.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return link;
    };

    public Optional<FileSharingLink> findById(int id) {
        String sql = "SELECT * FROM file_sharing_links WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<FileSharingLink> findByToken(String token) {
        String sql = "SELECT * FROM file_sharing_links WHERE token = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, token));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<FileSharingLink> findByFileId(int fileId) {
        String sql = "SELECT * FROM file_sharing_links WHERE file_id = ?";
        return jdbcTemplate.query(sql, rowMapper, fileId);
    }

    public FileSharingLink create(FileSharingLink link) {
        String sql = "INSERT INTO file_sharing_links (file_id, token, access_level, expires_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, link.getFileId(), link.getToken(), link.getAccessLevel(), link.getExpiresAt());
        // This is a simplification; a real app might need to fetch the created object back.
        return link;
    }

    public boolean delete(int linkId) {
        String sql = "DELETE FROM file_sharing_links WHERE id = ?";
        return jdbcTemplate.update(sql, linkId) > 0;
    }
}