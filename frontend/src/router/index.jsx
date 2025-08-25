import React, { lazy, Suspense } from 'react';
import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';

// Layouts and Core Components
import App from '../App';
import MinimalLayout from '../components/layout/MinimalLayout';
import ErrorLayout from '../components/layout/ErrorLayout';
import ProtectedRoute from './ProtectedRoute';
import AdminRoute from './AdminRoute';
import { ToastProvider } from '../context/ToastContext';

// Eagerly load the LoginPage, Error Pages, and the new ChatPage
import LoginPage from '../pages/LoginPage';
import ErrorPage from '../pages/error/ErrorPage';
import NotFoundPage from '../pages/error/NotFoundPage';
import ChatPage from '../pages/ChatPage'; // Eagerly load ChatPage
import MaintenancePage from '../pages/error/MaintenancePage';

// Lazy load all other pages
const DashboardPage = lazy(() => import('../pages/DashboardPage'));
const StoragePage = lazy(() => import('../pages/StoragePage'));
const StorageItemDetailsPage = lazy(() => import('../pages/StorageItemDetailsPage'));
const QrActionPage = lazy(() => import('../pages/QrActionPage'));
const EventsPage = lazy(() => import('../pages/EventsPage'));
const EventDetailsPage = lazy(() => import('../pages/EventDetailsPage'));
const LehrgaengePage = lazy(() => import('../pages/LehrgaengePage'));
const MeetingDetailsPage = lazy(() => import('../pages/MeetingDetailsPage'));
const ProfilePage = lazy(() => import('../pages/ProfilePage'));
const PasswordPage = lazy(() => import('../pages/PasswordPage'));
const FilesPage = lazy(() => import('../pages/FilesPage'));
const FileEditorPage = lazy(() => import('../pages/files/FileEditorPage'));
const FeedbackPage = lazy(() => import('../pages/FeedbackPage'));
const EventFeedbackPage = lazy(() => import('../pages/EventFeedbackPage'));
const CalendarPage = lazy(() => import('../pages/CalendarPage'));
const PackKitPage = lazy(() => import('../pages/PackKitPage'));
const SearchResultsPage = lazy(() => import('../pages/SearchResultsPage'));
const ChangelogPage = lazy(() => import('../pages/ChangelogPage'));
const TeamDirectoryPage = lazy(() => import('../pages/TeamDirectoryPage'));
const UserProfilePage = lazy(() => import('../pages/UserProfilePage'));
const AnnouncementsPage = lazy(() => import('../pages/AnnouncementsPage'));
const HelpListPage = lazy(() => import('../pages/HelpListPage'));
const HelpDetailsPage = lazy(() => import('../pages/HelpDetailsPage'));
const SettingsPage = lazy(() => import('../pages/SettingsPage'));
const NotificationsPage = lazy(() => import('../pages/NotificationsPage'));

// Admin Pages
const AdminDashboardPage = lazy(() => import('../pages/admin/AdminDashboardPage'));
const AdminUsersIndex = lazy(() => import('../pages/admin/AdminUsersIndex'));
const AdminUsersPage = lazy(() => import('../pages/admin/AdminUsersPage'));
const AdminRequestsPage = lazy(() => import('../pages/admin/AdminRequestsPage'));
const AdminEventsIndex = lazy(() => import('../pages/admin/AdminEventsIndex'));
const AdminEventsPage = lazy(() => import('../pages/admin/AdminEventsPage'));
const AdminEventDebriefingPage = lazy(() => import('../pages/admin/AdminEventDebriefingPage'));
const AdminDebriefingsListPage = lazy(() => import('../pages/admin/AdminDebriefingsListPage'));
const AdminEventRolesPage = lazy(() => import('../pages/admin/AdminEventRolesPage'));
const AdminCoursesIndex = lazy(() => import('../pages/admin/AdminCoursesIndex'));
const AdminCoursesPage = lazy(() => import('../pages/admin/AdminCoursesPage'));
const AdminMeetingsPage = lazy(() => import('../pages/admin/AdminMeetingsPage'));
const AdminStorageIndex = lazy(() => import('../pages/admin/AdminStorageIndex'));
const AdminStoragePage = lazy(() => import('../pages/admin/AdminStoragePage'));
const AdminDefectivePage = lazy(() => import('../pages/admin/AdminDefectivePage'));
const AdminDamageReportsPage = lazy(() => import('../pages/admin/AdminDamageReportsPage'));
const AdminLogPage = lazy(() => import('../pages/admin/AdminLogPage'));
const AdminKitsPage = lazy(() => import('../pages/admin/AdminKitsPage'));
const AdminMatrixPage = lazy(() => import('../pages/admin/AdminMatrixPage'));
const AdminReportsPage = lazy(() => import('../pages/admin/AdminReportsPage'));
const AdminSystemPage = lazy(() => import('../pages/admin/AdminSystemPage'));
const AdminContentIndex = lazy(() => import('../pages/admin/AdminContentIndex'));
const AdminFilesPage = lazy(() => import('../pages/admin/AdminFilesPage'));
const AdminFeedbackPage = lazy(() => import('../pages/admin/AdminFeedbackPage'));
const AdminAchievementsPage = lazy(() => import('../pages/admin/AdminAchievementsPage'));
const AdminWikiPage = lazy(() => import('../pages/admin/AdminWikiPage'));
const AdminNotificationsPage = lazy(() => import('../pages/admin/AdminNotificationsPage'));
const AdminVenuesPage = lazy(() => import('../pages/admin/AdminVenuesPage'));
const AdminChecklistTemplatesPage = lazy(() => import('../pages/admin/AdminChecklistTemplatesPage'));
const AdminChangelogPage = lazy(() => import('../pages/admin/AdminChangelogPage'));
const AdminAnnouncementsPage = lazy(() => import('../pages/admin/AdminAnnouncementsPage'));
const AdminTrainingRequestsPage = lazy(() => import('../pages/admin/AdminTrainingRequestsPage'));
const AdminDocumentationPage = lazy(() => import('../pages/admin/AdminDocumentationPage'));
const AdminReportsIndex = lazy(() => import('../pages/admin/AdminReportsIndex'));
const AdminSystemIndex = lazy(() => import('../pages/admin/AdminSystemIndex'));
const AdminAuthLogPage = lazy(() => import('../pages/admin/AdminAuthLogPage'));


import ErrorTrigger from '../pages/error/ErrorTrigger';
import ForbiddenPage from '../pages/error/ForbiddenPage';

const router = createBrowserRouter([
	{
		path: '/',
		element: (
			<ProtectedRoute>
				<App />
			</ProtectedRoute>
		),
		errorElement: <ErrorLayout><ErrorPage /></ErrorLayout>,
		children: [
			{ index: true, element: <Navigate to="/home" replace /> },
			{ path: 'home', element: <DashboardPage /> },
			{ path: 'lager', element: <StoragePage /> },
			{ path: 'lager/details/:itemId', element: <StorageItemDetailsPage /> },
			{ path: 'veranstaltungen', element: <EventsPage /> },
			{ path: 'veranstaltungen/details/:eventId', element: <EventDetailsPage /> },
			{ path: 'lehrgaenge', element: <LehrgaengePage /> },
			{ path: 'lehrgaenge/details/:meetingId', element: <MeetingDetailsPage /> },
			{ path: 'profil', element: <ProfilePage /> },
			{ path: 'profil/einstellungen', element: <SettingsPage /> },
			{ path: 'passwort', element: <PasswordPage /> },
			{ path: 'dateien', element: <FilesPage /> },
			{ path: 'dateien/edit/:fileId', element: <FileEditorPage /> },
			{ path: 'feedback', element: <FeedbackPage /> },
			{ path: 'feedback/event/:eventId', element: <EventFeedbackPage /> },
			{ path: 'kalender', element: <CalendarPage /> },
			{ path: 'suche', element: <SearchResultsPage /> },
			{ path: 'changelogs', element: <ChangelogPage /> },
			{ path: 'team', element: <TeamDirectoryPage /> },
			{ path: 'team/:userId', element: <UserProfilePage /> },
			{ path: 'bulletin-board', element: <AnnouncementsPage /> },
			{ path: 'help', element: <HelpListPage /> },
			{ path: 'help/:pageKey', element: <HelpDetailsPage /> },
			{ path: 'chat', element: <ChatPage /> },
			{ path: 'chat/:conversationId', element: <ChatPage /> },
			{ path: 'notifications', element: <NotificationsPage /> },
			{ path: 'test-500', element: <ErrorTrigger /> },

			{
				path: 'admin',
				element: <AdminRoute />,
				children: [
					{ index: true, element: <Navigate to="/admin/dashboard" replace /> },
					{ path: 'dashboard', element: <AdminDashboardPage /> },
					{
						path: 'mitglieder', element: <AdminUsersIndex />, children: [
							{ path: 'users', element: <AdminUsersPage /> },
							{ path: 'requests', element: <AdminRequestsPage /> },
							{ path: 'training-requests', element: <AdminTrainingRequestsPage /> },
							{ path: 'achievements', element: <AdminAchievementsPage /> },
						]
					},
					{
						path: 'veranstaltungen', element: <AdminEventsIndex />, children: [
							{ path: 'events', element: <AdminEventsPage /> },
							{ path: 'debriefings', element: <AdminDebriefingsListPage /> },
							{ path: 'roles', element: <AdminEventRolesPage /> },
							{ path: 'venues', element: <AdminVenuesPage /> },
							{ path: 'checklist-templates', element: <AdminChecklistTemplatesPage /> },
						]
					},
					{ path: 'veranstaltungen/:eventId/debriefing', element: <AdminEventDebriefingPage /> },
					{
						path: 'lehrgaenge', element: <AdminCoursesIndex />, children: [
							{ path: 'courses', element: <AdminCoursesPage /> },
							{ path: 'matrix', element: <AdminMatrixPage /> },
							{ path: 'meetings/:courseId', element: <AdminMeetingsPage /> },
						]
					},
					{
						path: 'lager', element: <AdminStorageIndex />, children: [
							{ path: 'items', element: <AdminStoragePage /> },
							{ path: 'kits', element: <AdminKitsPage /> },
							{ path: 'defekte', element: <AdminDefectivePage /> },
							{ path: 'damage-reports', element: <AdminDamageReportsPage /> },
						]
					},
					{
						path: 'content', element: <AdminContentIndex />, children: [
							{ path: 'announcements', element: <AdminAnnouncementsPage /> },
							{ path: 'dateien', element: <AdminFilesPage /> },
							{ path: 'dateien/edit/:fileId', element: <FileEditorPage /> },
							{ path: 'feedback', element: <AdminFeedbackPage /> },
							{ path: 'changelogs', element: <AdminChangelogPage /> },
							{ path: 'documentation', element: <AdminDocumentationPage /> },
							{ path: 'benachrichtigungen', element: <AdminNotificationsPage /> },
						]
					},
					{
						path: 'reports', element: <AdminReportsIndex />, children: [
							{ path: 'analysis', element: <AdminReportsPage /> },
							{ path: 'log', element: <AdminLogPage /> },
						]
					},
					{
						path: 'system', element: <AdminSystemIndex />, children: [
							{ path: 'status', element: <AdminSystemPage /> },
							{ path: 'auth-log', element: <AdminAuthLogPage /> },
							{ path: 'wiki', element: <AdminWikiPage /> },
						]
					},
				],
			},
		],
	},
	{
		path: '/pack-kit/:kitId',
		element: (
			<ProtectedRoute>
				<ToastProvider>
					<MinimalLayout />
				</ToastProvider>
			</ProtectedRoute>
		),
		children: [
			{
				index: true,
				element: <PackKitPage />
			}
		]
	},
	{
		path: '/lager/qr-aktion/:itemId',
		element: (
			<ProtectedRoute>
				<ToastProvider>
					<MinimalLayout />
				</ToastProvider>
			</ProtectedRoute>
		),
		children: [
			{
				index: true,
				element: <QrActionPage />
			}
		]
	},
	{
		path: '/login',
		element: <Suspense fallback={<div>Laden...</div>}><LoginPage /></Suspense>,
	},
	{
		path: '/forbidden',
		element: <ErrorLayout><ForbiddenPage /></ErrorLayout>,
	},
	{
		path: '/maintenance',
		element: <ErrorLayout><MaintenancePage /></ErrorLayout>,
	},
	{
		path: '*',
		element: <ErrorLayout><NotFoundPage /></ErrorLayout>,
	}
]);

export default router;