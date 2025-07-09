package de.technikteam.model;

/**
 * Represents a record from the `event_skill_requirements` table. It defines a
 * specific skill (by referencing a `Course`) and the number of people with that
 * skill required for a particular event.
 */
public class SkillRequirement {
	private int requiredCourseId;
	private int requiredPersons;
	private String courseName;

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