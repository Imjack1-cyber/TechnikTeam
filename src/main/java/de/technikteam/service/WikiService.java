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
     * Fetches all wiki entries and organizes them into a hierarchical tree structure.
     * The structure is a map of maps, where the final value is a WikiEntry object.
     *
     * @return A Map representing the root of the file tree.
     */
    public Map<String, Object> getWikiTreeAsData() {
        List<WikiEntry> allEntries = wikiDAO.getAllWikiEntries();
        // Use LinkedHashMap to preserve the order of insertion (alphabetical from DB)
        Map<String, Object> rootNode = new LinkedHashMap<>();

        for (WikiEntry entry : allEntries) {
            String[] pathParts = entry.getFilePath().split("/");
            Map<String, Object> currentNode = rootNode;

            // Traverse or create directory structure
            for (int i = 0; i < pathParts.length - 1; i++) {
                String part = pathParts[i];
                // Get the next directory level, or create it if it doesn't exist.
                // The key is the directory name. The value is another map.
                currentNode = (Map<String, Object>) currentNode
                        .computeIfAbsent(part, k -> new LinkedHashMap<String, Object>());
            }

            // Add the file (the WikiEntry itself) to the final directory
            String fileName = pathParts[pathParts.length - 1];
            if (!fileName.isEmpty()) {
                currentNode.put(fileName, entry);
            }
        }
        logger.debug("Successfully built wiki tree with {} root elements.", rootNode.size());
        return rootNode;
    }
}