import { registerRootComponent } from 'expo';
import 'react-native-gesture-handler'; // keep at the very top

import App from './App';

// --- WIDGET REGISTRATION HAS BEEN REMOVED ---
// This file's only job is now to register the main app component.

// Main app entry
registerRootComponent(App);