package de.technikteam.model;

import java.time.LocalDateTime;

public class PageDocumentation {
	private int id;
	private String pageKey;
	private String title;
	private String pagePath;
	private String features;
	private String relatedPages; 
	private boolean adminOnly;
	private Integer wikiEntryId;
	private String category; 
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String wikiLink; 

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPageKey() {
		return pageKey;
	}

	public void setPageKey(String pageKey) {
		this.pageKey = pageKey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPagePath() {
		return pagePath;
	}

	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}

	public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public String getRelatedPages() {
		return relatedPages;
	}

	public void setRelatedPages(String relatedPages) {
		this.relatedPages = relatedPages;
	}

	public boolean isAdminOnly() {
		return adminOnly;
	}

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public Integer getWikiEntryId() {
		return wikiEntryId;
	}

	public void setWikiEntryId(Integer wikiEntryId) {
		this.wikiEntryId = wikiEntryId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getWikiLink() {
		return wikiLink;
	}

	public void setWikiLink(String wikiLink) {
		this.wikiLink = wikiLink;
	}
}