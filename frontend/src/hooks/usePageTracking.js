import { useEffect } from 'react';
import { useNavigationState } from '@react-navigation/native';
import { useUIStore } from '../store/uiStore';
import pageRoutes from '../router/pageRoutes';

const usePageTracking = () => {
    const setCurrentPageKey = useUIStore(state => state.setCurrentPageKey);

    // This hook gets the entire navigation state.
    const navState = useNavigationState(state => state);

    useEffect(() => {
        if (navState) {
            // Recursively find the deepest active route.
            let route = navState.routes[navState.index];
            while (route.state) {
                route = route.routes[route.state.index];
            }

            const currentRouteName = route.name;
            const pageKey = pageRoutes[currentRouteName] || null;
            
            setCurrentPageKey(pageKey);
        }
    }, [navState, setCurrentPageKey]);

    // This hook doesn't return anything; it just updates the global store.
    return null; 
};

export default usePageTracking;