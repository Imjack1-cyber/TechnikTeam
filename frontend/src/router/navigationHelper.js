import { navigationRef } from './navigation';

/**
 * A centralized navigation utility to parse backend URL paths and navigate
 * to the appropriate React Navigation screen.
 * @param {string} url The URL path from the backend (e.g., "/veranstaltungen/details/123").
 */
export function navigateFromUrl(url) {
  if (!url || typeof url !== 'string' || !navigationRef.isReady()) {
    return;
  }

  console.log(`Attempting to navigate from URL: ${url}`);
  let match;

  // --- Parameterized User Routes ---
  match = url.match(/^\/veranstaltungen\/details\/(\d+)$/);
  if (match) {
    navigationRef.navigate('Veranstaltungen', { screen: 'EventDetails', params: { eventId: match[1] } });
    return;
  }
  
  match = url.match(/^\/lehrgaenge\/details\/(\d+)$/);
  if (match) {
    navigationRef.navigate('MeetingDetails', { meetingId: match[1] });
    return;
  }
  
  match = url.match(/^\/lager\/details\/(\d+)$/);
  if (match) {
    navigationRef.navigate('StorageItemDetails', { itemId: match[1] });
    return;
  }
  
  match = url.match(/^\/team\/(\d+)$/);
  if (match) {
    navigationRef.navigate('UserProfile', { userId: match[1] });
    return;
  }

  // --- Parameterized Admin Routes ---
  match = url.match(/^\/admin\/events\/debriefing\/(\d+)$/);
  if (match) {
    navigationRef.navigate('Event Management', { screen: 'AdminEventDebriefing', params: { eventId: match[1] } });
    return;
  }

  match = url.match(/^\/admin\/courses\/meetings\/(\d+)$/);
  if (match) {
    navigationRef.navigate('Lehrgänge & Skills', { screen: 'AdminMeetings', params: { courseId: match[1] } });
    return;
  }
  
  // --- Parameterized search route ---
  match = url.match(/^\/suche\?q=(.+)$/);
  if (match) {
    navigationRef.navigate('Search', { q: decodeURIComponent(match[1]) });
    return;
  }

  // --- Static Routes (User and Admin) ---
  const staticRoutes = {
    '/home': () => navigationRef.navigate('Dashboard'),
    '/bulletin-board': () => navigationRef.navigate('Anschlagbrett'),
    '/notifications': () => navigationRef.navigate('Benachrichtigungen'),
    '/team': () => navigationRef.navigate('Team'),
    '/chat': () => navigationRef.navigate('Chat'),
    '/lehrgaenge': () => navigationRef.navigate('Lehrgänge'),
    '/veranstaltungen': () => navigationRef.navigate('Veranstaltungen'),
    '/lager': () => navigationRef.navigate('Lager'),
    '/dateien': () => navigationRef.navigate('Dateien'),
    '/kalender': () => navigationRef.navigate('Kalender'),
    '/feedback': () => navigationRef.navigate('Feedback'),
    '/changelogs': () => navigationRef.navigate('Changelogs'),
    '/profil': () => navigationRef.navigate('Profile'),
    // Admin
    '/admin/dashboard': () => navigationRef.navigate('Admin Dashboard'),
    '/admin/users/manage': () => navigationRef.navigate('Benutzer & Anträge', { screen: 'AdminUsers' }),
    '/admin/users/requests': () => navigationRef.navigate('Benutzer & Anträge', { screen: 'AdminRequests' }),
    '/admin/events/manage': () => navigationRef.navigate('Event Management', { screen: 'AdminEvents' }),
    '/admin/storage/manage': () => navigationRef.navigate('Lager & Material', { screen: 'AdminStorage' }),
    '/admin/courses/manage': () => navigationRef.navigate('Lehrgänge & Skills', { screen: 'AdminCourses' }),
    '/admin/courses/matrix': () => navigationRef.navigate('Lehrgänge & Skills', { screen: 'AdminMatrix' }),
    '/admin/reports/overview': () => navigationRef.navigate('Berichte', { screen: 'AdminReports' }),
    '/admin/reports/log': () => navigationRef.navigate('Berichte', { screen: 'AdminLog' }),
    '/admin/system/status': () => navigationRef.navigate('System & Entwicklung', { screen: 'AdminSystemPage' }),
    '/admin/system/auth-log': () => navigationRef.navigate('System & Entwicklung', { screen: 'AdminAuthLog' }),
  };

  const action = staticRoutes[url];
  if (action) {
    action();
    return;
  }

  console.warn(`No navigation route found for URL: ${url}`);
  // As a final fallback, navigate to the dashboard.
  navigationRef.navigate('Dashboard');
}