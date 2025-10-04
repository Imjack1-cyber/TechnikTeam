import { AppRegistry, Platform } from 'react-native';
import { registerRootComponent } from 'expo';
import 'react-native-gesture-handler'; // keep at the very top

import App from './App';

// Main app entry (required for Expo Go & builds)
// Widgets are now automatically discovered and registered by the @bittingz/expo-widgets plugin
// via the configuration in app.config.js. No manual registration is needed here.
registerRootComponent(App);