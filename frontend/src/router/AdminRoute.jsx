import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

const AdminRoute = () => {
	const isAdmin = useAuthStore((state) => state.isAdmin);

	// Check for authentication is handled by ProtectedRoute, this just checks for admin role.
	if (!isAdmin) {
		// Instead of rendering a component directly, we navigate to a dedicated route for 403.
		// This keeps the URL consistent with the error being shown.
		return <Navigate to="/forbidden" replace />;
	}

	return <Outlet />;
};

export default AdminRoute;