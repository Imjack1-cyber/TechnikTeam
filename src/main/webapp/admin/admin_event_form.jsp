<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Event bearbeiten" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<!-- 
admin_event_form.jsp: The view for creating and editing a single event, including its skill requirements.

    Served by: AdminEventServlet (doGet with action=new|edit).

    Submits to: AdminEventServlet (doPost with action=create|update).

    Dependencies: Includes header.jspf, admin_navigation.jspf, footer.jspf. Contains inline JavaScript to dynamically add more skill requirement fields.
-->

<h1>
	<c:out
		value="${empty event ? 'Neues Event erstellen' : 'Event bearbeiten'}" />
</h1>

<div class="card form-container">
	<form action="${pageContext.request.contextPath}/admin/events"
		method="post">
		<input type="hidden" name="action"
			value="${empty event ? 'create' : 'update'}">
		<c:if test="${not empty event}">
			<input type="hidden" name="id" value="${event.id}">
		</c:if>

		<%-- Event-Stammdaten --%>
		<div class="form-group">
			<label>Name des Events</label><input type="text" name="name"
				value="${event.name}" required>
		</div>
		<div class="form-group">
			<label>Datum &amp; Uhrzeit</label><input type="datetime-local"
				name="eventDateTime" value="${event.eventDateTime}" required>
		</div>
		<div class="form-group">
			<label>Beschreibung</label>
			<textarea name="description" rows="4">${event.description}</textarea>
		</div>
		<c:if test="${not empty event}">
			<div class="form-group">
				<label>Status</label><select name="status"><option
						value="GEPLANT" ${event.status == 'GEPLANT' ? 'selected' : ''}>Geplant</option>
					<option value="KOMPLETT"
						${event.status == 'KOMPLETT' ? 'selected' : ''}>Komplett</option>
					<option value="ABGESCHLOSSEN"
						${event.status == 'ABGESCHLOSSEN' ? 'selected' : ''}>Abgeschlossen</option></select>
			</div>
		</c:if>

		<!-- Container für benötigte Qualifikationen -->
		<div class="card"
			style="margin-top: 1.5rem; padding: 1rem; background-color: var(--secondary-color);">
			<h3 class="card-title" style="color: var(--text-color);">Benötigte
				Qualifikationen</h3>
			<div id="requirements-container">
				<%-- Vorhandene Anforderungen laden --%>
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
			<button type="button" id="add-requirement-btn" class="btn-small"
				style="margin-top: 1rem;">Anforderung hinzufügen</button>
		</div>

		<button type="submit" class="btn" style="margin-top: 2rem;">Event
			Speichern</button>
	</form>
</div>

<script>
document.getElementById('add-requirement-btn').addEventListener('click', () => {
    const container = document.getElementById('requirements-container');
    const newRow = document.createElement('div');
    newRow.className = 'requirement-row';
    // Das HTML enthält jetzt kein Textfeld für "skillName" mehr
    newRow.innerHTML = `
        <select name="requiredCourseId">
            <option value="">-- Lehrgang auswählen --</option>
            <c:forEach var="course" items="${allCourses}">
                <option value="${course.id}">${course.name}</option>
            </c:forEach>
        </select>
        <input type="number" name="requiredPersons" value="1" placeholder="Anzahl" min="1" style="max-width: 100px;">
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