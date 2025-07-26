package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.DatabaseManager;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.Event;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class StorageService {
	private static final Logger logger = LogManager.getLogger(StorageService.class);

	private final DatabaseManager dbManager;
	private final StorageDAO storageDAO;
	private final StorageLogDAO storageLogDAO;
	private final EventDAO eventDAO;
	private final AdminLogService adminLogService;

	@Inject
	public StorageService(DatabaseManager dbManager, StorageDAO storageDAO, StorageLogDAO storageLogDAO,
			EventDAO eventDAO, AdminLogService adminLogService) {
		this.dbManager = dbManager;
		this.storageDAO = storageDAO;
		this.storageLogDAO = storageLogDAO;
		this.eventDAO = eventDAO;
		this.adminLogService = adminLogService;
	}

	public boolean processTransaction(int itemId, int quantity, String type, User user, Integer eventId, String notes) {
		try (Connection conn = dbManager.getConnection()) {
			conn.setAutoCommit(false);
			try {
				StorageItem item = storageDAO.getItemById(itemId, conn);
				if (item == null) {
					throw new IllegalArgumentException("Item with ID " + itemId + " not found.");
				}

				boolean success;
				if ("checkout".equals(type)) {
					if (item.getAvailableQuantity() < quantity) {
						throw new IllegalStateException("Not enough items available to check out.");
					}
					success = storageDAO.performCheckout(itemId, quantity, user.getId(), eventId, conn);
				} else if ("checkin".equals(type)) {
					if (item.getMaxQuantity() > 0 && (item.getQuantity() + quantity > item.getMaxQuantity())) {
						throw new IllegalStateException("Not enough space to check in this many items.");
					}
					success = storageDAO.performCheckin(itemId, quantity, conn);
				} else {
					throw new IllegalArgumentException("Invalid transaction type: " + type);
				}

				if (success) {
					String finalNotes = notes;
					if ("checkout".equals(type) && eventId != null) {
						Event event = eventDAO.getEventById(eventId, conn);
						if (event != null) {
							String autoNote = "Für Event: " + event.getName();
							finalNotes = (notes != null && !notes.trim().isEmpty()) ? autoNote + " - " + notes
									: autoNote;
						}
					}
					int quantityChange = "checkin".equals(type) ? quantity : -quantity;
					storageLogDAO.logTransaction(itemId, user.getId(), quantityChange, finalNotes,
							eventId != null ? eventId : 0, conn);

					conn.commit();

					String action = "checkin".equals(type) ? "eingeräumt" : "entnommen";
					String logDetails = String.format("%d x '%s' (ID: %d) %s. Notiz: %s", quantity, item.getName(),
							itemId, action, finalNotes);
					adminLogService.log(user.getUsername(), "STORAGE_TRANSACTION", logDetails);

					return true;
				} else {
					throw new SQLException("DAO operation returned false, indicating failure.");
				}
			} catch (Exception e) {
				conn.rollback();
				logger.error("Transaction rolled back for storage transaction.", e);
				return false;
			}
		} catch (SQLException e) {
			logger.error("Failed to get connection for storage transaction.", e);
			return false;
		}
	}
	
	public boolean updateDefectiveItemStatus(int itemId, String status, int quantity, String reason, User adminUser) {
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                StorageItem item = storageDAO.getItemById(itemId, conn);
                if (item == null) {
                    throw new IllegalArgumentException("Item with ID " + itemId + " not found.");
                }

                boolean success;
                String logDetails;

                if ("UNREPAIRABLE".equals(status)) {
                    if (item.getQuantity() < quantity) {
                        throw new IllegalStateException("Cannot mark more items as unrepairable than exist.");
                    }
                    if (item.getDefectiveQuantity() < quantity) {
                        throw new IllegalStateException("Cannot mark more items as unrepairable than are currently defective.");
                    }
                    success = storageDAO.permanentlyReduceQuantities(itemId, quantity, reason, conn);
                    logDetails = String.format("Permanently removed %d x '%s' (ID: %d) from stock (unrepairable). Reason: %s", quantity, item.getName(), itemId, reason);
                    adminLogService.log(adminUser.getUsername(), "ITEM_UNREPAIRABLE", logDetails);
                } else { 
                    int newDefectiveTotal = item.getDefectiveQuantity() + quantity;
                    if (item.getQuantity() < newDefectiveTotal) {
                        throw new IllegalStateException("Total defective quantity cannot exceed total quantity.");
                    }
                    success = storageDAO.updateDefectiveStatus(itemId, newDefectiveTotal, reason, conn);
                    logDetails = String.format("Defect status for '%s' (ID: %d) updated: %d defective. Reason: %s", item.getName(), itemId, newDefectiveTotal, reason);
                    adminLogService.log(adminUser.getUsername(), "UPDATE_DEFECT_STATUS", logDetails);
                }

                if (success) {
                    conn.commit();
                    return true;
                } else {
                    throw new SQLException("DAO operation failed, rolling back.");
                }
            } catch (Exception e) {
                conn.rollback();
                logger.error("Transaction rolled back for defect status update on item {}", itemId, e);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Failed to get DB connection for defect status update.", e);
            return false;
        }
    }
}