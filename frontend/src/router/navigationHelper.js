import { navigationRef } from './navigation';

/**

    A centralized navigation utility to parse backend URL paths and navigate

    to the appropriate React Navigation screen.

    @param {string} url The URL path from the backend (e.g., "/veranstaltungen/details/123").
    */
export function navigateFromUrl(url) {
  if (!url || typeof url !== 'string' || !navigationRef.isReady()) {
    return;
  }

  console.log('Attempting to navigate from URL: ${url}');

  const navigate = (routeName, params) => navigationRef.navigate(routeName, params);

  // Match /path/details/:id
  let match = url.match('/^/(\w+)/details/(\d+)$/');
  if (match) {
    const resource = match[1];
    const id = match[2];
    switch (resource) {
      case 'veranstaltungen':
        return navigate('Veranstaltungen', { screen: 'EventDetails', params: { eventId: id } });
      case 'lehrgaenge':
        return navigate('MeetingDetails', { meetingId: id });
      case 'lager':
        return navigate('StorageItemDetails', { itemId: id });
      default:
        break;
    }
  }

  // Match /chat/:id
  match = url.match('/^/chat/(\d+)$/');
  if (match) {
    const conversationId = match[1];
    return navigate('Chat', { screen: 'MessageView', params: { conversationId } });
  }

  // Match simple top-level routes
  switch (url) {
    case '/profil':
      return navigate('Profile');
    case '/home':
      return navigate('Dashboard');
    case '/bulletin-board':
      return navigate('Anschlagbrett');
    case '/notifications':
      return navigate('Benachrichtigungen');
    case '/team':
      return navigate('Team');
    case '/chat':
      return navigate('Chat');
    case '/lehrgaenge':
      return navigate('Lehrgänge');
    case '/veranstaltungen':
      return navigate('Veranstaltungen');
    case '/lager':
      return navigate('Lager');
    case '/dateien':
      return navigate('Dateien');
    case '/kalender':
      return navigate('Kalender');
    case '/feedback':
      return navigate('Feedback');
    case '/changelogs':
      return navigate('Changelogs');
  }

  console.warn('No navigation route found for URL: ${url}');
}