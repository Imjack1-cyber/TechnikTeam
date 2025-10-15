import * as WebBrowser from 'expo-web-browser';
import * as Linking from 'expo-linking';
import apiClient from './apiClient';
import { useAuthStore } from '../store/authStore';

// Get the correct base URL for the web version
const getWebUrl = () => {
    const mode = useAuthStore.getState().backendMode;
    const host = mode === 'dev' ? 'technikteamdev.qs0.de' : 'technikteam.qs0.de';
    return `https://${host}/TechnikTeam`;
};

/**
 * Start passkey registration by opening the web profile page in an in-app browser.
 * An SSO token is generated to automatically log the user in on the web.
 */
const startRegistration = async () => {
    try {
        // Request a single-use SSO token from the backend
        const ssoResult = await apiClient.post('/public/profile/sso-token');
        if (!ssoResult.success) {
            throw new Error('Could not generate a temporary login token.');
        }
        
        // Construct the URL to the web profile page with the token and an action
        const webUrl = getWebUrl();
        const url = `${webUrl}/profil?action=register-passkey&sso_token=${ssoResult.data.token}`;
        
        // Open the in-app browser
        await WebBrowser.openBrowserAsync(url);
    } catch (err) {
        // We throw the error so the UI can catch it and display a message
        throw err;
    }
};

/**
 * Start passkey authentication by opening the web login page.
 * After successful login, the web app will redirect back to the native app
 * via a deep link, carrying a new session token.
 */
const startAuthentication = async () => {
    try {
        const webUrl = getWebUrl();
        // The redirect URL is the app's custom scheme
        const redirectUrl = Linking.createURL('sso');
        // The clientType=native tells the backend to issue a long-lived token
        const url = `${webUrl}/login?clientType=native&redirect_uri=${encodeURIComponent(redirectUrl)}`;

        // Open the in-app browser to the login page
        await WebBrowser.openBrowserAsync(url);
    } catch (err) {
        throw err;
    }
};

const getFriendlyPasskeyErrorMessage = (err) => {
  if (!err) return 'Ein unbekannter Fehler ist aufgetreten.';
  console.error('Passkey Web Flow Error:', err);
  return err.message || 'Ein unbekannter Fehler ist aufgetreten.';
};

// Export the new web-based native implementation
export const passkeyService = {
  startRegistration,
  startAuthentication,
  getFriendlyPasskeyErrorMessage,
};