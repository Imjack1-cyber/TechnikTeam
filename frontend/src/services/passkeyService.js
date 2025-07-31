import apiClient from './apiClient';
import { useAuthStore } from '../store/authStore';

const bufferDecode = (value) => Uint8Array.from(atob(value.replace(/_/g, '/').replace(/-/g, '+')), c => c.charCodeAt(0));
const bufferEncode = (value) => btoa(String.fromCharCode.apply(null, new Uint8Array(value))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');

const registerPasskey = async (deviceName) => {
	try {
		const createOptionsResponse = await apiClient.post('/auth/passkey/register/start', { deviceName });

		if (!createOptionsResponse.success) {
			throw new Error(createOptionsResponse.message);
		}

		const createOptions = JSON.parse(createOptionsResponse.data);

		createOptions.challenge = bufferDecode(createOptions.challenge);
		createOptions.user.id = bufferDecode(createOptions.user.id);

		const credential = await navigator.credentials.create({ publicKey: createOptions });

		const credentialForServer = {
			id: credential.id,
			rawId: bufferEncode(credential.rawId),
			type: credential.type,
			response: {
				clientDataJSON: bufferEncode(credential.response.clientDataJSON),
				attestationObject: bufferEncode(credential.response.attestationObject),
			},
		};

		const result = await apiClient.post(`/auth/passkey/register/finish?deviceName=${encodeURIComponent(deviceName)}`, credentialForServer);

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
		const getOptionsResponse = await apiClient.post(`/auth/passkey/login/start?username=${encodeURIComponent(username)}`);
		if (!getOptionsResponse.success) {
			throw new Error(getOptionsResponse.message);
		}

		const getOptions = JSON.parse(getOptionsResponse.data);
		getOptions.challenge = bufferDecode(getOptions.challenge);
		if (getOptions.allowCredentials) {
			for (let cred of getOptions.allowCredentials) {
				cred.id = bufferDecode(cred.id);
			}
		}

		const credential = await navigator.credentials.get({ publicKey: getOptions });

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

		const result = await apiClient.post('/auth/passkey/login/finish', credentialForServer);
		if (result.success && result.data.token) {
			const { token, fetchUserSession } = useAuthStore.getState();
			useAuthStore.setState({ token: result.data.token });
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