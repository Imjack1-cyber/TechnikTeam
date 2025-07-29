package de.technikteam.service;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.Event;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageService {
	private static final Logger logger = LogManager.getLogger(StorageService.class);

	private final StorageDAO storageDAO;
	private final StorageLogDAO storageLogDAO;
	private final EventDAO eventDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public StorageService(StorageDAO storageDAO, StorageLogDAO storageLogDAO, EventDAO eventDAO,
			AdminLogService adminLogService) {
		this.storageDAO = storageDAO;
		this.storageLogDAO = storageLogDAO;
		this.eventDAO = eventDAO;
		this.adminLogService = adminLogService;
	}

	@Transactional
	public boolean processTransaction(int itemId, int quantity, String type, User user, Integer eventId, String notes) {
		StorageItem item = storageDAO.getItemById(itemId);
		if (item == null)
			throw new IllegalArgumentException("Item with ID " + itemId + " not found.");

		if ("checkout".equals(type)) {
			if (item.getAvailableQuantity() < quantity)
				throw new IllegalStateException("Not enough items available to check out.");
			item.setQuantity(item.getQuantity() - quantity);
			item.setStatus("CHECKED_OUT");
			item.setCurrentHolderUserId(user.getId());
			item.setAssignedEventId(eventId != null ? eventId : 0);
		} else if ("checkin".equals(type)) {
			if (item.getMaxQuantity() > 0 && (item.getQuantity() + quantity > item.getMaxQuantity())) {
				throw new IllegalStateException("Not enough space to check in this many items.");
			}
			item.setQuantity(item.getQuantity() + quantity);
			if (item.getQuantity() >= item.getMaxQuantity()) {
				item.setStatus("IN_STORAGE");
				item.setCurrentHolderUserId(0);
				item.setAssignedEventId(0);
			}
		} else {
			throw new IllegalArgumentException("Invalid transaction type: " + type);
		}

		storageDAO.updateItem(item);

		String finalNotes = notes;
		if ("checkout".equals(type) && eventId != null) {
			Event event = eventDAO.getEventById(eventId);
			if (event != null) {
				String autoNote = "Für Event: " + event.getName();
				finalNotes = (notes != null && !notes.trim().isEmpty()) ? autoNote + " - " + notes : autoNote;
			}
		}
		int quantityChange = "checkin".equals(type) ? quantity : -quantity;
		storageLogDAO.logTransaction(itemId, user.getId(), quantityChange, finalNotes, eventId != null ? eventId : 0);

		String action = "checkin".equals(type) ? "eingeräumt" : "entnommen";
		String logDetails = String.format("%d x '%s' (ID: %d) %s. Notiz: %s", quantity, item.getName(), itemId, action,
				finalNotes);
		adminLogService.log(user.getUsername(), "STORAGE_TRANSACTION", logDetails);

		return true;
	}

	@Transactional
	public boolean updateDefectiveItemStatus(int itemId, String status, int quantity, String reason, User adminUser) {
		StorageItem item = storageDAO.getItemById(itemId);
		if (item == null) {
			throw new IllegalArgumentException("Item with ID " + itemId + " not found.");
		}

		String logDetails;

		if ("UNREPAIRABLE".equals(status)) {
			if (item.getQuantity() < quantity || item.getDefectiveQuantity() < quantity) {
				throw new IllegalStateException("Cannot mark more items as unrepairable than exist or are defective.");
			}
			item.setQuantity(item.getQuantity() - quantity);
			item.setDefectiveQuantity(item.getDefectiveQuantity() - quantity);
			item.setDefectReason(reason);
			logDetails = String.format("Permanently removed %d x '%s' (ID: %d) from stock (unrepairable). Reason: %s",
					quantity, item.getName(), itemId, reason);
			adminLogService.log(adminUser.getUsername(), "ITEM_UNREPAIRABLE", logDetails);
		} else {
			int newDefectiveTotal = item.getDefectiveQuantity() + quantity;
			if (item.getQuantity() < newDefectiveTotal) {
				throw new IllegalStateException("Total defective quantity cannot exceed total quantity.");
			}
			item.setDefectiveQuantity(newDefectiveTotal);
			item.setDefectReason(reason);
			logDetails = String.format("Defect status for '%s' (ID: %d) updated: %d defective. Reason: %s",
					item.getName(), itemId, newDefectiveTotal, reason);
			adminLogService.log(adminUser.getUsername(), "UPDATE_DEFECT_STATUS", logDetails);
		}

		storageDAO.updateItem(item);
		return true;
	}
}