/**
 * Handles all client-side logic for WebAuthn (Passkey) authentication.
 */
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = document.body.dataset.contextPath || '';
    const csrfToken = document.body.dataset.csrfToken;

    // --- UTILITY FUNCTIONS ---
    // These functions convert between ArrayBuffer and Base64URL strings
    const bufferDecode = (value) => Uint8Array.from(atob(value.replace(/_/g, '/').replace(/-/g, '+')), c => c.charCodeAt(0));
    const bufferEncode = (value) => btoa(String.fromCharCode.apply(null, new Uint8Array(value))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');

    // --- REGISTRATION LOGIC ---
    const registerPasskeyBtn = document.getElementById('register-passkey-btn');
    if (registerPasskeyBtn) {
        registerPasskeyBtn.addEventListener('click', async () => {
            try {
                // 1. Get challenge from server
                const createOptionsRes = await fetch(`${contextPath}/api/auth/passkey/register/start`);
                const createOptions = await createOptionsRes.json();
                
                // 2. Decode challenge and user handle
                createOptions.challenge = bufferDecode(createOptions.challenge);
                createOptions.user.id = bufferDecode(createOptions.user.id);
                
                // 3. Call browser's WebAuthn API
                const credential = await navigator.credentials.create({ publicKey: createOptions });

                // 4. Encode the response data to send to server
                const credentialForServer = {
                    id: credential.id,
                    rawId: bufferEncode(credential.rawId),
                    type: credential.type,
                    response: {
                        clientDataJSON: bufferEncode(credential.response.clientDataJSON),
                        attestationObject: bufferEncode(credential.response.attestationObject),
                    },
                };
                
                // 5. Prompt for a device name
                const deviceName = prompt('Bitte geben Sie einen Namen für dieses Gerät ein (z.B. "Mein Laptop"):', 'Mein Gerät');
                if (!deviceName) return; // User cancelled

                // 6. Send response to server to finish registration
                const finishRes = await fetch(`${contextPath}/api/auth/passkey/register/finish?deviceName=${encodeURIComponent(deviceName)}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(credentialForServer)
                });

                const result = await finishRes.json();
                if (result.success) {
                    showToast('Gerät erfolgreich registriert!', 'success');
                    setTimeout(() => window.location.reload(), 1500);
                } else {
                    throw new Error(result.message || 'Registrierung fehlgeschlagen.');
                }
            } catch (err) {
                console.error('Passkey registration error:', err);
                showToast(`Fehler bei der Registrierung: ${err.message}`, 'danger');
            }
        });
    }
    
    // --- DELETE PASSKEY LOGIC ---
    document.querySelectorAll('.delete-passkey-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const form = btn.closest('form');
            showConfirmationModal('Diesen Passkey wirklich entfernen?', () => form.submit());
        });
    });

    // --- LOGIN LOGIC ---
    const loginPasskeyBtn = document.getElementById('login-passkey-btn');
    if (loginPasskeyBtn) {
        loginPasskeyBtn.addEventListener('click', async () => {
            try {
                const username = document.getElementById('username').value;
                if (!username) {
                    showToast('Bitte geben Sie zuerst Ihren Benutzernamen ein.', 'info');
                    return;
                }
                
                // 1. Get challenge from server
                const getOptionsRes = await fetch(`${contextPath}/api/auth/passkey/login/start`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: `username=${encodeURIComponent(username)}`
                });
                const getOptions = await getOptionsRes.json();

                // 2. Decode challenge
                getOptions.challenge = bufferDecode(getOptions.challenge);
                
                // 3. Call browser's WebAuthn API
                const credential = await navigator.credentials.get({ publicKey: getOptions });

                // 4. Encode response data for server
                const credentialForServer = {
                    id: credential.id,
                    rawId: bufferEncode(credential.rawId),
                    type: credential.type,
                    response: {
                        clientDataJSON: bufferEncode(credential.response.clientDataJSON),
                        authenticatorData: bufferEncode(credential.response.authenticatorData),
                        signature: bufferEncode(credential.response.signature),
                        userHandle: credential.response.userHandle ? bufferEncode(credential.response.userHandle) : null,
                    },
                };
                
                // 5. Send to server for verification
                const finishRes = await fetch(`${contextPath}/api/auth/passkey/login/finish`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(credentialForServer)
                });
                
                const result = await finishRes.json();
                if(result.success) {
                    showToast('Erfolgreich eingeloggt!', 'success');
                    window.location.href = `${contextPath}/home`;
                } else {
                    throw new Error(result.message || 'Login fehlgeschlagen.');
                }
            } catch (err) {
                console.error('Passkey login error:', err);
                showToast(`Fehler beim Login: ${err.message}`, 'danger');
            }
        });
    }
});