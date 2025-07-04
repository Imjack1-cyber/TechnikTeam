package de.technikteam.model;

import java.util.List;

/**
 * Represents a "kit" or "case" from the `inventory_kits` table. A kit is a
 * container for a predefined collection of StorageItems.
 */
public class InventoryKit {
	private int id;
	private String name;
	private String description;

	// Transient field for holding items when needed
	private List<InventoryKitItem> items;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<InventoryKitItem> getItems() {
		return items;
	}

	public void setItems(List<InventoryKitItem> items) {
		this.items = items;
	}
}