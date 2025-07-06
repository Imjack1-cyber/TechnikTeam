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

<div class="responsive-dashboard-grid">
	<div class="card" style="grid-column: 1/-1;">
		<h2 class="card-title">Aufgaben</h2>
		<div id="task-list-container">
			<c:if test="${empty event.eventTasks}">
				<p>Für dieses Event wurden noch keine Aufgaben erstellt.</p>
			</c:if>
			<c:forEach var="task" items="${event.eventTasks}">
				<div class="card" style="margin-bottom: 1rem;">
					<div
						style="display: flex; justify-content: space-between; align-items: start;">
						<div>
							<span
								class="status-badge ${task.status == 'ERLEDIGT' ? 'status-ok' : 'status-warn'}">${task.status}</span>
							<h4 style="margin-top: 0.5rem;">${task.displayOrder}.
								${task.description}</h4>
						</div>
						<c:if test="${hasTaskManagementPermission}">
							<div>
								<button class="btn btn-small edit-task-btn"
									data-task-id="${task.id}">Bearbeiten</button>
							</div>
						</c:if>
					</div>

					<p style="margin-top: 1rem;">
						<strong>Zugewiesen an:</strong>
						<c:if test="${task.requiredPersons > 0}">
							<span class="text-muted">Offener Pool
								(${fn:length(task.assignedUsers)} / ${task.requiredPersons}
								Plätze)</span>
						</c:if>
						<c:out value="${task.getAssignedUsernames()}" />
					</p>

					<c:if
						test="${not empty task.requiredItems || not empty task.requiredKits}">
						<p style="margin-top: 1rem;">
							<strong>Benötigtes Material:</strong>
						</p>
						<ul style="padding-left: 1.5rem;">
							<c:forEach var="item" items="${task.requiredItems}">
								<li>${item.quantity}x${item.name}</li>
							</c:forEach>
							<c:forEach var="kit" items="${task.requiredKits}">
								<li>1x Kit: ${kit.name}</li>
							</c:forEach>
						</ul>
					</c:if>

					<c:if test="${event.status == 'LAUFEND'}">
						<div
							style="margin-top: 1.5rem; border-top: 1px solid var(--border-color); padding-top: 1rem;">
							<c:set var="isTaskAssignedToCurrentUser" value="false" />
							<c:forEach var="assigned" items="${task.assignedUsers}">
								<c:if test="${assigned.id == sessionScope.user.id}">
									<c:set var="isTaskAssignedToCurrentUser" value="true" />
								</c:if>
							</c:forEach>

							<c:if test="${task.requiredPersons > 0}">
								<c:choose>
									<c:when test="${isTaskAssignedToCurrentUser}">
										<form action="${pageContext.request.contextPath}/task-action"
											method="post">
											<input type="hidden" name="action" value="unclaim"> <input
												type="hidden" name="taskId" value="${task.id}">
											<button type="submit"
												class="btn btn-danger-outline btn-small">Aufgabe
												zurückgeben</button>
										</form>
									</c:when>
									<c:when
										test="${fn:length(task.assignedUsers) < task.requiredPersons}">
										<form action="${pageContext.request.contextPath}/task-action"
											method="post">
											<input type="hidden" name="action" value="claim"> <input
												type="hidden" name="taskId" value="${task.id}">
											<button type="submit" class="btn btn-success btn-small">Aufgabe
												übernehmen</button>
										</form>
									</c:when>
								</c:choose>
							</c:if>

							<c:if
								test="${isTaskAssignedToCurrentUser and task.status == 'OFFEN'}">
								<button class="btn btn-primary btn-small mark-task-done-btn"
									data-task-id="${task.id}">Als erledigt markieren</button>
							</c:if>
						</div>
					</c:if>
				</div>
			</c:forEach>
		</div>
		<c:if test="${hasTaskManagementPermission}">
			<button class="btn btn-success" id="new-task-btn"
				style="margin-top: 1rem;">
				<i class="fas fa-plus"></i> Neue Aufgabe
			</button>
		</c:if>
	</div>

	<c:if
		test="${event.status == 'LAUFEND' and (isUserAssigned or hasTaskManagementPermission)}">
		<div class="card" style="grid-column: 1/-1;">
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
	</c:if>
</div>

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

<c:if test="${hasTaskManagementPermission}">
	<jsp:include page="/WEB-INF/jspf/task_modal.jspf" />
</c:if>

<%-- CORRECTED: Embed pre-serialized JSON from the servlet for safety --%>
<script id="allUsersData" type="application/json">${assignedUsersJson}</script>
<script id="allItemsData" type="application/json">${allItemsJson}</script>
<script id="allKitsData" type="application/json">${allKitsJson}</script>
<script id="allTasksData" type="application/json">${tasksJson}</script>


<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script>
	document.body.dataset.eventId = "${event.id}";
	document.body.dataset.userId = "${sessionScope.user.id}";
	document.body.dataset.isAdmin = "${sessionScope.user.roleName == 'ADMIN'}";
</script>
<script
	src="${pageContext.request.contextPath}/js/public/eventDetails.js"></script>