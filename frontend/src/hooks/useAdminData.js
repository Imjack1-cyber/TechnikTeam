import { useState, useEffect } from 'react';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';

// Granular hooks for specific data needs
export const useAdminRolesAndPermissions = () => {
	const [data, setData] = useState({ roles: [], groupedPermissions: {}, loading: true, error: null });
	useEffect(() => {
		const fetchData = async () => {
			try {
				// FIX: Changed endpoint to /form-data/users
				const result = await apiClient.get('/form-data/users');
				if (result.success) {
					setData({ roles: result.data.roles, groupedPermissions: result.data.groupedPermissions, loading: false, error: null });
				} else { throw new Error(result.message); }
			} catch (err) {
				setData({ roles: [], groupedPermissions: {}, loading: false, error: err.message });
			}
		};
		fetchData();
	}, []);
	return data;
};

export const useAdminCourses = () => {
	const [data, setData] = useState({ courses: [], loading: true, error: null });
	const { isAdmin, userPermissions } = useAuthStore(state => ({
		isAdmin: state.isAdmin,
		userPermissions: state.user?.permissions || []
	}));
	const canRead = isAdmin || userPermissions.includes('COURSE_READ');

	useEffect(() => {
		if (!canRead) {
			setData({ courses: [], loading: false, error: null });
			return;
		}
		const fetchData = async () => {
			try {
				const result = await apiClient.get('/courses');
				if (result.success) {
					setData({ courses: result.data, loading: false, error: null });
				} else { throw new Error(result.message); }
			} catch (err) {
				setData({ courses: [], loading: false, error: err.message });
			}
		};
		fetchData();
	}, [canRead]);
	return data;
};

// Main hook remains for components that need everything
const useAdminData = () => {
	const [data, setData] = useState({
		roles: [],
		groupedPermissions: {},
		storageItems: [],
		courses: [],
		users: [],
		venues: [],
		loading: true,
		error: null,
	});

	const { isAdmin, userPermissions } = useAuthStore(state => ({
		isAdmin: state.isAdmin,
		userPermissions: state.user?.permissions || []
	}));

	const canReadCourses = isAdmin || userPermissions.includes('COURSE_READ');
	const canReadStorage = isAdmin || userPermissions.includes('STORAGE_READ');
	const canReadUsers = isAdmin || userPermissions.includes('USER_READ');
	const canReadVenues = isAdmin || userPermissions.includes('EVENT_CREATE');


	useEffect(() => {
		const fetchData = async () => {
			try {
				const promises = [
					// FIX: Changed endpoint to /form-data/users
					apiClient.get('/form-data/users'), // roles & permissions
					canReadStorage ? apiClient.get('/storage') : Promise.resolve({ success: true, data: [] }),
					canReadCourses ? apiClient.get('/courses') : Promise.resolve({ success: true, data: [] }),
					canReadUsers ? apiClient.get('/users') : Promise.resolve({ success: true, data: [] }),
					canReadVenues ? apiClient.get('/admin/venues') : Promise.resolve({ success: true, data: [] }),
				];

				const [usersFormData, storageItemsData, coursesData, usersData, venuesData] = await Promise.all(promises);

				if (usersFormData.success && storageItemsData.success && coursesData.success && usersData.success && venuesData.success) {
					setData({
						roles: usersFormData.data.roles,
						groupedPermissions: usersFormData.data.groupedPermissions,
						storageItems: storageItemsData.data,
						courses: coursesData.data,
						users: usersData.data,
						venues: venuesData.data,
						loading: false,
						error: null,
					});
				} else {
					throw new Error('Eine oder mehrere Admin-Datenquellen konnten nicht geladen werden.');
				}
			} catch (err) {
				setData(prev => ({
					...prev,
					loading: false,
					error: err.message || 'Fehler beim Laden der Admin-Formulardaten.',
				}));
			}
		};

		fetchData();
	}, [canReadCourses, canReadStorage, canReadUsers, canReadVenues]);

	return data;
};

export default useAdminData;