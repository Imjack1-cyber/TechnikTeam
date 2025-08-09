package de.technikteam.model;

/**
 * Represents a single permission from the `permissions` table. It defines a
 * specific, granular action that can be assigned to a role.
 */
public class Permission {
	private int id;
	private String permissionKey;
	private String description;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPermissionKey() {
		return permissionKey;
	}

	public void setPermissionKey(String permissionKey) {
		this.permissionKey = permissionKey;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}