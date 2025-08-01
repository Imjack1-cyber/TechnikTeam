import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import ForbiddenPage from '../pages/error/ForbiddenPage'; // Import the Forbidden page

const AdminRoute = () => {
	const isAdmin = useAuthStore((state) => state.isAdmin);

	// Check for authentication is handled by ProtectedRoute, this just checks for admin role.
	if (!isAdmin) {
		// Instead of redirecting, show a proper 403 Forbidden page.
		return <ForbiddenPage />;
	}

	return <Outlet />;
};

export default AdminRoute;