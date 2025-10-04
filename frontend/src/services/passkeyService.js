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


// Recursively convert PublicKeyCredential and its nested ArrayBuffers to a JSON-compatible object
// with Base64URL-encoded binary data.
function publicKeyCredentialToJson(cred) {
    if (!cred) return cred;

    if (cred instanceof Array) {
        return cred.map(publicKeyCredentialToJson);
    }

    if (cred instanceof ArrayBuffer) {
        return bufferToBase64Url(cred);
    }

    if (cred instanceof Object) {
        const obj = {};
        for (const key in cred) {
            if (Object.prototype.hasOwnProperty.call(cred, key)) {
                obj[key] = publicKeyCredentialToJson(cred[key]);
            }
        }
        return obj;
    }

    return cred;
}


const startRegistration = async (options) => {
    // The server sends JSON with Base64URL strings. We need to convert them to ArrayBuffers for the browser API.
    const publicKey = {
        ...options,
        challenge: base64UrlToBuffer(options.challenge),
        user: {
            ...options.user,
            id: base64UrlToBuffer(options.user.id),
        },
    };

    if (publicKey.excludeCredentials) {
        publicKey.excludeCredentials = publicKey.excludeCredentials.map(cred => ({
            ...cred,
            id: base64UrlToBuffer(cred.id),
        }));
    }

    const credential = await navigator.credentials.create({ publicKey });

    // Manually construct a clean JSON object for the backend, converting all ArrayBuffers to Base64URL.
    // This avoids sending extra fields like `publicKeyAlgorithm` that the server parser rejects.
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


const startAuthentication = async (options) => {
    // Convert Base64URL strings in options back to ArrayBuffers for navigator.credentials.get
    const publicKey = {
        ...options,
        challenge: base64UrlToBuffer(options.challenge),
    };

    if (publicKey.allowCredentials) {
        publicKey.allowCredentials = publicKey.allowCredentials.map(cred => {
            const newCred = {
                type: cred.type,
                id: base64UrlToBuffer(cred.id),
            };
            // The transports property is optional, but if present, it must be an array.
            // This handles cases where it might be null from the backend.
            if (Array.isArray(cred.transports)) {
                newCred.transports = cred.transports;
            }
            return newCred;
        });
    }

    const credential = await navigator.credentials.get({ publicKey });

    // Manually construct a clean JSON object for the backend.
    const jsonFriendlyCredential = {
        id: credential.id,
        rawId: bufferToBase64Url(credential.rawId),
        type: credential.type,
        response: {
            authenticatorData: bufferToBase64Url(credential.response.authenticatorData),
            clientDataJSON: bufferToBase64Url(credential.response.clientDataJSON),
            signature: bufferToBase64Url(credential.response.signature),
            userHandle: credential.response.userHandle ? bufferToBase64Url(credential.response.userHandle) : null,
        },
        clientExtensionResults: credential.getClientExtensionResults(),
    };

    return jsonFriendlyCredential;
};

export const passkeyService = {
    startRegistration,
    startAuthentication
};