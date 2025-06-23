package de.technikteam.model;

import de.technikteam.config.DateFormatter;
import java.time.LocalDateTime;

/**
 * A Data Transfer Object (DTO) to represent a single entry from the storage_log table,
 * enriched with the username of the person who performed the transaction.
 */
public class StorageLogEntry {
    private int id;
    private int itemId;
    private int userId;
    private String username;
    private int quantityChange;
    private String notes;
    private LocalDateTime transactionTimestamp;

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(LocalDateTime transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    public String getFormattedTimestamp() {
        return DateFormatter.formatDateTime(this.transactionTimestamp);
    }
}