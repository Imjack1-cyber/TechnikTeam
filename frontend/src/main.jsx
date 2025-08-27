import { AppRegistry } from 'react-native';
import App from './App';
import { name as appName } from '../app.json';
import { useAuthStore } from './store/authStore';
import apiClient from './services/apiClient';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { NavigationContainerRef } from '@react-navigation/native';
import * as React from 'react';

// This is a conceptual migration. The actual logic runs inside App.js or a custom bootstrap file.
// In React Native, there isn't a pre-render async function like this.
// Instead, this logic would be placed in a loading screen component or in the App.js useEffect hook.

export const navigationRef = React.createRef();

const initializeApp = async () => {
    // Inject the logout and maintenance navigation callbacks into the apiClient
    apiClient.setup({ 
        onUnauthorized: useAuthStore.getState().logout,
        onMaintenance: () => {
            if (navigationRef.current) {
                navigationRef.current.navigate('Maintenance');
            }
        }
    });

    const token = await AsyncStorage.getItem('technikteam-auth-token');
	if (token) {
		apiClient.setAuthToken(token);
		try {
			await useAuthStore.getState().fetchUserSession();
		} catch (error) {
			console.log("Session token from storage is invalid. Clearing.");
		}
	} else {
		console.log("No auth token found in storage.");
	}
};

// Run initialization logic before registering the main component
initializeApp();

AppRegistry.registerComponent(appName, () => App);