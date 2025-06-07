<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Event bearbeiten" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>
	<c:out
		value="${empty event ? 'Neues Event erstellen' : 'Event bearbeiten'}" />
</h1>
<form action="${pageContext.request.contextPath}/admin/events"
	method="post" class="form-container">
	<c:if test="${not empty event}">
		<input type="hidden" name="id" value="${event.id}">
	</c:if>
	<div class="form-group">
		<label for="name">Name</label> <input type="text" name="name"
			value="${event.name}" required>
	</div>
	<div class="form-group">
		<label for="eventDateTime">Datum/Zeit</label> <input
			type="datetime-local" name="eventDateTime"
			value="${event.eventDateTime}" required>
	</div>
	<%-- Fügen Sie diesen Block am Ende des Formulars hinzu, vor dem Speichern-Button --%>
	<div class="card" style="margin-top: 2rem;">
		<h2 class="card-title">Benötigte Qualifikationen</h2>
		<div id="requirements-container">
			<%-- Vorhandene Anforderungen laden --%>
			<c:forEach var="req" items="${event.skillRequirements}">
				<div class="requirement-row">
					<select name="requiredCourseId"><c:forEach var="course"
							items="${allCourses}">
							<option value="${course.id}"
								${req.requiredCourseId == course.id ? 'selected' : ''}>${course.name}</option>
						</c:forEach></select> <input type="number" name="requiredPersons"
						value="${req.requiredPersons}" placeholder="Anzahl" min="1">
					<input type="text" name="skillName" value="${req.skillName}"
						placeholder="Rollenbezeichnung (z.B. Ton)">
					<button type="button" class="btn-small btn-danger"
						onclick="this.parentElement.remove()">Entfernen</button>
				</div>
			</c:forEach>
		</div>
		<button type="button" id="add-requirement-btn" class="btn-small"
			style="margin-top: 1rem;">Anforderung hinzufügen</button>
	</div>

	<script>
document.getElementById('add-requirement-btn').addEventListener('click', () => {
    const container = document.getElementById('requirements-container');
    const newRow = document.createElement('div');
    newRow.className = 'requirement-row';
    newRow.innerHTML = `
        <select name="requiredCourseId">
            <option value="">-- Lehrgang auswählen --</option>
            <c:forEach var="course" items="${allCourses}">
                <option value="${course.id}">${course.name}</option>
            </c:forEach>
        </select>
        <input type="number" name="requiredPersons" value="1" placeholder="Anzahl" min="1">
        <input type="text" name="skillName" placeholder="Rollenbezeichnung (z.B. Ton)">
        <button type="button" class="btn-small btn-danger" onclick="this.parentElement.remove()">Entfernen</button>
    `;
    container.appendChild(newRow);
});
</script>
	<style>
.requirement-row {
	display: flex;
	gap: 10px;
	margin-bottom: 10px;
}
</style>
	<button type="submit" class="btn">Speichern</button>
</form>
<c:import url="/WEB-INF/jspf/footer.jspf" />