// src/main/webapp/js/public/profile.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const container = document.getElementById('profile-container');
	if (!container) return;

	let originalValues = {};

	// --- API Abstraction ---
	const api = {
		getProfile: () => fetch(`${contextPath}/api/v1/public/profile`).then(res => res.json()),
		requestChange: (data) => fetch(`${contextPath}/api/v1/public/profile/request-change`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json()),
		updateChatColor: (color) => fetch(`${contextPath}/api/v1/public/profile/chat-color`, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ chatColor: color })
		}).then(res => res.json()),
		deletePasskey: (id) => fetch(`${contextPath}/api/v1/public/profile/passkeys/${id}`, {
			method: 'DELETE'
		}).then(res => res.json())
	};

	// --- Utility Functions ---
	const escape = (str) => {
		if (str === null || typeof str === 'undefined') return '';
		const div = document.createElement('div');
		div.innerText = str;
		return div.innerHTML;
	};

	const formatDate = (dateString) => {
		if (!dateString) return '';
		return new Date(dateString).toLocaleString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
	};

	// --- RENDER FUNCTIONS ---
	const renderDetails = (data) => {
		const container = document.getElementById('profile-details-container');
		const user = data.user;
		container.innerHTML = `
            <h2 class="card-title">Stammdaten</h2>
            ${data.hasPendingRequest ? `<div class="info-message"><i class="fas fa-info-circle"></i> Sie haben eine ausstehende Profiländerung, die von einem Administrator geprüft wird.</div>` : ''}
            <form id="profile-form">
                <ul class="details-list">
                    <li><strong>Benutzername:</strong> <input type="text" name="username" class="form-group" value="${escape(user.username)}" readonly style="display: inline-block; width: auto; background-color: transparent; border-color: transparent;"></li>
                    <li><strong>Jahrgang:</strong> <input type="number" name="classYear" class="form-group editable-field" value="${user.classYear || ''}" readonly></li>
                    <li><strong>Klasse:</strong> <input type="text" name="className" class="form-group editable-field" value="${escape(user.className)}" readonly></li>
                    <li><strong>E-Mail:</strong> <input type="email" name="email" class="form-group editable-field" value="${escape(user.email)}" readonly></li>
                </ul>
                <div style="margin-top: 1.5rem; display: flex; gap: 0.5rem;">
                    ${!data.hasPendingRequest ? `
                        <button type="button" id="edit-profile-btn" class="btn btn-secondary">Profil bearbeiten</button>
                        <button type="submit" id="submit-profile-btn" class="btn btn-success" style="display: none;">Antrag einreichen</button>
                        <button type="button" id="cancel-edit-btn" class="btn" style="background-color: var(--text-muted-color); display: none;">Abbrechen</button>
                    ` : ''}
                </div>
            </form>
            <hr style="margin: 1.5rem 0;">
            <ul class="details-list">
                <li style="align-items: center; gap: 1rem;"><strong>Chat-Farbe:</strong>
                    <form id="chat-color-form" style="display: flex; align-items: center; gap: 0.5rem;">
                        <input type="color" name="chatColor" value="${escape(user.chatColor || '#E9ECEF')}" title="Wähle deine Chat-Farbe">
                        <button type="submit" class="btn btn-small">Speichern</button>
                    </form>
                </li>
                <li><a href="${contextPath}/passwort" class="btn btn-secondary">Passwort ändern</a></li>
            </ul>`;
	};

	const renderSecurity = (data) => {
		const container = document.getElementById('profile-security-container');
		container.innerHTML = `
            <h2 class="card-title">Sicherheit (Passkeys)</h2>
            <p>Registrieren Sie Geräte für einen passwortlosen Login.</p>
            <button id="register-passkey-btn" class="btn btn-success" style="margin-bottom: 1rem;"><i class="fas fa-plus-circle"></i> Neues Gerät registrieren</button>
            <h3 style="margin-top: 1.5rem; font-size: 1.1rem;">Registrierte Geräte</h3>
            <ul class="details-list">
                ${data.passkeys.length === 0 ? '<li>Keine Passkeys registriert.</li>' : data.passkeys.map(key => `
                <li>
                    <span><i class="fas fa-key"></i> ${escape(key.name)}
                        <small style="display: block; color: var(--text-muted-color);">Registriert am: ${formatDate(key.createdAt)}</small>
                    </span>
                    <button type="button" class="btn btn-small btn-danger-outline delete-passkey-btn" data-id="${key.id}">Entfernen</button>
                </li>`).join('')}
            </ul>`;
	};

	const renderQualifications = (data) => {
		const container = document.getElementById('profile-qualifications-container');
		container.innerHTML = `
            <h2 class="card-title">Meine Qualifikationen</h2>
            <div class="table-wrapper" style="max-height: 400px; overflow-y: auto;">
                <table class="data-table">
                    <thead><tr><th>Lehrgang</th><th>Status</th></tr></thead>
                    <tbody>
                        ${data.qualifications.length === 0 ? '<tr><td colspan="2">Keine Qualifikationen erworben.</td></tr>' : data.qualifications.map(qual => `
                        <tr>
                            <td>${escape(qual.courseName)}</td>
                            <td>${escape(qual.status)}</td>
                        </tr>`).join('')}
                    </tbody>
                </table>
            </div>`;
	};

	const renderAchievements = (data) => {
		const container = document.getElementById('profile-achievements-container');
		container.innerHTML = `
            <h2 class="card-title">Meine Abzeichen</h2>
            ${data.achievements.length === 0 ? '<p>Du hast noch keine Abzeichen verdient. Nimm an Events teil, um sie freizuschalten!</p>' : ''}
            <div style="display: flex; flex-wrap: wrap; gap: 1rem;">
                ${data.achievements.map(ach => `
                <div class="card" style="flex: 1; min-width: 250px; text-align: center;">
                    <i class="fas ${escape(ach.iconClass)}" style="font-size: 3rem; color: var(--primary-color); margin-bottom: 1rem;"></i>
                    <h4 style="margin: 0;">${escape(ach.name)}</h4>
                    <p style="color: var(--text-muted-color); font-size: 0.9rem;">${escape(ach.description)}</p>
                    <small>Verdient am: ${new Date(ach.earnedAt).toLocaleDateString('de-DE')}</small>
                </div>`).join('')}
            </div>`;
	};

	const renderEventHistory = (data) => {
		const container = document.getElementById('profile-history-container');
		container.innerHTML = `
             <h2 class="card-title">Meine Event-Historie</h2>
             <div class="table-wrapper" style="max-height: 500px; overflow-y: auto;">
                <table class="data-table">
                    <thead><tr><th>Event</th><th>Datum</th><th>Dein Status</th><th>Feedback</th></tr></thead>
                    <tbody>
                        ${data.eventHistory.length === 0 ? '<tr><td colspan="4">Keine Event-Historie vorhanden.</td></tr>' : data.eventHistory.map(event => `
                        <tr>
                            <td><a href="${contextPath}/veranstaltungen/details?id=${event.id}">${escape(event.name)}</a></td>
                            <td>${formatDate(event.eventDateTime)} Uhr</td>
                            <td>${escape(event.userAttendanceStatus)}</td>
                            <td>${event.status === 'ABGESCHLOSSEN' && event.userAttendanceStatus === 'ZUGEWIESEN' ? `<a href="${contextPath}/feedback?action=submitEventFeedback&eventId=${event.id}" class="btn btn-small">Feedback geben</a>` : ''}</td>
                        </tr>`).join('')}
                    </tbody>
                </table>
             </div>`;
	};


	const loadProfile = async () => {
		try {
			const result = await api.getProfile();
			if (!result.success) throw new Error(result.message);
			const data = result.data;

			renderDetails(data);
			renderSecurity(data);
			renderQualifications(data);
			renderAchievements(data);
			renderEventHistory(data);

		} catch (error) {
			container.innerHTML = `<div class="card error-message">Profil konnte nicht geladen werden: ${error.message}</div>`;
		}
	};

	const toggleEditMode = (isEditing) => {
		const editableFields = document.querySelectorAll('.editable-field');
		const editBtn = document.getElementById('edit-profile-btn');
		const submitBtn = document.getElementById('submit-profile-btn');
		const cancelBtn = document.getElementById('cancel-edit-btn');

		editableFields.forEach(field => {
			field.readOnly = !isEditing;
			field.style.backgroundColor = isEditing ? 'var(--bg-color)' : '';
			field.style.border = isEditing ? '1px solid var(--border-color)' : '1px solid transparent';
		});

		if (editBtn) editBtn.style.display = isEditing ? 'none' : 'inline-flex';
		if (submitBtn) submitBtn.style.display = isEditing ? 'inline-flex' : 'none';
		if (cancelBtn) cancelBtn.style.display = isEditing ? 'inline-flex' : 'none';

		if (isEditing) {
			originalValues = {};
			editableFields.forEach(input => {
				originalValues[input.name] = input.value;
			});
		}
	};

	// --- Event Delegation for Actions ---
	container.addEventListener('click', async e => {
		const editBtn = e.target.closest('#edit-profile-btn');
		const cancelBtn = e.target.closest('#cancel-edit-btn');
		const deletePasskeyBtn = e.target.closest('.delete-passkey-btn');

		if (editBtn) { toggleEditMode(true); }
		if (cancelBtn) {
			document.querySelectorAll('.editable-field').forEach(input => {
				input.value = originalValues[input.name];
			});
			toggleEditMode(false);
		}

		if (deletePasskeyBtn) {
			const id = deletePasskeyBtn.dataset.id;
			showConfirmationModal('Diesen Passkey wirklich entfernen?', async () => {
				const result = await api.deletePasskey(id);
				if (result.success) {
					showToast('Passkey entfernt.', 'success');
					loadProfile();
				} else { showToast(result.message, 'danger'); }
			});
		}
	});

	container.addEventListener('submit', async e => {
		e.preventDefault();
		if (e.target.id === 'profile-form') {
			const data = {
				className: document.querySelector('input[name="className"]').value,
				classYear: document.querySelector('input[name="classYear"]').value,
				email: document.querySelector('input[name="email"]').value,
			};
			const result = await api.requestChange(data);
			if (result.success) {
				showToast('Änderungsantrag erfolgreich eingereicht.', 'success');
				toggleEditMode(false);
				loadProfile();
			} else {
				showToast(result.message || 'Fehler beim Einreichen.', 'danger');
			}
		}
		if (e.target.id === 'chat-color-form') {
			const color = document.querySelector('input[name="chatColor"]').value;
			const result = await api.updateChatColor(color);
			showToast(result.message, result.success ? 'success' : 'danger');
		}
	});

	loadProfile();
}); ```