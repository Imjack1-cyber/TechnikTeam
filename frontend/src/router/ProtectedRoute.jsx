import React from 'react';
import { useAuthStore } from '../store/authStore';
import LoginPage from '../pages/LoginPage';

const ProtectedRoute = ({ children }) => {
	const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

	// In React Navigation, the navigator itself will conditionally render
	// the auth stack (LoginPage) or the main app stack. This component
	// is a conceptual placeholder for that logic. If used directly,
	// it would render the LoginPage for unauthenticated users.
	if (!isAuthenticated) {
		return <LoginPage />;
	}

	return children;
};

export default ProtectedRoute;