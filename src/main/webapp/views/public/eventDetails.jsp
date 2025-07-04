<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Event Details: ${event.name}" />
</c:import>


<div
	style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; margin-bottom: 0.5rem;">
	<h1>
		<c:out value="${event.name}" />
	</h1>
	<c:if test="${not empty event.status}">
		<c:set var="statusClass"
			value="${event.status == 'LAUFEND' ? 'status-warn' : (event.status == 'ABGESCHLOSSEN' or event.status == 'ABGESAGT') ? 'status-info' : 'status-ok'}" />
		<span class="status-badge ${statusClass}"><c:out
				value="${event.status}" /></span>
	</c:if>
</div>

<p class="details-subtitle">
	<strong>Zeitraum:</strong>
	<c:out value="${event.formattedEventDateTimeRange}" />
	<c:if test="${not empty event.location}">
		<span style="margin-left: 1rem;"><strong>Ort:</strong> <c:out
				value="${event.location}" /></span>
	</c:if>
</p>

<%-- CORRECTED: Replaced the complex contains() check with a simple and robust roleName comparison --%>
<c:if
	test="${event.status == 'LAUFEND' and (isUserAssigned or sessionScope.user.roleName == 'ADMIN')}">
	<div class="dashboard-grid">
		<div class="card">
			<h2 class="card-title">Aufgaben</h2>
			<c:if test="${sessionScope.user.permissions.contains('TASK_MANAGE')}">
				<div id="admin-task-manager">
					<ul id="task-list-admin" class="details-list">
						<c:if test="${empty event.eventTasks}">
							<li>Noch keine Aufgaben erstellt.</li>
						</c:if>
						<c:forEach var="task" items="${event.eventTasks}">
							<li id="task-item-${task.id}">
								<div style="flex-grow: 1;">
									<strong><c:out value="${task.description}" /></strong><br>
									<small>Zugewiesen: <c:out
											value="${not empty task.assignedUsernames ? task.assignedUsernames : 'Niemand'}" /></small>
								</div>
								<div style="display: flex; gap: 0.5rem; flex-shrink: 0;">
									<span
										class="status-badge ${task.status == 'ERLEDIGT' ? 'status-ok' : 'status-warn'}"><c:out
											value="${task.status}" /></span>
									<button class="btn btn-small assign-task-btn"
										data-task-id="${task.id}">Zuweisen</button>
									<button class="btn btn-small btn-danger delete-task-btn"
										data-task-id="${task.id}"
										data-task-desc="${fn:escapeXml(task.description)}">×</button>
								</div>
							</li>
						</c:forEach>
					</ul>
					<form action="${pageContext.request.contextPath}/admin/tasks"
						method="post"
						style="margin-top: 1rem; border-top: 1px solid var(--border-color); padding-top: 1rem;">
						<input type="hidden" name="action" value="create"><input
							type="hidden" name="eventId" value="${event.id}">
						<div class="form-group">
							<label for="task-description">Neue Aufgabe</label> <input
								type="text" name="description" id="task-description" required
								placeholder="z.B. Mischpult aufbauen">
						</div>
						<button type="submit" class="btn btn-small">Aufgabe
							erstellen</button>
					</form>
				</div>
			</c:if>
			<c:if
				test="${!sessionScope.user.permissions.contains('TASK_MANAGE')}">
				<ul id="task-list-user" class="details-list">
					<c:set var="userHasTasks" value="false" />
					<c:forEach var="task" items="${event.eventTasks}">
						<c:if
							test="${fn:contains(task.assignedUsernames, sessionScope.user.username) and task.status == 'OFFEN'}">
							<c:set var="userHasTasks" value="true" />
							<li id="task-item-user-${task.id}"><label
								style="display: flex; align-items: center; gap: 0.5rem; width: 100%; cursor: pointer;">
									<input type="checkbox" class="task-checkbox"
									data-task-id="${task.id}"
									style="width: auto; height: 1.2rem; flex-shrink: 0;"> <span><c:out
											value="${task.description}" /></span>
							</label></li>
						</c:if>
					</c:forEach>
					<c:if test="${!userHasTasks}">
						<li>Keine offenen Aufgaben für dich.</li>
					</c:if>
				</ul>
			</c:if>
		</div>
		<div class="card">
			<h2 class="card-title">Event-Chat</h2>
			<div id="chat-box"
				style="height: 300px; overflow-y: auto; border: 1px solid var(--border-color); padding: 0.5rem; margin-bottom: 1rem; background: var(--bg-color);"></div>
			<form id="chat-form" style="display: flex; gap: 0.5rem;">
				<input type="text" id="chat-message-input" class="form-group"
					style="flex-grow: 1; margin: 0;"
					placeholder="Nachricht eingeben...">
				<button type="submit" class="btn">Senden</button>
			</form>
		</div>
	</div>
</c:if>

<div class="dashboard-grid">
	<div class="card">
		<h2 class="card-title">Beschreibung</h2>
		<p>
			<c:out
				value="${not empty event.description ? event.description : 'Keine Beschreibung für dieses Event vorhanden.'}" />
		</p>
	</div>
	<div class="card">
		<h2 class="card-title">Benötigter Personalbedarf</h2>
		<ul class="details-list">
			<c:if test="${empty event.skillRequirements}">
				<li>Keine speziellen Qualifikationen benötigt.</li>
			</c:if>
			<c:forEach var="req" items="${event.skillRequirements}">
				<li><strong><c:out value="${req.courseName}" />:</strong> <span><c:out
							value="${req.requiredPersons}" /> Person(en)</span></li>
			</c:forEach>
		</ul>
	</div>
	<div class="card">
		<h2 class="card-title">Reserviertes Material</h2>
		<ul class="details-list">
			<c:if test="${empty event.reservedItems}">
				<li>Kein Material für dieses Event reserviert.</li>
			</c:if>
			<c:forEach var="item" items="${event.reservedItems}">
				<li><c:out value="${item.name}" /> <span><c:out
							value="${item.quantity}" />x</span></li>
			</c:forEach>
		</ul>
	</div>
	<div class="card">
		<h2 class="card-title">Anhänge</h2>
		<ul class="details-list">
			<c:if test="${empty event.attachments}">
				<li>Keine Anhänge für dieses Event vorhanden.</li>
			</c:if>
			<c:forEach var="att" items="${event.attachments}">
				<li><a
					href="${pageContext.request.contextPath}/download?file=${att.filepath}"><c:out
							value="${att.filename}" /></a></li>
			</c:forEach>
		</ul>
	</div>
</div>

<div class="card">
	<h2 class="card-title">Zugewiesenes Team</h2>
	<ul class="details-list">
		<c:if test="${empty event.assignedAttendees}">
			<li>Noch kein Team zugewiesen.</li>
		</c:if>
		<c:forEach var="attendee" items="${event.assignedAttendees}">
			<li><c:out value="${attendee.username}" /></li>
		</c:forEach>
	</ul>
</div>

<div style="margin-top: 2rem;">
	<a href="${pageContext.request.contextPath}/veranstaltungen"
		class="btn"><i class="fas fa-arrow-left"></i> Zurück zur
		Event-Übersicht</a>
</div>

<div class="modal-overlay" id="assign-task-modal">
	<div class="modal-content">
		<button class="modal-close-btn" type="button" aria-label="Schließen">×</button>
		<h3>Aufgabe zuweisen</h3>
		<form action="${pageContext.request.contextPath}/admin/tasks"
			method="post">
			<input type="hidden" name="action" value="assign"><input
				type="hidden" name="eventId" value="${event.id}"><input
				type="hidden" name="taskId" id="modal-task-id">
			<div class="form-group">
				<label>Verfügbare Teammitglieder</label>
				<div id="modal-user-checkboxes"
					style="display: flex; flex-direction: column; gap: 0.5rem;">
					<c:forEach var="user" items="${assignedUsers}">
						<label><input type="checkbox" name="userIds"
							value="${user.id}"> <c:out value="${user.username}" /></label>
					</c:forEach>
				</div>
			</div>
			<button type="submit" class="btn">Zuweisung speichern</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script>
	// Provide data for the main eventDetails.js script
	document.body.dataset.eventId = "${event.id}";
	document.body.dataset.userId = "${sessionScope.user.id}";
	// CORRECTED: Use the simpler, more robust roleName check
	document.body.dataset.isAdmin = "${sessionScope.user.roleName == 'ADMIN'}";
</script>
<script
	src="${pageContext.request.contextPath}/js/public/eventDetails.js"></script>