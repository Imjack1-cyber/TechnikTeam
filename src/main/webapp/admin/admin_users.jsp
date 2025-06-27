<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Benutzerverwaltung" />
</c:import>

<h1>
	<i class="fas fa-users-cog"></i> Benutzerverwaltung
</h1>

<%-- Session Messages --%>
<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">
		<i class="fas fa-check-circle"></i>
		<c:out value="${sessionScope.successMessage}" />
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.passwordResetInfo}">
	<p class="success-message" id="password-reset-alert">${sessionScope.passwordResetInfo}</p>
	<c:remove var="passwordResetInfo" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">
		<i class="fas fa-exclamation-triangle"></i>
		<c:out value="${sessionScope.errorMessage}" />
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.infoMessage}">
	<p class="info-message">
		<i class="fas fa-info-circle"></i>
		<c:out value="${sessionScope.infoMessage}" />
	</p>
	<c:remove var="infoMessage" scope="session" />
</c:if>

<div class="table-controls">
	<button type="button" class="btn" id="new-user-btn">
		<i class="fas fa-user-plus"></i> Neuen Benutzer anlegen
	</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Benutzer filtern..." aria-label="Benutzer filtern">
	</div>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="user" items="${userList}">
		<div class="list-item-card"
			data-searchable-content="${user.username} ${user.role}">
			<h3 class="card-title">${user.username}</h3>
			<div class="card-row">
				<span>Rolle:</span> <span>${user.role}</span>
			</div>
			<div class="card-actions">
				<button type="button" class="btn btn-small edit-user-btn"
					data-fetch-url="<c:url value='/admin/users?action=getUserData&id=${user.id}'/>">Bearbeiten</button>
				<a href="<c:url value='/admin/users?action=details&id=${user.id}'/>"
					class="btn btn-small">Details</a>
				<c:if test="${sessionScope.user.id != user.id}">
					<form action="<c:url value='/admin/users'/>" method="post"
						class="js-confirm-form"
						data-confirm-message="Benutzer '${fn:escapeXml(user.username)}' wirklich löschen?">
						<input type="hidden" name="action" value="delete"><input
							type="hidden" name="userId" value="${user.id}">
						<button type="submit" class="btn btn-small btn-danger">Löschen</button>
					</form>
				</c:if>
			</div>
		</div>
	</c:forEach>
</div>

<!-- Desktop Table View -->
<div class="desktop-table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="number">ID</th>
				<th class="sortable" data-sort-type="string">Benutzername</th>
				<th class="sortable" data-sort-type="string">Rolle</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="user" items="${userList}">
				<tr>
					<td>${user.id}</td>
					<td>${user.username}</td>
					<td>${user.role}</td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
						<button type="button" class="btn btn-small edit-user-btn"
							data-fetch-url="<c:url value='/admin/users?action=getUserData&id=${user.id}'/>">Bearbeiten</button>
						<a
						href="<c:url value='/admin/users?action=details&id=${user.id}'/>"
						class="btn btn-small">Details</a> <c:if
							test="${sessionScope.user.id != user.id}">
							<form action="<c:url value='/admin/users'/>" method="post"
								class="js-confirm-form"
								data-confirm-message="Benutzer '${fn:escapeXml(user.username)}' wirklich löschen?">
								<input type="hidden" name="action" value="delete"><input
									type="hidden" name="userId" value="${user.id}">
								<button type="submit" class="btn btn-small btn-danger">Löschen</button>
							</form>
							<form action="<c:url value='/admin/users'/>" method="post"
								class="js-confirm-form"
								data-confirm-message="Passwort für '${fn:escapeXml(user.username)}' zurücksetzen? Das neue Passwort wird angezeigt.">
								<input type="hidden" name="action" value="resetPassword"><input
									type="hidden" name="userId" value="${user.id}">
								<button type="submit" class="btn btn-small btn-warning">Passwort
									Reset</button>
							</form>
						</c:if>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<%@ include file="/WEB-INF/jspf/user_modals.jspf"%>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />

<script>
document.addEventListener('DOMContentLoaded', () => {
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	const passwordAlert = document.getElementById('password-reset-alert');
	if (passwordAlert) {
		const passwordElement = passwordAlert.querySelector('strong.copyable-password');
		if(passwordElement) {
			navigator.clipboard.writeText(passwordElement.textContent)
                .then(() => console.log('Password copied to clipboard'))
                .catch(err => console.error('Failed to copy password:', err));
		}
	}

	const modal = document.getElementById('user-modal');
	const form = document.getElementById('user-modal-form');
	const title = document.getElementById('user-modal-title');
	const actionInput = form.querySelector('input[name="action"]');
	const idInput = form.querySelector('input[name="userId"]');
	const usernameInput = form.querySelector('#username-modal');
	const passwordInput = form.querySelector('#password-modal');
	const passwordGroup = form.querySelector('#password-group');
	const roleInput = form.querySelector('#role-modal');
	const classYearInput = form.querySelector('#classYear-modal');
	const classNameInput = form.querySelector('#className-modal');
	const closeModalBtn = modal.querySelector('.modal-close-btn');

	const closeModal = () => modal.classList.remove('active');

	document.getElementById('new-user-btn').addEventListener('click', () => {
		form.reset();
		title.textContent = "Neuen Benutzer anlegen";
		actionInput.value = "create";
		idInput.value = "";
		passwordInput.required = true;
		passwordGroup.style.display = 'block';
		modal.classList.add('active');
		usernameInput.focus();
	});

	document.querySelectorAll('.edit-user-btn').forEach(btn => {
		btn.addEventListener('click', async () => {
			form.reset();
			const fetchUrl = btn.dataset.fetchUrl;
			try {
				const response = await fetch(fetchUrl);
				if (!response.ok) throw new Error('Could not fetch user data');
				const data = await response.json();

				title.textContent = `Benutzer bearbeiten: ${data.username}`;
				actionInput.value = "update";
				idInput.value = data.id;
				usernameInput.value = data.username || '';
				roleInput.value = data.role || 'NUTZER';
				classYearInput.value = data.classYear || '';
				classNameInput.value = data.className || '';
				passwordInput.required = false;
				passwordGroup.style.display = 'none';
				modal.classList.add('active');
			} catch (error) {
				console.error('Failed to open edit modal:', error);
				alert('Benutzerdaten konnten nicht geladen werden.');
			}
		});
	});

	closeModalBtn.addEventListener('click', closeModal);
	modal.addEventListener('click', (e) => { if (e.target === modal) closeModal(); });
	document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && modal.classList.contains('active')) closeModal(); });
});
</script>