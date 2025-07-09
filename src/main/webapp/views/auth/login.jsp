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
				<label for="password">Passwort</label> <input type="password"
					id="password" name="password" required
					autocomplete="current-password">
			</div>
			<button type="submit" class="btn" style="width: 100%;">Anmelden</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />