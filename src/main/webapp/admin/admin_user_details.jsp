<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Benutzerdetails" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Benutzerdetails: ${userToEdit.username}</h1>

<!-- User Data Form -->
<div class="card">
	<h2 class="card-title">Stammdaten</h2>
	<form action="${pageContext.request.contextPath}/admin/users"
		method="post" class="user-form">
		<input type="hidden" name="action" value="update"> <input
			type="hidden" name="userId" value="${userToEdit.id}">
		<div class="form-group">
			<label>Benutzername</label><input type="text" name="username"
				value="${userToEdit.username}" required>
		</div>
		<div class="form-group">
			<label>Rolle</label><select name="role"><option
					value="NUTZER" ${userToEdit.role == 'NUTZER' ? 'selected' : ''}>Nutzer</option>
				<option value="ADMIN"
					${userToEdit.role == 'ADMIN' ? 'selected' : ''}>Admin</option></select>
		</div>
		<div class="form-group">
			<label>Registriert seit</label><input type="text"
				value="<java-time:format value="${userToEdit.createdAt}" pattern="dd.MM.yyyy HH:mm"/>"
				readonly>
		</div>
		<div class="form-group">
			<label>Jahrgang</label>
			<input type="number" name="classYear" value="${userToEdit.classYear}">
		</div>
		<div class="form-group">
			<label>Klasse</label><input type="text" name="className"
				value="${userToEdit.className}">
		</div>
		<button type="submit" class="btn">Stammdaten speichern</button>
	</form>
</div>

<!-- Qualifications Management Table -->
<div class="card" style="margin-top: 2rem;">
	<h2 class="card-title">Lehrg&auml;nge &amp; Qualifikationen verwalten</h2>
	<c:if test="${empty qualifications}">
		<p>Dieser Benutzer hat noch keine Lehrgänge besucht.</p>
	</c:if>
	<table class="styled-table"
		<c:if test="${empty qualifications}">style="display:none;"</c:if>>
		<thead>
			<tr>
				<th>Lehrgang</th>
				<th>Status</th>
				<th>Absolviert am</th>
				<th>Aktion</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="q" items="${qualifications}">
				<form action="${pageContext.request.contextPath}/admin/users"
					method="post">
					<input type="hidden" name="action" value="updateQualification">
					<input type="hidden" name="userId" value="${userToEdit.id}">
					<input type="hidden" name="courseId" value="${q.courseId}">
					<tr>
						<td>${q.courseName}</td>
						<td><select name="status"><option value="BESUCHT"
									${q.status == 'BESUCHT' ? 'selected' : ''}>Besucht</option>
								<option value="ABSOLVIERT"
									${q.status == 'ABSOLVIERT' ? 'selected' : ''}>Absolviert</option></select></td>
						<td><input type="date" name="completionDate"
							value="${q.completionDate}"></td>
						<td><button type="submit" class="btn-small">Update</button></td>
					</tr>
				</form>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Event History Table -->
<div class="card" style="margin-top: 2rem;">
	<h2 class="card-title">Event-Teilnahmehistorie</h2>
	<c:if test="${empty eventHistory}">
		<p>Dieser Benutzer hat noch an keinen Events teilgenommen.</p>
	</c:if>
	<table class="styled-table"
		<c:if test="${empty eventHistory}">style="display:none;"</c:if>>
		<thead>
			<tr>
				<th>Event</th>
				<th>Datum</th>
				<th>Teilnahmestatus</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="event" items="${eventHistory}">
				<tr>
					<td>${event.name}</td>
					<td><java-time:format value="${event.eventDateTime}"
							pattern="dd.MM.yyyy" /></td>
					<td>${event.userAttendanceStatus}</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />