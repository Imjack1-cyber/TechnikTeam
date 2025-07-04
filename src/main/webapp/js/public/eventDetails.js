document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const eventId = document.body.dataset.eventId || '';
	const currentUserId = document.body.dataset.userId || '';
	const isAdmin = document.body.dataset.isAdmin === 'true';

	if (!eventId) {
		console.error("Event ID is missing from the page. Real-time features disabled.");
		return;
	}

	const chatBox = document.getElementById('chat-box');
	if (chatBox) {
		const chatForm = document.getElementById('chat-form');
		const chatInput = document.getElementById('chat-message-input');
		const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
		const wsUrl = `${wsProtocol}//${window.location.host}${contextPath}/ws/chat/${eventId}`;
		let socket;

		const connect = () => {
			socket = new WebSocket(wsUrl);
			socket.onopen = () => fetchMessages();
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
						const messageElement = document.getElementById(`message-text-${data.payload.messageId}`);
						const editedMarkerElement = document.getElementById(`message-edited-marker-${data.payload.messageId}`);
						if (messageElement) messageElement.textContent = data.payload.newText;
						if (editedMarkerElement) editedMarkerElement.style.display = 'inline';
						break;
				}
			};
			socket.onclose = (event) => console.warn('WebSocket connection closed.', event);
			socket.onerror = (error) => console.error('WebSocket error:', error);
		};

		const appendMessage = (message) => {
			const isCurrentUser = String(message.userId) === String(currentUserId);

			const container = document.createElement('div');
			container.className = 'chat-message-container';
			container.id = `message-container-${message.id}`;
			if (isCurrentUser) container.classList.add('current-user');
			if (message.isDeleted) container.classList.add('deleted-message');

			const bubble = document.createElement('div');
			bubble.className = 'chat-bubble';

			const textElement = document.createElement('span');
			textElement.className = 'chat-text';
			textElement.id = `message-text-${message.id}`;

			if (message.isDeleted) {
				textElement.innerHTML = `<i>Nachricht von <b>${message.username}</b> gelöscht von <b>${message.deletedByUsername}</b> um ${new Date(message.deletedAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })} Uhr</i>`;
			} else {
				textElement.textContent = message.messageText;

				if (!isCurrentUser) {
					const usernameElement = document.createElement('strong');
					usernameElement.className = 'chat-username';
					usernameElement.textContent = message.username;
					bubble.appendChild(usernameElement);
				}

				const timeElement = document.createElement('span');
				timeElement.className = 'chat-timestamp';
				timeElement.textContent = message.formattedSentAt || new Date(message.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' });

				const editedMarker = document.createElement('span');
				editedMarker.className = 'chat-edited-marker';
				editedMarker.id = `message-edited-marker-${message.id}`;
				editedMarker.textContent = ' (bearbeitet)';
				editedMarker.style.display = message.edited ? 'inline' : 'none';

				timeElement.prepend(editedMarker);
				bubble.appendChild(textElement);
				bubble.appendChild(timeElement);
			}

			if (message.isDeleted) {
				bubble.appendChild(textElement);
			}

			container.appendChild(bubble);

			if (!message.isDeleted && (isAdmin || isCurrentUser)) {
				const optionsMenu = document.createElement('div');
				optionsMenu.className = 'chat-options';

				if (isCurrentUser) {
					const editButton = document.createElement('button');
					editButton.className = 'chat-option-btn';
					editButton.innerHTML = '<i class="fas fa-pencil-alt"></i>';
					editButton.onclick = () => handleEdit(message.id);
					optionsMenu.appendChild(editButton);
				}

				const deleteButton = document.createElement('button');
				deleteButton.className = 'chat-option-btn';
				deleteButton.innerHTML = '<i class="fas fa-trash-alt"></i>';
				deleteButton.onclick = () => handleDelete(message.id);
				optionsMenu.appendChild(deleteButton);
				container.appendChild(optionsMenu);
			}

			const placeholder = chatBox.querySelector('.chat-placeholder');
			if (placeholder) placeholder.remove();
			chatBox.appendChild(container);
			chatBox.scrollTop = chatBox.scrollHeight;
		};

		const handleDelete = (messageId) => {
			showConfirmationModal("Nachricht wirklich löschen?", () => {
				socket.send(JSON.stringify({ type: 'delete_message', payload: { messageId } }));
			});
		};

		const handleSoftDelete = (payload) => {
			const container = document.getElementById(`message-container-${payload.messageId}`);
			if (container) {
				container.classList.add('deleted-message');
				container.querySelector('.chat-options')?.remove();
				const bubble = container.querySelector('.chat-bubble');
				bubble.innerHTML = `<span class="chat-text"><i>Nachricht gelöscht von <b>${payload.deletedByUsername}</b> um ${new Date(payload.deletedAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })} Uhr</i></span>`;
			}
		};

		const handleEdit = (messageId) => {
			const textElement = document.getElementById(`message-text-${messageId}`);
			const currentText = textElement.textContent;
			const editInput = document.createElement('input');
			editInput.type = 'text';
			editInput.value = currentText;
			editInput.className = 'chat-edit-input';

			editInput.onkeydown = (event) => {
				if (event.key === 'Enter') {
					if (editInput.value.trim() && editInput.value !== currentText) {
						socket.send(JSON.stringify({ type: 'update_message', payload: { messageId, newText: editInput.value } }));
					}
					editInput.replaceWith(textElement);
				} else if (event.key === 'Escape') {
					editInput.replaceWith(textElement);
				}
			};
			textElement.replaceWith(editInput);
			editInput.focus();
		};

		const fetchMessages = () => {
			fetch(`${contextPath}/api/event-chat?eventId=${eventId}`)
				.then(response => response.json())
				.then(messages => {
					chatBox.innerHTML = '';
					if (messages && messages.length > 0) messages.forEach(appendMessage);
					else {
						const placeholder = document.createElement('p');
						placeholder.textContent = 'Noch keine Nachrichten.';
						placeholder.className = 'chat-placeholder';
						placeholder.style.cssText = 'color:var(--text-muted-color); text-align: center; padding-top: 1rem;';
						chatBox.appendChild(placeholder);
					}
				}).catch(error => console.error("Error fetching initial chat messages:", error));
		};

		chatForm.addEventListener('submit', (event) => {
			event.preventDefault();
			const messageText = chatInput.value.trim();
			if (messageText && socket && socket.readyState === WebSocket.OPEN) {
				const payload = { type: "new_message", payload: { messageText: messageText } };
				socket.send(JSON.stringify(payload));
				chatInput.value = '';
			}
		});

		connect();
	}
});