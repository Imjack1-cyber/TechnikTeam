import React, { Suspense } from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import { useAuthStore } from './store/authStore';
import apiClient from './services/apiClient'; // Import apiClient here
import { ToastProvider } from './context/ToastContext';

const AUTH_TOKEN_KEY = 'technikteam-auth-token';

const initializeApp = async () => {
	// If we are on the maintenance page, do nothing. This prevents a redirect loop.
	if (window.location.pathname === '/maintenance') {
		return;
	}

	// Inject the logout function into the apiClient
	apiClient.setup({ onUnauthorized: useAuthStore.getState().logout });

	// Check for a token in local storage to restore session on page load
	const token = localStorage.getItem(AUTH_TOKEN_KEY);
	if (token) {
		apiClient.setAuthToken(token);
		try {
			await useAuthStore.getState().fetchUserSession();
		} catch (error) {
			console.log("Session token from storage is invalid. Clearing.");
			// The error handler in fetchUserSession will call logout(), which clears everything.
		}
	} else {
		// This is also a valid state for a first-time visitor.
		// We can try to get the CSRF token for the login form if needed.
		// For now, we do nothing and let ProtectedRoute handle redirection.
		console.log("No auth token found in storage.");
	}
};

initializeApp().then(() => {
	ReactDOM.createRoot(document.getElementById('root')).render(
		<React.StrictMode>
			<ToastProvider>
				<Suspense fallback={<div className="loading-fullscreen">Lade Anwendung...</div>}>
					<RouterProvider router={router} />
				</Suspense>
			</ToastProvider>
		</React.StrictMode>
	);
});