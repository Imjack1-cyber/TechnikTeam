import React, { useState, useEffect } from 'react';
import { useAuthStore } from './store/authStore';
import { ToastProvider } from './context/ToastContext';
import ToastContainer from './components/ui/ToastContainer';
import WarningNotification from './components/ui/WarningNotification';
import { useNotifications } from './hooks/useNotifications';
import RootNavigator from './router';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import apiClient from './services/apiClient';
import { getToken } from './lib/storage';
import SplashScreen from './components/common/SplashScreen';
import { NavigationContainer } from '@react-navigation/native';
import { navigationRef } from './router/navigation';

const AppContent = () => {
    const { warningNotification, dismissWarning } = useNotifications();
    return (
        <SafeAreaProvider>
            <NavigationContainer ref={navigationRef}>
                <RootNavigator />
            </NavigationContainer>
            <ToastContainer />
            {warningNotification && <WarningNotification notification={warningNotification} onDismiss={dismissWarning} />}
        </SafeAreaProvider>
    );
};

const App = () => {
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const initializeApp = async () => {
            apiClient.setup({
                onUnauthorized: useAuthStore.getState().logout,
                onMaintenance: () => {
                    if (navigationRef.current) {
                        navigationRef.current.navigate('Maintenance');
                    }
                }
            });

            const token = await getToken();
            if (token) {
                apiClient.setAuthToken(token);
                try {
                    await useAuthStore.getState().fetchUserSession();
                } catch (error) {
                    console.log("Session token from storage is invalid. Clearing.");
                }
            }
            setIsLoading(false);
        };

        initializeApp();
    }, []);

    if (isLoading) {
        return <SplashScreen />;
    }

	return (
        <ToastProvider>
		    <AppContent />
        </ToastProvider>
	);
};

export default App;