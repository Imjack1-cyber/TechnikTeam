package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.WikiEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class WikiService {
	private static final Logger logger = LogManager.getLogger(WikiService.class);
	private static final String WIKI_FILE = "Wiki.md";
	private final WikiDAO wikiDAO;

	private String projectTreeHtml;
	private final Map<String, WikiEntry> wikiEntriesByPath = new HashMap<>();

	@Inject
	public WikiService(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
		initialize();
	}

	private void initialize() {
		logger.info("Initializing WikiService...");
		List<WikiEntry> allEntries = wikiDAO.getAllWikiEntries();
		for (WikiEntry entry : allEntries) {
			wikiEntriesByPath.put(entry.getFilePath(), entry);
		}
		logger.debug("Loaded {} wiki entries from database into cache.", wikiEntriesByPath.size());
		this.projectTreeHtml = parseProjectTree();
		logger.info("WikiService initialized successfully. Project tree HTML has been generated.");
	}

	private static int getIndentLevel(String line) {
		int count = 0;
		for (char c : line.toCharArray()) {
			if (c == ' ') {
				count++;
			} else {
				break;
			}
		}
		return count / 4; // Assumes 4 spaces per indent level
	}

	private String parseProjectTree() {
		try (InputStream is = WikiService.class.getClassLoader().getResourceAsStream(WIKI_FILE)) {
			if (is == null) {
				return "<p class='error-message'>Wiki structure file (Wiki.md) not found.</p>";
			}
			List<String> lines = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines()
					.collect(Collectors.toList());

			StringBuilder html = new StringBuilder();
			boolean inTree = false;
			int lastLevel = -1;

			for (String line : lines) {
				if (line.contains("## Part 1: Project Tree")) {
					inTree = true;
					continue;
				}
				if (line.contains("## Part 2: Detailed File Documentation")) { // Stop if legacy file is used
					break;
				}
				if (!inTree || line.trim().isEmpty() || !line.trim().startsWith("-")) {
					continue;
				}

				int level = getIndentLevel(line);
				String content = line.trim().substring(1).trim();

				if (level > lastLevel) {
					html.append("<ul>");
				} else if (level < lastLevel) {
					html.append("</li></ul>".repeat(lastLevel - level));
					html.append("</li>");
				} else if (lastLevel != -1) {
					html.append("</li>");
				}
				html.append("<li>");

				Pattern linkPattern = Pattern.compile("\\[`?([^\\]`]+)`?\\]\\(([^)]+)\\)");
				Matcher matcher = linkPattern.matcher(content);

				if (matcher.find()) {
					String fileName = matcher.group(1);
					String filePath = matcher.group(2);

					if (filePath != null) {
						WikiEntry entry = wikiEntriesByPath.get(filePath);
						if (entry != null) {
							html.append("<a href=\"wiki/details?id=").append(entry.getId()).append("\">")
									.append(fileName).append("</a>");
						} else {
							html.append(fileName).append(" (No Doc)");
							logger.warn("No database entry found for wiki file path: {}", filePath);
						}
					} else {
						html.append(fileName).append(" (No Path)");
						logger.warn("No path found in link for: {}", fileName);
					}
				} else {
					html.append(content);
				}
				lastLevel = level;
			}

			if (lastLevel != -1) {
				html.append("</li></ul>".repeat(lastLevel + 1));
			}

			return html.toString();
		} catch (IOException e) {
			logger.error("Failed to read project tree from {}", WIKI_FILE, e);
			return "<p class='error-message'>Error reading wiki structure.</p>";
		}
	}

	public String getProjectTreeHtml() {
		return projectTreeHtml;
	}
}