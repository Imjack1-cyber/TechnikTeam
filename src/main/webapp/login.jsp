<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Login" />
</c:import>

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

<!-- We don't include a full footer here as it's a standalone page -->
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>