import React, { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const ErrorLayout = ({ children }) => {
	// Attempt to get the user's theme. Fallback to localStorage or light theme.
	const userTheme = useAuthStore.getState().user?.theme;

	useEffect(() => {
		const savedTheme = userTheme || localStorage.getItem('theme') || 'light';
		document.documentElement.setAttribute('data-theme', savedTheme);
		// Add a class to the body for specific error page styling if needed
		document.body.classList.add('error-page-active');

		// Cleanup function to remove the class when the component unmounts
		return () => {
			document.body.classList.remove('error-page-active');
		};
	}, [userTheme]);

	return (
		// The wrapper no longer centers content by default.
		// It now provides a full-page container for its children.
		<div className="error-page-wrapper">
			{children || <Outlet />}
		</div>
	);
};

export default ErrorLayout;