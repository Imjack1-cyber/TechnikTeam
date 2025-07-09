document.addEventListener('DOMContentLoaded', () => {
    const contextPath = document.body.dataset.contextPath || '';
    const modal = document.getElementById('role-permissions-modal');
    if (!modal) return;

    const modalTitle = document.getElementById('role-modal-title');
    const roleIdInput = document.getElementById('modal-role-id');
    const roleNameInput = document.getElementById('modal-role-name');
    const checkboxContainer = document.getElementById('permissions-checkbox-container');
    const allPermissions = JSON.parse('${fn:escapeXml(gson.toJson(allPermissions))}');

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
            allPermissions.forEach(p => {
                const isChecked = assignedIds.has(p.id) ? 'checked' : '';
                const isDisabled = p.permissionKey === 'ACCESS_ADMIN_PANEL' && roleName === 'ADMIN' ? 'disabled' : ''; 
                const label = document.createElement('label');
                label.style.display = 'flex';
                label.style.alignItems = 'flex-start';
                label.innerHTML = `
                    <input type="checkbox" name="permissionIds" value="${p.id}" ${isChecked} ${isDisabled} style="margin-top: 5px; margin-right: 10px;">
                    <div>
                        <strong>${p.permissionKey}</strong>
                        <small style="display: block; color: var(--text-muted-color);">${p.description}</small>
                    </div>
                `;
                checkboxContainer.appendChild(label);
            });

        } catch (error) {
            console.error("Error fetching role permissions:", error);
            checkboxContainer.innerHTML = '<p class="error-message">Fehler beim Laden der Berechtigungen.</p>';
        }
    };

    document.querySelectorAll('.edit-permissions-btn').forEach(btn => {
        btn.addEventListener('click', () => openModal(btn));
    });

    modal.querySelector('.modal-close-btn').addEventListener('click', () => modal.classList.remove('active'));
});