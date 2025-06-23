<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  passwort.jsp
  
  This page provides a form for a logged-in user to change their own password.
  It requires them to enter their current password for verification and to
  confirm their new password. It now includes a toggle to show/hide the password.
  
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
			<p class="success-message">
				<c:out value="${successMessage}" />
			</p>
		</c:if>
		<c:if test="${not empty errorMessage}">
			<p class="error-message">
				<c:out value="${errorMessage}" />
			</p>
		</c:if>

		<form action="${pageContext.request.contextPath}/passwort"
			method="post">
			<div class="form-group">
				<label for="currentPassword">Aktuelles Passwort</label>
				<div class="password-wrapper">
					<input type="password" id="currentPassword" name="currentPassword"
						required autocomplete="current-password"> <i
						class="fas fa-eye password-toggle"></i>
				</div>
			</div>
			<div class="form-group">
				<label for="newPassword">Neues Passwort</label>
				<div class="password-wrapper">
					<input type="password" id="newPassword" name="newPassword" required
						autocomplete="new-password"> <i
						class="fas fa-eye password-toggle"></i>
				</div>
			</div>
			<div class="form-group">
				<label for="confirmPassword">Neues Passwort bestätigen</label>
				<div class="password-wrapper">
					<input type="password" id="confirmPassword" name="confirmPassword"
						required autocomplete="new-password"> <i
						class="fas fa-eye password-toggle"></i>
				</div>
			</div>
			<button type="submit" class="btn">Passwort speichern</button>
		</form>
	</div>
</div>

<style>
.password-wrapper {
	position: relative;
}

.password-wrapper input {
	padding-right: 2.5rem;
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
document.querySelectorAll('.password-toggle').forEach(toggle => {
    toggle.addEventListener('click', () => {
        const passwordInput = toggle.previousElementSibling;
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        toggle.classList.toggle('fa-eye');
        toggle.classList.toggle('fa-eye-slash');
    });
});
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />