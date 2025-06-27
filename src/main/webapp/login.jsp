<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- FIX: This page no longer imports the standard header/nav to be a clean, standalone page. --%>
<!DOCTYPE html>
<html lang="de" data-theme="light">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Anmeldung - TechnikTeam</title>
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

	<div class="login-page-container">
		<div class="login-box">
			<h1>
				<i class="fas fa-bolt"></i> TechnikTeam
			</h1>

			<c:if test="${not empty errorMessage}">
				<p class="error-message" id="error-message">
					<c:out value="${errorMessage}" />
				</p>
			</c:if>

			<form action="<c:url value='/login'/>" method="post">
				<div class="form-group">
					<label for="username">Benutzername</label> <input type="text"
						id="username" name="username" required autocomplete="username"
						autofocus>
				</div>
				<div class="form-group">
					<label for="password">Passwort</label> <input type="password"
						id="password" name="password" required
						autocomplete="current-password">
				</div>
				<button type="submit" class="btn" style="width: 100%;">Anmelden</button>
			</form>
			<div
				style="text-align: center; margin: 1rem 0; color: var(--text-muted-color);">ODER</div>
			<button id="passkey-login-btn" class="btn"
				style="width: 100%; background-color: var(--text-muted-color); border-color: var(--text-muted-color);">
				<i class="fas fa-fingerprint"></i> Mit Passkey anmelden
			</button>
		</div>
	</div>

	<script>
// This script is self-contained as main.js is not included on this page.
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = document.body.dataset.contextPath || '';

    // Passkey Login Logic
    const passkeyLoginBtn = document.getElementById('passkey-login-btn');
    const errorMessageElement = document.getElementById('error-message');

    if (passkeyLoginBtn) {
        passkeyLoginBtn.addEventListener('click', async () => {
            try {
                const startResp = await fetch(`${contextPath}/api/passkey/login/start`, { method: 'POST' });
                if (!startResp.ok) throw new Error('Could not start passkey login.');
                const requestOptionsJson = await startResp.json();
                
                // Decode from server's Base64URL to ArrayBuffer for the browser API
                requestOptionsJson.challenge = bufferDecode(requestOptionsJson.challenge);
                if (requestOptionsJson.allowCredentials) {
                    for (let cred of requestOptionsJson.allowCredentials) {
                        cred.id = bufferDecode(cred.id);
                    }
                }

                const assertion = await navigator.credentials.get({ publicKey: requestOptionsJson });

                const verificationResp = await fetch(`${contextPath}/api/passkey/login/finish`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        id: assertion.id,
                        rawId: bufferEncode(assertion.rawId),
                        response: {
                            authenticatorData: bufferEncode(assertion.response.authenticatorData),
                            clientDataJSON: bufferEncode(assertion.response.clientDataJSON),
                            signature: bufferEncode(assertion.response.signature),
                            userHandle: assertion.response.userHandle ? bufferEncode(assertion.response.userHandle) : null,
                        },
                        type: assertion.type
                    })
                });

                if (verificationResp.ok) {
                    window.location.href = `${contextPath}/home`;
                } else {
                    const errorText = await verificationResp.text();
                    if(errorMessageElement) errorMessageElement.textContent = `Passkey Login fehlgeschlagen: ${errorText}`;
                }

            } catch (err) {
                console.error('Passkey login error:', err);
                if(errorMessageElement) errorMessageElement.textContent = 'Passkey-Operation fehlgeschlagen oder abgebrochen.';
            }
        });
    }

    // Helper functions for base64url encoding/decoding
    function bufferDecode(value) {
        const str = value.replace(/-/g, '+').replace(/_/g, '/');
        const decoded = atob(str);
        const buffer = new Uint8Array(decoded.length);
        for (let i = 0; i < decoded.length; i++) {
            buffer[i] = decoded.charCodeAt(i);
        }
        return buffer.buffer;
    }

    function bufferEncode(value) {
        return btoa(String.fromCharCode.apply(null, new Uint8Array(value)))
            .replace(/\+/g, '-')
            .replace(/\//g, '_')
            .replace(/=/g, '');
    }
});
</script>
</body>
</html>