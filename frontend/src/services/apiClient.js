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

		const method = options.method || 'GET';

		// Set Content-Type for JSON, but not for FormData, which needs the browser to set it.
		if (!(options.body instanceof FormData)) {
			headers['Content-Type'] = 'application/json';
		}

		// For any state-changing request, add the CSRF token header if it exists.
		if (['POST', 'PUT', 'DELETE'].includes(method.toUpperCase())) {
			const match = document.cookie.match(new RegExp('(^| )' + 'XSRF-TOKEN' + '=([^;]+)'));
			if (match) {
				headers['X-XSRF-TOKEN'] = match[2];
			} else {
				if (endpoint !== '/auth/login') {
					console.warn('CSRF token not found. State-changing requests may fail.');
				}
			}
		}

		try {
			const response = await fetch(`${BASE_URL}${endpoint}`, {
				...options,
				headers: headers,
				credentials: 'include'
			});

			if (response.status === 401) {
				onUnauthorizedCallback();
				throw new Error('Nicht autorisiert. Ihre Sitzung ist möglicherweise abgelaufen.');
			}
			if (response.status === 403) {
				// This should now only happen for CSRF errors.
				throw new Error('Zugriff verweigert. Möglicherweise ist ein CSRF-Token-Problem aufgetreten.');
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