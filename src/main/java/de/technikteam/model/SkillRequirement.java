package de.technikteam.model;

public class SkillRequirement {
	// Der Name des Lehrgangs wird jetzt aus der Course-Beziehung geholt
	private int requiredCourseId;
	private String courseName; // Nützlich für die Anzeige
	private int requiredPersons;

	// Getters and Setters
	public int getRequiredCourseId() {
		return requiredCourseId;
	}

	public void setRequiredCourseId(int requiredCourseId) {
		this.requiredCourseId = requiredCourseId;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public int getRequiredPersons() {
		return requiredPersons;
	}

	public void setRequiredPersons(int requiredPersons) {
		this.requiredPersons = requiredPersons;
	}
}