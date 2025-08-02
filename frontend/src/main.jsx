import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import { useAuthStore } from './store/authStore';
import apiClient from './services/apiClient'; // Import apiClient here

const initializeApp = async () => {
	// Inject the logout function into the apiClient
	apiClient.setup({ onUnauthorized: useAuthStore.getState().logout });

	// Pre-fetch CSRF token after setup
	await apiClient.fetchCsrfToken();

	const { user, fetchUserSession } = useAuthStore.getState();
	if (!user) {
		try {
			await fetchUserSession();
		} catch (error) {
			console.log("No valid session found. User needs to log in.");
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