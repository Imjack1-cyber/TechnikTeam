import { useEffect } from 'react';
import { AppState, Platform, NativeModules } from 'react-native';
import apiClient from '../services/apiClient';
import { useWidgetStore } from '../store/widgetStore';
import { useAuthStore } from '../store/authStore';

// Get a reference to our custom native module
const { SharedDataModule } = NativeModules;

const useWidgetDataRefresher = () => {
    const setWidgetData = useWidgetStore(state => state.setWidgetData);
    const isAuthenticated = useAuthStore(state => state.isAuthenticated);

    const refreshData = async () => {
        if (Platform.OS === 'web' || !isAuthenticated) {
            return;
        }

        console.log('[WidgetRefresher] App state is active. Starting widget data refresh...');

        try {
            const result = await apiClient.get('/public/dashboard/widget-data');
            if (result.success && result.data) {
                const dataToStore = {
                    nextEvent: result.data.nextEvent,
                    openTasks: result.data.openTasks,
                    latestAnnouncement: result.data.latestAnnouncement,
                    error: null,
                    lastUpdated: new Date().toISOString(),
                };
                
                // Persist data in the Zustand store (for the app)
                setWidgetData(dataToStore);
                
                // Send the data to the native side via our custom module
                if (SharedDataModule) {
                    const jsonData = JSON.stringify(dataToStore);
                    SharedDataModule.setData(jsonData);
                    console.log('[WidgetRefresher] Successfully sent data to native SharedPreferences.');
                }
            } else {
                throw new Error(result.message || "API call was not successful or returned no data.");
            }
        } catch (error) {
            console.error('[WidgetRefresher] Failed to refresh widget data:', error);
            const errorData = { 
                nextEvent: null, openTasks: [], latestAnnouncement: null,
                error: error.message,
                lastUpdated: new Date().toISOString(),
            };
            
            // Persist error state
            setWidgetData(errorData);

            // Also send error state to native side so widgets can display an error
            if (SharedDataModule) {
                SharedDataModule.setData(JSON.stringify(errorData));
            }
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