<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Benutzerdetails bearbeiten" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<!--  
admin_user_details.jsp: The detailed view for a single user, which includes their profile data, qualifications, and event history. Crucially, this page also contains the form to update the user's information.

    Served by: AdminUserServlet (doGet with action=details).

    Submits to: AdminUserServlet (doPost with action=update).

    Dependencies: Includes header.jspf, admin_navigation.jspf, footer.jspf.
-->

<h1>
	Benutzerdetails bearbeiten:
	<c:out value="${userToEdit.username}" />
</h1>
<a href="${pageContext.request.contextPath}/admin/users"
	style="display: inline-block; margin-bottom: 1rem;">« Zurück zur
	Benutzerliste</a>

<!-- Display success or error messages -->
<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="card form-container">
	<form action="${pageContext.request.contextPath}/admin/users"
		method="post">
		<input type="hidden" name="action" value="update"> <input
			type="hidden" name="userId" value="${userToEdit.id}">

		<h2 class="card-title">Stammdaten</h2>
		<div class="form-group">
			<label for="username">Benutzername</label> <input type="text"
				id="username" name="username" value="${userToEdit.username}"
				required>
		</div>
		<div class="form-group">
			<label for="role">Rolle</label> <select name="role" id="role">
				<option value="NUTZER"
					${userToEdit.role == 'NUTZER' ? 'selected' : ''}>Nutzer</option>
				<option value="ADMIN"
					${userToEdit.role == 'ADMIN' ? 'selected' : ''}>Admin</option>
			</select>
		</div>
		<div class="form-group">
			<label for="classYear">Jahrgang</label> <input type="number"
				id="classYear" name="classYear" value="${userToEdit.classYear}"
				placeholder="z.B. 2025">
		</div>
		<div class="form-group">
			<label for="className">Klasse</label> <input type="text"
				id="className" name="className" value="${userToEdit.className}"
				placeholder="z.B. 10b">
		</div>
		<%-- Ändern Sie das Input-Feld für das Erstellungsdatum --%>
		<div class="form-group">
			<label>Registriert seit</label> <input type="text"
				value="${userToEdit.formattedCreatedAt} Uhr" readonly
				class="readonly-field">
		</div>
		<button type="submit" class="btn">Änderungen speichern</button>
	</form>
</div>

<style>
.form-container {
	max-width: 700px;
	margin: auto;
}

/* Style für schreibgeschützte Felder, damit sie klar als solche erkennbar sind */
.readonly-field {
	background-color: var(--secondary-color);
	border: 1px solid var(--border-color);
	cursor: not-allowed;
	color: #777;
}

[data-theme="dark"] .readonly-field {
	color: #aaa;
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />