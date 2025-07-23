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

		<c:if test="${not empty sessionScope.lockoutEndTime}">
			<div class="error-message" id="lockout-timer"
				data-end-time="${sessionScope.lockoutEndTime}"
				data-lockout-level="${sessionScope.lockoutLevel}">Lade
				Timer...</div>
			<c:remove var="lockoutEndTime" scope="session" />
			<c:remove var="lockoutLevel" scope="session" />
		</c:if>

		<form action="<c:url value='/login'/>" method="post">
			<div class="form-group">
				<label for="username">Benutzername</label> <input type="text"
					id="username" name="username"
					value="<c:out value='${failedUsername}'/>" required
					autocomplete="username" autofocus>
				<c:remove var="failedUsername" scope="session" />
			</div>
			<div class="form-group">
				<label for="password">Passwort</label>
				<div class="password-input-wrapper">
					<input type="password" id="password" name="password" required
						autocomplete="current-password"> <span
						class="password-toggle-icon"> <i class="fas fa-eye"></i>
					</span>
				</div>
			</div>
			<button type="submit" class="btn" style="width: 100%; margin-bottom: 0.75rem;"
				${not empty sessionScope.lockoutEndTime ? 'disabled' : ''}>Anmelden</button>
			<button type="button" id="login-passkey-btn" class="btn btn-secondary" style="width: 100%;"
				${not empty sessionScope.lockoutEndTime ? 'disabled' : ''}>
				<i class="fas fa-fingerprint"></i> Mit Passkey anmelden
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/auth/login.js"></script>
<script src="${pageContext.request.contextPath}/js/auth/passkey_auth.js"></script>