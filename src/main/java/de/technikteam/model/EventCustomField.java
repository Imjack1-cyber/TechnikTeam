package de.technikteam.model;

/**
 * Represents a custom field that can be added to an event sign-up form.
 */
public class EventCustomField {
	private int id;
	private int eventId;
	private String fieldName;
	private String fieldType;
	private boolean isRequired;
	private String fieldOptions;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean required) {
		isRequired = required;
	}
	
	public String getFieldOptions() {
		return fieldOptions; 
	}
	
	public void setFieldOptions(String fieldOptions) {
		this.fieldOptions = fieldOptions; 
	}
}