import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import { useAuthStore } from './store/authStore';

const initializeApp = async () => {
	// We check for the user object now instead of the token
	const { user, fetchUserSession } = useAuthStore.getState();
	// The existence of the user object relies on a valid HttpOnly cookie session.
	// If the user object is not present, we need to fetch it.
	if (!user) {
		try {
			await fetchUserSession();
		} catch (error) {
			console.log("No valid session found. User needs to log in.");
			// If fetchUserSession fails, it automatically logs the user out in the store,
			// so no further action is needed here.
		}
	}
};

initializeApp().then(() => {
	ReactDOM.createRoot(document.getElementById('root')).render(
		<React.StrictMode>
			<RouterProvider router={router} />
		</React.StrictMode>
	);
});