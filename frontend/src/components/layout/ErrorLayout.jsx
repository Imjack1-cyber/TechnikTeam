import React, { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const ErrorLayout = ({ children }) => {
	// Attempt to get the user's theme. Fallback to localStorage or light theme.
	const userTheme = useAuthStore.getState().user?.theme;

	useEffect(() => {
		const savedTheme = userTheme || localStorage.getItem('theme') || 'light';
		document.documentElement.setAttribute('data-theme', savedTheme);
		document.body.classList.add('error-page-active');

		// Cleanup function
		return () => {
			document.body.classList.remove('error-page-active');
			// Optional: reset theme if leaving error state is desired,
			// but it's generally fine to leave it.
		};
	}, [userTheme]);

	return (
		<div className="error-page-wrapper">
			{/* Render children directly. If Outlet is needed, routing must be nested. */}
			{children || <Outlet />}
		</div>
	);
};

export default ErrorLayout;