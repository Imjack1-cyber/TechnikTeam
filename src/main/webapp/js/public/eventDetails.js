// src/main/webapp/js/public/eventDetails.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const eventId = document.body.dataset.eventId || '';
	const currentUserId = document.body.dataset.userId || '';
	const isAdminOrLeader = document.body.dataset.isAdmin === 'true';

	const container = document.getElementById('event-details-container');
	if (!container) return;

	// --- API Abstraction ---
	const api = {
		getEvent: (id) => fetch(`${contextPath}/api/v1/public/events/${id}`).then(res => {
			if (!res.ok) { throw new Error(`HTTP error! status: ${res.status}`); }
			return res.json();
		})
	};

	// --- RENDER FUNCTIONS ---
	const escape = (str) => {
		if (str === null || typeof str === 'undefined') return '';
		const div = document.createElement('div');
		div.innerText = str;
		return div.innerHTML;
	};

	const renderHeader = (event) => {
		const statusClass = event.status === 'LAUFEND' ? 'status-warn' : (event.status === 'ABGESCHLOSSEN' || event.status === 'ABGESAGT') ? 'status-info' : 'status-ok';
		return `
            <div style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; margin-bottom: 0.5rem;">
                <h1>${escape(event.name)}</h1>
                <span class="status-badge ${statusClass}">${escape(event.status)}</span>
            </div>
            <p class="details-subtitle">
                <strong>Zeitraum:</strong> ${escape(event.formattedEventDateTimeRange)}
                ${event.location ? `<span style="margin-left: 1rem;"><strong>Ort:</strong> ${escape(event.location)}</span>` : ''}
            </p>`;
	};

	const renderTasks = (event) => {
		let tasksHtml = '';
		if (!event.eventTasks || event.eventTasks.length === 0) {
			tasksHtml = '<p>Für dieses Event wurden noch keine Aufgaben erstellt.</p>';
		} else {
			tasksHtml = event.eventTasks.map(task => {
				const assignedUsers = (task.assignedUsers || []).map(u => escape(u.username)).join(', ') || 'Niemand';
				const requiredMaterials = (task.requiredItems || []).map(item => `<li><a href="${contextPath}/lager/details?id=${item.id}">${escape(item.quantity)}x ${escape(item.name)}</a></li>`).join('');
				const requiredKits = (task.requiredKits || []).map(kit => `<li><a href="${contextPath}/pack-kit?kitId=${kit.id}">1x Kit: ${escape(kit.name)}</a></li>`).join('');
				const isTaskAssignedToCurrentUser = (task.assignedUsers || []).some(u => u.id == currentUserId);

				let actionButtons = '';
				if (event.status === 'LAUFEND') {
					if (task.requiredPersons > 0) {
						if (isTaskAssignedToCurrentUser) {
							actionButtons += `<form action="${contextPath}/task-action" method="post"><input type="hidden" name="csrfToken" value="${document.body.dataset.csrfToken}"><input type="hidden" name="action" value="unclaim"><input type="hidden" name="taskId" value="${task.id}"><button type="submit" class="btn btn-danger-outline btn-small">Aufgabe zurückgeben</button></form>`;
						} else if ((task.assignedUsers || []).length < task.requiredPersons) {
							actionButtons += `<form action="${contextPath}/task-action" method="post"><input type="hidden" name="csrfToken" value="${document.body.dataset.csrfToken}"><input type="hidden" name="action" value="claim"><input type="hidden" name="taskId" value="${task.id}"><button type="submit" class="btn btn-success btn-small">Aufgabe übernehmen</button></form>`;
						}
					}
					if (isTaskAssignedToCurrentUser && task.status === 'OFFEN') {
						actionButtons += `<button class="btn btn-primary btn-small mark-task-done-btn" data-task-id="${task.id}">Als erledigt markieren</button>`;
					}
				}

				return `
                <div class="card" style="margin-bottom: 1rem;">
					<div style="display: flex; justify-content: space-between; align-items: start;">
						<div>
							<span class="status-badge ${task.status === 'ERLEDIGT' ? 'status-ok' : 'status-warn'}">${escape(task.status)}</span>
							<h4 style="margin-top: 0.5rem;">${escape(task.displayOrder)}. ${escape(task.description)}</h4>
						</div>
						${isAdminOrLeader ? `<div><button class="btn btn-small edit-task-btn" data-task-id="${task.id}">Bearbeiten</button></div>` : ''}
					</div>
					<div class="markdown-content" style="margin-top: 1rem;">${marked.parse(task.details || '', { sanitize: true })}</div>
					<p style="margin-top: 1rem;"><strong>Zugewiesen an:</strong> ${task.requiredPersons > 0 ? `<span class="text-muted">Offener Pool (${(task.assignedUsers || []).length} / ${task.requiredPersons} Plätze)</span>` : ''} ${assignedUsers}</p>
					${(requiredMaterials || requiredKits) ? `<p style="margin-top: 1rem;"><strong>Benötigtes Material:</strong></p><ul style="padding-left: 1.5rem;">${requiredMaterials}${requiredKits}</ul>` : ''}
                    ${actionButtons ? `<div style="margin-top: 1.5rem; border-top: 1px solid var(--border-color); padding-top: 1rem; display: flex; gap: 0.5rem;">${actionButtons}</div>` : ''}
				</div>`;
			}).join('');
		}
		return `
            <div class="card" style="grid-column: 1/-1;">
                <h2 class="card-title">Aufgaben</h2>
                <div id="task-list-container">${tasksHtml}</div>
                ${isAdminOrLeader ? `<button class="btn btn-success" id="new-task-btn" style="margin-top: 1rem;"><i class="fas fa-plus"></i> Neue Aufgabe</button>` : ''}
            </div>`;
	};

	const renderChat = (event) => {
		if (event.status !== 'LAUFEND') {
			return `<div class="card" style="grid-column: 1/-1;"><h2 class="card-title">Event-Chat</h2><p class="info-message">Der Chat ist nur aktiv, während das Event läuft.</p></div>`;
		}
		return `
            <div class="card" style="grid-column: 1/-1;">
                <h2 class="card-title">Event-Chat</h2>
                <div id="chat-box" style="height: 300px; overflow-y: auto; border: 1px solid var(--border-color); padding: 0.5rem; margin-bottom: 1rem; background: var(--bg-color);"></div>
                <div style="position: relative;">
                    <form id="chat-form" style="display: flex; gap: 0.5rem;">
                        <input type="text" id="chat-message-input" class="form-group" style="flex-grow: 1; margin: 0;" placeholder="Nachricht eingeben... @ für Erwähnungen" autocomplete="off">
                        <button type="submit" class="btn">Senden</button>
                    </form>
                    <div id="mention-popup" style="display: none; position: absolute; bottom: 100%; left: 0; background: var(--surface-color); border: 1px solid var(--border-color); border-radius: 6px; box-shadow: var(--shadow-md); max-height: 150px; overflow-y: auto; z-index: 10;">
                    </div>
                </div>
            </div>`;
	};

	const renderDescription = (event) => `<div class="card"><h2 class="card-title">Beschreibung</h2><div class="markdown-content">${marked.parse(event.description || 'Keine Beschreibung für dieses Event vorhanden.', { sanitize: true })}</div></div>`;
	const renderRequirements = (event) => `<div class="card"><h2 class="card-title">Benötigter Personalbedarf</h2><ul class="details-list">${!event.skillRequirements || event.skillRequirements.length === 0 ? '<li>Keine speziellen Qualifikationen benötigt.</li>' : event.skillRequirements.map(req => `<li><strong>${escape(req.courseName)}:</strong> <span>${escape(req.requiredPersons)} Person(en)</span></li>`).join('')}</ul></div>`;
	const renderMaterials = (event) => `<div class="card"><h2 class="card-title">Reserviertes Material</h2><ul class="details-list">${!event.reservedItems || event.reservedItems.length === 0 ? '<li>Kein Material für dieses Event reserviert.</li>' : event.reservedItems.map(item => `<li>${escape(item.name)} <span>${escape(item.quantity)}x</span></li>`).join('')}</ul></div>`;
	const renderAttachments = (event) => `<div class="card"><h2 class="card-title">Anhänge</h2><ul class="details-list">${!event.attachments || event.attachments.length === 0 ? '<li>Keine Anhänge für dieses Event vorhanden.</li>' : event.attachments.map(att => `<li><a href="${contextPath}/download?id=${att.id}">${escape(att.filename)}</a></li>`).join('')}</ul></div>`;
	const renderTeam = (event) => `<div class="card"><h2 class="card-title">Zugewiesenes Team</h2><ul class="details-list">${!event.assignedAttendees || event.assignedAttendees.length === 0 ? '<li>Noch kein Team zugewiesen.</li>' : event.assignedAttendees.map(attendee => `<li>${escape(attendee.username)}</li>`).join('')}</ul></div>`;

	const loadEventDetails = async () => {
		try {
			const result = await api.getEvent(eventId);
			if (!result.success) {
				if (result.message.includes("Access Denied") || result.message.includes("Authentication required")) {
					container.innerHTML = `<h1 class="error-message">Zugriff verweigert</h1><p>Sie sind nicht berechtigt, die Details für dieses Event einzusehen.</p><a href="${contextPath}/veranstaltungen" class="btn">Zurück zur Übersicht</a>`;
				} else {
					throw new Error(result.message);
				}
				return;
			}

			const event = result.data;
			document.title = `Event Details: ${event.name}`;

			container.querySelector('#event-header-placeholder').innerHTML = renderHeader(event);

			let contentHtml = renderTasks(event);
			contentHtml += renderChat(event);
			contentHtml += renderDescription(event);
			contentHtml += renderRequirements(event);
			contentHtml += renderMaterials(event);
			contentHtml += renderAttachments(event);
			contentHtml += renderTeam(event);

			container.querySelector('#event-content-placeholder').innerHTML = contentHtml;

			if (event.status === 'LAUFEND') {
				initializeWebSocket(event.chatMessages, event.assignedAttendees);
			}
			// Re-initialize markdown rendering for content loaded via API
			document.querySelectorAll('.markdown-content').forEach(el => window.renderMarkdown(el));

		} catch (error) {
			console.error("Failed to load event details:", error);
			container.innerHTML = `<div class="error-message">Event-Details konnten nicht geladen werden: ${error.message}</div>`;
		}
	};

	const initializeWebSocket = (initialMessages, assignedUsers) => {
		const chatBox = document.getElementById('chat-box');
		const chatForm = document.getElementById('chat-form');
		const chatInput = document.getElementById('chat-message-input');
		const mentionPopup = document.getElementById('mention-popup');
		if (!chatBox || !chatForm || !chatInput) return;

		chatBox.innerHTML = '';
		if (initialMessages && initialMessages.length > 0) {
			initialMessages.forEach(msg => appendMessage(msg));
		}

		const websocketProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
		const websocketUrl = `${websocketProtocol}//${window.location.host}${contextPath}/ws/chat/${eventId}`;
		const socket = new WebSocket(websocketUrl);

		socket.onmessage = (event) => {
			const data = JSON.parse(event.data);
			switch (data.type) {
				case 'new_message':
					appendMessage(data.payload);
					break;
				case 'message_soft_deleted':
					handleSoftDelete(data.payload);
					break;
				case 'message_updated':
					const msgTextEl = document.getElementById(`message-text-${data.payload.messageId}`);
					const editedMarkerEl = document.getElementById(`message-edited-marker-${data.payload.messageId}`);
					if (msgTextEl) msgTextEl.innerHTML = marked.parse(data.payload.newText, { sanitize: true });
					if (editedMarkerEl) editedMarkerEl.style.display = 'inline';
					break;
			}
		};

		chatForm.addEventListener('submit', (e) => {
			e.preventDefault();
			const messageText = chatInput.value.trim();
			if (messageText && socket && socket.readyState === WebSocket.OPEN) {
				socket.send(JSON.stringify({ type: "new_message", payload: { messageText } }));
				chatInput.value = '';
			}
		});

		chatInput.addEventListener('keyup', (e) => {
			if (e.key === '@') {
				if (assignedUsers.length > 0) {
					mentionPopup.innerHTML = assignedUsers.map(u => `<div class="mention-item" data-username="${escape(u.username)}">${escape(u.username)}</div>`).join('');
					mentionPopup.style.display = 'block';
				}
			} else if (mentionPopup.style.display === 'block' && e.key !== 'Shift') {
				mentionPopup.style.display = 'none';
			}
		});

		mentionPopup.addEventListener('click', (e) => {
			if (e.target.classList.contains('mention-item')) {
				const username = e.target.dataset.username;
				const text = chatInput.value;
				const atIndex = text.lastIndexOf('@');
				chatInput.value = text.substring(0, atIndex + 1) + username + ' ';
				mentionPopup.style.display = 'none';
				chatInput.focus();
			}
		});
	};

	const appendMessage = (message) => {
		const isCurrentUser = String(message.userId) === String(currentUserId);
		const chatBox = document.getElementById('chat-box');
		const container = document.createElement('div');
		container.className = 'chat-message-container';
		container.id = `message-container-${message.id}`;
		if (isCurrentUser) container.classList.add('current-user');

		const bubble = document.createElement('div');
		bubble.className = 'chat-bubble';
		bubble.id = `chat-bubble-${message.id}`;
		bubble.style.backgroundColor = isCurrentUser ? 'var(--primary-color)' : (message.chatColor || '#E9ECEF');

		if (message.isDeleted) {
			bubble.classList.add('deleted');
			bubble.innerHTML = `<span class="chat-deleted-info">Nachricht von ${escape(message.username)} wurde von ${escape(message.deletedByUsername)} gelöscht</span>`;
		} else {
			const time = new Date(message.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' });
			const editedMarker = `<span class="chat-edited-marker" id="message-edited-marker-${message.id}" style="display: ${message.edited ? 'inline' : 'none'};"> (bearbeitet)</span>`;

			bubble.innerHTML = `
                ${!isCurrentUser ? `<strong class="chat-username">${escape(message.username)}</strong>` : ''}
                <span class="chat-text" id="message-text-${message.id}">${marked.parse(message.messageText, { sanitize: true })}</span>
                <span class="chat-timestamp">${time}${editedMarker}</span>`;

			const optionsMenu = document.createElement('div');
			optionsMenu.className = 'chat-options';
			if (isAdminOrLeader || isCurrentUser) {
				optionsMenu.innerHTML = `<button class="chat-option-btn" title="Löschen"><i class="fas fa-trash-alt"></i></button>`;
			}
			if (isCurrentUser) {
				optionsMenu.innerHTML = `<button class="chat-option-btn" title="Bearbeiten"><i class="fas fa-pencil-alt"></i></button>` + optionsMenu.innerHTML;
			}
			container.appendChild(optionsMenu);
		}

		container.prepend(bubble);
		chatBox.appendChild(container);
		chatBox.scrollTop = chatBox.scrollHeight;
	};

	const handleSoftDelete = (payload) => {
		const bubble = document.getElementById(`chat-bubble-${payload.messageId}`);
		const container = document.getElementById(`message-container-${payload.messageId}`);
		if (bubble && container) {
			container.querySelector('.chat-options')?.remove();
			bubble.classList.add('deleted');
			bubble.innerHTML = `<span class="chat-deleted-info">Nachricht von ${escape(payload.originalUsername)} wurde von ${escape(payload.deletedByUsername)} gelöscht</span>`;
		}
	};

	// --- INITIAL LOAD ---
	loadEventDetails();
});