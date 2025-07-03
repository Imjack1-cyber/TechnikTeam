document.addEventListener('DOMContentLoaded', () => {
const contextPath = "${pageContext.request.contextPath}";
const eventId = "${event.id}";
const currentUserId = "${sessionScope.user.id}";
const isAdmin = "${sessionScope.user.role}" === "ADMIN";

if (!eventId) {
	console.error("Event ID is missing from the page model. Real-time features disabled.");
	return;
}

// --- Admin-specific JS for task management ---
if (isAdmin) {
	const assignModal = document.getElementById('assign-task-modal');
	if (assignModal) {
		const modalTaskIdInput = document.getElementById('modal-task-id');
		const modalCloseBtn = assignModal.querySelector('.modal-close-btn');

		document.querySelectorAll('.assign-task-btn').forEach(btn => {
			btn.addEventListener('click', (e) => {
				const taskId = e.currentTarget.dataset.taskId;
				if (taskId) {
					modalTaskIdInput.value = taskId;
					assignModal.classList.add('active');
				}
			});
		});

		if (modalCloseBtn) modalCloseBtn.addEventListener('click', () => assignModal.classList.remove('active'));
		assignModal.addEventListener('click', e => { if (e.target === assignModal) assignModal.classList.remove('active'); });
	}

	document.querySelectorAll('.delete-task-btn').forEach(btn => {
		btn.addEventListener('click', (e) => {
			e.preventDefault();
			const button = e.currentTarget;
			const taskId = button.dataset.taskId;
			const taskDesc = button.dataset.taskDesc;
			const taskItem = document.getElementById(`task-item-${taskId}`);

			if (taskId) {
				showConfirmationModal(`Aufgabe "${taskDesc}" wirklich löschen?`, () => {
					fetch(contextPath + '/admin/tasks?taskId=' + taskId, { method: 'DELETE' })
						.then(res => {
							if (res.ok) {
								taskItem.remove();
							} else {
								alert('Löschen fehlgeschlagen!');
							}
						});
				});
			}
		});
	});
}

// --- User-specific JS for completing tasks ---
document.querySelectorAll('.task-checkbox').forEach(checkbox => {
	checkbox.addEventListener('change', (e) => {
		const taskId = e.target.dataset.taskId;
		const params = new URLSearchParams({ taskId: taskId, status: e.target.checked ? 'ERLEDIGT' : 'OFFEN' });
		fetch(contextPath + '/task-action', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: params })
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
		fetch(contextPath + '/api/event-chat?eventId=' + eventId)
			.then(res => {
				if (!res.ok) { throw new Error(`HTTP error! status: ${res.status}`); }
				return res.json();
			})
			.then(messages => {
				chatBox.innerHTML = '';
				if (messages && messages.length > 0) {
					messages.forEach(msg => {
						const p = document.createElement('p');
						p.style.marginBottom = '0.25rem';
						if (msg.userId == currentUserId) p.style.textAlign = 'right';

						const strong = document.createElement('strong');
						strong.textContent = msg.username + ': ';
						if (msg.userId == currentUserId) strong.style.color = 'var(--primary-color)';

						p.appendChild(strong);
						p.appendChild(document.createTextNode(msg.messageText));
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
			fetch(contextPath + '/api/event-chat', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: formData })
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