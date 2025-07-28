import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

const AdminRoute = () => {
	const isAdmin = useAuthStore((state) => state.isAdmin);

	if (!isAdmin) {
		// Redirect non-admin users to the home page or an unauthorized page
		return <Navigate to="/home" replace />;
	}

	// Outlet renders the nested child route (e.g., AdminDashboardPage)
	return <Outlet />;
};

export default AdminRoute;