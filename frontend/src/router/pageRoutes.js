// This file maps static frontend routes to their corresponding documentation page keys.
// It's used by the AppLayout to show the contextual help button.

const pageRoutes = {
	// User Pages
	'/home': 'dashboard',
	'/team': 'team_directory',
	'/chat': 'chat',
	'/lehrgaenge': 'lehrgaenge',
	'/veranstaltungen': 'events',
	'/lager': 'storage',
	'/dateien': 'files',
	'/kalender': 'calendar',
	'/feedback': 'feedback',
	'/changelogs': 'changelogs',
	'/profil': 'profile',
	'/profil/einstellungen': 'settings',
	'/passwort': 'password_change',
	'/suche': 'search_results',
	'/help': 'help_list',

	// Admin Pages
	'/admin/dashboard': 'admin_dashboard',
	'/admin/announcements': 'admin_announcements',
	'/admin/mitglieder': 'admin_users',
	'/admin/mitglieder/requests': 'admin_requests',
	'/admin/mitglieder/training-requests': 'admin_training_requests',
	'/admin/veranstaltungen': 'admin_events',
	'/admin/veranstaltungen/debriefings': 'admin_debriefings_list',
	'/admin/veranstaltungen/roles': 'admin_event_roles',
	'/admin/veranstaltungen/venues': 'admin_venues',
	'/admin/veranstaltungen/checklist-templates': 'admin_checklist_templates',
	'/admin/lehrgaenge': 'admin_courses',
	'/admin/lehrgaenge/matrix': 'admin_matrix',
	'/admin/lager': 'admin_storage',
	'/admin/lager/kits': 'admin_kits',
	'/admin/lager/defekte': 'admin_defective_items',
	'/admin/lager/damage-reports': 'admin_damage_reports',
	'/admin/content': 'admin_content_index',
	'/admin/content/dateien': 'admin_files',
	'/admin/content/feedback': 'admin_feedback',
	'/admin/content/changelogs': 'admin_changelogs',
	'/admin/content/documentation': 'admin_documentation',
	'/admin/reports': 'admin_reports',
	'/admin/reports/log': 'admin_log',
	'/admin/reports/system': 'admin_system',
	'/admin/benachrichtigungen': 'admin_notifications',
	'/admin/achievements': 'admin_achievements',
	'/admin/wiki': 'admin_wiki',
};

export default pageRoutes;