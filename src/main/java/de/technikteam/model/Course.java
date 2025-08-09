package de.technikteam.model;

/**
 * Represents a parent course template from the `courses` table. This is not a
 * schedulable event itself, but a blueprint for a type of training (e.g.,
 * "Grundlehrgang Tontechnik"). Individual dates/sessions for a course are
 * handled by the 'Meeting' model.
 */
public class Course {
	private int id;
	private String name;
	private String abbreviation;
	private String description;

	public Course() {
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

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}