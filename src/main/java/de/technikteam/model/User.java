package de.technikteam.model;

import java.time.LocalDateTime;
import java.util.Set;

public class User {
	private int id;
	private String username;
	private int roleId;
	private String roleName;
	private Set<String> permissions;
	private LocalDateTime createdAt;
	private int classYear;
	private String className;
	private String email;
	private String chatColor;
	private String theme;

	public User() {
	}

	public User(int id, String username, String roleName) {
		this.id = id;
		this.username = username;
		this.roleName = roleName;
	}

	/**
	 * A central method to determine if the user has any permission that grants them
	 * access to the admin panel. This prevents logic duplication in filters and
	 * navigation builders.
	 *
	 * @return true if the user has any admin-level permission, false otherwise.
	 */
	public boolean hasAdminAccess() {
		if (permissions == null) {
			return false;
		}
		// A user has admin access if they have the master key OR any other specific
		// admin-level permission.
		return permissions.contains("ACCESS_ADMIN_PANEL") || permissions.stream()
				.anyMatch(p -> !"ACCESS_ADMIN_PANEL".equals(p) && (p.contains("_READ") || p.contains("_MANAGE")
						|| p.contains("_UPDATE") || p.contains("_CREATE") || p.contains("_DELETE")));
	}

	// --- Standard Getters and Setters ---

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getClassYear() {
		return classYear;
	}

	public void setClassYear(int classYear) {
		this.classYear = classYear;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getChatColor() {
		return chatColor;
	}

	public void setChatColor(String chatColor) {
		this.chatColor = chatColor;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getFormattedCreatedAt() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.createdAt);
	}
}