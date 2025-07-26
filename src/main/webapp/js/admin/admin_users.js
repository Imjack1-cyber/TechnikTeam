// src/main/webapp/js/admin/admin_users.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const csrfToken = document.body.dataset.csrfToken; // Although not used by the new API, we keep it for potential future use

	const modal = document.getElementById('user-modal');
	if (!modal) return;

	// --- API Abstraction for the new v1 UserResource ---
	const api = {
		getUser: (id) => fetch(`${contextPath}/api/v1/users/${id}`).then(res => res.json()),
		createUser: (data) => fetch(`${contextPath}/api/v1/users`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json()),
		updateUser: (id, data) => fetch(`${contextPath}/api/v1/users/${id}`, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json()),
		deleteUser: (id) => fetch(`${contextPath}/api/v1/users/${id}`, { method: 'DELETE' }).then(res => res.json()),
		resetPassword: (id) => fetch(`${contextPath}/api/v1/users/${id}/reset-password`, { method: 'POST' }).then(res => res.json()),
		unlockUser: (id) => fetch(`${contextPath}/api/v1/users/${id}/unlock`, { method: 'POST' }).then(res => res.json())
	};

	// --- Modal Elements ---
	const form = document.getElementById('user-modal-form');
	const title = document.getElementById('user-modal-title');
	const actionInput = form.querySelector('#user-modal-action');
	const idInput = form.querySelector('#userId-modal');
	const usernameInput = form.querySelector('#username-modal');
	const passwordInput = form.querySelector('#password-modal');
	const passwordGroup = form.querySelector('#password-group');
	const roleInput = form.querySelector('#role-modal');
	const classYearInput = form.querySelector('#classYear-modal');
	const classNameInput = form.querySelector('#className-modal');
	const emailInput = form.querySelector('#email-modal');
	const permissionsContainer = document.getElementById('permissions-checkbox-container');
	const closeModalBtn = modal.querySelector('.modal-close-btn');

	const groupedPermissions = JSON.parse(document.getElementById('allPermissionsData').textContent);

	const closeModal = () => modal.classList.remove('active');

	const populatePermissions = (assignedIds = new Set()) => {
		permissionsContainer.innerHTML = '';
		for (const [groupName, permissionsInGroup] of Object.entries(groupedPermissions)) {
			const details = document.createElement('details');
			details.open = true;
			const summary = document.createElement('summary');
			summary.style.fontWeight = 'bold';
			summary.style.cursor = 'pointer';
			summary.style.padding = '0.5rem 0';
			summary.textContent = groupName;

			const groupDiv = document.createElement('div');
			groupDiv.style.paddingLeft = '1rem';

			permissionsInGroup.forEach(p => {
				const isChecked = assignedIds.has(p.id) ? 'checked' : '';
				const label = document.createElement('label');
				label.style.display = 'flex';
				label.style.alignItems = 'flex-start';
				label.style.marginBottom = '0.5rem';
				label.innerHTML = `
                    <input type="checkbox" name="permissionIds" value="${p.id}" ${isChecked} style="margin-top: 5px; margin-right: 10px;">
                    <div>
                        <strong>${p.permissionKey.replace(groupName + '_', '')}</strong>
                        <small style="display: block; color: var(--text-muted-color);">${p.description}</small>
                    </div>
                `;
				groupDiv.appendChild(label);
			});
			details.appendChild(summary);
			details.appendChild(groupDiv);
			permissionsContainer.appendChild(details);
		}
	};

	document.getElementById('new-user-btn')?.addEventListener('click', () => {
		form.reset();
		title.textContent = "Neuen Benutzer anlegen";
		actionInput.value = "create";
		idInput.value = "";
		passwordInput.required = true;
		passwordGroup.style.display = 'block';
		roleInput.value = "3";
		populatePermissions();
		modal.classList.add('active');
		usernameInput.focus();
	});

	document.querySelectorAll('.edit-user-btn').forEach(btn => {
		btn.addEventListener('click', async () => {
			const userId = btn.dataset.userId;
			try {
				// Use the new API endpoint
				const result = await api.getUser(userId);
				if (!result.success) throw new Error(result.message);

				const user = result.data;
				const assignedPermissionIds = new Set(user.permissions.map(p => p.id)); // Assuming permissions are nested objects now

				form.reset();
				title.textContent = `Benutzer bearbeiten: ${user.username}`;
				actionInput.value = "update";
				idInput.value = user.id;
				usernameInput.value = user.username || '';
				roleInput.value = user.roleId || '3';
				classYearInput.value = user.classYear || '';
				classNameInput.value = user.className || '';
				emailInput.value = user.email || '';
				passwordInput.required = false;
				passwordGroup.style.display = 'none';
				populatePermissions(assignedPermissionIds);
				modal.classList.add('active');
			} catch (error) {
				console.error('Failed to open edit modal:', error);
				showToast('Benutzerdaten konnten nicht geladen werden.', 'danger');
			}
		});
	});

	form.addEventListener('submit', async (event) => {
		event.preventDefault();
		const action = actionInput.value;
		const userId = idInput.value;

		const formData = new FormData(form);
		const data = Object.fromEntries(formData.entries());
		data.permissionIds = formData.getAll('permissionIds').map(id => parseInt(id));

		try {
			const result = (action === 'create')
				? await api.createUser(data)
				: await api.updateUser(userId, data);

			if (result.success) {
				closeModal();
				showToast(result.message, 'success');
				if (action === 'create') {
					window.location.reload();
				} else {
					updateTableRow(result.data);
				}
			} else {
				showToast(result.message || 'Ein unbekannter Fehler ist aufgetreten.', 'danger');
			}
		} catch (error) {
			console.error('Error submitting form:', error);
			showToast('Ein Netzwerkfehler ist aufgetreten.', 'danger');
		}
	});

	// --- Handle Action Buttons (Delete, Reset, Unlock) ---
	document.body.addEventListener('click', async (event) => {
		const resetBtn = event.target.closest('.js-reset-password-btn');
		const unlockBtn = event.target.closest('.js-unlock-btn');
		const deleteBtn = event.target.closest('.js-delete-btn');

		if (resetBtn) {
			const userId = resetBtn.dataset.userId;
			const username = resetBtn.dataset.username;
			showConfirmationModal(`Passwort für '${username}' wirklich zurücksetzen?`, async () => {
				const result = await api.resetPassword(userId);
				if (result.success) {
					const bannerContainer = document.querySelector('.main-content');
					document.querySelectorAll('.password-reset-alert').forEach(el => el.remove());
					const banner = document.createElement('p');
					banner.className = 'password-reset-alert';
					banner.innerHTML = `<i class="fas fa-key"></i> ${result.message.replace(result.data.newPassword, `<strong class="copyable-password" title="In Zwischenablage kopieren">${result.data.newPassword}</strong>`)}`;
					bannerContainer.prepend(banner);
				} else {
					showToast(result.message, 'danger');
				}
			});
		}

		if (unlockBtn) {
			const userId = unlockBtn.dataset.userId;
			const username = unlockBtn.dataset.username;
			showConfirmationModal(`Die Login-Sperre für '${username}' wirklich aufheben?`, async () => {
				const result = await api.unlockUser(userId);
				showToast(result.message, result.success ? 'success' : 'danger');
			});
		}

		if (deleteBtn) {
			const userId = deleteBtn.dataset.userId;
			const username = deleteBtn.dataset.username;
			showConfirmationModal(`Benutzer '${username}' wirklich löschen?`, async () => {
				const result = await api.deleteUser(userId);
				if (result.success) {
					showToast(result.message, 'success');
					removeTableRow(userId);
				} else {
					showToast(result.message, 'danger');
				}
			});
		}
	});

	// --- Helper functions for live UI updates ---
	const updateTableRow = (user) => {
		const row = document.querySelector(`tr[data-user-id='${user.id}']`);
		if (row) {
			row.querySelector("td[data-field='username']").textContent = user.username;
			row.querySelector("td[data-field='roleName']").textContent = user.roleName;
		}
	};
	const removeTableRow = (userId) => {
		document.querySelector(`tr[data-user-id='${userId}']`)?.remove();
	};

	// --- Modal Closing and Tabs ---
	closeModalBtn.addEventListener('click', closeModal);
	modal.addEventListener('click', (event) => { if (event.target === modal) closeModal(); });
	document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && modal.classList.contains('active')) closeModal(); });

	const tabButtons = modal.querySelectorAll('.modal-tab-button');
	const tabContents = modal.querySelectorAll('.modal-tab-content');
	tabButtons.forEach(button => {
		button.addEventListener('click', () => {
			tabButtons.forEach(btn => btn.classList.remove('active'));
			button.classList.add('active');
			tabContents.forEach(content => {
				content.classList.toggle('active', content.id === button.dataset.tab);
			});
		});
	});
});