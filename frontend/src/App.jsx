import React, { useState, useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';
import ToastContainer from './components/ui/ToastContainer';
import { ToastProvider } from './context/ToastContext';
import { useNotifications } from './hooks/useNotifications';
import WarningNotification from './components/ui/WarningNotification';

const AppLayout = () => {
	const [isNavOpen, setIsNavOpen] = useState(false);
	const location = useLocation();
	const { warningNotification, dismissWarning } = useNotifications();

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