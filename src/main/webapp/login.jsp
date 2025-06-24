<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  login.jsp
  
  This is the main login page for the application. It provides a simple form
  for users to enter their username and password, with a toggle to show/hide
  the password. The form is submitted to the LoginServlet for authentication.
  
  - It is served by: LoginServlet (doGet).
  - It can also be the welcome-file defined in web.xml.
  - Expected attributes:
    - 'errorMessage' (String): An error message to display if login fails (optional).
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Anmeldung" />
</c:import>

<div class="login-wrapper">
	<div class="login-box">
		<h1>Willkommen zurück</h1>

		<c:if test="${not empty errorMessage}">
			<p class="error-message" id="error-message">
				<c:out value="${errorMessage}" />
			</p>
		</c:if>

		<form action="${pageContext.request.contextPath}/login" method="post">
			<div class="form-group">
				<label for="username">Benutzername</label> <input type="text"
					id="username" name="username" required autocomplete="username"
					autofocus>
			</div>
			<div class="form-group">
				<label for="password">Passwort</label>
				<div class="password-wrapper">
					<input type="password" id="password" name="password" required
						autocomplete="current-password"> <i
						class="fas fa-eye password-toggle"></i>
				</div>
			</div>
			<button type="submit" class="btn" style="width: 100%;">Anmelden</button>
		</form>
		<div
			style="text-align: center; margin: 1rem 0; color: var(--text-muted-color);">ODER</div>
		<button id="passkey-login-btn" class="btn btn-secondary"
			style="width: 100%;">
			<i class="fas fa-fingerprint"></i> Mit Passkey anmelden
		</button>
	</div>
</div>
<style>
/* Scoped styles for password visibility toggle */
.password-wrapper {
	position: relative;
}

.password-wrapper input {
	padding-right: 2.5rem; /* Make space for the icon */
}

.password-toggle {
	position: absolute;
	right: 1rem;
	top: 50%;
	transform: translateY(-50%);
	cursor: pointer;
	color: var(--text-muted-color);
}
</style>
<script>
// Attach event listener to all password toggles on the page
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = "${pageContext.request.contextPath}";

    document.querySelectorAll('.password-toggle').forEach(toggle => {
        toggle.addEventListener('click', () => {
            const passwordInput = toggle.previousElementSibling;
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            // Toggle the icon
            toggle.classList.toggle('fa-eye');
            toggle.classList.toggle('fa-eye-slash');
        });
    });

    // Passkey Login Logic
    const passkeyLoginBtn = document.getElementById('passkey-login-btn');
    const errorMessageElement = document.getElementById('error-message');

    if (passkeyLoginBtn) {
        passkeyLoginBtn.addEventListener('click', async () => {
            try {
                // 1. Start authentication
                const startResp = await fetch(`${contextPath}/api/passkey/login/start`, { method: 'POST' });
                if (!startResp.ok) throw new Error('Could not start passkey login.');
                const requestOptions = await startResp.json();
                
                // Convert base64url to ArrayBuffer
                requestOptions.challenge = bufferDecode(requestOptions.challenge);
                if (requestOptions.allowCredentials) {
                    for (let cred of requestOptions.allowCredentials) {
                        cred.id = bufferDecode(cred.id);
                    }
                }

                // 2. Prompt user with WebAuthn API
                const assertion = await navigator.credentials.get({ publicKey: requestOptions });

                // 3. Finish authentication
                const verificationResp = await fetch(`${contextPath}/api/passkey/login/finish?userHandle=${assertion.response.userHandle}`, {
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
                    window.location.href = `${contextPath}/home`; // Redirect on success
                } else {
                    const errorText = await verificationResp.text();
                    errorMessageElement.textContent = `Passkey Login fehlgeschlagen: ${errorText}`;
                    errorMessageElement.style.display = 'block';
                }

            } catch (err) {
                console.error('Passkey login error:', err);
                errorMessageElement.textContent = 'Passkey-Operation fehlgeschlagen oder abgebrochen.';
                errorMessageElement.style.display = 'block';
            }
        });
    }

    // Helper functions for base64url encoding/decoding
    function bufferDecode(value) {
        const str = value.replace(/-/g, '+').replace(/_/g, '/');
        const
			decoded = atob(str);
        const
			buffer = new Uint8Array(decoded.length);
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
<%-- The footer is omitted on the login page for a cleaner, focused look. --%>
</body>
</html>