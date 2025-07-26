// src/main/webapp/js/public/events.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const tableBody = document.querySelector('.searchable-table tbody');
	const mobileList = document.querySelector('.mobile-card-list.searchable-table');

	if (!tableBody || !mobileList) return;

	// --- API Abstraction ---
	const api = {
		getEvents: () => fetch(`${contextPath}/api/v1/public/events`).then(res => res.json()),
		signUp: (eventId, data) => fetch(`${contextPath}/api/v1/public/events/${eventId}/signup`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json()),
		signOut: (eventId, reason) => fetch(`${contextPath}/api/v1/public/events/${eventId}/signoff`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ reason })
		}).then(res => res.json())
	};

	const escape = (str) => {
		if (str === null || typeof str === 'undefined') return '';
		const div = document.createElement('div');
		div.innerText = str;
		return div.innerHTML;
	};

	const renderEvents = (events) => {
		tableBody.innerHTML = '';
		mobileList.innerHTML = '';

		if (!events || events.length === 0) {
			tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center;">Derzeit stehen keine Veranstaltungen an.</td></tr>`;
			mobileList.innerHTML = `<div class="card"><p>Derzeit stehen keine Veranstaltungen an.</p></div>`;
			return;
		}

		events.forEach(event => {
			const statusClass = event.status === 'LAUFEND' ? 'status-warn' : (event.status === 'ABGESCHLOSSEN' || event.status === 'ABGESAGT') ? 'status-info' : 'status-ok';
			let actionHtml = '';

			if (event.userAttendanceStatus === 'OFFEN' || event.userAttendanceStatus === 'ABGEMELDET') {
				actionHtml = `<button type="button" class="btn btn-small btn-success signup-btn" data-event-id="${event.id}" data-event-name="${escape(event.name)}" ${!event.userQualified ? 'disabled title="Du erfüllst die Anforderungen für dieses Event nicht."' : ''}>Anmelden</button>`;
			} else if (event.userAttendanceStatus === 'ANGEMELDET') {
				actionHtml = `<button type="button" class="btn btn-small btn-danger signoff-btn" data-event-id="${event.id}" data-event-name="${escape(event.name)}" data-event-status="${event.status}">Abmelden</button>`;
			}

			const statusText = event.userAttendanceStatus === 'ZUGEWIESEN' ? '<strong class="text-success">Zugewiesen</strong>' : escape(event.userAttendanceStatus);

			const row = document.createElement('tr');
			row.innerHTML = `<td><a href="${contextPath}/veranstaltungen/details?id=${event.id}">${escape(event.name)}</a></td><td data-sort-value="${event.eventDateTime}">${escape(event.formattedEventDateTimeRange)}</td><td><span class="status-badge ${statusClass}">${escape(event.status)}</span></td><td>${statusText}</td><td><div style="display: flex; gap: 0.5rem;">${actionHtml}</div></td>`;
			tableBody.appendChild(row);

			const card = document.createElement('div');
			card.className = 'list-item-card';
			card.innerHTML = `<h3 class="card-title"><a href="${contextPath}/veranstaltungen/details?id=${event.id}">${escape(event.name)}</a></h3><div class="card-row"><span>Zeitraum:</span> <strong>${escape(event.formattedEventDateTimeRange)}</strong></div><div class="card-row"><span>Event-Status:</span> <span><span class="status-badge ${statusClass}">${escape(event.status)}</span></span></div><div class="card-row"><span>Dein Status:</span> <strong>${statusText}</strong></div><div class="card-actions">${actionHtml}</div>`;
			mobileList.appendChild(card);
		});
	};

	const loadEvents = async () => {
		try {
			const result = await api.getEvents();
			if (result.success) {
				renderEvents(result.data);
			} else { throw new Error(result.message); }
		} catch (error) {
			console.error("Failed to load events:", error);
			tableBody.innerHTML = `<tr><td colspan="5" class="error-message">Fehler beim Laden der Events.</td></tr>`;
		}
	};

	// --- Event Delegation for Actions ---
	document.body.addEventListener('click', async (e) => {
		const signupBtn = e.target.closest('.signup-btn');
		const signoffBtn = e.target.closest('.signoff-btn');

		if (signupBtn) {
			openSignupModal(signupBtn);
		}

		if (signoffBtn) {
			const eventId = signoffBtn.dataset.eventId;
			const eventName = unescape(signoffBtn.dataset.eventName);
			const eventStatus = signoffBtn.dataset.eventStatus;

			if (eventStatus === 'LAUFEND') {
				document.getElementById('signoff-event-id').value = eventId;
				document.getElementById('signoff-reason-modal').classList.add('active');
			} else {
				showConfirmationModal(`Wirklich vom Event '${eventName}' abmelden?`, async () => {
					try {
						const result = await api.signOut(eventId, null);
						if (result.success) {
							showToast('Erfolgreich abgemeldet.', 'success');
							loadEvents();
						} else { throw new Error(result.message); }
					} catch (error) {
						showToast(error.message || 'Abmelden fehlgeschlagen.', 'danger');
					}
				});
			}
		}
	});

	// --- Modal Logic ---
	const signupModal = document.getElementById('signup-modal');
	const signupForm = document.getElementById('signup-form');
	signupForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const eventId = document.getElementById('signup-event-id').value;
		const formData = new FormData(signupForm);
		const customFieldData = Object.fromEntries(formData.entries());

		try {
			const result = await api.signUp(eventId, customFieldData);
			if (result.success) {
				showToast('Erfolgreich angemeldet!', 'success');
				signupModal.classList.remove('active');
				loadEvents();
			} else { throw new Error(result.message); }
		} catch (error) {
			showToast(error.message || 'Anmeldung fehlgeschlagen.', 'danger');
		}
	});

	const signoffReasonForm = document.getElementById('signoff-reason-form');
	signoffReasonForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const eventId = document.getElementById('signoff-event-id').value;
		const reason = document.getElementById('signoff-reason').value;
		try {
			const result = await api.signOut(eventId, reason);
			if (result.success) {
				showToast('Erfolgreich abgemeldet.', 'success');
				document.getElementById('signoff-reason-modal').classList.remove('active');
				loadEvents();
			} else { throw new Error(result.message); }
		} catch (error) {
			showToast(error.message || 'Abmelden fehlgeschlagen.', 'danger');
		}
	});

	// Initial Load
	loadEvents();
});