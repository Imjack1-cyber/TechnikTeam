// Helper functions to convert between ArrayBuffer and Base64URL
function bufferToBase64Url(buffer) {
    if (!buffer) return null;
    return btoa(String.fromCharCode(...new Uint8Array(buffer)))
        .replace(/=/g, '')
        .replace(/\+/g, '-')
        .replace(/\//g, '_');
}

function base64UrlToBuffer(base64Url) {
    if (typeof base64Url !== 'string') {
        throw new TypeError('Expected a Base64URL string.');
    }
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const padLength = (4 - (base64.length % 4)) % 4;
    const padded = base64 + '='.repeat(padLength);
    const binary = atob(padded);
    const buffer = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
        buffer[i] = binary.charCodeAt(i);
    }
    return buffer.buffer;
}

// --- Shared sanitizer for credential descriptors (used in both registration & auth)
function sanitizeCredentialDescriptors(creds) {
    if (!Array.isArray(creds)) return undefined;

    return creds
        .map(cred => {
            if (!cred || typeof cred.id !== 'string') return null;
            const clean = {
                type: cred.type || 'public-key',
                id: base64UrlToBuffer(cred.id),
            };
            // Only include transports if it's a valid non-empty array of strings
            if (Array.isArray(cred.transports) && cred.transports.every(t => typeof t === 'string')) {
                clean.transports = cred.transports;
            }
            return clean;
        })
        .filter(Boolean);
}

// --- WebAuthn Registration
const startRegistration = async (options) => {
    if (!options || !options.challenge) {
        throw new Error("Missing required WebAuthn registration options from server.");
    }

    const publicKey = {
        ...options,
        challenge: base64UrlToBuffer(options.challenge),
        user: {
            ...options.user,
            id: base64UrlToBuffer(options.user.id),
        },
        excludeCredentials: sanitizeCredentialDescriptors(options.excludeCredentials),
    };

    console.log("[Passkey] Starting registration with sanitized options:", publicKey);

    const credential = await navigator.credentials.create({ publicKey });

    // Convert to JSON-friendly format for backend
    const jsonFriendlyCredential = {
        id: credential.id,
        rawId: bufferToBase64Url(credential.rawId),
        type: credential.type,
        response: {
            attestationObject: bufferToBase64Url(credential.response.attestationObject),
            clientDataJSON: bufferToBase64Url(credential.response.clientDataJSON),
        },
        clientExtensionResults: credential.getClientExtensionResults(),
    };

    return jsonFriendlyCredential;
};

// --- WebAuthn Authentication
const startAuthentication = async (options) => {
    if (!options || !options.challenge) {
        throw new Error("Missing required WebAuthn authentication options from server.");
    }

    const publicKey = {
        ...options,
        challenge: base64UrlToBuffer(options.challenge),
        allowCredentials: sanitizeCredentialDescriptors(options.allowCredentials),
    };

    console.log("[Passkey] Starting authentication with sanitized options:", publicKey);

    const credential = await navigator.credentials.get({ publicKey });

    const jsonFriendlyCredential = {
        id: credential.id,
        rawId: bufferToBase64Url(credential.rawId),
        type: credential.type,
        response: {
            authenticatorData: bufferToBase64Url(credential.response.authenticatorData),
            clientDataJSON: bufferToBase64Url(credential.response.clientDataJSON),
            signature: bufferToBase64Url(credential.response.signature),
            userHandle: credential.response.userHandle
                ? bufferToBase64Url(credential.response.userHandle)
                : null,
        },
        clientExtensionResults: credential.getClientExtensionResults(),
    };

    return jsonFriendlyCredential;
};

// --- Friendly error message generator
const getFriendlyPasskeyErrorMessage = (err) => {
    console.error("Passkey Error:", err);

    if (err.name === 'NotAllowedError') {
        return 'Der Vorgang wurde vom Benutzer abgebrochen oder es wurden keine passenden Passkeys auf diesem Gerät gefunden.';
    }
    if (err.name === 'InvalidStateError') {
        return 'Dieser Passkey ist bereits registriert oder wird aktuell verwendet.';
    }
    if (err.name === 'NotSupportedError') {
        return 'Ihr Browser oder Gerät unterstützt Passkeys nicht.';
    }
    if (err.name === 'SecurityError') {
        return 'Sicherheitsfehler. Passkeys können nur über eine sichere HTTPS-Verbindung verwendet werden.';
    }
    if (err.message?.includes('transports')) {
        return 'Ungültige Passkey-Daten vom Server empfangen (transports-Feld fehlerhaft). Bitte erneut versuchen.';
    }

    return err.message || 'Ein unbekannter Fehler ist aufgetreten. Bitte versuchen Sie es erneut.';
};

// --- ES Module export
export const passkeyService = {
    startRegistration,
    startAuthentication,
    getFriendlyPasskeyErrorMessage
};