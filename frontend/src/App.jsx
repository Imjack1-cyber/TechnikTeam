import React, { useState, useEffect, useCallback } from 'react';
import { Outlet, useLocation, Link, useRevalidator } from 'react-router-dom';
import { useRegisterSW } from 'virtual:pwa-register/react';
import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';
import ToastContainer from './components/ui/ToastContainer';
import { ToastProvider } from './context/ToastContext';
import { useNotifications } from './hooks/useNotifications';
import WarningNotification from './components/ui/WarningNotification';
import ChangelogModal from './components/ui/ChangelogModal';
import UpdateNotification from './components/ui/UpdateNotification';
import apiClient from './services/apiClient';
import { useAuthStore } from './store/authStore';
import pageRoutes from './router/pageRoutes';

const AppLayout = () => {
	const [isNavOpen, setIsNavOpen] = useState(false);
	const location = useLocation();
	const revalidator = useRevalidator();

	// PWA update logic
	const {
		offlineReady: [offlineReady, setOfflineReady],
		needRefresh: [needRefresh, setNeedRefresh],
		updateServiceWorker,
	} = useRegisterSW({
		onRegistered(r) {
			console.log('Service Worker registered:', r);
		},
		onRegisterError(error) {
			console.log('Service Worker registration error:', error);
		},
	});


	const handleEventUpdate = useCallback((updatedEventId) => {
		const match = location.pathname.match(/\/veranstaltungen\/details\/(\d+)/);
		if (match && match[1] === String(updatedEventId)) {
			revalidator.revalidate();
		}
	}, [location.pathname, revalidator]);

	const { warningNotification, dismissWarning } = useNotifications(handleEventUpdate);
	const [changelog, setChangelog] = useState(null);
	const [isChangelogVisible, setIsChangelogVisible] = useState(false);
	const { isAuthenticated, layout } = useAuthStore(state => ({
		isAuthenticated: state.isAuthenticated,
		layout: state.layout,
	}));
	const sidebarPosition = layout.sidebarPosition || 'left';
	const isVerticalLayout = sidebarPosition === 'left' || sidebarPosition === 'right';

	const currentPageHelpKey = pageRoutes[location.pathname];
	const showHelpButton = layout.showHelpButton !== false;


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
		if (isNavOpen && isVerticalLayout) {
			document.body.classList.add('nav-open');
		} else {
			document.body.classList.remove('nav-open');
		}
	}, [isNavOpen, isVerticalLayout]);

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
			{isVerticalLayout && <Header onNavToggle={() => setIsNavOpen(!isNavOpen)} />}
			{isNavOpen && isVerticalLayout && <div className="page-overlay" onClick={() => setIsNavOpen(false)}></div>}
			<div className="main-content-wrapper">
				<main className="main-content">
					<Outlet />
				</main>
			</div>
			<ToastContainer />
			{needRefresh && <UpdateNotification onUpdate={() => updateServiceWorker(true)} />}
			{showHelpButton && currentPageHelpKey && (
				<Link to={`/help/${currentPageHelpKey}`} className="help-fab" title="Hilfe für diese Seite">
					<i className="fas fa-question"></i>
				</Link>
			)}
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