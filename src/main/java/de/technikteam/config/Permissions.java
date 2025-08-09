package de.technikteam.config;

/**
 * A central repository for all permission key constants used throughout the
 * application. This prevents the use of "magic strings" for permission checks,
 * reducing typos and making the code more maintainable and readable.
 */
public final class Permissions {

	private Permissions() {
	}

	// --- GLOBAL ---
	public static final String ACCESS_ADMIN_PANEL = "ACCESS_ADMIN_PANEL";

	// --- USER MANAGEMENT ---
	public static final String USER_CREATE = "USER_CREATE";
	public static final String USER_READ = "USER_READ";
	public static final String USER_UPDATE = "USER_UPDATE";
	public static final String USER_DELETE = "USER_DELETE";
	public static final String USER_PASSWORD_RESET = "USER_PASSWORD_RESET";

	// --- EVENT MANAGEMENT ---
	public static final String EVENT_CREATE = "EVENT_CREATE";
	public static final String EVENT_READ = "EVENT_READ";
	public static final String EVENT_UPDATE = "EVENT_UPDATE";
	public static final String EVENT_DELETE = "EVENT_DELETE";
	public static final String EVENT_MANAGE_ASSIGNMENTS = "EVENT_MANAGE_ASSIGNMENTS";
	public static final String EVENT_MANAGE_TASKS = "EVENT_MANAGE_TASKS";
	public static final String EVENT_DEBRIEFING_VIEW = "EVENT_DEBRIEFING_VIEW";
	public static final String EVENT_DEBRIEFING_MANAGE = "EVENT_DEBRIEFING_MANAGE";

	// --- COURSE & MEETING MANAGEMENT ---
	public static final String COURSE_CREATE = "COURSE_CREATE";
	public static final String COURSE_READ = "COURSE_READ";
	public static final String COURSE_UPDATE = "COURSE_UPDATE";
	public static final String COURSE_DELETE = "COURSE_DELETE";

	// --- STORAGE & INVENTORY ---
	public static final String STORAGE_CREATE = "STORAGE_CREATE";
	public static final String STORAGE_READ = "STORAGE_READ";
	public static final String STORAGE_UPDATE = "STORAGE_UPDATE";
	public static final String STORAGE_DELETE = "STORAGE_DELETE";
	public static final String DAMAGE_REPORT_MANAGE = "DAMAGE_REPORT_MANAGE";

	// --- KIT MANAGEMENT ---
	public static final String KIT_CREATE = "KIT_CREATE";
	public static final String KIT_READ = "KIT_READ";
	public static final String KIT_UPDATE = "KIT_UPDATE";
	public static final String KIT_DELETE = "KIT_DELETE";

	// --- QUALIFICATIONS ---
	public static final String QUALIFICATION_READ = "QUALIFICATION_READ";
	public static final String QUALIFICATION_UPDATE = "QUALIFICATION_UPDATE";

	// --- FILE MANAGEMENT ---
	public static final String FILE_CREATE = "FILE_CREATE";
	public static final String FILE_READ = "FILE_READ";
	public static final String FILE_UPDATE = "FILE_UPDATE";
	public static final String FILE_DELETE = "FILE_DELETE";
	public static final String FILE_MANAGE = "FILE_MANAGE";

	// --- SYSTEM & REPORTING ---
	public static final String LOG_READ = "LOG_READ";
	public static final String REPORT_READ = "REPORT_READ";
	public static final String SYSTEM_READ = "SYSTEM_READ";

	// --- ACHIEVEMENTS ---
	public static final String ACHIEVEMENT_CREATE = "ACHIEVEMENT_CREATE";
	public static final String ACHIEVEMENT_UPDATE = "ACHIEVEMENT_UPDATE";
	public static final String ACHIEVEMENT_DELETE = "ACHIEVEMENT_DELETE";
	public static final String ACHIEVEMENT_VIEW = "ACHIEVEMENT_VIEW";

	// --- NOTIFICATIONS ---
	public static final String NOTIFICATION_SEND = "NOTIFICATION_SEND";

	// --- DOCUMENTATION ---
	public static final String DOCUMENTATION_MANAGE = "DOCUMENTATION_MANAGE";

	// --- NAVIGATION-SPECIFIC ---
	public static final String ADMIN_DASHBOARD_ACCESS = "ADMIN_DASHBOARD_ACCESS";
}