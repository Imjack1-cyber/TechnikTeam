import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createDrawerNavigator } from '@react-navigation/drawer';

import { useAuthStore } from '../store/authStore';

// Custom Sidebar Component
import Sidebar from '../components/layout/Sidebar';

// Screens
import LoginPage from '../pages/LoginPage';
import DashboardPage from '../pages/DashboardPage';
import AnnouncementsPage from '../pages/AnnouncementsPage';
import NotificationsPage from '../pages/NotificationsPage';
import TeamDirectoryPage from '../pages/TeamDirectoryPage';
import UserProfilePage from '../pages/UserProfilePage';
import ChatPage from '../pages/ChatPage';
import LehrgaengePage from '../pages/LehrgaengePage';
import MeetingDetailsPage from '../pages/MeetingDetailsPage';
import EventsPage from '../pages/EventsPage';
import EventDetailsPage from '../pages/EventDetailsPage';
import StoragePage from '../pages/StoragePage';
import StorageItemDetailsPage from '../pages/StorageItemDetailsPage';
import FilesPage from '../pages/FilesPage';
import CalendarPage from '../pages/CalendarPage';
import FeedbackPage from '../pages/FeedbackPage';
import ChangelogPage from '../pages/ChangelogPage';
import ProfilePage from '../pages/ProfilePage';
import SettingsPage from '../pages/SettingsPage';
import PasswordPage from '../pages/PasswordPage';
import SearchResultsPage from '../pages/SearchResultsPage';
import HelpListPage from '../pages/HelpListPage';
import HelpDetailsPage from '../pages/HelpDetailsPage';

// Admin Screens (Grouped)
import AdminDashboardPage from '../pages/admin/AdminDashboardPage';
import AdminUsersIndex from '../pages/admin/AdminUsersIndex';
import AdminEventsIndex from '../pages/admin/AdminEventsIndex';
// ... import all other admin screens

// Minimal Layout Screens
import PackKitPage from '../pages/PackKitPage';
import QrActionPage from '../pages/QrActionPage';

// Error Screens
import ForbiddenPage from '../pages/error/ForbiddenPage';
import MaintenancePage from '../pages/error/MaintenancePage';
import NotFoundPage from '../pages/error/NotFoundPage';


const Drawer = createDrawerNavigator();
const Stack = createStackNavigator();

const MainDrawerNavigator = () => {
    return (
        <Drawer.Navigator
            drawerContent={(props) => <Sidebar {...props} />}
            screenOptions={{ headerShown: false }} // Custom header is used inside pages
        >
            <Drawer.Screen name="Dashboard" component={DashboardPage} />
            <Drawer.Screen name="Anschlagbrett" component={AnnouncementsPage} />
            <Drawer.Screen name="Benachrichtigungen" component={NotificationsPage} />
            <Drawer.Screen name="Team" component={TeamDirectoryPage} />
            <Drawer.Screen name="Chat" component={ChatPage} />
            <Drawer.Screen name="Lehrgänge" component={LehrgaengePage} />
            <Drawer.Screen name="Veranstaltungen" component={EventsPage} />
            <Drawer.Screen name="Lager" component={StoragePage} />
            <Drawer.Screen name="Dateien" component={FilesPage} />
            <Drawer.Screen name="Kalender" component={CalendarPage} />
            <Drawer.Screen name="Feedback" component={FeedbackPage} />
            <Drawer.Screen name="Changelogs" component={ChangelogPage} />
            
            {/* Admin Screens would be added here conditionally based on user role */}
            <Drawer.Screen name="Admin Dashboard" component={AdminDashboardPage} />
            <Drawer.Screen name="Benutzer & Anträge" component={AdminUsersIndex} />
            <Drawer.Screen name="Event Management" component={AdminEventsIndex} />
            {/* ... other admin index screens */}
        </Drawer.Navigator>
    );
};

const AppStack = () => {
    return (
        <Stack.Navigator screenOptions={{ headerShown: false }}>
            <Stack.Screen name="MainDrawer" component={MainDrawerNavigator} />
            {/* Detail screens that should not have the drawer */}
            <Stack.Screen name="UserProfile" component={UserProfilePage} />
            <Stack.Screen name="MeetingDetails" component={MeetingDetailsPage} />
            <Stack.Screen name="EventDetails" component={EventDetailsPage} />
            <Stack.Screen name="StorageItemDetails" component={StorageItemDetailsPage} />
            <Stack.Screen name="Profile" component={ProfilePage} />
            <Stack.Screen name="Settings" component={SettingsPage} />
            <Stack.Screen name="PasswordChange" component={PasswordPage} />
            <Stack.Screen name="Search" component={SearchResultsPage} />
            <Stack.Screen name="HelpList" component={HelpListPage} />
            <Stack.Screen name="HelpDetails" component={HelpDetailsPage} />
            
            {/* Admin Detail Screens */}
            {/* e.g., <Stack.Screen name="AdminUsers" component={AdminUsersPage} /> */}
            
            {/* Minimal Layout Screens */}
            <Stack.Screen name="PackKit" component={PackKitPage} />
            <Stack.Screen name="QrAction" component={QrActionPage} />
        </Stack.Navigator>
    );
};

const AuthStack = () => {
    return (
        <Stack.Navigator screenOptions={{ headerShown: false }}>
            <Stack.Screen name="Login" component={LoginPage} />
        </Stack.Navigator>
    );
};

const RootNavigator = () => {
	const { isAuthenticated, maintenanceStatus, isAdmin } = useAuthStore();

    if (maintenanceStatus.mode === 'HARD' && !isAdmin) {
        return (
            <Stack.Navigator screenOptions={{ headerShown: false }}>
                <Stack.Screen name="Maintenance" component={MaintenancePage} />
                 <Stack.Screen name="Login" component={LoginPage} />
            </Stack.Navigator>
        );
    }

	return (
		<NavigationContainer>
			{isAuthenticated ? <AppStack /> : <AuthStack />}
		</NavigationContainer>
	);
};

export default RootNavigator;