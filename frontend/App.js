import React, { useState, useEffect } from 'react';
import { useAuthStore } from './src/store/authStore';
import { ToastProvider } from './src/context/ToastContext';
import ToastContainer from './src/components/ui/ToastContainer';
import WarningNotification from './src/components/ui/WarningNotification';
import { useNotifications } from './src/hooks/useNotifications';
import { usePushNotifications } from './src/hooks/usePushNotifications';
import RootNavigator from './src/router';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import apiClient from './src/services/apiClient';
import { getToken } from './src/lib/storage';
import SplashScreen from './src/components/common/SplashScreen';
import { NavigationContainer } from '@react-navigation/native';
import { navigationRef } from './src/router/navigation';
import DownloadsIndicator from './src/components/ui/DownloadsIndicator';
import MaintenanceBanner from './src/components/ui/MaintenanceBanner';

// This is the crucial linking configuration for React Navigation on the web.
const linking = {
  prefixes: [
    'https://technikteam.duckdns.org', 
    'https://technikteamdev.duckdns.org',
    'https://technikteam.qs0.de', 
    'https://technikteamdev.qs0.de'
  ], // Add app domains for deep linking
  config: {
    screens: {
        // Public, unauthenticated screens
        Verification: 'verify/:token',
        FileShare: 'share/:token',
        SchedulingPoll: 'poll/:uuid',

        // Screens outside the drawer/main authenticated stack
        Login: 'login',
        Maintenance: 'maintenance',
        // Main App Screens are now nested under the 'App' route
        App: {
            screens: {
                MainDrawer: {
                    path: '', // The drawer itself doesn't add to the path
                    screens: {
                        Dashboard: 'home',
                        Anschlagbrett: 'bulletin-board',
                        Benachrichtigungen: 'notifications',
                        Team: 'team',
                        Chat: 'chat/:conversationId?',
                        Lehrgänge: 'lehrgaenge',
                        Veranstaltungen: {
                            path: 'veranstaltungen',
                            screens: {
                                EventsList: '',
                                EventDetails: 'details/:eventId'
                            }
                        },
                        Lager: 'lager',
                        Dateien: 'dateien',
                        Kalender: 'kalender',
                        Feedback: 'feedback',
                        Changelogs: 'changelogs',

                        // Admin Screens are nested stacks now
                        "Admin Dashboard": {
                            path: 'admin/dashboard',
                            screens: {
                                AdminDashboardPage: ''
                            }
                        },
                        "Benutzer & Anträge": {
                            path: 'admin/users',
                            screens: {
                                AdminUsersIndex: '',
                                AdminUsers: 'manage',
                                AdminRequests: 'requests',
                                AdminTrainingRequests: 'training-requests',
                                AdminAchievements: 'achievements'
                            }
                        },
                        "Event Management": {
                            path: 'admin/events',
                            screens: {
                                AdminEventsIndex: '',
                                AdminEvents: 'manage',
                                AdminDebriefingsList: 'debriefings',
                                AdminEventRoles: 'roles',
                                AdminVenues: 'venues',
                                AdminChecklistTemplates: 'checklist-templates',
                                AdminEventDebriefing: 'debriefing/:eventId'
                            }
                        },
                        "Lager & Material": {
                            path: 'admin/storage',
                            screens: {
                                AdminStorageIndex: '',
                                AdminStorage: 'manage',
                                AdminKits: 'kits',
                                AdminDefective: 'defective',
                                AdminDamageReports: 'damage-reports'
                            }
                        },
                         "Lehrgänge & Skills": {
                            path: 'admin/courses',
                            screens: {
                                AdminCoursesIndex: '',
                                AdminCourses: 'manage',
                                AdminMeetings: 'meetings/:courseId',
                                AdminMatrix: 'matrix'
                            }
                        },
                        "Inhalte & Kommunikation": {
                             path: 'admin/content',
                             screens: {
                                 AdminContentIndex: '',
                                 AdminAnnouncements: 'announcements',
                                 AdminFiles: 'files',
                                 AdminFileEditor: 'files/edit/:fileId',
                                 AdminFeedback: 'feedback',
                                 AdminChangelogs: 'changelogs',
                                 AdminDocumentation: 'documentation',
                                 AdminNotifications: 'notifications'
                             }
                        },
                        "Berichte": {
                            path: 'admin/reports',
                            screens: {
                                AdminReportsIndex: '',
                                AdminReports: 'overview',
                                AdminLog: 'log'
                            }
                        },
                        "System & Entwicklung": {
                            path: 'admin/system',
                            screens: {
                                AdminSystemIndex: '',
                                AdminSystemPage: 'status',
                                AdminAuthLog: 'auth-log',
                                AdminGeoIp: 'geoip',
                                AdminWiki: 'wiki'
                            }
                        }
                    },
                },
                // Screens accessible from within the stack but not directly in the drawer
                Profile: 'profil',
                Settings: 'profil/einstellungen',
                PasswordChange: 'passwort',
                MeetingDetails: 'lehrgaenge/details/:meetingId',
                StorageItemDetails: 'lager/details/:itemId',
                UserProfile: 'team/:userId',
                EventFeedback: 'feedback/event/:eventId',
                PackKit: 'pack-kit/:kitId',
                QrAction: 'lager/qr-aktion/:itemId',
                FileEditor: 'files/edit/:fileId',
                Search: 'suche',
                HelpList: 'help',
                HelpDetails: 'help/:pageKey',
                Forbidden: 'forbidden',
                IdCard: 'profil/id-card',
                NotFound: '*', // Catch-all for 404
            }
        }
    },
  },
};


const AppContent = () => {
    const { warningNotification, dismissWarning } = useNotifications();
    usePushNotifications(); // Initialize push notification handling
    return (
        <>
            <MaintenanceBanner />
            <RootNavigator />
            <ToastContainer />
            <DownloadsIndicator />
            {warningNotification && <WarningNotification notification={warningNotification} onDismiss={dismissWarning} />}
        </>
    );
};

export default function App() {
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const initializeApp = async () => {
            apiClient.setup({
                onUnauthorized: async () => {
                    await useAuthStore.getState().logout();
                },
                onMaintenance: () => {
                    if (navigationRef.isReady()) {
                        navigationRef.navigate('Maintenance');
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
        <SafeAreaProvider>
            <ToastProvider>
                <NavigationContainer ref={navigationRef} linking={linking} fallback={<SplashScreen />}>
                    <AppContent />
                </NavigationContainer>
            </ToastProvider>
        </SafeAreaProvider>
	);
}