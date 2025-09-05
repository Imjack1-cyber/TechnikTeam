import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { useAuthStore } from '../store/authStore';

// Custom Components
import Sidebar from '../components/layout/Sidebar';
import Header from '../components/layout/Header';
import ErrorBoundary from '../components/common/ErrorBoundary';

// --- Import ALL Screen Components ---
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
import FileEditorPage from '../pages/files/FileEditorPage';
import CalendarPage from '../pages/CalendarPage';
import FeedbackPage from '../pages/FeedbackPage';
import EventFeedbackPage from '../pages/EventFeedbackPage';
import ChangelogPage from '../pages/ChangelogPage';
import ProfilePage from '../pages/ProfilePage';
import SettingsPage from '../pages/SettingsPage';
import PasswordPage from '../pages/PasswordPage';
import SearchResultsPage from '../pages/SearchResultsPage';
import HelpListPage from '../pages/HelpListPage';
import HelpDetailsPage from '../pages/HelpDetailsPage';
import PackKitPage from '../pages/PackKitPage';
import QrActionPage from '../pages/QrActionPage';
import ForbiddenPage from '../pages/error/ForbiddenPage';
import MaintenancePage from '../pages/error/MaintenancePage';
import NotFoundPage from '../pages/error/NotFoundPage';

// --- Import ALL Admin Screen Components ---
import AdminDashboardPage from '../pages/admin/AdminDashboardPage';
import AdminUsersIndex from '../pages/admin/AdminUsersIndex';
import AdminUsersPage from '../pages/admin/AdminUsersPage';
import AdminRequestsPage from '../pages/admin/AdminRequestsPage';
import AdminTrainingRequestsPage from '../pages/admin/AdminTrainingRequestsPage';
import AdminAchievementsPage from '../pages/admin/AdminAchievementsPage';
import AdminEventsIndex from '../pages/admin/AdminEventsIndex';
import AdminEventsPage from '../pages/admin/AdminEventsPage';
import AdminDebriefingsListPage from '../pages/admin/AdminDebriefingsListPage';
import AdminEventDebriefingPage from '../pages/admin/AdminEventDebriefingPage';
import AdminEventRolesPage from '../pages/admin/AdminEventRolesPage';
import AdminVenuesPage from '../pages/admin/AdminVenuesPage';
import AdminChecklistTemplatesPage from '../pages/admin/AdminChecklistTemplatesPage';
import AdminCoursesIndex from '../pages/admin/AdminCoursesIndex';
import AdminCoursesPage from '../pages/admin/AdminCoursesPage';
import AdminMeetingsPage from '../pages/admin/AdminMeetingsPage';
import AdminMatrixPage from '../pages/admin/AdminMatrixPage';
import AdminStorageIndex from '../pages/admin/AdminStorageIndex';
import AdminStoragePage from '../pages/admin/AdminStoragePage';
import AdminKitsPage from '../pages/admin/AdminKitsPage';
import AdminDefectivePage from '../pages/admin/AdminDefectivePage';
import AdminDamageReportsPage from '../pages/admin/AdminDamageReportsPage';
import AdminContentIndex from '../pages/admin/AdminContentIndex';
import AdminAnnouncementsPage from '../pages/admin/AdminAnnouncementsPage';
import AdminFilesPage from '../pages/admin/AdminFilesPage';
import AdminFileEditorPage from '../pages/admin/AdminFileEditorPage';
import AdminFeedbackPage from '../pages/admin/AdminFeedbackPage';
import AdminChangelogPage from '../pages/admin/AdminChangelogPage';
import AdminDocumentationPage from '../pages/admin/AdminDocumentationPage';
import AdminNotificationsPage from '../pages/admin/AdminNotificationsPage';
import AdminReportsIndex from '../pages/admin/AdminReportsIndex';
import AdminReportsPage from '../pages/admin/AdminReportsPage';
import AdminLogPage from '../pages/admin/AdminLogPage';
import AdminSystemIndex from '../pages/admin/AdminSystemIndex';
import AdminSystemPage from '../pages/admin/AdminSystemPage';
import AdminAuthLogPage from '../pages/admin/AdminAuthLogPage';
import AdminGeoIpPage from '../pages/admin/AdminGeoIpPage';
import AdminWikiPage from '../pages/admin/AdminWikiPage';


const Drawer = createDrawerNavigator();
const Stack = createStackNavigator();

// --- Admin Stacks ---
const AdminUsersStack = () => (
    <Stack.Navigator><Stack.Screen name="AdminUsersIndex" component={AdminUsersIndex} options={{ headerShown: false }} /><Stack.Screen name="AdminUsers" component={AdminUsersPage} /><Stack.Screen name="AdminRequests" component={AdminRequestsPage} /><Stack.Screen name="AdminTrainingRequests" component={AdminTrainingRequestsPage} /><Stack.Screen name="AdminAchievements" component={AdminAchievementsPage} /></Stack.Navigator>
);
const AdminEventsStack = () => (
    <Stack.Navigator><Stack.Screen name="AdminEventsIndex" component={AdminEventsIndex} options={{ headerShown: false }} /><Stack.Screen name="AdminEvents" component={AdminEventsPage} /><Stack.Screen name="AdminDebriefingsList" component={AdminDebriefingsListPage} /><Stack.Screen name="AdminEventDebriefing" component={AdminEventDebriefingPage} /><Stack.Screen name="AdminEventRoles" component={AdminEventRolesPage} /><Stack.Screen name="AdminVenues" component={AdminVenuesPage} /><Stack.Screen name="AdminChecklistTemplates" component={AdminChecklistTemplatesPage} /></Stack.Navigator>
);
const AdminStorageStack = () => (
    <Stack.Navigator><Stack.Screen name="AdminStorageIndex" component={AdminStorageIndex} options={{ headerShown: false }} /><Stack.Screen name="AdminStorage" component={AdminStoragePage} /><Stack.Screen name="AdminKits" component={AdminKitsPage} /><Stack.Screen name="AdminDefective" component={AdminDefectivePage} /><Stack.Screen name="AdminDamageReports" component={AdminDamageReportsPage} /></Stack.Navigator>
);
const AdminCoursesStack = () => (
    <Stack.Navigator><Stack.Screen name="AdminCoursesIndex" component={AdminCoursesIndex} options={{ headerShown: false }} /><Stack.Screen name="AdminCourses" component={AdminCoursesPage} /><Stack.Screen name="AdminMeetings" component={AdminMeetingsPage} /><Stack.Screen name="AdminMatrix" component={AdminMatrixPage} /></Stack.Navigator>
);
const AdminContentStack = () => (
    <Stack.Navigator><Stack.Screen name="AdminContentIndex" component={AdminContentIndex} options={{ headerShown: false }} /><Stack.Screen name="AdminAnnouncements" component={AdminAnnouncementsPage} /><Stack.Screen name="AdminFiles" component={AdminFilesPage} /><Stack.Screen name="AdminFileEditor" component={AdminFileEditorPage} /><Stack.Screen name="AdminFeedback" component={AdminFeedbackPage} /><Stack.Screen name="AdminChangelogs" component={AdminChangelogPage} /><Stack.Screen name="AdminDocumentation" component={AdminDocumentationPage} /><Stack.Screen name="AdminNotifications" component={AdminNotificationsPage} /></Stack.Navigator>
);
const AdminReportsStack = () => (
    <Stack.Navigator><Stack.Screen name="AdminReportsIndex" component={AdminReportsIndex} options={{ headerShown: false }} /><Stack.Screen name="AdminReports" component={AdminReportsPage} /><Stack.Screen name="AdminLog" component={AdminLogPage} /></Stack.Navigator>
);
const AdminSystemStack = () => (
    <Stack.Navigator><Stack.Screen name="AdminSystemIndex" component={AdminSystemIndex} options={{ headerShown: false }} /><Stack.Screen name="AdminSystemPage" component={AdminSystemPage} /><Stack.Screen name="AdminAuthLog" component={AdminAuthLogPage} /><Stack.Screen name="AdminGeoIp" component={AdminGeoIpPage} /><Stack.Screen name="AdminWiki" component={AdminWikiPage} /></Stack.Navigator>
);


const MainDrawerNavigator = () => {
    return (
        <Drawer.Navigator
            drawerContent={(props) => <Sidebar {...props} />}
            screenOptions={{
                header: (props) => <Header {...props} />,
                headerShown: true
            }}
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
            <Drawer.Screen name="Profile" component={ProfilePage} />

            <Drawer.Screen name="Admin Dashboard" component={AdminDashboardPage} />
            <Drawer.Screen name="Benutzer & Anträge" component={AdminUsersStack} />
            <Drawer.Screen name="Event Management" component={AdminEventsStack} />
            <Drawer.Screen name="Lager & Material" component={AdminStorageStack} />
            <Drawer.Screen name="Lehrgänge & Skills" component={AdminCoursesStack} />
            <Drawer.Screen name="Inhalte & Kommunikation" component={AdminContentStack} />
            <Drawer.Screen name="Berichte" component={AdminReportsStack} />
            <Drawer.Screen name="System & Entwicklung" component={AdminSystemStack} />
        </Drawer.Navigator>
    );
};

const AppStack = () => {
    return (
        <Stack.Navigator
            screenOptions={{
                header: (props) => <Header {...props} />,
            }}
        >
            <Stack.Screen name="MainDrawer" component={MainDrawerNavigator} options={{ headerShown: false }}/>
            
            <Stack.Screen name="UserProfile" component={UserProfilePage} options={{ title: 'Benutzerprofil' }} />
            <Stack.Screen name="MeetingDetails" component={MeetingDetailsPage} options={{ title: 'Meeting-Details' }}/>
            <Stack.Screen name="EventDetails" component={EventDetailsPage} options={{ title: 'Event-Details' }}/>
            <Stack.Screen name="StorageItemDetails" component={StorageItemDetailsPage} options={{ title: 'Lagerartikel-Details' }} />
            <Stack.Screen name="Settings" component={SettingsPage} options={{ title: 'Einstellungen' }} />
            <Stack.Screen name="PasswordChange" component={PasswordPage} options={{ title: 'Passwort ändern' }} />
            <Stack.Screen name="Search" component={SearchResultsPage} options={{ title: 'Suchergebnisse' }} />
            <Stack.Screen name="HelpList" component={HelpListPage} options={{ title: 'Hilfe' }} />
            <Stack.Screen name="HelpDetails" component={HelpDetailsPage} options={{ title: 'Hilfe-Detail' }} />
            <Stack.Screen name="EventFeedback" component={EventFeedbackPage} options={{ title: 'Event-Feedback' }} />
            <Stack.Screen name="PackKit" component={PackKitPage} options={{ headerShown: false }} />
            <Stack.Screen name="QrAction" component={QrActionPage} options={{ headerShown: false }} />
            <Stack.Screen name="FileEditor" component={FileEditorPage} options={{ title: 'Datei-Editor' }} />
            <Stack.Screen name="NotFound" component={NotFoundPage} options={{ title: 'Nicht gefunden' }}/>

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
        <ErrorBoundary>
            {isAuthenticated ? <AppStack /> : <AuthStack />}
        </ErrorBoundary>
    );
};

export default RootNavigator;