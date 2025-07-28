import { useState, useEffect } from 'react';
import apiClient from '@/services/apiClient';

/**
 * A custom hook to fetch and cache shared data needed for admin forms,
 * such as lists of roles and permissions.
 * Assumes a new endpoint /api/v1/users/form-data exists.
 */
const useAdminData = () => {
	const [data, setData] = useState({
		roles: [],
		groupedPermissions: {},
		loading: true,
		error: null,
	});

	useEffect(() => {
		const fetchData = async () => {
			try {
				// This is a hypothetical combined endpoint for efficiency.
				const result = await apiClient.get('/users/form-data');
				if (result.success) {
					setData({
						roles: result.data.roles,
						groupedPermissions: result.data.groupedPermissions,
						loading: false,
						error: null,
					});
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				setData({
					roles: [],
					groupedPermissions: {},
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