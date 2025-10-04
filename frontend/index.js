import { AppRegistry, Platform } from 'react-native';
import { registerRootComponent } from 'expo';
import 'react-native-gesture-handler'; // keep at the very top

import App from './App';
import UpcomingEventWidget from './src/widgets/UpcomingEventWidget';
import OpenTasksWidget from './src/widgets/OpenTasksWidget';
import AdminActionsWidget from './src/widgets/AdminActionsWidget';
import AnnouncementsWidget from './src/widgets/AnnouncementsWidget';

// Main app entry (required for Expo Go & builds)
registerRootComponent(App);

// Only register extra widgets on native platforms
if (Platform.OS !== 'web') {
  AppRegistry.registerComponent('UpcomingEventWidget', () => UpcomingEventWidget);
  AppRegistry.registerComponent('OpenTasksWidget', () => OpenTasksWidget);
  AppRegistry.registerComponent('AdminActionsWidget', () => AdminActionsWidget);
  AppRegistry.registerComponent('AnnouncementsWidget', () => AnnouncementsWidget);
}