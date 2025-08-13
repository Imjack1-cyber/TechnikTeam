package de.technikteam.model;

import java.time.LocalDate;
import java.util.List;

public class Changelog {
	private int id;
	private String version;
	private LocalDate releaseDate;
	private String title;
	private String notes;
	private List<Integer> seenByUserIds; 

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public LocalDate getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public List<Integer> getSeenByUserIds() {
		return seenByUserIds;
	}

	public void setSeenByUserIds(List<Integer> seenByUserIds) {
		this.seenByUserIds = seenByUserIds;
	}
}