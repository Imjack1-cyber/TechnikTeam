import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import { useAuthStore } from './store/authStore';

// Initialize auth state by checking for a token and fetching user data if it exists.
const initializeApp = async () => {
	const { token, fetchUserSession } = useAuthStore.getState();
	if (token) {
		try {
			await fetchUserSession();
		} catch (error) {
			console.log("Session token is invalid or expired. User is logged out.");
			// The 401 interceptor in apiClient will trigger the logout.
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