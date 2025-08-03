import { useState, useEffect } from 'react';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';

// Granular hooks for specific data needs
export const useAdminRolesAndPermissions = () => {
	const [data, setData] = useState({ roles: [], groupedPermissions: {}, loading: true, error: null });
	useEffect(() => {
		const fetchData = async () => {
			try {
				const result = await apiClient.get('/users/form-data');
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
	const canRead = useAuthStore(state => state.user.permissions.includes('COURSE_READ'));
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
		loading: true,
		error: null,
	});

	const canReadCourses = useAuthStore(state => state.user.permissions.includes('COURSE_READ'));
	const canReadStorage = useAuthStore(state => state.user.permissions.includes('STORAGE_READ'));
	const canReadUsers = useAuthStore(state => state.user.permissions.includes('USER_READ'));


	useEffect(() => {
		const fetchData = async () => {
			try {
				const promises = [
					apiClient.get('/users/form-data'), // roles & permissions
					canReadStorage ? apiClient.get('/storage') : Promise.resolve({ success: true, data: [] }),
					canReadCourses ? apiClient.get('/courses') : Promise.resolve({ success: true, data: [] }),
					canReadUsers ? apiClient.get('/users') : Promise.resolve({ success: true, data: [] }),
				];

				const [usersFormData, storageItemsData, coursesData, usersData] = await Promise.all(promises);

				if (usersFormData.success && storageItemsData.success && coursesData.success && usersData.success) {
					setData({
						roles: usersFormData.data.roles,
						groupedPermissions: usersFormData.data.groupedPermissions,
						storageItems: storageItemsData.data,
						courses: coursesData.data,
						users: usersData.data,
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
	}, [canReadCourses, canReadStorage, canReadUsers]);

	return data;
};

export default useAdminData;