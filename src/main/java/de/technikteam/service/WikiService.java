package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.WikiEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class WikiService {
	private static final Logger logger = LogManager.getLogger(WikiService.class);
	private final WikiDAO wikiDAO;

	@Inject
	public WikiService(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
	}

	/**
	 * Fetches all wiki entries and organizes them into a hierarchical tree
	 * structure. The structure is a map of maps, where the final value is a
	 * WikiEntry object.
	 *
	 * @return A Map representing the root of the file tree.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getWikiTreeAsData() {
		List<WikiEntry> allEntries = wikiDAO.getAllWikiEntries();
		Map<String, Object> rootNode = new LinkedHashMap<>();

		for (WikiEntry entry : allEntries) {
			String[] pathParts = entry.getFilePath().split("/");
			Map<String, Object> currentNode = rootNode;

			for (int i = 0; i < pathParts.length - 1; i++) {
				String part = pathParts[i];
				// This cast is safe within this algorithm as we are intentionally building a
				// nested map structure.
				currentNode = (Map<String, Object>) currentNode.computeIfAbsent(part,
						k -> new LinkedHashMap<String, Object>());
			}

			String fileName = pathParts[pathParts.length - 1];
			if (!fileName.isEmpty()) {
				currentNode.put(fileName, entry);
			}
		}
		logger.debug("Successfully built wiki tree with {} root elements.", rootNode.size());
		return rootNode;
	}
}