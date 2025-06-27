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
		</div>
	</div>

	<script>
// This script is self-contained as main.js is not included on this page.
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = document.body.dataset.contextPath || '';

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