import { useState, useEffect } from 'react';
import apiClient from '../services/apiClient';

/**
 * A custom hook to fetch and cache shared data needed for admin forms,
 * such as lists of roles, permissions, and all storage items.
 */
const useAdminData = () => {
	const [data, setData] = useState({
		roles: [],
		groupedPermissions: {},
		storageItems: [],
		loading: true,
		error: null,
	});

	useEffect(() => {
		const fetchData = async () => {
			try {
				// Use Promise.all to fetch data concurrently
				const [usersFormData, storageItemsData] = await Promise.all([
					apiClient.get('/users/form-data'),
					apiClient.get('/storage')
				]);

				if (usersFormData.success && storageItemsData.success) {
					setData({
						roles: usersFormData.data.roles,
						groupedPermissions: usersFormData.data.groupedPermissions,
						storageItems: storageItemsData.data,
						loading: false,
						error: null,
					});
				} else {
					throw new Error('Failed to fetch one or more admin data sources.');
				}
			} catch (err) {
				setData({
					roles: [],
					groupedPermissions: {},
					storageItems: [],
					loading: false,
					error: err.message || 'Failed to fetch admin form data.',
				});
			}
		};

		fetchData();
	}, []);

	return data;
};

export default useAdminData;