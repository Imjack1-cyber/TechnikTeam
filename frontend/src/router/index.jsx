import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from '../App';
import LoginPage from '../pages/LoginPage';
import DashboardPage from '../pages/DashboardPage';
import StoragePage from '../pages/StoragePage';
import StorageItemDetailsPage from '../pages/StorageItemDetailsPage';
import EventsPage from '../pages/EventsPage';
import EventDetailsPage from '../pages/EventDetailsPage';
import LehrgaengePage from '../pages/LehrgaengePage';
import MeetingDetailsPage from '../pages/MeetingDetailsPage';
import ProfilePage from '../pages/ProfilePage';
import PasswordPage from '../pages/PasswordPage';
import FilesPage from '../pages/FilesPage';
import FeedbackPage from '../pages/FeedbackPage';
import EventFeedbackPage from '../pages/EventFeedbackPage';
import CalendarPage from '../pages/CalendarPage';
import PackKitPage from '../pages/PackKitPage';
import AdminDashboardPage from '../pages/admin/AdminDashboardPage';
import AdminUsersPage from '../pages/admin/AdminUsersPage';
import AdminCoursesPage from '../pages/admin/AdminCoursesPage';
import AdminMeetingsPage from '../pages/admin/AdminMeetingsPage';
import AdminStoragePage from '../pages/admin/AdminStoragePage';
import AdminDefectivePage from '../pages/admin/AdminDefectivePage';
import AdminLogPage from '../pages/admin/AdminLogPage';
import AdminKitsPage from '../pages/admin/AdminKitsPage';
import AdminMatrixPage from '../pages/admin/AdminMatrixPage';
import AdminReportsPage from '../pages/admin/AdminReportsPage';
import ProtectedRoute from './ProtectedRoute';
import AdminRoute from './AdminRoute';
import MinimalLayout from '../components/layout/MinimalLayout';

const router = createBrowserRouter([
	{
		path: '/',
		element: (
			<ProtectedRoute>
				<App />
			</ProtectedRoute>
		),
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
			{ path: 'passwort', element: <PasswordPage /> },
			{ path: 'dateien', element: <FilesPage /> },
			{ path: 'feedback', element: <FeedbackPage /> },
			{ path: 'feedback/event/:eventId', element: <EventFeedbackPage /> },
			{ path: 'kalender', element: <CalendarPage /> },
			{ path: 'my-feedback', element: <Navigate to="/feedback" replace /> },

			{
				path: 'admin',
				element: <AdminRoute />,
				children: [
					{ index: true, element: <Navigate to="/admin/dashboard" replace /> },
					{ path: 'dashboard', element: <AdminDashboardPage /> },
					{ path: 'mitglieder', element: <AdminUsersPage /> },
					{ path: 'lehrgaenge', element: <AdminCoursesPage /> },
					{ path: 'lehrgaenge/:courseId/meetings', element: <AdminMeetingsPage /> },
					{ path: 'lager', element: <AdminStoragePage /> },
					{ path: 'defekte', element: <AdminDefectivePage /> },
					{ path: 'log', element: <AdminLogPage /> },
					{ path: 'kits', element: <AdminKitsPage /> },
					{ path: 'matrix', element: <AdminMatrixPage /> },
					{ path: 'berichte', element: <AdminReportsPage /> },
				],
			},
		],
	},
	{
		path: '/pack-kit/:kitId',
		element: (
			<ProtectedRoute>
				<MinimalLayout />
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
		path: '/login',
		element: <LoginPage />,
	},
	{
		path: '*',
		element: <Navigate to="/" replace />,
	}
]);

export default router;