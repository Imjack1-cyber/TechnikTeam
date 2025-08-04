import {
	startRegistration,
	startAuthentication,
} from '@simplewebauthn/browser';
import apiClient from './apiClient';

const passkeyService = {
	/**
	 * Registers a new passkey for the currently logged-in user.
	 * @param {string} deviceName A user-friendly name for the new passkey/device.
	 * @returns {Promise<boolean>} A promise that resolves to true on success.
	 */
	async register(deviceName) {
		// 1. Get registration options from the server
		const startResponse = await apiClient.post('/passkey/register/start');
		if (!startResponse.success) {
			throw new Error(startResponse.message || "Could not start registration.");
		}

		// 2. Use the browser's WebAuthn API to create the credential
		let attestationResponse;
		try {
			attestationResponse = await startRegistration(startResponse.data);
		} catch (error) {
			// This can happen if the user cancels the browser prompt.
			console.error("Browser registration failed:", error);
			throw new Error("Registration was cancelled or failed in the browser.");
		}

		// 3. Send the new credential to the server to finish registration
		const finishResponse = await apiClient.post('/passkey/register/finish', {
			deviceName: deviceName,
			credential: JSON.stringify(attestationResponse),
		});

		if (!finishResponse.success) {
			throw new Error(finishResponse.message || "Server could not verify the new credential.");
		}

		return true;
	},

	/**
	 * Authenticates a user with a passkey.
	 * @param {string} username The username of the user to authenticate.
	 * @returns {Promise<object|null>} A promise that resolves with the user object on success, or null on failure.
	 */
	async login(username) {
		// 1. Get authentication options from the server
		const startResponse = await apiClient.post('/passkey/login/start', { username });
		if (!startResponse.success) {
			throw new Error(startResponse.message || "Could not start authentication.");
		}

		// 2. Use the browser's WebAuthn API to get an assertion
		let assertionResponse;
		try {
			assertionResponse = await startAuthentication(startResponse.data);
		} catch (error) {
			console.error("Browser authentication failed:", error);
			throw new Error("Authentication was cancelled or failed in the browser.");
		}

		// 3. Send the assertion to the server to finish authentication
		const finishResponse = await apiClient.post('/passkey/login/finish', {
			credential: JSON.stringify(assertionResponse),
		});

		if (finishResponse.success && finishResponse.data) {
			return finishResponse.data; // Return the user object
		} else {
			throw new Error(finishResponse.message || "Server could not verify the authentication.");
		}
	},

	/**
	 * Deletes a passkey credential for the logged-in user.
	 * @param {number} credentialDbId The database ID of the credential to delete.
	 * @returns {Promise<boolean>} A promise that resolves to true on success.
	 */
	async deleteCredential(credentialDbId) {
		const response = await apiClient.delete(`/passkey/credentials/${credentialDbId}`);
		if (!response.success) {
			throw new Error(response.message || 'Could not delete passkey.');
		}
		return true;
	},
};

export { passkeyService };