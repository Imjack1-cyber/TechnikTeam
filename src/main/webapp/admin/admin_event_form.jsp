<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
admin_event_form.jsp

This JSP provides the form for an administrator to edit an
existing event. It includes fields for the event's name, date/time, description,
status, and a dynamic section for specifying skill requirements. Creating a
new event is handled by a modal on the admin_events_list.jsp page.

    It is served by: AdminEventServlet (doGet with action=edit).

    It submits to: AdminEventServlet (doPost with action=update).

    Expected attributes:

        'event' (de.technikteam.model.Event): The event to edit.

        'allCourses' (List<de.technikteam.model.Course>): List of all available courses for the skill requirements dropdown.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Event bearbeiten" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Event bearbeiten</h1>
<div class="form-center-wrapper">
	<div class="card">
		<form action="${pageContext.request.contextPath}/admin/events"
			method="post">
			<input type="hidden" name="action" value="update"> <input
				type="hidden" name="id" value="${event.id}">
			<div class="form-group">
				<label for="name">Name des Events</label> <input type="text"
					id="name" name="name" value="${event.name}" required>
			</div>
			<div class="responsive-form-grid">
				<div class="form-group">
					<label for="eventDateTime">Beginn</label> <input
						type="datetime-local" id="eventDateTime" name="eventDateTime"
						value="${event.eventDateTime}" required>
				</div>
				<div class="form-group">
					<label for="endDateTime">Ende (optional)</label> <input
						type="datetime-local" id="endDateTime" name="endDateTime"
						value="${event.endDateTime}">
				</div>
			</div>
			<div class="form-group">
				<label for="description">Beschreibung</label>
				<textarea id="description" name="description" rows="4">${event.description}</textarea>
			</div>
			<div class="form-group">
				<label for="status">Status</label> <select id="status" name="status">
					<option value="GEPLANT"
						${event.status == 'GEPLANT' ? 'selected' : ''}>Geplant</option>
					<option value="KOMPLETT"
						${event.status == 'KOMPLETT' ? 'selected' : ''}>Komplett
						(Team steht)</option>
					<option value="LAUFEND"
						${event.status == 'LAUFEND' ? 'selected' : ''}>Laufend</option>
					<option value="ABGESCHLOSSEN"
						${event.status == 'ABGESCHLOSSEN' ? 'selected' : ''}>Abgeschlossen</option>
				</select>
			</div>

			<!-- Container for skill requirements -->
			<div class="card"
				style="margin-top: 1.5rem; padding: 1rem; background-color: var(--bg-color);">
				<h3 class="card-title" style="border: none; padding: 0;">Benötigte
					Qualifikationen</h3>
				<div id="requirements-container">
					<c:forEach var="req" items="${event.skillRequirements}">
						<div class="requirement-row">
							<select name="requiredCourseId">
								<c:forEach var="course" items="${allCourses}">
									<option value="${course.id}"
										${req.requiredCourseId == course.id ? 'selected' : ''}>${course.name}</option>
								</c:forEach>
							</select> <input type="number" name="requiredPersons"
								value="${req.requiredPersons}" placeholder="Anzahl" min="1"
								style="max-width: 100px;">
							<button type="button" class="btn-small btn-danger"
								onclick="this.parentElement.remove()">X</button>
						</div>
					</c:forEach>
				</div>
				<button type="button" id="add-requirement-btn" class="btn btn-small"
					style="margin-top: 1rem;">Anforderung hinzufügen</button>
			</div>

			<div style="display: flex; gap: 1rem; margin-top: 2rem;">
				<button type="submit" class="btn">Event Speichern</button>
				<a href="${pageContext.request.contextPath}/admin/events"
					class="btn" style="background-color: var(--text-muted-color);">Abbrechen</a>
			</div>
		</form>
	</div>
</div>
<script>
document.getElementById('add-requirement-btn').addEventListener('click', () => {
const container = document.getElementById('requirements-container');
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
</script>
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

<c:import url="/WEB-INF/jspf/footer.jspf" />