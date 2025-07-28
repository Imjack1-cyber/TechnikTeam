import { useAuthStore } from '../store/authStore';

const BASE_URL = '/api/v1';

/**
 * A wrapper around the fetch API that automatically adds the
 * Authorization header and handles common API responses like 401 Unauthorized.
 */
const apiClient = {
	async request(endpoint, options = {}) {
		//getState() allows us to access the store outside of a React component
		const { token, logout } = useAuthStore.getState();

		const headers = {
			'Content-Type': 'application/json',
			...options.headers,
		};

		if (token) {
			headers['Authorization'] = `Bearer ${token}`;
		}

		// For multipart/form-data, let the browser set the Content-Type header with the boundary
		if (options.body instanceof FormData) {
			delete headers['Content-Type'];
		}

		try {
			const response = await fetch(`${BASE_URL}${endpoint}`, {
				...options,
				headers: headers,
			});

			if (response.status === 401) {
				// Token is invalid or expired, trigger a global logout
				logout();
				// We throw an error to stop the current operation and prevent further processing.
				// The router's ProtectedRoute will handle the redirect to the login page.
				throw new Error('Unauthorized: Session has expired. Please log in again.');
			}

			// Handle cases where the response might not have a body (e.g., 204 No Content)
			if (response.status === 204) {
				return { success: true, message: 'Operation successful.', data: null };
			}

			const result = await response.json();

			if (!response.ok) {
				// Throw an error with the message from the API response if it exists
				throw new Error(result.message || `HTTP error! status: ${response.status}`);
			}

			return result;

		} catch (error) {
			console.error(`API Client Error: ${options.method || 'GET'} ${endpoint}`, error);
			// Re-throw the error so the calling function (e.g., in a component) can handle it
			throw error;
		}
	},

	get(endpoint) {
		return this.request(endpoint, { method: 'GET' });
	},

	post(endpoint, body) {
		const options = {
			method: 'POST',
			body: body instanceof FormData ? body : JSON.stringify(body),
		};
		return this.request(endpoint, options);
	},

	put(endpoint, body) {
		return this.request(endpoint, { method: 'PUT', body: JSON.stringify(body) });
	},

	delete(endpoint) {
		return this.request(endpoint, { method: 'DELETE' });
	},
};

export default apiClient;