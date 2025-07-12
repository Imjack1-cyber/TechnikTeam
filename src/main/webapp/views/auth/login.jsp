<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Anmeldung" />
	<c:param name="showNav" value="false" />
</c:import>

<div class="login-page-container">
	<div class="login-box">
		<h1>
			<i class="fas fa-bolt"></i> TechnikTeam
		</h1>
		<c:import url="/WEB-INF/jspf/message_banner.jspf" />
		<form action="<c:url value='/login'/>" method="post">
			<div class="form-group">
				<label for="username">Benutzername</label> <input type="text"
					id="username" name="username" required autocomplete="username"
					autofocus>
			</div>
			<div class="form-group">
				<label for="password">Passwort</label>
				<div class="password-input-wrapper">
					<input type="password" id="password" name="password" required
						autocomplete="current-password"> <span id="togglePassword"
						class="password-toggle-icon"> <i class="fas fa-eye"></i>
					</span>
				</div>
			</div>
			<button type="submit" class="btn" style="width: 100%;">Anmelden</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script>
	document.addEventListener('DOMContentLoaded', () => {
		const togglePassword = document.getElementById('togglePassword');
		const passwordInput = document.getElementById('password');

		if (togglePassword && passwordInput) {
			togglePassword.addEventListener('click', function() {
				const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
				passwordInput.setAttribute('type', type);
				this.querySelector('i').classList.toggle('fa-eye');
				this.querySelector('i').classList.toggle('fa-eye-slash');
			});
		}
	});
</script>