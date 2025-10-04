import { useEffect } from 'react';
import { AppState, Platform } from 'react-native';
import apiClient from '../services/apiClient';
import { useWidgetStore } from '../store/widgetStore';
import { useAuthStore } from '../store/authStore';

// Define a dummy update function for web to prevent crashes.
let updateAllWidgets = async () => {};

// On native platforms, dynamically import the actual update function.
if (Platform.OS !== 'web') {
    try {
        // This require() is evaluated at runtime and only on native.
        updateAllWidgets = require('@bittingz/expo-widgets').updateAllWidgets;
    } catch (e) {
        console.error("Could not load '@bittingz/expo-widgets'. Widgets will not function.", e);
    }
}

const useWidgetDataRefresher = () => {
    const setWidgetData = useWidgetStore(state => state.setWidgetData);
    const isAuthenticated = useAuthStore(state => state.isAuthenticated);

    const refreshData = async () => {
        // The function is now safe to call on any platform, but we still
        // only want to perform the logic on native.
        if (Platform.OS === 'web' || !isAuthenticated) {
            return;
        }

        console.log('[useWidgetDataRefresher] App became active. Refreshing widget data...');

        try {
            const result = await apiClient.get('/public/dashboard/widget-data');
            if (result.success) {
                console.log('[useWidgetDataRefresher] Fetched data:', result.data);
                // Update the persisted Zustand store
                setWidgetData({
                    nextEvent: result.data.nextEvent,
                    openTasks: result.data.openTasks,
                    latestAnnouncement: result.data.latestAnnouncement,
                    error: null,
                });
                // Trigger a native widget update
                await updateAllWidgets();
                console.log('[useWidgetDataRefresher] Successfully updated all widgets.');
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('[useWidgetDataRefresher] Failed to refresh widget data:', error);
            // Also persist the error state so widgets can display it
            setWidgetData({ error: error.message });
            await updateAllWidgets();
        }
    };

    useEffect(() => {
        if (Platform.OS === 'web') {
            return; // Exit early on web
        }

        // Run once on mount for native
        refreshData();

        // Subscribe to app state changes for native
        const subscription = AppState.addEventListener('change', nextAppState => {
            if (nextAppState === 'active') {
                refreshData();
            }
        });

        return () => {
            subscription.remove();
        };
    }, [isAuthenticated]); // Rerun setup if authentication state changes

    // This hook is for side-effects only
    return null;
};

export default useWidgetDataRefresher;
