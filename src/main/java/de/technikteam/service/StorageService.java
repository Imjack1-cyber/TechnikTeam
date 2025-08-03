package de.technikteam.service;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.Event;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageService {

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
			throw new IllegalArgumentException("Artikel mit ID " + itemId + " nicht gefunden.");

		if ("checkout".equals(type)) {
			if (item.getAvailableQuantity() < quantity)
				throw new IllegalStateException("Nicht genügend Artikel zum Entnehmen verfügbar.");
			item.setQuantity(item.getQuantity() - quantity);
			item.setStatus("CHECKED_OUT");
			item.setCurrentHolderUserId(user.getId());
			item.setAssignedEventId(eventId != null ? eventId : 0);
		} else if ("checkin".equals(type)) {
			if (item.getMaxQuantity() > 0 && (item.getQuantity() + quantity > item.getMaxQuantity())) {
				throw new IllegalStateException("Nicht genügend Platz, um diese Menge einzuräumen.");
			}
			item.setQuantity(item.getQuantity() + quantity);
			if (item.getQuantity() >= item.getMaxQuantity()) {
				item.setStatus("IN_STORAGE");
				item.setCurrentHolderUserId(0);
				item.setAssignedEventId(0);
			}
		} else {
			throw new IllegalArgumentException("Ungültiger Transaktionstyp: " + type);
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
			throw new IllegalArgumentException("Artikel mit ID " + itemId + " nicht gefunden.");
		}

		String logDetails;

		if ("UNREPAIRABLE".equals(status)) {
			if (item.getQuantity() < quantity || item.getDefectiveQuantity() < quantity) {
				throw new IllegalStateException(
						"Es können nicht mehr Artikel als irreparabel markiert werden, als vorhanden oder defekt sind.");
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
				throw new IllegalStateException(
						"Die Gesamtzahl der defekten Artikel kann die Gesamtmenge nicht überschreiten.");
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