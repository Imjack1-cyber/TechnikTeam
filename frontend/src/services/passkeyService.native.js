// Native implementation of the passkey service using expo-passkeys.
import * as Passkeys from 'expo-passkeys';

/**
 * Initiates a passkey registration ceremony on a native device.
 * @param {object} options - The PublicKeyCredentialCreationOptions received from the backend.
 * @returns {Promise<object>} A JSON-friendly representation of the created credential.
 */
const startRegistration = async (options) => {
    console.log("[passkeyService.native] Starting registration with options:", options);
    try {
        const credential = await Passkeys.create(options);
        return credential;
    } catch (e) {
        // Re-throw the error to be handled by the caller, which will use getFriendlyPasskeyErrorMessage
        throw e;
    }
};

/**
 * Initiates a passkey authentication ceremony on a native device.
 * @param {object} options - The PublicKeyCredentialRequestOptions received from the backend.
 * @returns {Promise<object>} A JSON-friendly representation of the asserted credential.
 */
const startAuthentication = async (options) => {
    console.log("[passkeyService.native] Starting authentication with options:", options);
    try {
        const credential = await Passkeys.get(options);
        return credential;
    } catch (e) {
        // Re-throw the error
        throw e;
    }
};

/**
 * Provides user-friendly error messages for common native passkey errors.
 * @param {Error} err - The error object thrown by the passkey operation.
 * @returns {string} A user-friendly error message.
 */
const getFriendlyPasskeyErrorMessage = (err) => {
    console.error("Native Passkey Error:", err);
    if (err.code === 'ERR_PASSKEYS_CANCELLED') {
        return 'Der Vorgang wurde vom Benutzer abgebrochen.';
    }
    if (err.code === 'ERR_PASSKEYS_NOT_SUPPORTED') {
        return 'Ihr Gerät oder Betriebssystem unterstützt Passkeys nicht.';
    }
    if (err.code === 'ERR_PASSKEYS_UNKNOWN') {
        return 'Ein unbekannter nativer Fehler ist aufgetreten. Bitte versuchen Sie es erneut.';
    }
    return err.message || 'Ein unbekannter Fehler ist aufgetreten.';
};

module.exports.passkeyService = {
    startRegistration,
    startAuthentication,
    getFriendlyPasskeyErrorMessage
};