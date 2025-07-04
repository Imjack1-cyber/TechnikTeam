document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			showConfirmationModal(this.dataset.confirmMessage || 'Sind Sie sicher?', () => this.submit());
		});
	});

	// --- Modal References ---
	const assignModal = document.getElementById('assign-users-modal');
	const eventModal = document.getElementById('event-modal');
	const reqContainer = document.getElementById('modal-requirements-container');
	const resContainer = document.getElementById('modal-reservations-container');
	const cfContainer = document.getElementById('modal-custom-fields-container');
	const attachmentsList = document.getElementById('modal-attachments-list');
	const kitSelect = document.getElementById('kit-selection-modal');

	// --- Data from JSP for dynamic fields ---
	const allCourses = JSON.parse(document.getElementById('allCoursesData').textContent);
	const allItems = JSON.parse(document.getElementById('allItemsData').textContent);

	// --- Assign Users Modal Logic ---
	const assignForm = document.getElementById('assign-users-form');
	const assignModalTitle = document.getElementById('assign-users-modal-title');
	const assignCheckboxes = document.getElementById('user-checkboxes-container');
	const assignEventIdInput = assignForm.querySelector('input[name="eventId"]');

	const openAssignModal = async (btn) => {
		const eventId = btn.dataset.eventId;
		const eventName = btn.dataset.eventName;
		assignModalTitle.textContent = `Team für "${eventName}" zuweisen`;
		assignEventIdInput.value = eventId;
		assignCheckboxes.innerHTML = '<p>Lade Benutzer...</p>';
		assignModal.classList.add('active');
		try {
			const response = await fetch(`${contextPath}/admin/veranstaltungen?action=getAssignmentData&id=${eventId}`);
			if (!response.ok) throw new Error('Could not fetch assignment data.');
			const data = await response.json();
			assignCheckboxes.innerHTML = '';
			if (data.signedUpUsers && data.signedUpUsers.length > 0) {
				data.signedUpUsers.forEach(user => {
					const isChecked = data.assignedUserIds.includes(user.id) ? 'checked' : '';
					assignCheckboxes.innerHTML += `
						<label class="checkbox-label">
							<input type="checkbox" name="userIds" value="${user.id}" ${isChecked}>
							${user.username}
						</label>`;
				});
			} else {
				assignCheckboxes.innerHTML = '<p>Es haben sich noch keine Benutzer für dieses Event angemeldet.</p>';
			}
		} catch (error) {
			assignCheckboxes.innerHTML = '<p class="error-message">Fehler beim Laden der Benutzerdaten.</p>';
			console.error('Error fetching assignment data:', error);
		}
	};
	document.querySelectorAll('.assign-users-btn').forEach(btn => btn.addEventListener('click', () => openAssignModal(btn)));
	assignModal.querySelector('.modal-close-btn').addEventListener('click', () => assignModal.classList.remove('active'));

	// --- Generic Row Creation and Addition Functions ---
	const createRow = (container) => {
		const newRow = document.createElement('div'); newRow.className = 'dynamic-row';
		const removeBtn = document.createElement('button'); removeBtn.type = 'button'; removeBtn.className = 'btn-small btn-danger';
		removeBtn.innerHTML = '×'; removeBtn.onclick = () => newRow.remove();
		newRow.appendChild(removeBtn); container.appendChild(newRow);
		return newRow;
	};

	const addRequirementRow = (courseId = '', personCount = 1) => {
		const row = createRow(reqContainer);
		const select = document.createElement('select'); select.name = 'requiredCourseId'; select.className = 'form-group';
		select.innerHTML = '<option value="">-- Lehrgang --</option>' + allCourses.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
		select.value = courseId;
		const input = document.createElement('input'); input.type = 'number'; input.name = 'requiredPersons'; input.value = personCount; input.min = '1'; input.className = 'form-group';
		row.prepend(select, input);
	};

	const addReservationRow = (itemId = '', quantity = 1) => {
		const row = createRow(resContainer);
		const select = document.createElement('select'); select.name = 'itemId'; select.className = 'form-group';
		select.innerHTML = '<option value="">-- Material --</option>' + allItems.map(i => `<option value="${i.id}">${i.name}</option>`).join('');
		select.value = itemId;
		const input = document.createElement('input'); input.type = 'number'; input.name = 'itemQuantity'; input.value = quantity; input.min = '1'; input.className = 'form-group';
		row.prepend(select, input);
	};

	const addCustomFieldRow = (fieldName = '', fieldType = 'TEXT') => {
		const row = createRow(cfContainer);
		const nameInput = document.createElement('input');
		nameInput.type = 'text';
		nameInput.name = 'customFieldName';
		nameInput.placeholder = 'Frage eingeben (z.B. T-Shirt Größe)';
		nameInput.className = 'form-group';
		nameInput.style.flexGrow = '2';
		nameInput.value = fieldName;

		const typeSelect = document.createElement('select');
		typeSelect.name = 'customFieldType';
		typeSelect.className = 'form-group';
		typeSelect.innerHTML = `<option value="TEXT">Text</option><option value="BOOLEAN">Ja/Nein</option>`;
		typeSelect.value = fieldType;

		row.prepend(nameInput, typeSelect);
	};

	const addAttachmentRow = (id, filename, filepath) => {
		const li = document.createElement('li'); li.id = `attachment-item-${id}`;
		li.innerHTML = `<a href="${contextPath}/download?file=${filepath}" target="_blank">${filename}</a>`;
		const removeBtn = document.createElement('button'); removeBtn.type = 'button'; removeBtn.className = 'btn btn-small btn-danger-outline';
		removeBtn.innerHTML = '×';
		removeBtn.onclick = () => {
			showConfirmationModal(`Anhang '${filename}' wirklich löschen?`, async () => {
				try {
					const response = await fetch(`${contextPath}/admin/veranstaltungen`, { method: 'POST', body: new URLSearchParams({ action: 'deleteAttachment', id: id }) });
					if (response.ok) li.remove();
					else alert('Fehler beim Löschen des Anhangs.');
				} catch (e) {
					alert('Netzwerkfehler beim Löschen des Anhangs.');
				}
			});
		};
		li.appendChild(removeBtn); attachmentsList.appendChild(li);
	};

	// --- Kit Selection Logic ---
	if (kitSelect) {
		kitSelect.addEventListener('change', async () => {
			const kitId = kitSelect.value;
			if (!kitId) return;

			try {
				const response = await fetch(`${contextPath}/admin/kits?action=getKitItems&id=${kitId}`);
				if (!response.ok) throw new Error('Could not fetch kit items');
				const items = await response.json();
				items.forEach(item => addReservationRow(item.itemId, item.quantity));
			} catch (e) {
				console.error("Error fetching kit items:", e);
				alert("Fehler beim Laden der Kit-Inhalte.");
			}
			// Reset selection to allow re-adding the same kit
			kitSelect.value = '';
		});
	}

	// --- Edit/Create Event Modal Logic ---
	document.getElementById('modal-add-requirement-btn').addEventListener('click', () => addRequirementRow());
	document.getElementById('modal-add-reservation-btn').addEventListener('click', () => addReservationRow());
	document.getElementById('modal-add-custom-field-btn').addEventListener('click', () => addCustomFieldRow());

	const eventForm = document.getElementById('event-modal-form');
	const eventModalTitle = document.getElementById('event-modal-title');
	const actionInput = document.getElementById('event-modal-action');
	const idInput = document.getElementById('event-modal-id');

	const resetEventModal = () => {
		eventForm.reset();
		reqContainer.innerHTML = '';
		resContainer.innerHTML = '';
		cfContainer.innerHTML = '';
		attachmentsList.innerHTML = '';
	};

	const openEventModal = () => eventModal.classList.add('active');
	const closeEventModal = () => eventModal.classList.remove('active');
	eventModal.querySelector('.modal-close-btn').addEventListener('click', closeEventModal);
	eventModal.addEventListener('click', e => { if (e.target === eventModal) closeEventModal(); });

	document.getElementById('new-event-btn').addEventListener('click', () => {
		resetEventModal();
		eventModalTitle.textContent = "Neues Event anlegen";
		actionInput.value = "create";
		idInput.value = "";
		openEventModal();
	});

	document.querySelectorAll('.edit-event-btn').forEach(btn => {
		btn.addEventListener('click', async () => {
			const eventId = btn.dataset.eventId;
			try {
				const response = await fetch(`${contextPath}/admin/veranstaltungen?action=getEventData&id=${eventId}`);
				if (!response.ok) throw new Error('Event data could not be fetched.');
				const event = await response.json();
				resetEventModal();
				eventModalTitle.textContent = "Event bearbeiten";
				actionInput.value = "update";
				idInput.value = event.id;
				eventForm.querySelector('#name-modal').value = event.name || '';
				eventForm.querySelector('#location-modal').value = event.location || '';
				eventForm.querySelector('#leaderUserId-modal').value = event.leaderUserId || '';
				eventForm.querySelector('#eventDateTime-modal').value = event.eventDateTime ? event.eventDateTime.substring(0, 16) : '';
				eventForm.querySelector('#endDateTime-modal').value = event.endDateTime ? event.endDateTime.substring(0, 16) : '';
				eventForm.querySelector('#description-modal').value = event.description || '';

				event.skillRequirements?.forEach(req => addRequirementRow(req.requiredCourseId, req.requiredPersons));
				event.reservedItems?.forEach(res => addReservationRow(res.id, res.quantity));
				event.customFields?.forEach(cf => addCustomFieldRow(cf.fieldName, cf.fieldType));
				event.attachments?.forEach(att => addAttachmentRow(att.id, att.filename, att.filepath));

				openEventModal();
			} catch (error) {
				console.error('Error opening edit modal:', error);
				alert('Fehler beim Laden der Event-Daten.');
			}
		});
	});

	// --- Tab Logic ---
	const tabButtons = eventModal.querySelectorAll('.modal-tab-button');
	const tabContents = eventModal.querySelectorAll('.modal-tab-content');
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