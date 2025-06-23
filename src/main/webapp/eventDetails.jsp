<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
  eventDetails.jsp
  
  This JSP displays the detailed view of a single event. It shows general info
  (description, requirements, assigned team) for all users. For events with a
  status of 'LAUFEND', it reveals an interactive section with a task list and
  a real-time chat, available only to assigned team members and admins.
  
  - It is served by: EventDetailsServlet.
  - Expected attributes:
    - 'event' (de.technikteam.model.Event): The event object, populated with all necessary details.
    - 'assignedUsers' (List<User>): For admins, the list of users assigned to the event.
    - 'isUserAssigned' (boolean): For regular users, indicates if they are on the team.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Event Details" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<div class="details-container" data-event-id="${event.id}">

	<div
		style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; margin-bottom: 0.5rem;">
		<h1>
			<c:out value="${event.name}" />
		</h1>
		<c:if test="${not empty event.status}">
			<c:set var="statusClass"
				value="${event.status == 'KOMPLETT' or event.status == 'ZUGEWIESEN' ? 'status-ok' : event.status == 'LAUFEND' ? 'status-warn' : event.status == 'ABGESCHLOSSEN' ? 'status-info' : 'status-info'}" />
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

	<%-- Interactive section for running events, visible only to assigned users and admins --%>
	<c:if
		test="${event.status == 'LAUFEND' and (isUserAssigned or sessionScope.user.role == 'ADMIN')}">
		<div class="responsive-dashboard-grid">
			<div class="card">
				<h2 class="card-title">Aufgaben</h2>
				<%-- Admin view for tasks: manage all tasks --%>
				<c:if test="${sessionScope.user.role == 'ADMIN'}">
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
												value="${not empty task.assignedUsernames ? task.assignedUsernames : 'Niemand'}" />
										</small>
									</div>
									<div style="display: flex; gap: 0.5rem; flex-shrink: 0;">
										<span
											class="status-badge ${task.status == 'ERLEDIGT' ? 'status-ok' : 'status-warn'}"><c:out
												value="${task.status}" /></span>
										<button class="btn btn-small assign-task-btn"
											data-task-id="${task.id}">Zuweisen</button>
										<button class="btn btn-small btn-danger delete-task-btn"
											data-task-id="${task.id}">×</button>
									</div>
								</li>
							</c:forEach>
						</ul>
						<form action="${pageContext.request.contextPath}/admin/tasks"
							method="post"
							style="margin-top: 1rem; border-top: 1px solid var(--border-color); padding-top: 1rem;">
							<input type="hidden" name="action" value="create"> <input
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
				<%-- User view for tasks: see and complete only their own tasks --%>
				<c:if test="${sessionScope.user.role != 'ADMIN'}">
					<ul id="task-list-user" class="details-list">
						<c:if test="${empty event.eventTasks}">
							<li>Keine Aufgaben vorhanden.</li>
						</c:if>
						<c:forEach var="task" items="${event.eventTasks}">
							<c:if
								test="${fn:contains(task.assignedUsernames, sessionScope.user.username) and task.status == 'OFFEN'}">
								<li id="task-item-user-${task.id}"><label
									style="display: flex; align-items: center; gap: 0.5rem; width: 100%; cursor: pointer;">
										<input type="checkbox" class="task-checkbox"
										data-task-id="${task.id}"
										style="width: auto; height: 1.2rem; flex-shrink: 0;">
										<span><c:out value="${task.description}" /></span>
								</label></li>
							</c:if>
						</c:forEach>
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

	<div class="responsive-dashboard-grid">
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
					<li><strong><c:out value="${req.courseName}" />:</strong> <c:out
							value="${req.requiredPersons}" /> Person(en) benötigt</li>
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
				<li><a
					href="${pageContext.request.contextPath}/admin/users?action=details&id=${attendee.id}"><c:out
							value="${attendee.username}" /></a></li>
			</c:forEach>
		</ul>
	</div>
	<div style="margin-top: 2rem;">
		<a href="${pageContext.request.contextPath}/events" class="btn">Zurück
			zur Event-Übersicht</a>
	</div>
</div>

<div class="modal-overlay" id="assign-task-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3>Aufgabe zuweisen</h3>
		<form action="${pageContext.request.contextPath}/admin/tasks"
			method="post">
			<input type="hidden" name="action" value="assign"> <input
				type="hidden" name="eventId" value="${event.id}"> <input
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

<style>
.details-subtitle {
	font-size: 1.1rem;
	color: var(--text-muted-color);
	margin-bottom: 1.5rem;
}
</style>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const mainContainer = document.querySelector('.details-container');
    if (!mainContainer) return;
    
    const contextPath = "${pageContext.request.contextPath}";
    const eventId = mainContainer.dataset.eventId;
    const currentUserId = "${sessionScope.user.id}";
    const isAdmin = "${sessionScope.user.role}" === "ADMIN";

    if (!eventId) {
        console.error("Event ID is missing. Real-time features disabled.");
        return;
    }
    
    // --- Admin-specific JS for task management ---
    if (isAdmin) {
        const assignModal = document.getElementById('assign-task-modal');
        if (assignModal) {
            const modalTaskIdInput = document.getElementById('modal-task-id');
            const modalCloseBtn = assignModal.querySelector('.modal-close-btn');
            
            document.querySelectorAll('.assign-task-btn').forEach(btn => btn.addEventListener('click', () => {
                modalTaskIdInput.value = btn.dataset.taskId;
                assignModal.classList.add('active');
            }));
            
            if(modalCloseBtn) modalCloseBtn.addEventListener('click', () => assignModal.classList.remove('active'));
            assignModal.addEventListener('click', e => { if (e.target === assignModal) assignModal.classList.remove('active'); });
        }
        
        document.querySelectorAll('.delete-task-btn').forEach(btn => btn.addEventListener('click', (e) => {
            e.preventDefault();
            const taskItem = e.target.closest('li');
            const taskDescription = taskItem.querySelector('strong').textContent;
            showConfirmationModal(`Aufgabe "${taskDescription}" wirklich löschen?`, () => {
                 fetch(`${contextPath}/admin/tasks?taskId=${btn.dataset.taskId}`, { method: 'DELETE' })
                    .then(res => res.ok ? taskItem.remove() : alert('Löschen fehlgeschlagen!'));
            });
        }));
    }

    // --- User-specific JS for completing tasks ---
    document.querySelectorAll('.task-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', (e) => {
            const params = new URLSearchParams({ taskId: e.target.dataset.taskId, status: e.target.checked ? 'ERLEDIGT' : 'OFFEN' });
            fetch(`${contextPath}/task-action`, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params })
                .then(res => {
                    if (res.ok) {
                        if (e.target.checked) e.target.closest('li').style.display = 'none';
                    } else { e.target.checked = !e.target.checked; alert('Status konnte nicht aktualisiert werden.'); }
                }).catch(() => { e.target.checked = !e.target.checked; alert('Netzwerkfehler.'); });
        });
    });

    // --- JS for Event Chat (if present) ---
    const chatBox = document.getElementById('chat-box');
    if (chatBox) {
        const chatForm = document.getElementById('chat-form');
        const chatInput = document.getElementById('chat-message-input');
        
        const fetchMessages = () => {
            fetch(`${contextPath}/api/event-chat?eventId=${eventId}`)
                .then(res => res.ok ? res.json() : Promise.reject(`HTTP error! status: ${res.status}`))
                .then(messages => {
                    chatBox.innerHTML = ''; // Clear previous messages
                    if (messages.length > 0) {
                        messages.forEach(msg => {
                            const p = document.createElement('p');
                            p.style.marginBottom = '0.25rem';
                            if (msg.userId == currentUserId) {
                                p.style.textAlign = 'right';
                            }
                            
                            const strong = document.createElement('strong');
                            strong.textContent = msg.username + ': ';
                             if (msg.userId == currentUserId) {
                                strong.style.color = 'var(--primary-color)';
                            }

                            p.appendChild(strong);
                            p.appendChild(document.createTextNode(msg.messageText)); // Securely append text
                            chatBox.appendChild(p);
                        });
                    } else {
                        const p = document.createElement('p');
                        p.textContent = 'Noch keine Nachrichten.';
                        p.style.cssText = 'color:var(--text-muted-color); text-align: center; padding-top: 1rem;';
                        chatBox.appendChild(p);
                    }
                    chatBox.scrollTop = chatBox.scrollHeight;
                }).catch(error => console.error("Error fetching chat messages:", error));
        };
        
        chatForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const message = chatInput.value.trim();
            if (message) {
                const formData = new URLSearchParams({ eventId: eventId, messageText: message });
                fetch(`${contextPath}/api/event-chat`, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: formData })
                    .then(res => { 
                        if (res.ok) { 
                            chatInput.value = ''; 
                            fetchMessages();
                        } else { 
                            alert('Nachricht konnte nicht gesendet werden.'); 
                        } 
                    })
                    .catch(() => alert('Netzwerkfehler beim Senden.'));
            }
        });
        
        setInterval(fetchMessages, 3000);
        fetchMessages();
    }
});
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />