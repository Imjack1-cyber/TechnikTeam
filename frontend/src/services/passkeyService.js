import apiClient from './apiClient';

// Helper functions to convert between ArrayBuffer and Base64URL
const bufferDecode = (value) => Uint8Array.from(atob(value.replace(/_/g, '/').replace(/-/g, '+')), c => c.charCodeAt(0));
const bufferEncode = (value) => btoa(String.fromCharCode.apply(null, new Uint8Array(value))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');

const registerPasskey = async (deviceName) => {
    try {
        // 1. Get challenge from server
        const createOptions = await apiClient.get('/auth/passkey/register/start');

        // 2. Decode challenge and user handle for the browser API
        createOptions.challenge = bufferDecode(createOptions.challenge);
        createOptions.user.id = bufferDecode(createOptions.user.id);

        // 3. Call browser's WebAuthn API
        const credential = await navigator.credentials.create({ publicKey: createOptions });

        // 4. Encode the response data to send back to the server
        const credentialForServer = {
            id: credential.id,
            rawId: bufferEncode(credential.rawId),
            type: credential.type,
            response: {
                clientDataJSON: bufferEncode(credential.response.clientDataJSON),
                attestationObject: bufferEncode(credential.response.attestationObject),
            },
        };

        // 5. Send response to server to finish registration
        const result = await apiClient.post(`/auth/passkey/register/finish?deviceName=${encodeURIComponent(deviceName)}`, credentialForServer);
        
        if (!result.success) {
            throw new Error(result.message || 'Registration failed on the server.');
        }

        return result;

    } catch (err) {
        console.error('Passkey registration process failed:', err);
        throw err; // Re-throw to be caught by the UI component
    }
};

export const passkeyService = {
    registerPasskey,
};