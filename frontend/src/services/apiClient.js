import { useAuthStore } from '../store/authStore';

const BASE_URL = '/api/v1';

const apiClient = {
	async request(endpoint, options = {}) {
		const { token, logout } = useAuthStore.getState();

		const headers = {
			'Content-Type': 'application/json',
			...options.headers,
		};

		if (token) {
			headers['Authorization'] = `Bearer ${token}`;
		}

		if (options.body instanceof FormData) {
			delete headers['Content-Type'];
		}

		try {
			const response = await fetch(`${BASE_URL}${endpoint}`, {
				...options,
				headers: headers,
			});

			if (response.status === 401) {
				logout();
				throw new Error('Unauthorized: Session has expired. Please log in again.');
			}

			if (response.status === 204) {
				return { success: true, message: 'Operation successful.', data: null };
			}

			const result = await response.json();

			if (!response.ok) {
				throw new Error(result.message || `HTTP error! status: ${response.status}`);
			}

			return result;

		} catch (error) {
			console.error(`API Client Error: ${options.method || 'GET'} ${endpoint}`, error);
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