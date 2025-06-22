package de.technikteam.model;

/**
 * A simple model representing a category from the `file_categories` table, used
 * to organize uploaded files. It contains just a unique ID and a name.
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