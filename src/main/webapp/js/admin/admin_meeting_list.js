// src/main/webapp/js/admin/admin_meeting_list.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const courseId = document.body.dataset.courseId;
	if (!courseId) return;

	const tableBody = document.querySelector('.searchable-table tbody');
	const mobileList = document.querySelector('.mobile-card-list.searchable-table');
	const modal = document.getElementById('meeting-modal');

	if (!tableBody || !mobileList || !modal) return;

	// --- API Abstraction for v1 ---
	const api = {
		getAll: (courseId) => fetch(`${contextPath}/api/v1/meetings?courseId=${courseId}`).then(res => res.json()),
		getOne: (id) => fetch(`${contextPath}/api/v1/meetings/${id}`).then(res => res.json()),
		create: (formData) => fetch(`${contextPath}/api/v1/meetings`, { method: 'POST', body: formData }).then(res => res.json()),
		update: (id, formData) => fetch(`${contextPath}/api/v1/meetings/${id}`, { method: 'POST', body: formData }).then(res => res.json()),
		delete: (id) => fetch(`${contextPath}/api/v1/meetings/${id}`, { method: 'DELETE' }).then(res => res.json()),
		deleteAttachment: (meetingId, attachmentId) => fetch(`${contextPath}/api/v1/meetings/${meetingId}/attachments/${attachmentId}`, { method: 'DELETE' }).then(res => res.json())
	};

	// --- Modal Element References ---
	const form = document.getElementById('meeting-modal-form');
	const modalTitle = document.getElementById('meeting-modal-title');
	const actionInput = document.getElementById('meeting-action');
	const idInput = document.getElementById('meeting-id');
	const attachmentsList = document.getElementById('modal-attachments-list');

	/**
	 * Renders both the desktop table and mobile card view from meeting data.
	 * @param {Array} meetings - Array of meeting objects.
	 */
	const renderMeetings = (meetings) => {
		tableBody.innerHTML = '';
		mobileList.innerHTML = '';

		if (!meetings || meetings.length === 0) {
			const noDataRow = `<tr><td colspan="4" style="text-align: center;">Für diesen Lehrgang wurden noch keine Meetings geplant.</td></tr>`;
			const noDataCard = `<div class="card"><p>Für diesen Lehrgang wurden noch keine Meetings geplant.</p></div>`;
			tableBody.innerHTML = noDataRow;
			mobileList.innerHTML = noDataCard;
			return;
		}

		meetings.forEach(meeting => {
			const actionsHtml = `
                <button type="button" class="btn btn-small edit-meeting-btn" data-meeting-id="${meeting.id}">Bearbeiten & Anhänge</button>
                <button type="button" class="btn btn-small btn-danger delete-meeting-btn" data-meeting-id="${meeting.id}" data-meeting-name="${escape(meeting.name)}">Löschen</button>`;

			// Desktop Row
			const row = document.createElement('tr');
			row.innerHTML = `
                <td><a href="${contextPath}/meetingDetails?id=${meeting.id}">${escape(meeting.name)}</a></td>
                <td>${escape(meeting.formattedMeetingDateTimeRange)}</td>
                <td>${escape(meeting.leaderUsername || 'N/A')}</td>
                <td style="display: flex; gap: 0.5rem;">${actionsHtml}</td>`;
			tableBody.appendChild(row);

			// Mobile Card
			const card = document.createElement('div');
			card.className = 'list-item-card';
			card.innerHTML = `
                <h3 class="card-title"><a href="${contextPath}/meetingDetails?id=${meeting.id}">${escape(meeting.name)}</a></h3>
                <div class="card-row"><span>Zeitraum:</span> <strong>${escape(meeting.formattedMeetingDateTimeRange)}</strong></div>
                <div class="card-row"><span>Leitung:</span> <strong>${escape(meeting.leaderUsername || 'N/A')}</strong></div>
                <div class="card-actions">${actionsHtml}</div>`;
			mobileList.appendChild(card);
		});
	};

	/**
	 * Fetches all meetings from the API and triggers rendering.
	 */
	const loadMeetings = async () => {
		try {
			const result = await api.getAll(courseId);
			if (result.success) {
				renderMeetings(result.data);
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error("Failed to load meetings:", error);
			tableBody.innerHTML = `<tr><td colspan="4" class="error-message">Fehler beim Laden der Meetings.</td></tr>`;
		}
	};

	const resetModal = () => {
		form.reset();
		attachmentsList.innerHTML = '';
	};

	const addAttachmentRow = (attachment) => {
		const li = document.createElement('li');
		li.id = `attachment-item-${attachment.id}`;
		li.innerHTML = `<a href="${contextPath}/download?id=${attachment.id}" target="_blank">${escape(attachment.filename)}</a> (Rolle: ${escape(attachment.requiredRole)})`;
		const removeBtn = document.createElement('button');
		removeBtn.type = 'button';
		removeBtn.className = 'btn btn-small btn-danger-outline';
		removeBtn.innerHTML = '×';
		removeBtn.onclick = () => {
			showConfirmationModal(`Anhang '${escape(attachment.filename)}' wirklich löschen?`, async () => {
				const meetingId = idInput.value;
				try {
					const result = await api.deleteAttachment(meetingId, attachment.id);
					if (result.success) {
						showToast('Anhang gelöscht.', 'success');
						li.remove();
					} else {
						throw new Error(result.message);
					}
				} catch (error) {
					showToast(error.message || 'Löschen fehlgeschlagen.', 'danger');
				}
			});
		};
		li.appendChild(removeBtn);
		attachmentsList.appendChild(li);
	};

	form.addEventListener('submit', async (e) => {
		e.preventDefault();
		const meetingId = idInput.value;
		const isUpdate = !!meetingId;

		const formData = new FormData(form);

		try {
			const result = isUpdate ? await api.update(meetingId, formData) : await api.create(formData);
			if (result.success) {
				showToast(result.message, 'success');
				modal.classList.remove('active');
				loadMeetings();
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast(error.message || 'Speichern fehlgeschlagen', 'danger');
		}
	});

	// --- Event Delegation ---
	document.body.addEventListener('click', async (e) => {
		const newBtn = e.target.closest('#new-meeting-btn');
		const editBtn = e.target.closest('.edit-meeting-btn');
		const deleteBtn = e.target.closest('.delete-meeting-btn');

		if (newBtn) {
			resetModal();
			modalTitle.textContent = "Neues Meeting planen";
			actionInput.value = "create";
			idInput.value = "";
			modal.classList.add('active');
		}

		if (editBtn) {
			const meetingId = editBtn.dataset.meetingId;
			try {
				const result = await api.getOne(meetingId);
				if (!result.success) throw new Error(result.message);

				const { meeting, attachments } = result.data;
				resetModal();
				modalTitle.textContent = "Meeting bearbeiten";
				actionInput.value = "update"; // Though not used by API, good for state
				idInput.value = meeting.id;

				form.querySelector('#name-modal').value = meeting.name || '';
				form.querySelector('#location-modal').value = meeting.location || '';
				form.querySelector('#meetingDateTime-modal').value = meeting.meetingDateTime ? meeting.meetingDateTime.substring(0, 16) : '';
				form.querySelector('#endDateTime-modal').value = meeting.endDateTime ? meeting.endDateTime.substring(0, 16) : '';
				form.querySelector('#leader-modal').value = meeting.leaderUserId || '';
				form.querySelector('#description-modal').value = meeting.description || '';

				if (attachments && attachments.length > 0) {
					attachments.forEach(addAttachmentRow);
				} else {
					attachmentsList.innerHTML = '<li>Keine Anhänge vorhanden.</li>';
				}
				modal.classList.add('active');
			} catch (error) {
				showToast('Meeting-Daten konnten nicht geladen werden.', 'danger');
			}
		}

		if (deleteBtn) {
			const meetingId = deleteBtn.dataset.meetingId;
			const meetingName = unescape(deleteBtn.dataset.meetingName);
			showConfirmationModal(`Meeting '${meetingName}' wirklich löschen?`, async () => {
				try {
					const result = await api.delete(meetingId);
					if (result.success) {
						showToast(result.message, 'success');
						loadMeetings();
					} else {
						throw new Error(result.message);
					}
				} catch (error) {
					showToast(error.message || 'Löschen fehlgeschlagen.', 'danger');
				}
			});
		}
	});

	const escape = (str) => {
		if (!str) return '';
		return str.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>').replace(/"/g, '"').replace(/'/g, ''');
    };

	// Initial Load
	loadMeetings();
});