import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

const AdminRoute = () => {
	const isAdmin = useAuthStore((state) => state.isAdmin);

	if (!isAdmin) {
		return <Navigate to="/home" replace />;
	}

	return <Outlet />;
};

export default AdminRoute;