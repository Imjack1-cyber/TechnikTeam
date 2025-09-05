import { Platform } from 'react-native';
import { useAuthStore } from '../store/authStore';

const getApiBaseUrl = () => {
    const mode = useAuthStore.getState().backendMode;
    if (Platform.OS === 'web') {
        return '/TechnikTeam/api/v1';
    }
    const host = mode === 'dev' ? 'technikteamdev.duckdns.org' : 'technikteam.duckdns.org';
    return `https://${host}/TechnikTeam/api/v1`;
};

const getRootUrl = () => {
    const mode = useAuthStore.getState().backendMode;
    if (Platform.OS === 'web') {
        return '';
    }
    const host = mode === 'dev' ? 'technikteamdev.duckdns.org' : 'technikteam.duckdns.org';
    return `https://${host}/TechnikTeam`;
};

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
    getRootUrl: getRootUrl,
	request: async function(endpoint, options = {}) {
		const headers = { ...options.headers };
		if (authToken) {
			headers['Authorization'] = `Bearer ${authToken}`;
		}
		if (!(options.body instanceof FormData)) {
			headers['Content-Type'] = 'application/json';
		}
		try {
            const baseUrl = getApiBaseUrl();
			const response = await fetch(`${baseUrl}${endpoint}`, {
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
                await onUnauthorizedCallback();
                const authError = new Error('Session expired and user logged out.');
                authError.isAuthError = true;
                throw authError;
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
			if (error.isAuthError) {
                throw error;
            }
            const baseUrl = getApiBaseUrl();
			if (error.message.includes('Network request failed') || error.message.includes('Failed to fetch')) {
				console.error(`API Client Network Error: ${options.method || 'GET'} ${baseUrl}${endpoint}`, error);
				throw new Error('Netzwerkfehler: Das Backend ist nicht erreichbar.');
			}
			console.error(`API Client Error: ${options.method || 'GET'} ${baseUrl}${endpoint}`, error);
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