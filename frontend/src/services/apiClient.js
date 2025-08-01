import { useAuthStore } from '../store/authStore';

const BASE_URL = '/api/v1';

// This will be populated after the initial page load
let csrfToken = '';

const apiClient = {
	// Function to fetch the CSRF token, typically called once on app initialization
	async fetchCsrfToken() {
		try {
			// A simple GET request to a protected endpoint will return the CSRF cookie
			await this.get('/users/me'); // Using /me as it's a guaranteed protected route
		} catch (error) {
			console.warn("Could not pre-fetch CSRF token. It will be fetched on the first state-changing request.", error);
		}
	},

	request: async function(endpoint, options = {}) {
		const { logout } = useAuthStore.getState();
		const headers = {
			'Content-Type': 'application/json',
			...options.headers,
		};

		if (options.body instanceof FormData) {
			delete headers['Content-Type'];
		}

		// Add CSRF token for state-changing methods
		const method = options.method || 'GET';
		if (['POST', 'PUT', 'DELETE'].includes(method.toUpperCase())) {
			// Find the XSRF-TOKEN from cookies
			const match = document.cookie.match(new RegExp('(^| )' + 'XSRF-TOKEN' + '=([^;]+)'));
			if (match) {
				csrfToken = match[2];
			}
			if (csrfToken) {
				headers['X-XSRF-TOKEN'] = csrfToken;
			} else {
				console.warn('CSRF token not found. State-changing requests may fail.');
			}
		}

		try {
			const response = await fetch(`${BASE_URL}${endpoint}`, {
				...options,
				headers: headers,
				credentials: 'include' // Crucial for sending HttpOnly cookies
			});

			if (response.status === 401) {
				logout();
				throw new Error('Nicht authorisiert. Ihre Sitzung ist möglicherweise abgelaufen.');
			}
			if (response.status === 403) {
				throw new Error('Zugriff verweigert. Sie haben nicht die erforderlichen Berechtigungen.');
			}

			if (response.status === 204) {
				return { success: true, message: 'Operation successful.', data: null };
			}

			const contentType = response.headers.get("content-type");
			if (!contentType || !contentType.includes("application/json")) {
				const textError = await response.text();
				console.error("Non-JSON API response:", textError);
				throw new Error(`Serververbindung fehlgeschlagen (Status: ${response.status}). Das Backend ist möglicherweise offline.`);
			}

			const result = await response.json();

			if (!response.ok) {
				// Use a user-friendly generic message for server errors
				if (response.status >= 500) {
					throw new Error("Ein interner Serverfehler ist aufgetreten. Bitte versuchen Sie es später erneut.");
				}
				throw new Error(result.message || `Ein Fehler ist aufgetreten (Status: ${response.status})`);
			}

			return result;

		} catch (error) {
			if (error instanceof TypeError && error.message === 'Failed to fetch') {
				console.error(`API Client Network Error: ${options.method || 'GET'} ${BASE_URL}${endpoint}`, error);
				throw new Error('Netzwerkfehler: Das Backend ist nicht erreichbar. Bitte überprüfen Sie, ob der Server läuft.');
			}
			console.error(`API Client Error: ${options.method || 'GET'} ${BASE_URL}${endpoint}`, error);
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

// Pre-fetch the CSRF token when the module is loaded
apiClient.fetchCsrfToken();

export default apiClient;