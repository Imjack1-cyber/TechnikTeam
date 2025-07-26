document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const modal = document.getElementById('role-permissions-modal');
	if (!modal) return;

	const modalTitle = document.getElementById('role-modal-title');
	const roleIdInput = document.getElementById('modal-role-id');
	const roleNameInput = document.getElementById('modal-role-name');
	const checkboxContainer = document.getElementById('permissions-checkbox-container');
	const allPermissions = JSON.parse(document.getElementById('allPermissionsData').textContent || '[]');

	/**
	 * Opens the permissions modal and fetches the permissions for the selected role.
	 * @param {HTMLElement} btn The button that was clicked.
	 */
	const openModal = async (btn) => {
		const roleId = btn.dataset.roleId;
		const roleName = btn.dataset.roleName;

		modalTitle.textContent = `Berechtigungen für Rolle: ${roleName}`;
		roleIdInput.value = roleId;
		roleNameInput.value = roleName;
		checkboxContainer.innerHTML = '<p>Lade Berechtigungen...</p>';
		modal.classList.add('active');

		try {
			const response = await fetch(`${contextPath}/admin/roles?action=getRolePermissions&roleId=${roleId}`);
			if (!response.ok) throw new Error('Could not fetch role permissions');

			const data = await response.json();
			const assignedIds = new Set(data.permissionIds);

			checkboxContainer.innerHTML = ''; 

			const grouped = allPermissions.reduce((acc, p) => {
				const groupName = p.permissionKey.split('_')[0] || 'ALLGEMEIN';
				if (!acc[groupName]) acc[groupName] = [];
				acc[groupName].push(p);
				return acc;
			}, {});


			for (const groupName in grouped) {
				const details = document.createElement('details');
				details.open = true; 
				const summary = document.createElement('summary');
				summary.textContent = groupName;
				details.appendChild(summary);

				const groupDiv = document.createElement('div');
				groupDiv.className = 'permission-group';
				grouped[groupName].forEach(p => {
					const isChecked = assignedIds.has(p.id) ? 'checked' : '';
					const isDisabled = p.permissionKey === 'ACCESS_ADMIN_PANEL' && roleName === 'ADMIN' ? 'disabled' : '';
					const label = document.createElement('label');
					label.className = 'checkbox-label';
					label.innerHTML = `
                        <input type="checkbox" name="permissionIds" value="${p.id}" ${isChecked} ${isDisabled}>
                        <span>
                            <strong>${p.permissionKey}</strong>
                            <small>${p.description}</small>
                        </span>
                    `;
					groupDiv.appendChild(label);
				});
				details.appendChild(groupDiv);
				checkboxContainer.appendChild(details);
			}

		} catch (error) {
			console.error("Error fetching role permissions:", error);
			checkboxContainer.innerHTML = '<p class="error-message">Fehler beim Laden der Berechtigungen.</p>';
		}
	};

	document.querySelectorAll('.edit-permissions-btn').forEach(btn => {
		btn.addEventListener('click', (e) => openModal(e.currentTarget));
	});

	const closeModal = () => modal.classList.remove('active');

	modal.querySelector('.modal-close-btn').addEventListener('click', closeModal);
	modal.addEventListener('click', (e) => {
		if (e.target === modal) closeModal();
	});
});