package de.technikteam.model;

/**
 * Represents a user's response to a specific EventCustomField.
 */
public class EventCustomFieldResponse {
    private int id;
    private int fieldId;
    private int userId;
    private String responseValue;
    
    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFieldId() { return fieldId; }
    public void setFieldId(int fieldId) { this.fieldId = fieldId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getResponseValue() { return responseValue; }
    public void setResponseValue(String responseValue) { this.responseValue = responseValue; }
}