package de.technikteam.model;

/**
 * A Data Transfer Object (DTO) to represent a single, generic search result.
 * This is used to combine results from different database tables (events,
 * storage, etc.) into a unified list for the API response.
 */
public class SearchResultDTO {
	private String type; 
	private String title;
	private String url;
	private String snippet;

	public SearchResultDTO(String type, String title, String url, String snippet) {
		this.type = type;
		this.title = title;
		this.url = url;
		this.snippet = snippet;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}
}