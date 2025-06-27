<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Eventverwaltung" />
</c:import>

<h1>
	<i class="fas fa-calendar-check"></i> Eventverwaltung
</h1>
<p>Hier können Sie Events erstellen, bearbeiten, Personal zuweisen
	und den Status verwalten.</p>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">
		<i class="fas fa-check-circle"></i> ${sessionScope.successMessage}
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">
		<i class="fas fa-exclamation-triangle"></i>
		${sessionScope.errorMessage}
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="table-controls">
	<button type="button" class="btn" id="new-event-btn">
		<i class="fas fa-plus"></i> Neues Event anlegen
	</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<!-- Unified view for Mobile and Desktop, handled by CSS -->
<div class="table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name</th>
				<th class="sortable" data-sort-type="string">Zeitraum</th>
				<th class="sortable" data-sort-type="string">Status</th>
				<th style="width: 450px;">Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:if test="${empty eventList}">
				<tr>
					<td colspan="4" style="text-align: center;">Keine Events
						gefunden.</td>
				</tr>
			</c:if>
			<c:forEach var="event" items="${eventList}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a></td>
					<td>${event.formattedEventDateTimeRange}</td>
					<td><span
						class="status-badge ${event.status == 'KOMPLETT' or event.status == 'ZUGEWIESEN' ? 'status-ok' : event.status == 'LAUFEND' ? 'status-warn' : event.status == 'ABGESCHLOSSEN' ? 'status-info' : 'status-info'}">${event.status}</span></td>
					<td style="display: flex; gap: 5px; flex-wrap: wrap;">
						<button type="button" class="btn btn-small edit-event-btn"
							data-event-id="${event.id}">Bearbeiten</button> <c:if
							test="${event.status != 'ABGESCHLOSSEN'}">
							<button type="button"
								class="btn btn-small btn-success assign-users-btn"
								data-event-id="${event.id}"
								data-event-name="${fn:escapeXml(event.name)}">Zuweisen</button>
							<c:if
								test="${event.status == 'GEPLANT' || event.status == 'KOMPLETT'}">
								<form action="${pageContext.request.contextPath}/admin/events"
									method="post" style="display: inline;" class="js-confirm-form"
									data-confirm-message="Event '${fn:escapeXml(event.name)}' wirklich starten? Der Chat wird aktiviert.">
									<input type="hidden" name="action" value="updateStatus">
									<input type="hidden" name="id" value="${event.id}"> <input
										type="hidden" name="newStatus" value="LAUFEND">
									<button type="submit" class="btn btn-small btn-warning">Starten</button>
								</form>
							</c:if>
							<c:if test="${event.status == 'LAUFEND'}">
								<form action="${pageContext.request.contextPath}/admin/events"
									method="post" style="display: inline;" class="js-confirm-form"
									data-confirm-message="Event '${fn:escapeXml(event.name)}' wirklich abschließen?">
									<input type="hidden" name="action" value="updateStatus">
									<input type="hidden" name="id" value="${event.id}"> <input
										type="hidden" name="newStatus" value="ABGESCHLOSSEN">
									<button type="submit" class="btn btn-small"
										style="background-color: var(--text-muted-color);">Abschließen</button>
								</form>
							</c:if>
						</c:if>
						<form action="${pageContext.request.contextPath}/admin/events"
							method="post" class="inline-form js-confirm-form"
							data-confirm-message="Soll das Event '${fn:escapeXml(event.name)}' wirklich endgültig gelöscht werden?">
							<input type="hidden" name="action" value="delete"> <input
								type="hidden" name="id" value="${event.id}">
							<button type="submit" class="btn btn-small btn-danger">Löschen</button>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- All modals are now at the bottom of the page -->
<%@ include file="/WEB-INF/jspf/event_modals.jspf"%>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<style>
.dynamic-row {
	display: flex;
	gap: 10px;
	margin-bottom: 10px;
	align-items: center;
}

.dynamic-row select {
	flex-grow: 1;
}

.dynamic-row input {
	max-width: 100px;
}

.inline-form {
	display: inline;
}

.user-checkbox-list {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
	max-height: 300px;
	overflow-y: auto;
	padding: 0.5rem;
	border: 1px solid var(--border-color);
	border-radius: 6px;
}

.checkbox-label {
	display: flex;
	align-items: center;
	gap: 0.75rem;
	font-size: 1.1rem;
	cursor: pointer;
}

.checkbox-label input {
	width: auto;
	height: 1.2rem;
}
</style>
<script>
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = "${'${pageContext.request.contextPath}'}";
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			showConfirmationModal(this.dataset.confirmMessage || 'Sind Sie sicher?', () => this.submit());
		});
	});

	// --- Assign Users Modal Logic ---
	const assignModal = document.getElementById('assign-users-modal');
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
			const response = await fetch(`${contextPath}/admin/events?action=getAssignmentData&id=${eventId}`);
			if (!response.ok) throw new Error('Could not fetch assignment data.');
			const data = await response.json();
			assignCheckboxes.innerHTML = '';
			if(data.signedUpUsers && data.signedUpUsers.length > 0) {
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


	// --- Edit/Create Event Modal Logic ---
	const eventModal = document.getElementById('event-modal');
	const eventForm = document.getElementById('event-modal-form');
	const eventModalTitle = document.getElementById('event-modal-title');
	const actionInput = document.getElementById('event-modal-action');
	const idInput = document.getElementById('event-modal-id');
	const reqContainer = document.getElementById('modal-requirements-container');
	const resContainer = document.getElementById('modal-reservations-container');
	const attachmentsList = document.getElementById('modal-attachments-list');

	const allCourses = [<c:forEach var="c" items="${allCourses}">{id: ${c.id}, name: "${fn:escapeXml(c.name)}"},</c:forEach>];
	const allItems = [<c:forEach var="i" items="${allItems}">{id: ${i.id}, name: "${fn:escapeXml(i.name)} (verfügbar: ${i.availableQuantity})"},</c:forEach>];

	const openEventModal = () => eventModal.classList.add('active');
	const closeEventModal = () => eventModal.classList.remove('active');

	eventModal.querySelector('.modal-close-btn').addEventListener('click', closeEventModal);
	eventModal.addEventListener('click', e => { if (e.target === eventModal) closeEventModal(); });

	const resetEventModal = () => {
		eventForm.reset();
		reqContainer.innerHTML = ''; 
		resContainer.innerHTML = '';
		attachmentsList.innerHTML = '';
	};

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
				const response = await fetch(`${contextPath}/admin/events?action=getEventData&id=${eventId}`);
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

	const createRow = (container) => {
		const newRow = document.createElement('div'); newRow.className = 'dynamic-row';
		const removeBtn = document.createElement('button'); removeBtn.type = 'button'; removeBtn.className = 'btn-small btn-danger';
		removeBtn.innerHTML = '×'; removeBtn.onclick = () => newRow.remove();
		newRow.appendChild(removeBtn); container.appendChild(newRow);
		return newRow;
	};

	const addRequirementRow = (courseId = '', personCount = 1) => {
		const row = createRow(reqContainer);
		const select = document.createElement('select'); select.name = 'requiredCourseId';
		select.innerHTML = '<option value="">-- Lehrgang --</option>' + allCourses.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
		select.value = courseId;
		const input = document.createElement('input'); input.type = 'number'; input.name = 'requiredPersons'; input.value = personCount; input.min = '1';
		row.prepend(select, input);
	};
	document.getElementById('modal-add-requirement-btn').addEventListener('click', () => addRequirementRow());

	const addReservationRow = (itemId = '', quantity = 1) => {
		const row = createRow(resContainer);
		const select = document.createElement('select'); select.name = 'itemId';
		select.innerHTML = '<option value="">-- Material --</option>' + allItems.map(i => `<option value="${i.id}">${i.name}</option>`).join('');
		select.value = itemId;
		const input = document.createElement('input'); input.type = 'number'; input.name = 'itemQuantity'; input.value = quantity; input.min = '1';
		row.prepend(select, input);
	};
	document.getElementById('modal-add-reservation-btn').addEventListener('click', () => addReservationRow());
	
	const addAttachmentRow = (id, filename, filepath) => {
        const li = document.createElement('li'); li.id = `attachment-item-${id}`;
        li.innerHTML = `<a href="${contextPath}/download?file=${filepath}" target="_blank">${filename}</a>`;
        const removeBtn = document.createElement('button'); removeBtn.type = 'button'; removeBtn.className = 'btn btn-small btn-danger-outline';
        removeBtn.innerHTML = '×';
        removeBtn.onclick = () => {
            showConfirmationModal(`Anhang '${filename}' wirklich löschen?`, async () => {
                try {
                    const response = await fetch(`${contextPath}/admin/events`, { method: 'POST', body: new URLSearchParams({ action: 'deleteAttachment', id: id}) });
                    if (response.ok) li.remove();
                    else alert('Fehler beim Löschen des Anhangs.');
                } catch(e) {
                     alert('Netzwerkfehler beim Löschen des Anhangs.');
                }
            });
        };
        li.appendChild(removeBtn); attachmentsList.appendChild(li);
    };
});
</script>