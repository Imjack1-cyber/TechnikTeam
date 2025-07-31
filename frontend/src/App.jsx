import React, { useState, useEffect, Suspense } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';
import ToastContainer from './components/ui/ToastContainer';
import { ToastProvider } from './context/ToastContext';

function App() {
	const [isNavOpen, setIsNavOpen] = useState(false);
	const location = useLocation();

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
		<ToastProvider>
			<Sidebar />
			<Header onNavToggle={() => setIsNavOpen(!isNavOpen)} />
			{isNavOpen && <div className="page-overlay" onClick={() => setIsNavOpen(false)}></div>}
			<div className="main-content-wrapper">
				<main className="main-content">
					<Suspense fallback={<div>Seite wird geladen...</div>}>
						<Outlet />
					</Suspense>
				</main>
			</div>
			<ToastContainer />
		</ToastProvider>
	);
}

export default App;