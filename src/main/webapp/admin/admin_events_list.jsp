<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
admin_events_list.jsp

This JSP displays a list of all events for administrators. It provides a comprehensive
set of actions for each event, such as editing, assigning users, changing the
event status, and deleting. Creating and editing events are handled via a modal dialog.

    It is served by: AdminEventServlet (doGet).

    Expected attributes:
        'eventList' (List<de.technikteam.model.Event>): A list of all events.
        'allCourses' (List<de.technikteam.model.Course>): For the create/edit modal.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Eventverwaltung" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Eventverwaltung</h1>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>
<div class="table-controls">
	<button type="button" class="btn" id="new-event-btn">Neues
		Event anlegen</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<c:if test="${empty eventList}">
	<div class="card">
		<p>Es wurden noch keine Events erstellt.</p>
	</div>
</c:if>
<!-- MOBILE LAYOUT: CARD LIST -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="event" items="${eventList}">
		<div class="list-item-card"
			data-searchable-content="${event.name} ${event.status}">
			<h3 class="card-title">${event.name}</h3>
			<div class="card-row">
				<span>Zeitraum:</span> <span>${event.formattedEventDateTimeRange}</span>
			</div>
			<div class="card-row">
				<span>Status:</span> <span>${event.status}</span>
			</div>
			<div class="card-actions">
				<button type="button" class="btn btn-small edit-event-btn"
					data-event-id="${event.id}">Bearbeiten</button>
				<c:if test="${event.status != 'ABGESCHLOSSEN'}">
					<a
						href="${pageContext.request.contextPath}/admin/events?action=assign&id=${event.id}"
						class="btn btn-small btn-success">Zuweisen</a>
					<c:if
						test="${event.status == 'GEPLANT' || event.status == 'KOMPLETT'}">
						<form action="${pageContext.request.contextPath}/admin/events"
							method="post" style="display: inline;">
							<input type="hidden" name="action" value="updateStatus">
							<input type="hidden" name="id" value="${event.id}"> <input
								type="hidden" name="newStatus" value="LAUFEND">
							<button type="submit" class="btn btn-small"
								style="background-color: orange;">Starten</button>
						</form>
					</c:if>
					<c:if test="${event.status == 'LAUFEND'}">
						<form action="${pageContext.request.contextPath}/admin/events"
							method="post" style="display: inline;">
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
					data-confirm-message="Soll das Event '${event.name}' wirklich endgültig gelöscht werden?">
					<input type="hidden" name="action" value="delete"> <input
						type="hidden" name="id" value="${event.id}">
					<button type="submit" class="btn btn-small btn-danger">Löschen</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>
<!-- DESKTOP LAYOUT: TABLE -->
<div class="desktop-table-wrapper">
	<table class="desktop-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name</th>
				<th class="sortable" data-sort-type="string">Zeitraum</th>
				<th class="sortable" data-sort-type="string">Status</th>
				<th style="width: 450px;">Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="event" items="${eventList}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a></td>
					<td>${event.formattedEventDateTimeRange}</td>
					<td>${event.status}</td>
					<td style="display: flex; gap: 5px; flex-wrap: wrap;">
						<button type="button" class="btn btn-small edit-event-btn"
							data-event-id="${event.id}">Bearbeiten</button> <c:if
							test="${event.status != 'ABGESCHLOSSEN'}">
							<a
								href="${pageContext.request.contextPath}/admin/events?action=assign&id=${event.id}"
								class="btn btn-small btn-success">Zuweisen</a>
							<c:if
								test="${event.status == 'GEPLANT' || event.status == 'KOMPLETT'}">
								<form action="${pageContext.request.contextPath}/admin/events"
									method="post" style="display: inline;">
									<input type="hidden" name="action" value="updateStatus">
									<input type="hidden" name="id" value="${event.id}"> <input
										type="hidden" name="newStatus" value="LAUFEND">
									<button type="submit" class="btn btn-small"
										style="background-color: orange;">Starten</button>
								</form>
							</c:if>
							<c:if test="${event.status == 'LAUFEND'}">
								<form action="${pageContext.request.contextPath}/admin/events"
									method="post" style="display: inline;">
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
							data-confirm-message="Soll das Event '${event.name}' wirklich endgültig gelöscht werden?">
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
<!-- MODAL FOR NEW/EDIT EVENT -->
<div class="modal-overlay" id="event-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3 id="event-modal-title">Event</h3>
		<form id="event-modal-form"
			action="${pageContext.request.contextPath}/admin/events"
			method="post">
			<input type="hidden" name="action" id="event-modal-action"> <input
				type="hidden" name="id" id="event-modal-id">

			<div class="form-group">
				<label for="name-modal">Name des Events</label> <input type="text"
					id="name-modal" name="name" required>
			</div>
			<div class="responsive-form-grid">
				<div class="form-group">
					<label for="eventDateTime-modal">Beginn</label> <input
						type="datetime-local" id="eventDateTime-modal"
						name="eventDateTime" required>
				</div>
				<div class="form-group">
					<label for="endDateTime-modal">Ende (optional)</label> <input
						type="datetime-local" id="endDateTime-modal" name="endDateTime">
				</div>
			</div>
			<div class="form-group">
				<label for="description-modal">Beschreibung</label>
				<textarea id="description-modal" name="description" rows="4"></textarea>
			</div>

			<div class="card"
				style="margin-top: 1.5rem; padding: 1rem; background-color: var(--bg-color);">
				<h4 class="card-title" style="border: none; padding: 0;">Benötigte
					Qualifikationen</h4>
				<div id="modal-requirements-container"></div>
				<button type="button" id="modal-add-requirement-btn"
					class="btn btn-small" style="margin-top: 1rem;">Anforderung
					hinzufügen</button>
			</div>

			<div style="display: flex; gap: 1rem; margin-top: 2rem;">
				<button type="submit" class="btn">Event Speichern</button>
			</div>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<style>
.requirement-row {
	display: flex;
	gap: 10px;
	margin-bottom: 10px;
	align-items: center;
}

.requirement-row select {
	flex-grow: 1;
}

.requirement-row input {
	max-width: 100px;
}

.inline-form {
	display: inline;
}
</style>
<script>
document.addEventListener('DOMContentLoaded', () => {
	// Custom confirmation for delete forms
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	// Modal Logic
	const modalOverlay = document.getElementById('event-modal');
	const form = document.getElementById('event-modal-form');
	const modalTitle = document.getElementById('event-modal-title');
	const actionInput = document.getElementById('event-modal-action');
	const idInput = document.getElementById('event-modal-id');
	const reqContainer = document.getElementById('modal-requirements-container');

	const openModal = () => modalOverlay.classList.add('active');
	const closeModal = () => modalOverlay.classList.remove('active');

	modalOverlay.querySelector('.modal-close-btn').addEventListener('click', closeModal);
	modalOverlay.addEventListener('click', e => { if (e.target === modalOverlay) closeModal(); });
	document.addEventListener('keydown', e => { if (e.key === 'Escape' && modalOverlay.classList.contains('active')) closeModal(); });

	// Open "Create" Modal
	document.getElementById('new-event-btn').addEventListener('click', () => {
		form.reset();
		reqContainer.innerHTML = ''; // Clear old requirements
		modalTitle.textContent = "Neues Event anlegen";
		actionInput.value = "create";
		idInput.value = "";
		openModal();
	});

	// Open "Edit" Modal
	document.querySelectorAll('.edit-event-btn').forEach(btn => {
		btn.addEventListener('click', async () => {
			const eventId = btn.dataset.eventId;
			try {
				const response = await fetch(`${'${pageContext.request.contextPath}'}/admin/events?action=getEventData&id=${eventId}`);
				if (!response.ok) throw new Error('Event data could not be fetched.');
				const event = await response.json();

				form.reset();
				reqContainer.innerHTML = '';
				modalTitle.textContent = "Event bearbeiten";
				actionInput.value = "update";
				idInput.value = event.id;
				form.querySelector('#name-modal').value = event.name || '';
				form.querySelector('#eventDateTime-modal').value = event.eventDateTime ? event.eventDateTime.substring(0, 16) : '';
				form.querySelector('#endDateTime-modal').value = event.endDateTime ? event.endDateTime.substring(0, 16) : '';
				form.querySelector('#description-modal').value = event.description || '';
				
				// Populate skill requirements
				if(event.skillRequirements && event.skillRequirements.length > 0) {
					event.skillRequirements.forEach(req => addRequirementRow(req.requiredCourseId, req.requiredPersons));
				}
				
				openModal();

			} catch (error) {
				console.error('Error opening edit modal:', error);
				alert('Fehler beim Laden der Event-Daten.');
			}
		});
	});

	// Skill Requirement Logic
	const addRequirementRow = (courseId = '', personCount = 1) => {
		const newRow = document.createElement('div');
		newRow.className = 'requirement-row';
		
		const select = document.createElement('select');
		select.name = 'requiredCourseId';
		select.className = 'form-group';
		select.style.cssText = 'padding: 0.5rem; margin-bottom: 0;';
		let optionsHtml = '<option value="">-- Lehrgang auswählen --</option>';
		<c:forEach var="course" items="${allCourses}">
			optionsHtml += `<option value="${course.id}">${course.name}</option>`;
		</c:forEach>
		select.innerHTML = optionsHtml;
		select.value = courseId; // Set selected value
		
		const input = document.createElement('input');
		input.type = 'number';
		input.name = 'requiredPersons';
		input.value = personCount;
		input.min = '1';
		input.className = 'form-group';
		input.style.cssText = 'padding: 0.5rem; margin-bottom: 0;';

		const removeBtn = document.createElement('button');
		removeBtn.type = 'button';
		removeBtn.className = 'btn-small btn-danger';
		removeBtn.innerHTML = 'X';
		removeBtn.onclick = () => newRow.remove();

		newRow.appendChild(select);
		newRow.appendChild(input);
		newRow.appendChild(removeBtn);
		reqContainer.appendChild(newRow);
	};

	document.getElementById('modal-add-requirement-btn').addEventListener('click', () => addRequirementRow());
});
</script>