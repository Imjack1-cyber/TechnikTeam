// src/main/webapp/js/admin/admin_matrix.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const tableContainer = document.getElementById('matrix-table-container');
	const modalOverlay = document.getElementById('attendance-modal');
	if (!tableContainer || !modalOverlay) return;

	// --- API Abstraction for v1 ---
	const api = {
		getMatrixData: () => fetch(`${contextPath}/api/v1/matrix/qualifications`).then(res => res.json()),
		updateAttendance: (data) => fetch(`${contextPath}/api/v1/matrix/attendance`, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json())
	};

	// --- Modal Element References ---
	const modalForm = document.getElementById('attendance-modal-form');
	const modalTitle = document.getElementById('modal-title');
	const modalUserId = document.getElementById('modal-user-id');
	const modalMeetingId = document.getElementById('modal-meeting-id');
	const modalAttended = document.getElementById('modal-attended');
	const modalRemarks = document.getElementById('modal-remarks');
	const closeBtn = modalOverlay.querySelector('.modal-close-btn');

	let activeCell = null; // Store a reference to the clicked cell

	const openModal = (cell) => {
		activeCell = cell; // Keep track of which cell was clicked
		const userData = cell.dataset;
		modalTitle.textContent = `Nutzer: ${userData.userName} | Meeting: ${userData.meetingName}`;
		modalUserId.value = userData.userId;
		modalMeetingId.value = userData.meetingId;
		modalRemarks.value = userData.remarks;
		modalAttended.checked = (userData.attended === 'true');
		modalOverlay.classList.add('active');
	};

	const closeModal = () => {
		activeCell = null; // Clear the reference
		modalOverlay.classList.remove('active');
	};

	modalForm.addEventListener('submit', async (e) => {
		e.preventDefault();

		const data = {
			userId: parseInt(modalUserId.value, 10),
			meetingId: parseInt(modalMeetingId.value, 10),
			attended: modalAttended.checked,
			remarks: modalRemarks.value
		};

		try {
			const result = await api.updateAttendance(data);
			if (result.success) {
				showToast('Anwesenheit gespeichert.', 'success');
				// --- Live UI Update ---
				if (activeCell) {
					activeCell.dataset.attended = data.attended.toString();
					activeCell.dataset.remarks = data.remarks;
					activeCell.innerHTML = data.attended
						? '<span style="font-size: 1.2rem;">✔</span>'
						: '<span class="text-muted">-</span>';
				}
				closeModal();
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error("Error updating attendance:", error);
			showToast(error.message || 'Speichern fehlgeschlagen.', 'danger');
		}
	});

	const renderMatrix = (data) => {
		tableContainer.innerHTML = ''; // Clear loading message
		const table = document.createElement('table');
		table.className = 'data-table';

		// Build Header
		const thead = document.createElement('thead');
		const headerRow1 = document.createElement('tr');
		const headerRow2 = document.createElement('tr');
		headerRow1.innerHTML = `<th rowspan="2" class="sticky-header sticky-col" style="vertical-align: middle; left: 0; z-index: 15;">Nutzer / Lehrgang ↓</th>`;

		data.courses.forEach(course => {
			const meetings = data.meetingsByCourse[course.id] || [];
			const th1 = document.createElement('th');
			th1.colSpan = meetings.length;
			th1.className = 'sticky-header';
			th1.style.textAlign = 'center';
			th1.innerHTML = `<a href="${contextPath}/admin/lehrgaenge" title="Vorlagen verwalten">${escape(course.abbreviation)}</a>`;
			headerRow1.appendChild(th1);

			meetings.forEach(meeting => {
				const th2 = document.createElement('th');
				th2.className = 'sticky-header';
				th2.style.cssText = 'text-align: center; min-width: 120px;';
				th2.innerHTML = `<a href="${contextPath}/admin/meetings?courseId=${course.id}" title="Meetings für '${escape(course.name)}' verwalten">${escape(meeting.name)}</a>`;
				headerRow2.appendChild(th2);
			});
		});
		thead.appendChild(headerRow1);
		thead.appendChild(headerRow2);
		table.appendChild(thead);

		// Build Body
		const tbody = document.createElement('tbody');
		data.users.forEach(user => {
			const row = document.createElement('tr');
			row.innerHTML = `<td class="sticky-col" style="font-weight: 500; left: 0;"><a href="${contextPath}/admin/mitglieder?action=details&id=${user.id}">${escape(user.username)}</a></td>`;
			data.courses.forEach(course => {
				const meetings = data.meetingsByCourse[course.id] || [];
				meetings.forEach(meeting => {
					const attendanceKey = `${user.id}-${meeting.id}`;
					const attendance = data.attendanceMap[attendanceKey];
					const attended = attendance ? attendance.attended : false;
					const remarks = attendance ? (attendance.remarks || '') : '';

					const cell = document.createElement('td');
					cell.className = 'qual-cell';
					cell.dataset.userId = user.id;
					cell.dataset.userName = user.username;
					cell.dataset.meetingId = meeting.id;
					cell.dataset.meetingName = `${course.name} - ${meeting.name}`;
					cell.dataset.attended = attended.toString();
					cell.dataset.remarks = remarks;
					cell.style.cssText = "text-align: center; font-weight: bold; cursor: pointer;";
					cell.title = "Klicken zum Bearbeiten";
					cell.innerHTML = attended ? '<span style="font-size: 1.2rem;">✔</span>' : '<span class="text-muted">-</span>';
					row.appendChild(cell);
				});
			});
			tbody.appendChild(row);
		});
		table.appendChild(tbody);
		tableContainer.appendChild(table);
	};

	const loadMatrix = async () => {
		tableContainer.innerHTML = '<div class="card"><p>Lade Matrix-Daten...</p></div>';
		try {
			const result = await api.getMatrixData();
			if (result.success) {
				renderMatrix(result.data);
			} else { throw new Error(result.message); }
		} catch (error) {
			console.error("Failed to load matrix data:", error);
			tableContainer.innerHTML = `<div class="card error-message">Matrix konnte nicht geladen werden: ${error.message}</div>`;
		}
	};

	tableContainer.addEventListener('click', (e) => {
		const cell = e.target.closest('.qual-cell');
		if (cell) {
			openModal(cell);
		}
	});

	if (closeBtn) closeBtn.addEventListener('click', closeModal);
	modalOverlay.addEventListener('click', (event) => {
		if (event.target === modalOverlay) closeModal();
	});
	document.addEventListener('keydown', (event) => {
		if (event.key === 'Escape' && modalOverlay.classList.contains('active')) {
			closeModal();
		}
	});

	const escape = (str) => {
		if (!str) return '';
		return str.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>').replace(/"/g, '"').replace(/'/g, ''');
    };

	loadMatrix();
});