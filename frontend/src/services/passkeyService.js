import apiClient from './apiClient';

const bufferDecode = (value) => Uint8Array.from(atob(value.replace(/_/g, '/').replace(/-/g, '+')), c => c.charCodeAt(0));
const bufferEncode = (value) => btoa(String.fromCharCode.apply(null, new Uint8Array(value))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');

const registerPasskey = async (deviceName) => {
	try {
		const createOptions = await apiClient.get('/auth/passkey/register/start');

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

export const passkeyService = {
	registerPasskey,
};