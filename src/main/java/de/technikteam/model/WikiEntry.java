package de.technikteam.model;

/**
 * Represents a single documentation entry from the `wiki_documentation` table.
 */
public class WikiEntry {
	private int id;
	private String filePath;
	private String content;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}