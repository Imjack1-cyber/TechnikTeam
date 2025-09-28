import { registerRootComponent } from 'expo';
import 'react-native-gesture-handler'; // Should be at the top
import App from './App';
import UpcomingEventWidget from './src/widgets/UpcomingEventWidget';
import OpenTasksWidget from './src/widgets/OpenTasksWidget';
import AdminActionsWidget from './src/widgets/AdminActionsWidget';
import AnnouncementsWidget from './src/widgets/AnnouncementsWidget';


// registerRootComponent calls AppRegistry.registerComponent('main', () => App);
// It also ensures that whether you load the app in Expo Go or in a native build,
// the environment is set up appropriately
registerRootComponent(App);

// Register Widgets
if (Platform.OS !== 'web') {
    registerRootComponent(UpcomingEventWidget, 'UpcomingEventWidget');
    registerRootComponent(OpenTasksWidget, 'OpenTasksWidget');
    registerRootComponent(AdminActionsWidget, 'AdminActionsWidget');
    registerRootComponent(AnnouncementsWidget, 'AnnouncementsWidget');
}