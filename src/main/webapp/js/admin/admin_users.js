document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(event) {
			event.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	const passwordAlert = document.getElementById('password-reset-alert');
	if (passwordAlert) {
		const passwordElement = passwordAlert.querySelector('strong.copyable-password');
		if (passwordElement) {
			navigator.clipboard.writeText(passwordElement.textContent)
				.then(() => console.log('Password copied to clipboard'))
				.catch(err => console.error('Failed to copy password:', err));
		}
	}

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
				alert('Benutzerdaten konnten nicht geladen werden.');
			}
		});
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