import { useState, useEffect, useCallback } from 'react';
import apiClient from '../services/apiClient';

/**
 * A custom React hook to manage the state of an API call.
 * @param {Function} apiCall - The function from apiClient to execute.
 * @returns {object} An object containing data, loading state, error state, and a reload function.
 */
const useApi = (apiCall) => {
	const [data, setData] = useState(null);
	// Start in a loading state. This prevents the state transition during the initial render
	// that causes the "component suspended" error with lazy loading and routing.
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState(null);

	const fetchData = useCallback(async () => {
		// Don't fetch if the apiCall function isn't ready or is null
		if (!apiCall) {
			setLoading(false);
			setData(null); // Clear data if the call is removed
			return;
		}

		try {
			// setLoading(true) was here and caused the issue. It's now the initial state.
			setError(null);
			const result = await apiCall();
			if (result.success) {
				setData(result.data);
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Ein unerwarteter Fehler ist aufgetreten.');
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