package de.technikteam.model;

/**
 * Represents a single item from the `storage_items` (inventory) table. It holds
 * all data related to an inventory item, including its name, location,
 * quantity, and an optional image path. It also contains helper methods to
 * 
 * determine its availability status for UI display.
 */
public class StorageItem {
	private int id;
	private String name;
	private String location;
	private String cabinet;
	private String shelf;
	private String compartment;
	private int quantity;
	private int maxQuantity;
	private String imagePath;

	public StorageItem() {
	}

	/**
	 * Determines a human-readable availability status based on the current quantity
	 * relative to the maximum quantity.
	 * 
	 * @return A string representing the status (e.g., "Vollständig", "Niedriger
	 *         Bestand").
	 */
	public String getAvailabilityStatus() {
		if (this.maxQuantity <= 0) {
			return "Unbegrenzt";
		}
		if (this.quantity <= 0) {
			return "Vergriffen";
		}
		if (this.quantity >= this.maxQuantity) {
			return "Vollständig";
		}
		double percentage = (double) this.quantity / this.maxQuantity;
		if (percentage <= 0.25) {
			return "Niedriger Bestand";
		}
		return "Auf Lager";
	}

	/**
	 * Gets a corresponding CSS class based on the availability status, allowing for
	 * easy color-coding in the user interface.
	 * 
	 * @return A CSS class name (e.g., "status-ok", "status-danger").
	 */
	public String getAvailabilityStatusCssClass() {
		if (this.maxQuantity <= 0) {
			return "status-info"; // Unlimited
		}
		if (this.quantity <= 0) {
			return "status-danger"; // Out of Stock
		}
		if (this.quantity >= this.maxQuantity) {
			return "status-ok"; // Full
		}
		double percentage = (double) this.quantity / this.maxQuantity;
		if (percentage <= 0.25) {
			return "status-warn"; // Low Stock
		}
		return "status-ok"; // In Stock
	}

	// --- Getters and Setters ---

	public int getMaxQuantity() {
		return maxQuantity;
	}

	public void setMaxQuantity(int maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCabinet() {
		return cabinet;
	}

	public void setCabinet(String cabinet) {
		this.cabinet = cabinet;
	}

	public String getShelf() {
		return shelf;
	}

	public void setShelf(String shelf) {
		this.shelf = shelf;
	}

	public String getCompartment() {
		return compartment;
	}

	public void setCompartment(String compartment) {
		this.compartment = compartment;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}