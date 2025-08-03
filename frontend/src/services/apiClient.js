// REMOVED: import { useAuthStore } from '../store/authStore';

const BASE_URL = '/api/v1';

let csrfToken = '';
let onUnauthorizedCallback = () => { }; // Placeholder for the logout function

const apiClient = {
	// New function to inject the logout handler from the auth store
	setup: function(callbacks) {
		onUnauthorizedCallback = callbacks.onUnauthorized;
	},

	async fetchCsrfToken() {
		try {
			await this.get('/users/me');
		} catch (error) {
			console.warn("Could not pre-fetch CSRF token. It will be fetched on the first state-changing request.", error);
		}
	},

	request: async function(endpoint, options = {}) {
		// REMOVED: const { logout } = useAuthStore.getState();
		const headers = {
			'Content-Type': 'application/json',
			...options.headers,
		};

		if (options.body instanceof FormData) {
			delete headers['Content-Type'];
		}

		const method = options.method || 'GET';
		if (['POST', 'PUT', 'DELETE'].includes(method.toUpperCase())) {
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
				credentials: 'include'
			});

			if (response.status === 401) {
				onUnauthorizedCallback(); // Use the injected callback
				throw new Error('Nicht autorisiert. Ihre Sitzung ist möglicherweise abgelaufen.');
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

export default apiClient;