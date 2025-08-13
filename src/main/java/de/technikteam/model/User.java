package de.technikteam.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
	private String fcmToken;
	private String profileIconClass;
	private String adminNotes;
	private String dashboardLayout; 
	private String assignedEventRole; 
	private Integer assignedEventRoleId; 
	private int unseenNotificationsCount; 

	private String status;
	private LocalDateTime suspendedUntil;
	private String suspendedReason;
	private boolean isLocked; 

	private boolean isDeleted;
	private LocalDateTime deletedAt;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String passwordHash;

	public User() {
	}

	public User(int id, String username, String roleName) {
		this.id = id;
		this.username = username;
		this.roleName = roleName;
	}

	public boolean hasAdminAccess() {
		return "ADMIN".equals(this.roleName);
	}

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

	public String getFcmToken() {
		return fcmToken;
	}

	public void setFcmToken(String fcmToken) {
		this.fcmToken = fcmToken;
	}

	public String getProfileIconClass() {
		return profileIconClass;
	}

	public void setProfileIconClass(String profileIconClass) {
		this.profileIconClass = profileIconClass;
	}

	public String getAdminNotes() {
		return adminNotes;
	}

	public void setAdminNotes(String adminNotes) {
		this.adminNotes = adminNotes;
	}

	public String getDashboardLayout() {
		return dashboardLayout;
	}

	public void setDashboardLayout(String dashboardLayout) {
		this.dashboardLayout = dashboardLayout;
	}

	public String getAssignedEventRole() {
		return assignedEventRole;
	}

	public void setAssignedEventRole(String assignedEventRole) {
		this.assignedEventRole = assignedEventRole;
	}

	public Integer getAssignedEventRoleId() {
		return assignedEventRoleId;
	}

	public void setAssignedEventRoleId(Integer assignedEventRoleId) {
		this.assignedEventRoleId = assignedEventRoleId;
	}

	public int getUnseenNotificationsCount() {
		return unseenNotificationsCount;
	}

	public void setUnseenNotificationsCount(int unseenNotificationsCount) {
		this.unseenNotificationsCount = unseenNotificationsCount;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getSuspendedUntil() {
		return suspendedUntil;
	}

	public void setSuspendedUntil(LocalDateTime suspendedUntil) {
		this.suspendedUntil = suspendedUntil;
	}

	public String getSuspendedReason() {
		return suspendedReason;
	}

	public void setSuspendedReason(String suspendedReason) {
		this.suspendedReason = suspendedReason;
	}

	@JsonProperty("isLocked")
	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean locked) {
		isLocked = locked;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public String getFormattedCreatedAt() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.createdAt);
	}
}