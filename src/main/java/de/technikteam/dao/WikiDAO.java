package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.WikiEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class WikiDAO {
	private static final Logger logger = LogManager.getLogger(WikiDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public WikiDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	private WikiEntry mapResultSetToWikiEntry(ResultSet rs) throws SQLException {
		WikiEntry entry = new WikiEntry();
		entry.setId(rs.getInt("id"));
		entry.setFilePath(rs.getString("file_path"));
		entry.setContent(rs.getString("content"));
		return entry;
	}

	public Optional<WikiEntry> getWikiEntryById(int id) {
		String sql = "SELECT * FROM wiki_documentation WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToWikiEntry(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching wiki entry by ID {}", id, e);
		}
		return Optional.empty();
	}

	public Optional<WikiEntry> findByFilePath(String filePath) {
		String sql = "SELECT * FROM wiki_documentation WHERE file_path = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, filePath);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToWikiEntry(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching wiki entry by file_path {}", filePath, e);
		}
		return Optional.empty();
	}

	public List<WikiEntry> getAllWikiEntries() {
		List<WikiEntry> entries = new ArrayList<>();
		String sql = "SELECT * FROM wiki_documentation ORDER BY file_path";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				entries.add(mapResultSetToWikiEntry(rs));
			}
		} catch (SQLException e) {
			logger.error("Error fetching all wiki entries", e);
		}
		return entries;
	}

	public boolean updateWikiContent(int id, String content) {
		String sql = "UPDATE wiki_documentation SET content = ? WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, content);
			pstmt.setInt(2, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating wiki content for ID {}", id, e);
			return false;
		}
	}

	public Optional<WikiEntry> createWikiEntry(WikiEntry entry) {
		String sql = "INSERT INTO wiki_documentation (file_path, content) VALUES (?, ?)";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, entry.getFilePath());
			pstmt.setString(2, entry.getContent());
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						entry.setId(rs.getInt(1));
						return Optional.of(entry);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error creating wiki entry for path {}", entry.getFilePath(), e);
		}
		return Optional.empty();
	}

	public boolean deleteWikiEntry(int id) {
		String sql = "DELETE FROM wiki_documentation WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting wiki entry with ID {}", id, e);
			return false;
		}
	}
}