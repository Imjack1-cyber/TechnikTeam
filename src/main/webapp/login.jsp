<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Login - Technik Team</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="login-container">
		<h1>Anmeldung</h1>

		<!-- Display error message if it exists -->
		<c:if test="${not empty errorMessage}">
			<p class="error-message">${errorMessage}</p>
		</c:if>

		<form action="${pageContext.request.contextPath}/login" method="post">
			<div class="form-group">
				<label for="username">Benutzername</label> <input type="text"
					id="username" name="username" required>
			</div>
			<div class="form-group">
				<label for="password">Passwort</label> <input type="password"
					id="password" name="password" required>
			</div>
			<button type="submit" class="btn">Anmelden</button>
		</form>
	</div>
	<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>