import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { useAuthStore } from '../store/authStore';

// Custom Components
import Sidebar from '../components/layout/Sidebar';
import Header from '../components/layout/Header';
import ErrorBoundary from '../components/common/ErrorBoundary';
import usePageTracking from '../hooks/usePageTracking';

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
import FileSharePage from '../pages/FileSharePage';
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
import ErrorTrigger from '../pages/error/ErrorTrigger'; // For testing
import IdCardPage from '../pages/IdCardPage';
import VerificationPage from '../pages/VerificationPage';

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

// --- User Stacks ---
const EventsStack = () => (
    <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
        <Stack.Screen name="EventsList" component={EventsPage} options={{ title: 'Veranstaltungen' }} />
        <Stack.Screen name="EventDetails" component={EventDetailsPage} options={{ title: 'Event-Details' }} />
    </Stack.Navigator>
);


// --- Admin Stacks ---
const AdminUsersStack = () => (
    <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
        <Stack.Screen name="AdminUsersIndex" component={AdminUsersIndex} options={{ title: 'Benutzer & Anträge' }} />
        <Stack.Screen name="AdminUsers" component={AdminUsersPage} options={{ title: 'Benutzer Verwalten' }} />
        <Stack.Screen name="AdminRequests" component={AdminRequestsPage} options={{ title: 'Profilanträge' }} />
        <Stack.Screen name="AdminTrainingRequests" component={AdminTrainingRequestsPage} options={{ title: 'Lehrgangsanfragen' }} />
        <Stack.Screen name="AdminAchievements" component={AdminAchievementsPage} options={{ title: 'Abzeichen' }} />
    </Stack.Navigator>
);
const AdminEventsStack = () => (
    <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
        <Stack.Screen name="AdminEventsIndex" component={AdminEventsIndex} options={{ title: 'Event Management' }} />
        <Stack.Screen name="AdminEvents" component={AdminEventsPage} options={{ title: 'Events Verwalten' }}/>
        <Stack.Screen name="AdminDebriefingsList" component={AdminDebriefingsListPage} options={{ title: 'Debriefing-Übersicht' }}/>
        <Stack.Screen name="AdminEventDebriefing" component={AdminEventDebriefingPage} options={{ title: 'Event-Debriefing' }}/>
        <Stack.Screen name="AdminEventRoles" component={AdminEventRolesPage} options={{ title: 'Event-Rollen' }}/>
        <Stack.Screen name="AdminVenues" component={AdminVenuesPage} options={{ title: 'Veranstaltungsorte' }}/>
        <Stack.Screen name="AdminChecklistTemplates" component={AdminChecklistTemplatesPage} options={{ title: 'Checklist-Vorlagen' }}/>
    </Stack.Navigator>
);
const AdminStorageStack = () => (
    <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
        <Stack.Screen name="AdminStorageIndex" component={AdminStorageIndex} options={{ title: 'Lager & Material' }} />
        <Stack.Screen name="AdminStorage" component={AdminStoragePage} options={{ title: 'Lager Verwalten' }}/>
        <Stack.Screen name="AdminKits" component={AdminKitsPage} options={{ title: 'Kit-Verwaltung' }}/>
        <Stack.Screen name="AdminDefective" component={AdminDefectivePage} options={{ title: 'Defekte Artikel' }}/>
        <Stack.Screen name="AdminDamageReports" component={AdminDamageReportsPage} options={{ title: 'Schadensmeldungen' }}/>
    </Stack.Navigator>
);
const AdminCoursesStack = () => (
    <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
        <Stack.Screen name="AdminCoursesIndex" component={AdminCoursesIndex} options={{ title: 'Lehrgänge & Skills' }} />
        <Stack.Screen name="AdminCourses" component={AdminCoursesPage} options={{ title: 'Lehrgangs-Vorlagen' }}/>
        <Stack.Screen name="AdminMeetings" component={AdminMeetingsPage} options={{ title: 'Meetings' }}/>
        <Stack.Screen name="AdminMatrix" component={AdminMatrixPage} options={{ title: 'Qualifikations-Matrix' }}/>
    </Stack.Navigator>
);
const AdminContentStack = () => (
    <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
        <Stack.Screen name="AdminContentIndex" component={AdminContentIndex} options={{ title: 'Inhalte & Kommunikation' }} />
        <Stack.Screen name="AdminAnnouncements" component={AdminAnnouncementsPage} options={{ title: 'Anschlagbrett' }}/>
        <Stack.Screen name="AdminFiles" component={AdminFilesPage} options={{ title: 'Dateien' }}/>
        <Stack.Screen name="AdminFileEditor" component={AdminFileEditorPage} options={{ title: 'Datei-Editor' }}/>
        <Stack.Screen name="AdminFeedback" component={AdminFeedbackPage} options={{ title: 'Feedback' }}/>
        <Stack.Screen name="AdminChangelogs" component={AdminChangelogPage} options={{ title: 'Changelogs' }}/>
        <Stack.Screen name="AdminDocumentation" component={AdminDocumentationPage} options={{ title: 'Seiten-Doku' }}/>
        <Stack.Screen name="AdminNotifications" component={AdminNotificationsPage} options={{ title: 'Benachrichtigungen' }}/>
    </Stack.Navigator>
);
const AdminReportsStack = () => (
    <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
        <Stack.Screen name="AdminReportsIndex" component={AdminReportsIndex} options={{ title: 'Berichte' }} />
        <Stack.Screen name="AdminReports" component={AdminReportsPage} options={{ title: 'Berichte & Analysen' }}/>
        <Stack.Screen name="AdminLog" component={AdminLogPage} options={{ title: 'Aktions-Log' }}/>
    </Stack.Navigator>
);
const AdminSystemStack = () => (
    <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
        <Stack.Screen name="AdminSystemIndex" component={AdminSystemIndex} options={{ title: 'System & Entwicklung' }} />
        <Stack.Screen name="AdminSystemPage" component={AdminSystemPage} options={{ title: 'System-Status' }}/>
        <Stack.Screen name="AdminAuthLog" component={AdminAuthLogPage} options={{ title: 'Auth Log' }}/>
        <Stack.Screen name="AdminGeoIp" component={AdminGeoIpPage} options={{ title: 'GeoIP Filter' }}/>
        <Stack.Screen name="AdminWiki" component={AdminWikiPage} options={{ title: 'Technische Wiki' }}/>
    </Stack.Navigator>
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
            {/* --- User Pages --- */}
            <Drawer.Screen name="Dashboard" component={DashboardPage} />
            <Drawer.Screen name="Anschlagbrett" component={AnnouncementsPage} />
            <Drawer.Screen name="Benachrichtigungen" component={NotificationsPage} />
            <Drawer.Screen name="Team" component={TeamDirectoryPage} />
            <Drawer.Screen name="Chat" component={ChatPage} />
            <Drawer.Screen name="Lehrgänge" component={LehrgaengePage} />
            <Drawer.Screen name="Veranstaltungen" component={EventsStack} options={{ headerShown: false }}/>
            <Drawer.Screen name="Lager" component={StoragePage} />
            <Drawer.Screen name="Dateien" component={FilesPage} />
            <Drawer.Screen name="Kalender" component={CalendarPage} />
            <Drawer.Screen name="Feedback" component={FeedbackPage} />
            <Drawer.Screen name="Changelogs" component={ChangelogPage} />
            <Drawer.Screen name="Profile" component={ProfilePage} />

            {/* --- Admin Pages --- */}
            <Drawer.Screen name="Admin Dashboard" component={AdminDashboardPage} />
            <Drawer.Screen name="Benutzer & Anträge" component={AdminUsersStack} options={{ headerShown: false }} />
            <Drawer.Screen name="Event Management" component={AdminEventsStack} options={{ headerShown: false }} />
            <Drawer.Screen name="Lager & Material" component={AdminStorageStack} options={{ headerShown: false }} />
            <Drawer.Screen name="Lehrgänge & Skills" component={AdminCoursesStack} options={{ headerShown: false }} />
            <Drawer.Screen name="Inhalte & Kommunikation" component={AdminContentStack} options={{ headerShown: false }} />
            <Drawer.Screen name="Berichte" component={AdminReportsStack} options={{ headerShown: false }} />
            <Drawer.Screen name="System & Entwicklung" component={AdminSystemStack} options={{ headerShown: false }} />

            {/* --- Other screens nested within the Drawer to get the correct header --- */}
            <Drawer.Screen name="UserProfile" component={UserProfilePage} options={{ title: 'Benutzerprofil', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="MeetingDetails" component={MeetingDetailsPage} options={{ title: 'Meeting-Details', drawerItemStyle: { height: 0 } }}/>
            <Drawer.Screen name="StorageItemDetails" component={StorageItemDetailsPage} options={{ title: 'Lagerartikel-Details', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="Settings" component={SettingsPage} options={{ title: 'Einstellungen', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="PasswordChange" component={PasswordPage} options={{ title: 'Passwort ändern', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="Search" component={SearchResultsPage} options={{ title: 'Suchergebnisse', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="HelpList" component={HelpListPage} options={{ title: 'Hilfe', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="HelpDetails" component={HelpDetailsPage} options={{ title: 'Hilfe-Detail', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="EventFeedback" component={EventFeedbackPage} options={{ title: 'Event-Feedback', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="FileEditor" component={FileEditorPage} options={{ title: 'Datei-Editor', drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="IdCard" component={IdCardPage} options={{ title: 'Team Ausweis', drawerItemStyle: { height: 0 } }} />

            {/* --- Special screens that should NOT have the main header --- */}
            <Drawer.Screen name="PackKit" component={PackKitPage} options={{ headerShown: false, drawerItemStyle: { height: 0 } }} />
            <Drawer.Screen name="QrAction" component={QrActionPage} options={{ headerShown: false, drawerItemStyle: { height: 0 } }} />

            {/* --- Error screens, also hidden --- */}
            <Drawer.Screen name="NotFound" component={NotFoundPage} options={{ title: 'Nicht gefunden', drawerItemStyle: { height: 0 } }}/>
            <Drawer.Screen name="ErrorTrigger" component={ErrorTrigger} options={{ title: 'Trigger Error', drawerItemStyle: { height: 0 } }} />

        </Drawer.Navigator>
    );
};

const AppStack = () => {
    usePageTracking();

    return (
        <ErrorBoundary>
            <Stack.Navigator>
                <Stack.Screen name="MainDrawer" component={MainDrawerNavigator} options={{ headerShown: false }}/>
            </Stack.Navigator>
        </ErrorBoundary>
    );
};

const RootNavigator = () => {
	const { isAuthenticated, maintenanceStatus, isAdmin } = useAuthStore();

    return (
        <Stack.Navigator screenOptions={{ headerShown: false }}>
            {maintenanceStatus.mode === 'HARD' && !isAdmin ? (
                <Stack.Screen name="Maintenance" component={MaintenancePage} />
            ) : isAuthenticated ? (
                <Stack.Screen name="App" component={AppStack} />
            ) : (
                <Stack.Screen name="Login" component={LoginPage} />
            )}
             <Stack.Screen name="Verification" component={VerificationPage} />
             <Stack.Screen name="FileShare" component={FileSharePage} />
        </Stack.Navigator>
    );
};

export default RootNavigator;