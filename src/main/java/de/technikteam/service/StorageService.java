package de.technikteam.service;

import de.technikteam.api.v1.dto.NotificationPayload;
import de.technikteam.config.Permissions;
import de.technikteam.dao.*;
import de.technikteam.model.DamageReport;
import de.technikteam.model.Event;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StorageService {

	private final StorageDAO storageDAO;
	private final StorageLogDAO storageLogDAO;
	private final DamageReportDAO damageReportDAO;
	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final MaintenanceLogDAO maintenanceLogDAO;

	@Autowired
	public StorageService(StorageDAO storageDAO, StorageLogDAO storageLogDAO, DamageReportDAO damageReportDAO,
			UserDAO userDAO, EventDAO eventDAO, AdminLogService adminLogService,
			NotificationService notificationService, MaintenanceLogDAO maintenanceLogDAO) {
		this.storageDAO = storageDAO;
		this.storageLogDAO = storageLogDAO;
		this.damageReportDAO = damageReportDAO;
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
		this.maintenanceLogDAO = maintenanceLogDAO;
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
		notificationService.broadcastUIUpdate("STORAGE_ITEM", "UPDATED", item);

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
	public void handleItemStatusUpdate(int itemId, Map<String, Object> payload, User adminUser) {
		String action = (String) payload.get("action");
		if (action == null) {
			throw new IllegalArgumentException("Action is required.");
		}

		switch (action) {
		case "report_defect":
		case "report_unrepairable":
			int defectiveQuantity = Optional.ofNullable((Number) payload.get("quantity"))
					.orElseThrow(
							() -> new IllegalArgumentException("Quantity is required for defect/unrepairable actions."))
					.intValue();
			String reason = (String) payload.get("reason");
			String status = "report_unrepairable".equals(action) ? "UNREPAIRABLE" : "DEFECT";
			updateDefectiveItemStatus(itemId, status, defectiveQuantity, reason, adminUser);
			break;
		case "repair":
			int repairedQuantity = Optional.ofNullable((Number) payload.get("quantity"))
					.orElseThrow(() -> new IllegalArgumentException("Quantity is required for repair action."))
					.intValue();
			String notes = (String) payload.get("notes");
			repairItems(itemId, repairedQuantity, notes, adminUser);
			break;
		default:
			throw new IllegalArgumentException("Unknown action: " + action);
		}
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
		notificationService.broadcastUIUpdate("STORAGE_ITEM", "UPDATED", item);
		return true;
	}

	@Transactional
	public void repairItems(int itemId, int quantity, String notes, User adminUser) {
		StorageItem item = storageDAO.getItemById(itemId);
		if (item == null) {
			throw new IllegalArgumentException("Item not found.");
		}
		if (quantity > item.getDefectiveQuantity()) {
			throw new IllegalStateException("Cannot repair more items than are marked as defective.");
		}
		item.setDefectiveQuantity(item.getDefectiveQuantity() - quantity);
		storageDAO.updateItem(item);
		notificationService.broadcastUIUpdate("STORAGE_ITEM", "UPDATED", item);
		// Log maintenance action
		adminLogService.log(adminUser.getUsername(), "ITEM_REPAIRED",
				String.format("Repaired %d x '%s' (ID: %d). Notes: %s", quantity, item.getName(), itemId, notes));
	}

	@Transactional
	public DamageReport createDamageReport(int itemId, int reporterId, String description) {
		StorageItem item = storageDAO.getItemById(itemId);
		if (item == null) {
			throw new IllegalArgumentException("Der zu meldende Artikel existiert nicht.");
		}
		DamageReport report = damageReportDAO.createReport(itemId, reporterId, description);
		notificationService.broadcastUIUpdate("DAMAGE_REPORT", "CREATED", report);

		// Notify admins
		List<Integer> adminIds = userDAO.findUserIdsByPermission(Permissions.DAMAGE_REPORT_MANAGE);
		User reporter = userDAO.getUserById(reporterId);

        NotificationPayload payload = new NotificationPayload();
        payload.setTitle("Neue Schadensmeldung");
        payload.setDescription(String.format("%s hat einen Schaden für '%s' gemeldet.", reporter.getUsername(), item.getName()));
        payload.setLevel("Important");
        payload.setUrl("/admin/damage-reports");
        
		for (Integer adminId : adminIds) {
			notificationService.sendNotificationToUser(adminId, payload);
		}

		return report;
	}

	@Transactional
	public void confirmDamageReport(int reportId, int quantity, User adminUser) {
		DamageReport report = damageReportDAO.getReportById(reportId)
				.orElseThrow(() -> new IllegalArgumentException("Bericht nicht gefunden."));

		if (!"PENDING".equals(report.getStatus())) {
			throw new IllegalStateException("Dieser Bericht wurde bereits bearbeitet.");
		}

		// Update the storage item
		StorageItem item = storageDAO.getItemById(report.getItemId());
		int newDefectiveTotal = item.getDefectiveQuantity() + quantity;
		if (item.getQuantity() < newDefectiveTotal) {
			throw new IllegalStateException(
					"Die Gesamtzahl der defekten Artikel kann die Gesamtmenge nicht überschreiten.");
		}
		item.setDefectiveQuantity(newDefectiveTotal);
		if (item.getDefectReason() == null || item.getDefectReason().isBlank()) {
			item.setDefectReason(report.getReportDescription());
		} else {
			item.setDefectReason(item.getDefectReason() + " | Gemeldet: " + report.getReportDescription());
		}
		storageDAO.updateItem(item);
		notificationService.broadcastUIUpdate("STORAGE_ITEM", "UPDATED", item);

		// Update the report status
		damageReportDAO.updateStatus(reportId, "CONFIRMED", adminUser.getId(), "Bestätigt und als defekt verbucht.");
		notificationService.broadcastUIUpdate("DAMAGE_REPORT", "UPDATED", report);

		// Log the admin action
		adminLogService.log(adminUser.getUsername(), "DAMAGE_REPORT_CONFIRMED",
				String.format("Schadensmeldung #%d für '%s' bestätigt.", reportId, item.getName()));
	}

	@Transactional
	public void rejectDamageReport(int reportId, String adminNotes, User adminUser) {
		DamageReport report = damageReportDAO.getReportById(reportId)
				.orElseThrow(() -> new IllegalArgumentException("Bericht nicht gefunden."));

		if (!"PENDING".equals(report.getStatus())) {
			throw new IllegalStateException("Dieser Bericht wurde bereits bearbeitet.");
		}

		damageReportDAO.updateStatus(reportId, "REJECTED", adminUser.getId(), adminNotes);
		notificationService.broadcastUIUpdate("DAMAGE_REPORT", "UPDATED", report);

		adminLogService.log(adminUser.getUsername(), "DAMAGE_REPORT_REJECTED", String.format(
				"Schadensmeldung #%d für '%s' abgelehnt. Grund: %s", reportId, report.getItemName(), adminNotes));
	}
}