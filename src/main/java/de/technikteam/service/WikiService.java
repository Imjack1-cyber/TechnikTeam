package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.WikiEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class WikiService {
	private static final Logger logger = LogManager.getLogger(WikiService.class);
	private final WikiDAO wikiDAO;
	private Map<String, Object> wikiTreeData;

	@Inject
	public WikiService(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
		initialize();
	}

	private void initialize() {
		logger.info("Initializing WikiService and building data tree...");
		this.wikiTreeData = buildWikiTreeData();
		logger.info("WikiService initialized successfully. Data tree has been generated.");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> buildWikiTreeData() {
		List<WikiEntry> allEntries = wikiDAO.getAllWikiEntries();
		// Sort entries to ensure directories are processed before files within them.
		allEntries.sort(Comparator.comparing(WikiEntry::getFilePath));

		Map<String, Object> root = new LinkedHashMap<>();
		for (WikiEntry entry : allEntries) {
			String[] pathParts = entry.getFilePath().split("/");
			Map<String, Object> currentNode = root;
			for (int i = 0; i < pathParts.length - 1; i++) {
				String part = pathParts[i];
				currentNode = (Map<String, Object>) currentNode.computeIfAbsent(part,
						k -> new LinkedHashMap<String, Object>());
			}
			currentNode.put(pathParts[pathParts.length - 1], entry);
		}
		return root;
	}

	public Map<String, Object> getWikiTreeAsData() {
		return wikiTreeData;
	}
}