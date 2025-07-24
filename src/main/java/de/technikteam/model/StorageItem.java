package de.technikteam.model;

/**
 * Represents a single item from the `storage_items` (inventory) table.
 */
public class StorageItem {
	private int id;
	private String name;
	private String location;
	private String cabinet;
	private String compartment;
	private int quantity;
	private int maxQuantity;
	private int defectiveQuantity;
	private String defectReason;
	private double weightKg;
	private double priceEur;
	private String imagePath;

	private String status;
	private int currentHolderUserId;
	private int assignedEventId;
	private String currentHolderUsername;

	public StorageItem() {
	}

	public int getAvailableQuantity() {
		return this.quantity - this.defectiveQuantity;
	}

	public String getAvailabilityStatus() {
		int available = getAvailableQuantity();
		if (available <= 0 && maxQuantity > 0) {
			return "Vergriffen";
		}
		if (maxQuantity == 0) {
			return "Auf Lager"; // Untracked quantity
		}
		if (available >= maxQuantity) {
			return "Vollständig";
		}
		if ((double) available / maxQuantity <= 0.25) {
			return "Niedriger Bestand";
		}
		return "Auf Lager";
	}

	public String getAvailabilityStatusCssClass() {
		int available = getAvailableQuantity();
		if (available <= 0 && maxQuantity > 0) {
			return "status-danger";
		}
		if (maxQuantity > 0 && (double) available / maxQuantity <= 0.25) {
			return "status-warn";
		}
		return "status-ok";
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

	public int getMaxQuantity() {
		return maxQuantity;
	}

	public void setMaxQuantity(int maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

	public int getDefectiveQuantity() {
		return defectiveQuantity;
	}

	public void setDefectiveQuantity(int defectiveQuantity) {
		this.defectiveQuantity = defectiveQuantity;
	}

	public String getDefectReason() {
		return defectReason;
	}

	public void setDefectReason(String defectReason) {
		this.defectReason = defectReason;
	}

	public double getWeightKg() {
		return weightKg;
	}

	public void setWeightKg(double weightKg) {
		this.weightKg = weightKg;
	}

	public double getPriceEur() {
		return priceEur;
	}

	public void setPriceEur(double priceEur) {
		this.priceEur = priceEur;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getCurrentHolderUserId() {
		return currentHolderUserId;
	}

	public void setCurrentHolderUserId(int currentHolderUserId) {
		this.currentHolderUserId = currentHolderUserId;
	}

	public int getAssignedEventId() {
		return assignedEventId;
	}

	public void setAssignedEventId(int assignedEventId) {
		this.assignedEventId = assignedEventId;
	}

	public String getCurrentHolderUsername() {
		return currentHolderUsername;
	}

	public void setCurrentHolderUsername(String currentHolderUsername) {
		this.currentHolderUsername = currentHolderUsername;
	}
}