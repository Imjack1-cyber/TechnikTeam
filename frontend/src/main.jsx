import React, { Suspense } from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import { useAuthStore } from './store/authStore';
import apiClient from './services/apiClient'; // Import apiClient here

const initializeApp = async () => {
	// Inject the logout function into the apiClient
	apiClient.setup({ onUnauthorized: useAuthStore.getState().logout });

	try {
		// Attempt to fetch the user session. This will succeed if the JWT cookie is valid.
		// This GET request is also crucial because it will trigger the backend
		// to send the initial XSRF-TOKEN cookie needed for subsequent state-changing requests.
		await useAuthStore.getState().fetchUserSession();
	} catch (error) {
		// This is an expected and normal outcome if the user is not logged in.
		console.log("No valid session found. User will be directed to login page if required.");
	}
};

initializeApp().then(() => {
	ReactDOM.createRoot(document.getElementById('root')).render(
		<React.StrictMode>
			<Suspense fallback={<div className="loading-fullscreen">Lade Anwendung...</div>}>
				<RouterProvider router={router} />
			</Suspense>
		</React.StrictMode>
	);
});