package de.technikteam.service;

import com.google.inject.Singleton;
import de.technikteam.model.User;

@Singleton
public class AuthorizationService {

	/**
	 * Checks if a user has a specific permission.
	 * 
	 * @param user          The user to check.
	 * @param permissionKey The permission key to verify (e.g., "USER_CREATE").
	 * @return true if the user has the permission, false otherwise.
	 */
	public boolean checkPermission(User user, String permissionKey) {
		if (user == null || user.getPermissions() == null) {
			return false;
		}
		// A master admin can do anything.
		if (user.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			return true;
		}
		return user.getPermissions().contains(permissionKey);
	}
}