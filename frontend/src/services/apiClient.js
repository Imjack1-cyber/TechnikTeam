import { Platform } from 'react-native';

const ANDROID_API_URL = 'http://10.0.2.2:8081/TechnikTeam/api/v1';
// For web, we use a relative path. The browser will handle the host and protocol (HTTPS).
const WEB_API_URL = '/TechnikTeam/api/v1';

const BASE_URL = Platform.OS === 'web' ? WEB_API_URL : ANDROID_API_URL;

let onUnauthorizedCallback = () => {};
let onMaintenanceCallback = () => {};
let authToken = null;

const apiClient = {
	setup: function(callbacks) {
		onUnauthorizedCallback = callbacks.onUnauthorized;
		onMaintenanceCallback = callbacks.onMaintenance;
	},
	setAuthToken: function(token) {
		authToken = token;
	},
    getBaseUrl: function() {
        return Platform.OS === 'web' ? '' : 'http://10.0.2.2:8081/TechnikTeam';
    },
	request: async function(endpoint, options = {}) {
		const headers = { ...options.headers };
		if (authToken) {
			headers['Authorization'] = `Bearer ${authToken}`;
		}
		if (!(options.body instanceof FormData)) {
			headers['Content-Type'] = 'application/json';
		}
		try {
			const response = await fetch(`${BASE_URL}${endpoint}`, {
				...options,
				headers: headers,
			});
			const contentType = response.headers.get("content-type");
			const isJson = contentType && contentType.includes("application/json");
			if (response.status === 503) {
				onMaintenanceCallback();
				throw new Error('Die Anwendung befindet sich im Wartungsmodus.');
			}
			if (response.status === 401) {
				if (isJson) {
					const errorResult = await response.json();
					if (errorResult.message) throw new Error(errorResult.message);
				}
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
				throw new Error(`Serververbindung fehlgeschlagen (Status: ${response.status}).`);
			}
			const result = await response.json();
			if (!response.ok) {
				if (response.status >= 500) {
					throw new Error("Ein interner Serverfehler ist aufgetreten.");
				}
				throw new Error(result.message || `Ein Fehler ist aufgetreten (Status: ${response.status})`);
			}
			return result;
		} catch (error) {
			if (error.message.includes('Network request failed') || error.message.includes('Failed to fetch')) {
				console.error(`API Client Network Error: ${options.method || 'GET'} ${BASE_URL}${endpoint}`, error);
				throw new Error('Netzwerkfehler: Das Backend ist nicht erreichbar.');
			}
			console.error(`API Client Error: ${options.method || 'GET'} ${BASE_URL}${endpoint}`, error);
			throw error;
		}
	},
	get(endpoint) {
		return this.request(endpoint, { method: 'GET' });
	},
	post(endpoint, body) {
		const options = { method: 'POST', body: body instanceof FormData ? body : JSON.stringify(body) };
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