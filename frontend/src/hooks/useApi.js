import { useState, useEffect, useCallback, useRef } from 'react';
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
	const fetchDataRef = useRef();

	const fetchData = useCallback(async () => {
		if (!apiCall) {
			setLoading(false);
			setData(null);
			return;
		}
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
            if (err.isAuthError) {
                return;
            }
			setError(err.message || 'Ein unerwarteter Fehler ist aufgetreten.');
		} finally {
			setLoading(false);
		}
	}, [apiCall]);

	useEffect(() => {
		fetchDataRef.current = fetchData;
	}, [fetchData]);

	useEffect(() => {
		fetchData();
	}, [fetchData]);

    useEffect(() => {
        if (!subscribeTo) {
            return;
        }

        const entitiesToSubscribe = Array.isArray(subscribeTo) 
            ? subscribeTo.map(e => e.toUpperCase()) 
            : [subscribeTo.toUpperCase()];

        const unsubscribe = useUIStore.subscribe(
            (state, prevState) => {
                // Manually check if any of the subscribed entities have a new timestamp
                const hasChanged = entitiesToSubscribe.some(entity => 
                    state.refetchTriggers[entity] !== prevState.refetchTriggers[entity]
                );

                if (hasChanged) {
                    console.log(`[useApi] Refetch triggered for entities: ${entitiesToSubscribe.join(', ')}`);
                    // Use the ref to call the latest version of fetchData
                    if (fetchDataRef.current) {
                        fetchDataRef.current();
                    }
                }
            }
        );

        return () => unsubscribe();

    }, [subscribeTo]);

	return { data, loading, error, reload: fetchData };
};

export default useApi;