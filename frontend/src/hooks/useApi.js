import { useState, useEffect, useCallback } from 'react';
import { useUIStore } from '../store/uiStore';

/**
 * A custom React hook to manage the state of an API call.
 * @param {Function} apiCall - The function from apiClient to execute.
 * @param {object} [options] - Optional configuration.
 * @param {string|string[]} [options.subscribeTo] - An entity type or array of entity types to subscribe to for real-time updates.
 * @returns {object} An object containing data, loading state, error state, and a reload function.
 */
const useApi = (apiCall, options = {}) => {
	const [data, setData] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState(null);

	const { subscribeTo } = options;

	const fetchData = useCallback(async () => {
		// Don't fetch if the apiCall function isn't ready or is null
		if (!apiCall) {
			setLoading(false);
			setData(null); // Clear data if the call is removed
			return;
		}

		try {
			setLoading(true); // Set loading to true at the start of a fetch/refetch
			setError(null);
			const result = await apiCall();
			if (result.success) {
				setData(result.data);
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
            // If it's our special auth error, do nothing. The logout process has taken over.
            if (err.isAuthError) {
                return; // Suppress setting the error state for this component
            }
			setError(err.message || 'Ein unerwarteter Fehler ist aufgetreten.');
		} finally {
			setLoading(false);
		}
	}, [apiCall]);

	useEffect(() => {
		fetchData();
	}, [fetchData]);

    // Effect for real-time updates via SSE triggers
    useEffect(() => {
        if (!subscribeTo) {
            return;
        }

        const entitiesToSubscribe = Array.isArray(subscribeTo) ? subscribeTo : [subscribeTo];
        
        const unsubscribe = useUIStore.subscribe(
            (state) => {
                const relevantTriggers = {};
                entitiesToSubscribe.forEach(entity => {
                    relevantTriggers[entity] = state.refetchTriggers[entity.toUpperCase()];
                });
                return relevantTriggers;
            },
            () => {
                console.log(`[useApi] Refetch triggered for entities: ${entitiesToSubscribe.join(', ')}`);
                fetchData();
            },
            { equalityFn: (a, b) => JSON.stringify(a) === JSON.stringify(b) }
        );

        return () => unsubscribe();

    }, [subscribeTo, fetchData]);

	return { data, loading, error, reload: fetchData };
};

export default useApi;