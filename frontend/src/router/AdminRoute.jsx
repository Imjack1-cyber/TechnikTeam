import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

const AdminRoute = () => {
	const isAdmin = useAuthStore((state) => state.isAdmin);

	// In the simplified model, the UI should still be protected for admins.
	// This check prevents non-admin users from even seeing the admin pages.
	if (!isAdmin) {
		return <Navigate to="/forbidden" replace />;
	}

	return <Outlet />;
};

export default AdminRoute;