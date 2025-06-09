package de.technikteam.model;

/**
 * Represents a single category for organizing files.
 */
public class FileCategory {
	private int id;
	private String name;

	public FileCategory() {
	}

	// --- Getters and Setters ---

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}