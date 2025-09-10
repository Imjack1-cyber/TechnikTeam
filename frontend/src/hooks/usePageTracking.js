import { useEffect } from 'react';
import { useUIStore } from '../store/uiStore';
import pageRoutes from '../router/pageRoutes';
import { navigationRef } from '../router/navigation';

const usePageTracking = () => {
    const setCurrentPageKey = useUIStore(state => state.setCurrentPageKey);

    useEffect(() => {
        const updateCurrentPageKey = () => {
            if (navigationRef.isReady()) {
                let route = navigationRef.getCurrentRoute();
                if (route) {
                    // Navigate through nested navigators to find the deepest active route
                    while (route.state) {
                        route = route.routes[route.state.index];
                    }
                    
                    const pageKey = pageRoutes[route.name] || null;
                    setCurrentPageKey(pageKey);
                }
            }
        };

        // Add a listener that fires whenever the navigation state changes.
        const unsubscribe = navigationRef.addListener('state', updateCurrentPageKey);
        
        // Also run once after the navigator is ready, in case the initial state is what we need.
        const checkInitialState = () => {
            if (navigationRef.isReady()) {
                updateCurrentPageKey();
            }
        };
        
        // Wait a moment for the ref to be attached, then check initial state.
        const timeoutId = setTimeout(checkInitialState, 100);

        // Cleanup function to remove the listener when the component unmounts.
        return () => {
            clearTimeout(timeoutId);
            unsubscribe();
        };
    }, [setCurrentPageKey]);

    // This hook is for side-effects only and does not render anything.
    return null; 
};

export default usePageTracking;