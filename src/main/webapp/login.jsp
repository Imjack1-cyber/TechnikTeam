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
			<p class="error-message">
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
});
</script>
<%-- The footer is omitted on the login page for a cleaner, focused look. --%>
</body>
</html>