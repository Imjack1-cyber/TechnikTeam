<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Passwort ändern" />
</c:import>

<div style="max-width: 600px; margin: auto;">
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
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}">
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

<c:import url="/WEB-INF/jspf/main_footer.jspf" />