package de.technikteam.model;

public class ChecklistTemplateItem {
	private int id;
	private int templateId;
	private String itemText;
	private Integer storageItemId;
	private Integer quantity;
	private String storageItemName; 
	private int displayOrder;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public String getItemText() {
		return itemText;
	}

	public void setItemText(String itemText) {
		this.itemText = itemText;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Integer getStorageItemId() {
		return storageItemId;
	}

	public void setStorageItemId(Integer storageItemId) {
		this.storageItemId = storageItemId;
	}

	public String getStorageItemName() {
		return storageItemName;
	}

	public void setStorageItemName(String storageItemName) {
		this.storageItemName = storageItemName;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}