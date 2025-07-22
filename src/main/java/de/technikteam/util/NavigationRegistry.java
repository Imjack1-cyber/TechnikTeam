package de.technikteam.util;

import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A central registry for all navigation items in the application. This class
 * provides a single source of truth for the sidebar links and their required
 * permissions, ensuring consistency and simplifying access control logic.
 */
public final class NavigationRegistry {

	private static final List<NavigationItem> ALL_ITEMS = new ArrayList<>();

	// Defines all possible navigation links, their icons, and the permission
	// required to see them.
	// A null permission means the link is public to all authenticated users.
	static {
		// User Section
		ALL_ITEMS.add(new NavigationItem("Dashboard", "/home", "fa-home", null));
		ALL_ITEMS.add(new NavigationItem("Lehrgänge", "/lehrgaenge", "fa-graduation-cap", null));
		ALL_ITEMS.add(new NavigationItem("Veranstaltungen", "/veranstaltungen", "fa-calendar-check", null));
		ALL_ITEMS.add(new NavigationItem("Lager", "/lager", "fa-boxes", null));
		ALL_ITEMS.add(new NavigationItem("Dateien", "/dateien", "fa-folder-open", null));
		ALL_ITEMS.add(new NavigationItem("Kalender", "/kalender", "fa-calendar-alt", null));

		// Admin Section
		ALL_ITEMS.add(new NavigationItem("Admin Dashboard", "/admin/dashboard", "fa-tachometer-alt",
				"ADMIN_DASHBOARD_ACCESS"));
		ALL_ITEMS.add(new NavigationItem("Benutzer", "/admin/mitglieder", "fa-users-cog", "USER_READ"));
		ALL_ITEMS.add(new NavigationItem("Events", "/admin/veranstaltungen", "fa-calendar-plus", "EVENT_READ"));
		ALL_ITEMS.add(new NavigationItem("Lager", "/admin/lager", "fa-warehouse", "STORAGE_READ"));
		ALL_ITEMS.add(new NavigationItem("Dateien", "/admin/dateien", "fa-file-upload", "FILE_MANAGE"));
		ALL_ITEMS.add(new NavigationItem("Lehrgangs-Vorlagen", "/admin/lehrgaenge", "fa-book", "COURSE_READ"));
		ALL_ITEMS.add(new NavigationItem("Kit-Verwaltung", "/admin/kits", "fa-box-open", "KIT_READ"));
		ALL_ITEMS.add(new NavigationItem("Abzeichen", "/admin/achievements", "fa-award", "ACHIEVEMENT_VIEW"));
		ALL_ITEMS.add(new NavigationItem("Defekte Artikel", "/admin/defekte", "fa-wrench", "STORAGE_READ"));
		ALL_ITEMS.add(new NavigationItem("Quali-Matrix", "/admin/matrix", "fa-th-list", "QUALIFICATION_READ"));
		ALL_ITEMS.add(new NavigationItem("Berichte", "/admin/berichte", "fa-chart-pie", "REPORT_READ"));
		ALL_ITEMS.add(new NavigationItem("Aktions-Log", "/admin/log", "fa-clipboard-list", "LOG_READ"));
		ALL_ITEMS.add(new NavigationItem("System", "/admin/system", "fa-server", "SYSTEM_READ"));
	}

	private NavigationRegistry() {
		// Private constructor to prevent instantiation.
	}

	/**
	 * Builds a filtered list of navigation items based on the user's permissions.
	 *
	 * @param user The current user.
	 * @return A list of NavigationItem objects the user is allowed to see.
	 */
	public static List<NavigationItem> getNavigationItemsForUser(User user) {
		if (user == null || user.getPermissions() == null) {
			return new ArrayList<>();
		}

		final Set<String> userPermissions = user.getPermissions();

		return ALL_ITEMS.stream().filter(item -> {
			final String requiredPerm = item.getRequiredPermission();

			// Public items are always visible
			if (requiredPerm == null) {
				return true;
			}

			// Super-admins see all admin items
			if (userPermissions.contains("ACCESS_ADMIN_PANEL")) {
				return true;
			}

			// Special case: The "Admin Dashboard" link is visible if the user has *any*
			// admin-level access.
			if ("ADMIN_DASHBOARD_ACCESS".equals(requiredPerm)) {
				return user.hasAdminAccess();
			}

			// Special case: The "Abzeichen" link is visible if user can perform any
			// achievement action.
			if ("ACHIEVEMENT_VIEW".equals(requiredPerm)) {
				return userPermissions.contains("ACHIEVEMENT_CREATE") || userPermissions.contains("ACHIEVEMENT_UPDATE")
						|| userPermissions.contains("ACHIEVEMENT_DELETE");
			}

			// Standard permission check for all other items
			return userPermissions.contains(requiredPerm);
		}).collect(Collectors.toList());
	}
}