package de.technikteam.util;

import de.technikteam.config.Permissions;
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
	static {
		// User Section
		ALL_ITEMS.add(new NavigationItem("Dashboard", "/home", "fa-home", null));
		ALL_ITEMS.add(new NavigationItem("Anschlagbrett", "/bulletin-board", "fa-thumbtack", null));
		ALL_ITEMS.add(new NavigationItem("Benachrichtigungen", "/notifications", "fa-bell", null));
		ALL_ITEMS.add(new NavigationItem("Team", "/team", "fa-users", null));
		ALL_ITEMS.add(new NavigationItem("Chat", "/chat", "fa-comments", null));
		ALL_ITEMS.add(new NavigationItem("Lehrgänge", "/lehrgaenge", "fa-graduation-cap", null));
		ALL_ITEMS.add(new NavigationItem("Veranstaltungen", "/veranstaltungen", "fa-calendar-check", null));
		ALL_ITEMS.add(new NavigationItem("Lager", "/lager", "fa-boxes", null));
		ALL_ITEMS.add(new NavigationItem("Dateien", "/dateien", "fa-folder-open", null));
		ALL_ITEMS.add(new NavigationItem("Kalender", "/kalender", "fa-calendar-alt", null));
		ALL_ITEMS.add(new NavigationItem("Feedback", "/feedback", "fa-lightbulb", null));
		ALL_ITEMS.add(new NavigationItem("Changelogs", "/changelogs", "fa-history", null));

		// Admin Section
		ALL_ITEMS.add(new NavigationItem("Admin Dashboard", "/admin/dashboard", "fa-tachometer-alt",
				Permissions.ADMIN_DASHBOARD_ACCESS));
		ALL_ITEMS.add(new NavigationItem("Benutzer", "/admin/mitglieder", "fa-users-cog", Permissions.USER_READ));
		ALL_ITEMS.add(
				new NavigationItem("Events", "/admin/veranstaltungen", "fa-calendar-plus", Permissions.EVENT_READ));
		ALL_ITEMS.add(new NavigationItem("Lager", "/admin/lager", "fa-warehouse", Permissions.STORAGE_READ));
		ALL_ITEMS
				.add(new NavigationItem("Lehrgänge & Skills", "/admin/lehrgaenge", "fa-book", Permissions.COURSE_READ));
		ALL_ITEMS.add(new NavigationItem("Inhalte & System", "/admin/content", "fa-desktop", Permissions.FILE_MANAGE));
		ALL_ITEMS.add(new NavigationItem("Berichte & Logs", "/admin/reports", "fa-chart-pie", Permissions.REPORT_READ));
		ALL_ITEMS.add(
				new NavigationItem("Technische Wiki", "/admin/wiki", "fa-book-reader", Permissions.ACCESS_ADMIN_PANEL));
		ALL_ITEMS.add(new NavigationItem("API Docs", "/swagger-ui.html", "fa-code", Permissions.ACCESS_ADMIN_PANEL));
	}

	private NavigationRegistry() {
	}

	/**
	 * Builds a filtered list of navigation items based on the user's permissions.
	 *
	 * @param user The current user.
	 * @return A list of NavigationItem objects the user is allowed to see.
	 */
	public static List<NavigationItem> getNavigationItemsForUser(User user) {
		if (user == null) {
			return new ArrayList<>();
		}

		final boolean isAdmin = user.hasAdminAccess();
		final Set<String> userPermissions = user.getPermissions() != null ? user.getPermissions() : Set.of();

		return ALL_ITEMS.stream().filter(item -> {
			final String requiredPerm = item.getRequiredPermission();

			// Public items (no permission required) are always visible to any authenticated
			// user.
			if (requiredPerm == null) {
				return true;
			}

			// For admin links, check if the user is an admin or has the specific permission
			if (isAdmin) {
				return true;
			}

			return userPermissions.contains(requiredPerm);
		}).collect(Collectors.toList());
	}
}