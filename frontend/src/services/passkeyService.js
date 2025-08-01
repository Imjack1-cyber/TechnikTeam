import apiClient from './apiClient';
import { useAuthStore } from '../store/authStore';

// These functions handle the conversion between ArrayBuffer/Uint8Array and Base64URL encoding
const bufferDecode = (value) => Uint8Array.from(atob(value.replace(/_/g, '/').replace(/-/g, '+')), c => c.charCodeAt(0));
const bufferEncode = (value) => btoa(String.fromCharCode.apply(null, new Uint8Array(value))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');

const registerPasskey = async (deviceName) => {
	try {
		// Step 1: Get options from the server
		const createOptionsResponse = await apiClient.post('/auth/passkey/register/start', { deviceName });

		if (!createOptionsResponse.success) {
			throw new Error(createOptionsResponse.message);
		}

		const createOptions = createOptionsResponse.data;

		// Step 2: Decode server options from Base64URL to ArrayBuffers
		createOptions.challenge = bufferDecode(createOptions.challenge);
		createOptions.user.id = bufferDecode(createOptions.user.id);
		if (createOptions.excludeCredentials) {
			for (let cred of createOptions.excludeCredentials) {
				cred.id = bufferDecode(cred.id);
			}
		}

		// Step 3: Call the WebAuthn API to create the credential
		const credential = await navigator.credentials.create({ publicKey: createOptions });

		// Step 4: Encode the browser's response to Base64URL for the server
		const credentialForServer = {
			id: credential.id,
			rawId: bufferEncode(credential.rawId),
			type: credential.type,
			response: {
				clientDataJSON: bufferEncode(credential.response.clientDataJSON),
				attestationObject: bufferEncode(credential.response.attestationObject),
			},
		};

		// Step 5: Send the encoded response to the server to finish registration
		const result = await apiClient.post(`/auth/passkey/register/finish`, credentialForServer);

		if (!result.success) {
			throw new Error(result.message || 'Registration failed on the server.');
		}

		return result;

	} catch (err) {
		console.error('Passkey registration process failed:', err);
		throw err;
	}
};

const loginWithPasskey = async (username) => {
	try {
		// Step 1: Get options from the server
		const getOptionsResponse = await apiClient.post(`/auth/passkey/login/start?username=${encodeURIComponent(username)}`);
		if (!getOptionsResponse.success) {
			throw new Error(getOptionsResponse.message);
		}

		const getOptions = getOptionsResponse.data;

		// Step 2: Decode server options from Base64URL to ArrayBuffers
		getOptions.challenge = bufferDecode(getOptions.challenge);
		if (getOptions.allowCredentials) {
			for (let cred of getOptions.allowCredentials) {
				cred.id = bufferDecode(cred.id);
			}
		}

		// Step 3: Call the WebAuthn API to get the credential assertion
		const credential = await navigator.credentials.get({ publicKey: getOptions });

		// Step 4: Encode the browser's response to Base64URL for the server
		const credentialForServer = {
			id: credential.id,
			rawId: bufferEncode(credential.rawId),
			type: credential.type,
			response: {
				authenticatorData: bufferEncode(credential.response.authenticatorData),
				clientDataJSON: bufferEncode(credential.response.clientDataJSON),
				signature: bufferEncode(credential.response.signature),
				userHandle: credential.response.userHandle ? bufferEncode(credential.response.userHandle) : null,
			},
		};

		// Step 5: Send the encoded assertion to the server to finish login
		const result = await apiClient.post('/auth/passkey/login/finish', credentialForServer);
		if (result.success) {
			// The backend now sets an HttpOnly cookie, we just need to refresh the session state
			const { fetchUserSession } = useAuthStore.getState();
			await fetchUserSession();
			return true;
		} else {
			throw new Error(result.message || 'Passkey login failed on server.');
		}

	} catch (err) {
		console.error('Passkey login process failed:', err);
		throw err;
	}
};

export const passkeyService = {
	registerPasskey,
	loginWithPasskey,
};