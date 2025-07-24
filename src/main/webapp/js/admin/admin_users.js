document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	const modal = document.getElementById('user-modal');
	if (!modal) return;

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

	const newUserBtn = document.getElementById('new-user-btn');
	if (newUserBtn) {
		newUserBtn.addEventListener('click', () => {
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
	}

	document.querySelectorAll('.edit-user-btn').forEach(btn => {
		btn.addEventListener('click', async () => {
			form.reset();
			const fetchUrl = btn.dataset.fetchUrl;
			try {
				const response = await fetch(fetchUrl);
				if (!response.ok) throw new Error('Could not fetch user data');
				const data = await response.json();
				const user = data.user;
				const assignedPermissionIds = new Set(data.permissionIds);

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

	const updateTableRow = (user) => {
		const row = document.querySelector(`tr[data-user-id='${user.id}']`);
		if (row) {
			row.querySelector("td[data-field='username']").textContent = user.username;
			row.querySelector("td[data-field='roleName']").textContent = user.roleName;
		}
		const card = document.querySelector(`.list-item-card[data-user-id='${user.id}']`);
		if (card) {
			card.querySelector("h3[data-field='username']").textContent = user.username;
			card.querySelector("strong[data-field='roleName']").textContent = user.roleName;
		}
	};

	const removeTableRow = (userId) => {
		document.querySelector(`tr[data-user-id='${userId}']`)?.remove();
		document.querySelector(`.list-item-card[data-user-id='${userId}']`)?.remove();
	};


	form.addEventListener('submit', async (event) => {
		event.preventDefault();
		const action = actionInput.value;
		const formActionUrl = `${contextPath}/admin/action/user?action=${action}`;

		const formData = new URLSearchParams(new FormData(form));
		try {
			const response = await fetch(formActionUrl, {
				method: 'POST',
				body: formData
			});

			const result = await response.json();

			if (response.ok && result.success) {
				closeModal();
				showToast(result.message, 'success');
				if (action === 'create') {
					window.location.reload();
				} else if (action === 'update') {
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

	const handleAjaxFormSubmit = async (formElement) => {
		const formData = new URLSearchParams(new FormData(formElement));
		const actionUrl = formElement.getAttribute('action');

		try {
			const response = await fetch(actionUrl, { method: 'POST', body: formData });
			const result = await response.json();

			if (response.ok && result.success) {
				if (result.data && result.data.newPassword) {
					const bannerContainer = document.querySelector('.main-content');
					document.querySelectorAll('.password-reset-alert, .info-message, .success-message, .error-message').forEach(el => el.remove());

					const banner = document.createElement('p');
					banner.className = 'password-reset-alert';
					banner.id = 'password-reset-alert';
					banner.innerHTML = `<i class="fas fa-key"></i> ${result.message}`;
					bannerContainer.prepend(banner);
				} else {
					showToast(result.message, 'success');
				}

				const action = formData.get('action');
				if (action === 'delete') {
					removeTableRow(result.data.deletedUserId);
				}
			} else {
				showToast(result.message || 'Ein Fehler ist aufgetreten.', 'danger');
			}
		} catch (error) {
			console.error('Error submitting form via AJAX:', error);
			showToast('Ein Netzwerkfehler ist aufgetreten.', 'danger');
		}
	};

	document.body.addEventListener('submit', (event) => {
		const form = event.target;
		if (form.matches('.js-confirm-delete-form, .js-reset-password-form, .js-unlock-form')) {
			event.preventDefault();
			const message = form.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => handleAjaxFormSubmit(form));
		}
	});

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