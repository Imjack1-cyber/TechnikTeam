package de.technikteam.model;

/**
 * Represents the junction table record from `inventory_kit_items`, linking a
 * StorageItem to an InventoryKit with a specific quantity.
 */
public class InventoryKitItem {
	private int kitId;
	private int itemId;
	private int quantity;

	// Transient field for display
	private String itemName;

	// --- Getters and Setters ---
	public int getKitId() {
		return kitId;
	}

	public void setKitId(int kitId) {
		this.kitId = kitId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
}