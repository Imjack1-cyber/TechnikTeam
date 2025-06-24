<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
admin_users.jsp

This is the main user management page for administrators. It displays a list
of all users in the system with options to view their details, delete them, or
reset their password. Creating and editing users is handled via modal dialogs.

    It is served by: AdminUserServlet (doGet).
    It submits to: AdminUserServlet (doPost from the modals and forms).
    Expected attributes:
        'userList' (List<de.technikteam.model.User>): A list of all users.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Benutzerverwaltung" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Benutzerverwaltung</h1>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.passwordResetInfo}">
	<p class="success-message" id="password-reset-alert">${sessionScope.passwordResetInfo}</p>
	<c:remove var="passwordResetInfo" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="table-controls">
	<button type="button" class="btn" id="new-user-btn">Neuen
		Benutzer anlegen</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<c:if test="${empty userList}">
	<div class="card">
		<p>Es sind keine Benutzer registriert.</p>
	</div>
</c:if>
<!-- MOBILE LAYOUT -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="user" items="${userList}">
		<div class="list-item-card"
			data-searchable-content="${user.id} ${user.username} ${user.role}">
			<h3 class="card-title">${user.username}</h3>
			<div class="card-row">
				<span>ID:</span> <span>${user.id}</span>
			</div>
			<div class="card-row">
				<span>Rolle:</span> <span>${user.role}</span>
			</div>
			<div class="card-actions">
				<button type="button" class="btn btn-small edit-user-btn"
					data-id="${user.id}">Bearbeiten</button>
				<c:if test="${sessionScope.user.id != user.id}">
					<form action="${pageContext.request.contextPath}/admin/users"
						method="post" class="inline-form js-confirm-form"
						data-confirm-message="Benutzer '${user.username}' wirklich löschen?">
						<input type="hidden" name="action" value="delete"> <input
							type="hidden" name="userId" value="${user.id}">
						<button type="submit" class="btn btn-small btn-danger">Löschen</button>
					</form>
					<form action="${pageContext.request.contextPath}/admin/users"
						method="post" class="inline-form js-confirm-form"
						data-confirm-message="Passwort für '${user.username}' zurücksetzen? Das neue Passwort wird angezeigt.">
						<input type="hidden" name="action" value="resetPassword">
						<input type="hidden" name="userId" value="${user.id}">
						<button type="submit" class="btn btn-small btn-warning">Passwort
							Reset</button>
					</form>
				</c:if>
			</div>
		</div>
	</c:forEach>
</div>
<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table sortable-table searchable-table">
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
					<td><a
						href="${pageContext.request.contextPath}/admin/users?action=details&id=${user.id}"
						title="Detailansicht und Event-Historie für ${user.username} anzeigen">${user.username}</a></td>
					<td>${user.role}</td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
						<button type="button" class="btn btn-small edit-user-btn"
							data-id="${user.id}">Bearbeiten</button> <c:if
							test="${sessionScope.user.id != user.id}">
							<form action="${pageContext.request.contextPath}/admin/users"
								method="post" class="inline-form js-confirm-form"
								data-confirm-message="Benutzer '${user.username}' wirklich löschen?">
								<input type="hidden" name="action" value="delete"> <input
									type="hidden" name="userId" value="${user.id}">
								<button type="submit" class="btn btn-small btn-danger">Löschen</button>
							</form>
							<form action="${pageContext.request.contextPath}/admin/users"
								method="post" class="inline-form js-confirm-form"
								data-confirm-message="Passwort für '${user.username}' zurücksetzen? Das neue Passwort wird angezeigt.">
								<input type="hidden" name="action" value="resetPassword">
								<input type="hidden" name="userId" value="${user.id}">
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
<!-- MODAL FOR CREATE/EDIT USER -->
<div class="modal-overlay" id="user-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3 id="user-modal-title">Benutzer</h3>
		<form id="user-modal-form"
			action="${pageContext.request.contextPath}/admin/users" method="post">
			<input type="hidden" name="action" id="user-modal-action"> <input
				type="hidden" name="userId" id="user-modal-id">
			<div class="form-group">
				<label for="username-modal">Benutzername</label> <input type="text"
					id="username-modal" name="username" required>
			</div>
			<div class="form-group" id="password-group">
				<label for="password-modal">Passwort</label>
				<div class="password-wrapper">
					<input type="password" id="password-modal" name="password" required>
					<i class="fas fa-eye password-toggle"></i>
				</div>
				<small>Nur beim Erstellen erforderlich.</small>
			</div>
			<div class="form-group">
				<label for="role-modal">Rolle</label> <select name="role"
					id="role-modal">
					<option value="NUTZER">Nutzer</option>
					<option value="ADMIN">Admin</option>
				</select>
			</div>
			<div class="form-group">
				<label for="classYear-modal">Jahrgang</label> <input type="number"
					id="classYear-modal" name="classYear" placeholder="z.B. 2025">
			</div>
			<div class="form-group">
				<label for="className-modal">Klasse</label> <input type="text"
					id="className-modal" name="className" placeholder="z.B. 10b">
			</div>
			<button type="submit" class="btn">Speichern</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<style>
.inline-form {
	display: inline;
}

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

.copyable-password {
	background-color: var(--primary-color-light);
	padding: 0.2em 0.4em;
	border-radius: 4px;
	font-family: monospace;
}
</style>
<script>
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = "${pageContext.request.contextPath}";
	// Custom confirmation for forms
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => {
				this.submit(); // Submit the form if confirmed
			});
		});
	});

	// Password Reset Clipboard Copy
	const passwordAlert = document.getElementById('password-reset-alert');
	if (passwordAlert) {
		const passwordElement = passwordAlert.querySelector('.copyable-password');
		if(passwordElement) {
			const password = passwordElement.textContent;
			navigator.clipboard.writeText(password).then(() => {
				console.log('Password copied to clipboard');
			}).catch(err => {
				console.error('Failed to copy password: ', err);
			});
		}
	}

	// Modal Logic
	const modal = document.getElementById('user-modal');
	const form = document.getElementById('user-modal-form');
	const title = document.getElementById('user-modal-title');
	const actionInput = document.getElementById('user-modal-action');
	const idInput = document.getElementById('user-modal-id');
	const usernameInput = document.getElementById('username-modal');
	const passwordInput = document.getElementById('password-modal');
	const passwordGroup = document.getElementById('password-group');
	const roleInput = document.getElementById('role-modal');
	const classYearInput = document.getElementById('classYear-modal');
	const classNameInput = document.getElementById('className-modal');

	const closeModalBtn = modal.querySelector('.modal-close-btn');

	const closeModal = () => modal.classList.remove('active');

	const openCreateModal = () => {
		form.reset();
		title.textContent = "Neuen Benutzer anlegen";
		actionInput.value = "create";
		idInput.value = "";
		passwordInput.required = true;
		passwordGroup.style.display = 'block';
		modal.classList.add('active');
		usernameInput.focus();
	};

	const openEditModal = async (btn) => {
		form.reset();
        const userId = btn.dataset.id;
        try {
            const response = await fetch(`${contextPath}/admin/users?action=getUserData&id=${userId}`);
            if (!response.ok) throw new Error('Could not fetch user data');
            const data = await response.json();

            title.textContent = "Benutzer bearbeiten";
            actionInput.value = "update";
            idInput.value = data.id;
            usernameInput.value = data.username || '';
            roleInput.value = data.role || 'NUTZER';
            classYearInput.value = data.classYear || '';
            classNameInput.value = data.className || '';

            // Password is not required for updates, so hide the field
            passwordInput.required = false;
            passwordGroup.style.display = 'none';

            modal.classList.add('active');

        } catch (error) {
            console.error('Failed to open edit modal:', error);
            alert('Benutzerdaten konnten nicht geladen werden.');
        }
	};

	document.getElementById('new-user-btn').addEventListener('click', openCreateModal);
	document.querySelectorAll('.edit-user-btn').forEach(btn => {
		btn.addEventListener('click', () => openEditModal(btn));
	});

	closeModalBtn.addEventListener('click', closeModal);
	modal.addEventListener('click', (e) => {
		if (e.target === modal) closeModal();
	});
	document.addEventListener('keydown', (e) => {
		if (e.key === 'Escape' && modal.classList.contains('active')) closeModal();
	});

	// Password visibility toggle for modal
	modal.querySelectorAll('.password-toggle').forEach(toggle => {
		toggle.addEventListener('click', () => {
			const passwordInput = toggle.previousElementSibling;
			const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
			passwordInput.setAttribute('type', type);
			toggle.classList.toggle('fa-eye');
			toggle.classList.toggle('fa-eye-slash');
		});
	});
});
</script>