import { AppState } from 'react-native';
// In a real app, this would come from an environment config file
const BASE_URL = 'http://10.0.2.2:8081/TechnikTeam/api/v1'; // Android emulator default

let onUnauthorizedCallback = () => { }; // Placeholder for the logout function
let authToken = null; // Module-level variable to hold the token
let onMaintenanceCallback = () => { }; // Placeholder for navigation to maintenance screen

const apiClient = {
	setup: function(callbacks) {
		onUnauthorizedCallback = callbacks.onUnauthorized;
		onMaintenanceCallback = callbacks.onMaintenance;
	},

	setAuthToken: function(token) {
		authToken = token;
	},

	request: async function(endpoint, options = {}) {
		const headers = {
			...options.headers,
		};

		if (authToken) {
			headers['Authorization'] = `Bearer ${authToken}`;
		}

		// Set Content-Type for JSON, but not for FormData, which needs the browser to set it.
		if (!(options.body instanceof FormData)) {
			headers['Content-Type'] = 'application/json';
		}

		try {
			const response = await fetch(`${BASE_URL}${endpoint}`, {
				...options,
				headers: headers,
				// credentials: 'include' is a browser-specific concept for cookies, not needed for token auth
			});

			const contentType = response.headers.get("content-type");
			const isJson = contentType && contentType.includes("application/json");

			if (response.status === 503) {
				// In React Native, we can't redirect. We trigger a callback to handle navigation.
				onMaintenanceCallback();
				throw new Error('Die Anwendung befindet sich im Wartungsmodus.');
			}

			if (response.status === 401) {
				// Handle 401 specifically: could be a login failure or an expired session.
				if (isJson) {
					const errorResult = await response.json();
					// If there's a specific message (like "Wrong username or password"), throw it.
					if (errorResult.message) {
						throw new Error(errorResult.message);
					}
				}
				// Otherwise, it's a generic unauthorized, likely an expired session.
				onUnauthorizedCallback();
				throw new Error('Nicht autorisiert. Ihre Sitzung ist möglicherweise abgelaufen.');
			}

			if (response.status === 403) {
				if (isJson) {
					const errorResult = await response.json();
					throw new Error(errorResult.message || 'Zugriff verweigert.');
				}
				throw new Error('Zugriff verweigert.');
			}

			if (response.status === 204) {
				return { success: true, message: 'Operation successful.', data: null };
			}

			if (!isJson) {
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
			if (error instanceof TypeError && error.message.includes('Network request failed')) {
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