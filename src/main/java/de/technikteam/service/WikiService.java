package de.technikteam.service;

import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

	private static final String projectTreeHtml;
	private static final Map<String, String> documentationMap;
	private static final Map<String, String> anchorToPathMap;

	// Helper record for the two-pass parsing algorithm
	private record WikiBlockIndex(String path, String anchor, int startLine) {
	}

	static {
		documentationMap = new HashMap<>();
		anchorToPathMap = new HashMap<>();
		parseFileDocumentation();
		projectTreeHtml = parseProjectTree();
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

	private static String parseProjectTree() {
		try (InputStream is = WikiService.class.getClassLoader().getResourceAsStream(WIKI_FILE)) {
			if (is == null) {
				logger.error("Could not find {} in classpath resources.", WIKI_FILE);
				return "<p class='error-message'>Wiki content file not found.</p>";
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
				if (line.contains("## Part 2: Detailed File Documentation")) {
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

				Pattern linkPattern = Pattern.compile("\\[`([^`]+)`\\]\\(#([^)]+)\\)");
				Matcher matcher = linkPattern.matcher(content);

				if (matcher.find()) {
					String fileName = matcher.group(1);
					String anchor = matcher.group(2);
					String filePath = anchorToPathMap.get(anchor);

					if (filePath != null) {
						String encodedPath = URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString());
						html.append("<a href=\"wiki/details?file=").append(encodedPath).append("\">").append(fileName)
								.append("</a>");
					} else {
						html.append(fileName).append(" (Link error)");
						// This warning is now expected for non-file entries in the tree
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
			return "<p class='error-message'>Error reading wiki content.</p>";
		}
	}

	private static void parseFileDocumentation() {
		try (InputStream is = WikiService.class.getClassLoader().getResourceAsStream(WIKI_FILE)) {
			if (is == null) {
				logger.error("Could not find {} in classpath resources.", WIKI_FILE);
				return;
			}
			List<String> lines = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines()
					.collect(Collectors.toList());

			Pattern pathPattern = Pattern
					.compile("`?C:\\\\Users\\\\techn\\\\eclipse\\\\workspace\\\\TechnikTeam\\\\(.+?)`?$");
			Pattern anchorPattern = Pattern.compile("<a name=\"([^\"]+)\"></a>");

			boolean inDocsSection = false;
			List<WikiBlockIndex> blockIndices = new ArrayList<>();

			// --- PASS 1: Index all documentation blocks ---
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.contains("## Part 2: Detailed File Documentation")) {
					inDocsSection = true;
					continue;
				}
				if (!inDocsSection)
					continue;

				Matcher pathMatcher = pathPattern.matcher(line.trim());
				if (pathMatcher.find() && i + 1 < lines.size()) {
					String nextLine = lines.get(i + 1);
					Matcher anchorMatcher = anchorPattern.matcher(nextLine);
					if (anchorMatcher.find()) {
						String capturedGroup = pathMatcher.group(1).trim();
						if (capturedGroup.startsWith("`"))
							capturedGroup = capturedGroup.substring(1);
						if (capturedGroup.endsWith("`"))
							capturedGroup = capturedGroup.substring(0, capturedGroup.length() - 1);

						String path = capturedGroup.replace("\\", "/");
						String anchor = anchorMatcher.group(1);

						blockIndices.add(new WikiBlockIndex(path, anchor, i + 2)); // Content starts after anchor line
						anchorToPathMap.put(anchor, path);

						i++; // Skip the anchor line on the next iteration
					}
				}
			}

			// --- PASS 2: Extract content between indexed blocks ---
			for (int i = 0; i < blockIndices.size(); i++) {
				WikiBlockIndex currentBlock = blockIndices.get(i);
				int startLine = currentBlock.startLine();
				int endLine = (i + 1 < blockIndices.size()) ? blockIndices.get(i + 1).startLine() - 3 : lines.size();

				StringBuilder contentBuilder = new StringBuilder();
				for (int j = startLine; j < endLine; j++) {
					String line = lines.get(j);
					// Skip any horizontal rules that might have been block separators
					if (line.trim().equals("---"))
						continue;
					contentBuilder.append(line).append("\n");
				}
				documentationMap.put(currentBlock.path(), contentBuilder.toString().trim());
			}

		} catch (IOException e) {
			logger.error("Failed to parse file documentation from {}", WIKI_FILE, e);
		}
	}

	public String getProjectTreeHtml() {
		return projectTreeHtml;
	}

	public String getFileDocumentation(String filePath) {
		return documentationMap.getOrDefault(filePath,
				"# Error\n\nDocumentation for the specified file could not be found.");
	}
}