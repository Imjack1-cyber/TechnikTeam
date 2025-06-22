<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  passwort.jsp
  
  This page provides a form for a logged-in user to change their own password.
  It requires them to enter their current password for verification and to
  confirm their new password.
  
  - It is served by: PasswordServlet (doGet).
  - It submits to: PasswordServlet (doPost).
  - Expected attributes:
    - 'successMessage' (String): Message on successful change (optional).
    - 'errorMessage' (String): Message on validation failure (optional).
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Passwort ändern" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<div class="form-center-wrapper">
	<div class="card">
		<h1>Passwort ändern</h1>

		<c:if test="${not empty successMessage}">
			<p class="success-message">${successMessage}</p>
		</c:if>
		<c:if test="${not empty errorMessage}">
			<p class="error-message">${errorMessage}</p>
		</c:if>

		<form action="${pageContext.request.contextPath}/passwort"
			method="post">
			<div class="form-group">
				<label for="currentPassword">Aktuelles Passwort</label> <input
					type="password" id="currentPassword" name="currentPassword"
					required autocomplete="current-password">
			</div>
			<div class="form-group">
				<label for="newPassword">Neues Passwort</label> <input
					type="password" id="newPassword" name="newPassword" required
					autocomplete="new-password">
			</div>
			<div class="form-group">
				<label for="confirmPassword">Neues Passwort bestätigen</label> <input
					type="password" id="confirmPassword" name="confirmPassword"
					required autocomplete="new-password">
			</div>
			<button type="submit" class="btn">Passwort speichern</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />