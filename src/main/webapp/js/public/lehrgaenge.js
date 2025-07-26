// src/main/webapp/js/public/lehrgaenge.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const tableBody = document.querySelector('.searchable-table tbody');
	const mobileList = document.querySelector('.mobile-card-list.searchable-table');

	if (!tableBody || !mobileList) return;

	const api = {
		getMeetings: () => fetch(`${contextPath}/api/v1/public/meetings`).then(res => res.json()),
		signUp: (meetingId) => fetch(`${contextPath}/api/v1/public/meetings/${meetingId}/signup`, { method: 'POST' }).then(res => res.json()),
		signOut: (meetingId) => fetch(`${contextPath}/api/v1/public/meetings/${meetingId}/signoff`, { method: 'POST' }).then(res => res.json())
	};

	const escape = (str) => {
		if (str === null || typeof str === 'undefined') return '';
		const div = document.createElement('div');
		div.innerText = str;
		return div.innerHTML;
	};

	const renderMeetings = (meetings) => {
		tableBody.innerHTML = '';
		mobileList.innerHTML = '';

		if (!meetings || meetings.length === 0) {
			tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center;">Derzeit stehen keine Lehrgänge oder Meetings an.</td></tr>`;
			mobileList.innerHTML = `<div class="card"><p>Derzeit stehen keine Lehrgänge oder Meetings an.</p></div>`;
			return;
		}

		meetings.forEach(meeting => {
			let actionHtml = '';
			let statusHtml = '';

			if (meeting.userAttendanceStatus === 'ANGEMELDET') {
				actionHtml = `<button type="button" data-action="signoff" data-meeting-id="${meeting.id}" class="btn btn-small btn-danger meeting-action-btn">Abmelden</button>`;
				statusHtml = `<span class="text-success">Angemeldet</span>`;
			} else {
				actionHtml = `<button type="button" data-action="signup" data-meeting-id="${meeting.id}" class="btn btn-small btn-success meeting-action-btn">Anmelden</button>`;
				statusHtml = meeting.userAttendanceStatus === 'ABGEMELDET' ? `<span class="text-danger">Abgemeldet</span>` : 'Offen';
			}

			// Desktop Row
			const row = document.createElement('tr');
			row.innerHTML = `
                <td><a href="${contextPath}/meetingDetails?id=${meeting.id}">${escape(meeting.name)}</a></td>
                <td>${escape(meeting.parentCourseName)}</td>
                <td data-sort-value="${meeting.meetingDateTime}">${escape(meeting.formattedMeetingDateTimeRange)}</td>
                <td>${statusHtml}</td>
                <td>${actionHtml}</td>`;
			tableBody.appendChild(row);

			// Mobile Card
			const card = document.createElement('div');
			card.className = 'list-item-card';
			card.innerHTML = `
                <h3 class="card-title"><a href="${contextPath}/meetingDetails?id=${meeting.id}">${escape(meeting.name)}</a></h3>
                <div class="card-row"><span>Kurs:</span> <strong>${escape(meeting.parentCourseName)}</strong></div>
                <div class="card-row"><span>Zeitraum:</span> <strong>${escape(meeting.formattedMeetingDateTimeRange)}</strong></div>
                <div class="card-row"><span>Dein Status:</span> <strong>${statusHtml}</strong></div>
                <div class="card-actions">${actionHtml}</div>`;
			mobileList.appendChild(card);
		});
	};

	const loadMeetings = async () => {
		try {
			const result = await api.getMeetings();
			if (result.success) {
				renderMeetings(result.data);
			} else { throw new Error(result.message); }
		} catch (error) {
			console.error("Failed to load meetings:", error);
			tableBody.innerHTML = `<tr><td colspan="5" class="error-message">Fehler beim Laden der Lehrgänge.</td></tr>`;
		}
	};

	// Event delegation for action buttons
	document.body.addEventListener('click', async (e) => {
		const actionBtn = e.target.closest('.meeting-action-btn');
		if (!actionBtn) return;

		const action = actionBtn.dataset.action;
		const meetingId = actionBtn.dataset.meetingId;

		try {
			const result = (action === 'signup') ? await api.signUp(meetingId) : await api.signOut(meetingId);
			if (result.success) {
				showToast(action === 'signup' ? 'Erfolgreich angemeldet' : 'Erfolgreich abgemeldet', 'success');
				loadMeetings(); // Refresh the list
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast(error.message || 'Aktion fehlgeschlagen.', 'danger');
		}
	});

	loadMeetings();
});