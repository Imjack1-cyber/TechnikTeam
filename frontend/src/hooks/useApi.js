import { useState, useEffect, useCallback } from 'react';
import apiClient from '../services/apiClient';

/**
 * A custom React hook to manage the state of an API call.
 * @param {Function} apiCall - The function from apiClient to execute.
 * @returns {object} An object containing data, loading state, error state, and a reload function.
 */
const useApi = (apiCall) => {
	const [data, setData] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState(null);

	const fetchData = useCallback(async () => {
		try {
			setLoading(true);
			setError(null);
			const result = await apiCall();
			if (result.success) {
				setData(result.data);
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'An unexpected error occurred.');
		} finally {
			setLoading(false);
		}
	}, [apiCall]);

	useEffect(() => {
		fetchData();
	}, [fetchData]);

	return { data, loading, error, reload: fetchData };
};

export default useApi;