package de.technikteam.model;

public class SkillRequirement {
	private String skillName;
	private int requiredPersons;

	// Getters and Setters
	public String getSkillName() {
		return skillName;
	}

	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}

	public int getRequiredPersons() {
		return requiredPersons;
	}

	public void setRequiredPersons(int requiredPersons) {
		this.requiredPersons = requiredPersons;
	}
}