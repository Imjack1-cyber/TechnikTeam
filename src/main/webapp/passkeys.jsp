<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
  passkeys.jsp
  
  This page allows a logged-in user to manage their registered Passkeys (WebAuthn credentials).
  
  - It is served by: PasskeyManagementServlet (doGet).
  - Expected attributes:
    - 'passkeys' (List<PasskeyCredential>): A list of the user's registered passkeys.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Passkeys verwalten" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Passkeys verwalten</h1>
<p>Hier können Sie Passkeys hinzufügen oder entfernen, um sich
	passwortlos anzumelden.</p>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">
		<c:out value="${sessionScope.successMessage}" />
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">
		<c:out value="${sessionScope.errorMessage}" />
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="card">
	<div class="table-controls">
		<h2>Ihre registrierten Passkeys</h2>
		<button id="add-passkey-btn" class="btn">
			<i class="fas fa-plus"></i> Neuen Passkey hinzufügen
		</button>
	</div>

	<div class="desktop-table-wrapper">
		<table class="desktop-table">
			<thead>
				<tr>
					<th>Name</th>
					<th>Registriert am</th>
					<th>Aktion</th>
				</tr>
			</thead>
			<tbody>
				<c:if test="${empty passkeys}">
					<tr>
						<td colspan="3" style="text-align: center;">Sie haben noch
							keine Passkeys registriert.</td>
					</tr>
				</c:if>
				<c:forEach var="key" items="${passkeys}">
					<tr>
						<td><c:out value="${key.name}" /></td>
						<td><c:out value="${key.formattedCreatedAt}" /></td>
						<td>
							<form action="${pageContext.request.contextPath}/passkeys"
								method="post" class="js-confirm-form"
								data-confirm-message="Passkey '${fn:escapeXml(key.name)}' wirklich entfernen?">
								<input type="hidden" name="action" value="delete"> <input
									type="hidden" name="id" value="${key.id}">
								<button type="submit" class="btn btn-small btn-danger">
									<i class="fas fa-trash"></i> Entfernen
								</button>
							</form>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />

<script>
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = "${pageContext.request.contextPath}";
    const addPasskeyBtn = document.getElementById('add-passkey-btn');

    document.querySelectorAll('.js-confirm-form').forEach(form => {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
            showConfirmationModal(message, () => this.submit());
        });
    });

    if (addPasskeyBtn) {
        addPasskeyBtn.addEventListener('click', async () => {
            const passkeyName = prompt("Bitte geben Sie einen Namen für diesen Passkey ein (z.B. 'Mein Laptop', 'Handy'):", "Neuer Passkey");
            if (!passkeyName) {
                alert("Registrierung abgebrochen.");
                return;
            }

            try {
                // 1. Start registration from server
                const startResp = await fetch(`${contextPath}/api/passkey/register/start`, { method: 'POST' });
                if (!startResp.ok) throw new Error('Could not start passkey registration.');
                const creationOptions = await startResp.json();
                
                // Convert base64url strings to ArrayBuffers
                creationOptions.challenge = bufferDecode(creationOptions.challenge);
                creationOptions.user.id = bufferDecode(creationOptions.user.id);
                if (creationOptions.excludeCredentials) {
                     creationOptions.excludeCredentials.forEach(cred => {
                        cred.id = bufferDecode(cred.id);
                    });
                }
                
                // 2. Create credential with browser WebAuthn API
                const newCredential = await navigator.credentials.create({ publicKey: creationOptions });

                // 3. Send new credential to server to finish registration
                const finishResp = await fetch(`${contextPath}/api/passkey/register/finish?name=${encodeURIComponent(passkeyName)}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        id: newCredential.id,
                        rawId: bufferEncode(newCredential.rawId),
                        type: newCredential.type,
                        response: {
                            attestationObject: bufferEncode(newCredential.response.attestationObject),
                            clientDataJSON: bufferEncode(newCredential.response.clientDataJSON),
                        },
                    }),
                });

                if (finishResp.ok) {
                    alert('Passkey erfolgreich registriert!');
                    location.reload();
                } else {
                    const error = await finishResp.text();
                    throw new Error('Passkey-Registrierung fehlgeschlagen: ' + error);
                }

            } catch (err) {
                console.error("Passkey registration failed:", err);
                alert("Fehler bei der Passkey-Registrierung: " + err.message);
            }
        });
    }

    // Helper functions for base64url
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