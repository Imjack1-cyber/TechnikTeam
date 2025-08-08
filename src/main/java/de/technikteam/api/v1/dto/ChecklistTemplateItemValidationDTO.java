package de.technikteam.api.v1.dto;

public record ChecklistTemplateItemValidationDTO(int itemId, String itemName, int requestedQuantity,
		int availableQuantity) {
}