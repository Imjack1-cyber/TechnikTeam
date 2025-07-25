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
import java.util.ArrayList;
import java.util.List;

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

	public WikiEntry getWikiEntryById(int id) {
		String sql = "SELECT * FROM wiki_documentation WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToWikiEntry(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching wiki entry by ID {}", id, e);
		}
		return null;
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
}