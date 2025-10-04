
import { AppRegistry, Platform } from 'react-native';
import { registerRootComponent } from 'expo';
import 'react-native-gesture-handler'; // keep at the very top

import App from './App';
// Widget components are still imported so their code is part of the bundle,
// but they are no longer registered manually. The plugin handles this.
import './src/widgets/UpcomingEventWidget';
import './src/widgets/OpenTasksWidget';
import './src/widgets/AdminActionsWidget';
import './src/widgets/AnnouncementsWidget';

// Main app entry (required for Expo Go & builds)
registerRootComponent(App);
