import React from 'react';
import { useAuthStore } from '../store/authStore';
import ForbiddenPage from '../pages/error/ForbiddenPage';

const AdminRoute = ({ children }) => {
	const isAdmin = useAuthStore((state) => state.isAdmin);

	// In React Navigation, we render the component directly
	// instead of using a Navigate component. The navigator
	// will handle displaying this screen.
	if (!isAdmin) {
		return <ForbiddenPage />;
	}

	return children;
};

export default AdminRoute;