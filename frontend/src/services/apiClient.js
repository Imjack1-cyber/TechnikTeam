const BASE_URL = '/api/v1';

let onUnauthorizedCallback = () => { }; // Placeholder for the logout function

const apiClient = {
	setup: function(callbacks) {
		onUnauthorizedCallback = callbacks.onUnauthorized;
	},

	request: async function(endpoint, options = {}) {
		const headers = {
			...options.headers,
		};

		// Set Content-Type for JSON, but not for FormData, which needs the browser to set it.
		if (!(options.body instanceof FormData)) {
			headers['Content-Type'] = 'application/json';
		}

		try {
			const response = await fetch(`${BASE_URL}${endpoint}`, {
				...options,
				headers: headers,
				credentials: 'include'
			});

			const contentType = response.headers.get("content-type");
			const isJson = contentType && contentType.includes("application/json");

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