<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
admin_events_list.jsp

This JSP displays a list of all events for administrators. It provides a comprehensive
set of actions for each event, such as editing, assigning users, changing the
event status, and deleting. Creating new events is now handled via a modal dialog.

    It is served by: AdminEventServlet (doGet).

    Expected attributes:

        'eventList' (List<de.technikteam.model.Event>): A list of all events.

        'allCourses' (List<de.technikteam.model.Course>): For the "create" modal.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Eventverwaltung" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Eventverwaltung</h1>

<c:if test="

        
notemptysessionScope.successMessage">
	<pclass="success−message">notemptysessionScope.successMessage"><pclass="success−message">



	{sessionScope.successMessage}
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="

        
notemptysessionScope.errorMessage">
	<pclass="error−message">notemptysessionScope.errorMessage"><pclass="error−message">



	{sessionScope.errorMessage}
	</p>
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
				<a
					href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}"
					class="btn btn-small">Bearbeiten</a>
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
					method="post" style="display: inline;">
					<input type="hidden" name="action" value="delete"> <input
						type="hidden" name="id" value="${event.id}">
					<button type="submit" class="btn btn-small btn-danger"
						onclick="return confirm('Soll das Event \'${event.name}\' wirklich endgültig gelöscht werden?')">Löschen</button>
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
					<td style="display: flex; gap: 5px; flex-wrap: wrap;"><a
						href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}"
						class="btn btn-small">Bearbeiten</a> <c:if
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
							method="post" style="display: inline;">
							<input type="hidden" name="action" value="delete"> <input
								type="hidden" name="id" value="${event.id}">
							<button type="submit" class="btn btn-small btn-danger"
								onclick="return confirm('Soll das Event \'${event.name}\' wirklich endgültig gelöscht werden?')">Löschen</button>
						</form></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>
<!-- MODAL FOR NEW EVENT -->
<div class="modal-overlay" id="new-event-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3>Neues Event erstellen</h3>
		<form action="${pageContext.request.contextPath}/admin/events"
			method="post">
			<input type="hidden" name="action" value="create">
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
				<h3 class="card-title" style="border: none; padding: 0;">Benötigte
					Qualifikationen</h3>
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
</style>
<script>
document.addEventListener('DOMContentLoaded', () => {
// Modal Logic
const openModalBtn = document.getElementById('new-event-btn');
const modalOverlay = document.getElementById('new-event-modal');
if (!openModalBtn || !modalOverlay) return;

const closeModalBtn = modalOverlay.querySelector('.modal-close-btn');

const openModal = () => modalOverlay.classList.add('active');
const closeModal = () => modalOverlay.classList.remove('active');

openModalBtn.addEventListener('click', openModal);
closeModalBtn.addEventListener('click', closeModal);
modalOverlay.addEventListener('click', e => { if (e.target === modalOverlay) closeModal(); });
document.addEventListener('keydown', e => { if (e.key === 'Escape' && modalOverlay.classList.contains('active')) closeModal(); });

// Skill Requirement Logic for Modal
document.getElementById('modal-add-requirement-btn').addEventListener('click', () => {
const container = document.getElementById('modal-requirements-container');
const newRow = document.createElement('div');
newRow.className = 'requirement-row';
newRow.innerHTML = `
<select name="requiredCourseId" class="form-group" style="padding: 0.5rem; margin-bottom: 0;">
<option value="">-- Lehrgang auswählen --</option>
<c:forEach var="course" items="${allCourses}">
<option value="${course.id}">${course.name}</option>
</c:forEach>
</select>
<input type="number" name="requiredPersons" value="1" placeholder="Anzahl" min="1" class="form-group" style="padding: 0.5rem; margin-bottom: 0; max-width: 100px;">
<button type="button" class="btn-small btn-danger" onclick="this.parentElement.remove()">X</button>
`;
container.appendChild(newRow);
});
});
</script>