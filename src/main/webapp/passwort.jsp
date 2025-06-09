<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Passwort ändern" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<div class="form-container">
	<h1>Passwort ändern</h1>

	<c:if test="${not empty errorMessage}">
		<p class="error-message">${errorMessage}</p>
	</c:if>
	<c:if test="${not empty successMessage}">
		<p class="success-message">${successMessage}</p>
	</c:if>

	<form action="${pageContext.request.contextPath}/passwort"
		method="post">
		<div class="form-group">
			<label for="currentPassword">Aktuelles Passwort</label> <input
				type="password" id="currentPassword" name="currentPassword" required>
		</div>
		<div class="form-group">
			<label for="newPassword">Neues Passwort</label> <input
				type="password" id="newPassword" name="newPassword" required>
		</div>
		<div class="form-group">
			<label for="confirmPassword">Neues Passwort bestätigen</label> <input
				type="password" id="confirmPassword" name="confirmPassword" required>
		</div>
		<button type="submit" class="btn">Passwort speichern</button>
	</form>
</div>

<style>
.form-container {
	max-width: 500px;
	margin: 2rem auto;
	padding: 2rem;
	border: 1px solid var(--border-color);
	border-radius: 8px;
}

.success-message {
	color: #155724;
	background-color: #d4edda;
	border-color: #c3e6cb;
	padding: .75rem;
	border-radius: 4px;
	margin-bottom: 1rem;
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />