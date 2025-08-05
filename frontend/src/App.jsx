import React, { useState, useEffect, useCallback } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';
import ToastContainer from './components/ui/ToastContainer';
import { ToastProvider } from './context/ToastContext';
import { useNotifications } from './hooks/useNotifications';
import WarningNotification from './components/ui/WarningNotification';
import ChangelogModal from './components/ui/ChangelogModal';
import apiClient from './services/apiClient';
import { useAuthStore } from './store/authStore';

const AppLayout = () => {
	const [isNavOpen, setIsNavOpen] = useState(false);
	const location = useLocation();
	const { warningNotification, dismissWarning } = useNotifications();
	const [changelog, setChangelog] = useState(null);
	const [isChangelogVisible, setIsChangelogVisible] = useState(false);
	const { isAuthenticated, layout } = useAuthStore(state => ({
		isAuthenticated: state.isAuthenticated,
		layout: state.layout,
	}));
	const sidebarPosition = layout.sidebarPosition || 'left';


	const fetchChangelog = useCallback(async () => {
		if (isAuthenticated) {
			try {
				const result = await apiClient.get('/public/changelog/latest-unseen');
				if (result.success && result.data) {
					setChangelog(result.data);
					setIsChangelogVisible(true);
				}
			} catch (error) {
				console.error("Failed to fetch changelog:", error);
			}
		}
	}, [isAuthenticated]);

	useEffect(() => {
		fetchChangelog();
	}, [fetchChangelog]);


	const handleChangelogClose = async () => {
		if (changelog) {
			try {
				await apiClient.post(`/public/changelog/${changelog.id}/mark-seen`);
			} catch (error) {
				console.error("Failed to mark changelog as seen:", error);
			}
		}
		setIsChangelogVisible(false);
		setChangelog(null); // FIX: Reset state to prevent re-opening
	};

	useEffect(() => {
		// Close mobile navigation when the route changes
		setIsNavOpen(false);
	}, [location]);

	useEffect(() => {
		// Add/remove class from body for overlay effect
		if (isNavOpen) {
			document.body.classList.add('nav-open');
		} else {
			document.body.classList.remove('nav-open');
		}
	}, [isNavOpen]);

	useEffect(() => {
		// Apply layout classes to the body
		document.body.classList.add(`layout-sidebar-${sidebarPosition}`);
		return () => {
			document.body.classList.remove(`layout-sidebar-${sidebarPosition}`);
		};
	}, [sidebarPosition]);

	return (
		<>
			<Sidebar />
			<Header onNavToggle={() => setIsNavOpen(!isNavOpen)} />
			{isNavOpen && <div className="page-overlay" onClick={() => setIsNavOpen(false)}></div>}
			<div className="main-content-wrapper">
				<main className="main-content">
					<Outlet />
				</main>
			</div>
			<ToastContainer />
			{warningNotification && <WarningNotification notification={warningNotification} onDismiss={dismissWarning} />}
			{isChangelogVisible && changelog && (
				<ChangelogModal
					changelog={changelog}
					onClose={handleChangelogClose}
				/>
			)}
		</>
	);
};

function App() {
	return (
		<ToastProvider>
			<AppLayout />
		</ToastProvider>
	);
}

export default App;