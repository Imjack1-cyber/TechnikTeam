// Native implementation of the passkey service using expo-passkeys
const Passkeys = require('expo-passkeys');

/**
 * Ensures that allowCredentials and excludeCredentials arrays are valid
 * and that transports fields are arrays or removed.
 */
const sanitizeOptions = (options) => {
  const sanitized = { ...options };

  const sanitizeArray = (arr) => {
    if (!arr || !Array.isArray(arr)) return undefined;
    return arr.map(item => {
      const newItem = { ...item };
      if (newItem.transports && !Array.isArray(newItem.transports)) {
        delete newItem.transports;
      }
      return newItem;
    });
  };

  sanitized.allowCredentials = sanitizeArray(sanitized.allowCredentials);
  sanitized.excludeCredentials = sanitizeArray(sanitized.excludeCredentials);

  return sanitized;
};

/**
 * Start registration on native devices
 * @param {object} options PublicKeyCredentialCreationOptions from server
 */
const startRegistration = async (options) => {
  if (!Passkeys || typeof Passkeys.create !== 'function') {
    throw new Error('expo-passkeys is not available or create() is undefined');
  }

  const sanitizedOptions = sanitizeOptions(options);
  const credential = await Passkeys.create(sanitizedOptions);
  return credential;
};

/**
 * Start authentication on native devices
 * @param {object} options PublicKeyCredentialRequestOptions from server
 */
const startAuthentication = async (options) => {
  if (!Passkeys || typeof Passkeys.get !== 'function') {
    throw new Error('expo-passkeys is not available or get() is undefined');
  }

  const sanitizedOptions = sanitizeOptions(options);
  const credential = await Passkeys.get(sanitizedOptions);
  return credential;
};

/**
 * Returns user-friendly error messages for native passkeys
 */
const getFriendlyPasskeyErrorMessage = (err) => {
  if (!err) return 'Ein unbekannter Fehler ist aufgetreten.';
  console.error('Native Passkey Error:', err);

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

// Export using CommonJS to match the dispatcher
module.exports.passkeyService = {
  startRegistration,
  startAuthentication,
  getFriendlyPasskeyErrorMessage,
};