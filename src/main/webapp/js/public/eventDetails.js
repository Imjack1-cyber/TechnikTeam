document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const eventId = document.body.dataset.eventId || '';
	const currentUserId = document.body.dataset.userId || '';
	const isAdmin = document.body.dataset.isAdmin === 'true';

	if (!eventId) {
		console.error("Event ID is missing from the page. Real-time features disabled.");
		return;
	}

	const getTextColorForBackground = (hexColor) => {
		if (!hexColor || hexColor.length < 7) return '#000000';
		const r = parseInt(hexColor.slice(1, 3), 16);
		const g = parseInt(hexColor.slice(3, 5), 16);
		const b = parseInt(hexColor.slice(5, 7), 16);
		const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
		return luminance > 0.5 ? '#000000' : '#FFFFFF';
	};

	const formatAsLocaleTime = (dateString) => {
		if (!dateString) return '';
		return new Date(dateString).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' });
	};

	const chatBox = document.getElementById('chat-box');
	if (chatBox) {
		const chatForm = document.getElementById('chat-form');
		const chatInput = document.getElementById('chat-message-input');
		const websocketProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
		const websocketUrl = `${websocketProtocol}//${window.location.host}${contextPath}/ws/chat/${eventId}`;
		let socket;

		const connect = () => {
			socket = new WebSocket(websocketUrl);
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
						const messageTextElement = document.getElementById(`message-text-${data.payload.messageId}`);
						const editedMarkerElement = document.getElementById(`message-edited-marker-${data.payload.messageId}`);
						if (messageTextElement) messageTextElement.textContent = data.payload.newText;
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

			const bubbleElement = document.createElement('div');
			bubbleElement.className = 'chat-bubble';
			bubbleElement.id = `chat-bubble-${message.id}`;

			const bubbleBackgroundColor = isCurrentUser ? 'var(--primary-color)' : (message.chatColor || '#E9ECEF');
			bubbleElement.style.backgroundColor = bubbleBackgroundColor;
			bubbleElement.style.borderColor = bubbleBackgroundColor;
			bubbleElement.style.color = getTextColorForBackground(bubbleBackgroundColor);

			if (message.isDeleted) {
				renderDeletedState(bubbleElement, message);
			} else {
				renderNormalState(bubbleElement, message, isCurrentUser);
				const optionsMenu = createOptionsMenu(message, isCurrentUser);
				container.appendChild(optionsMenu);
			}

			container.prepend(bubbleElement);
			chatBox.appendChild(container);
			chatBox.scrollTop = chatBox.scrollHeight;
		};

		const renderNormalState = (bubbleElement, message, isCurrentUser) => {
			if (!isCurrentUser) {
				const usernameElement = document.createElement('strong');
				usernameElement.className = 'chat-username';
				usernameElement.style.color = 'black';
				usernameElement.textContent = message.username;
				bubbleElement.appendChild(usernameElement);
			}

			const textElement = document.createElement('span');
			textElement.className = 'chat-text';
			textElement.id = `message-text-${message.id}`;
			textElement.textContent = message.messageText;

			const timeElement = document.createElement('span');
			timeElement.className = 'chat-timestamp';
			timeElement.textContent = formatAsLocaleTime(message.sentAt);
			timeElement.style.color = bubbleElement.style.color === '#FFFFFF' ? 'rgba(255,255,255,0.7)' : 'var(--text-muted-color)';

			const editedMarker = document.createElement('span');
			editedMarker.className = 'chat-edited-marker';
			editedMarker.id = `message-edited-marker-${message.id}`;
			editedMarker.textContent = ' (bearbeitet)';
			editedMarker.style.display = message.edited ? 'inline' : 'none';

			timeElement.prepend(editedMarker);
			bubbleElement.appendChild(textElement);
			bubbleElement.appendChild(timeElement);
		};

		const renderDeletedState = (bubbleElement, message) => {
			let deletedText;
			if (message.username === message.deletedByUsername) {
				deletedText = `Nachricht wurde von ${message.username} gelöscht`;
			} else {
				deletedText = `Nachricht von ${message.username} wurde von ${message.deletedByUsername} gelöscht`;
			}
			bubbleElement.innerHTML = `<span class="chat-deleted-info">${deletedText}</span>`;
			bubbleElement.classList.add('deleted');
		};

		const createOptionsMenu = (message, isCurrentUser) => {
			const optionsMenu = document.createElement('div');
			optionsMenu.className = 'chat-options';
			if (isCurrentUser) {
				const editButton = document.createElement('button');
				editButton.className = 'chat-option-btn';
				editButton.innerHTML = '<i class="fas fa-pencil-alt"></i>';
				editButton.onclick = () => handleEdit(message.id);
				optionsMenu.appendChild(editButton);
			}
			if (isAdmin || isCurrentUser) {
				const deleteButton = document.createElement('button');
				deleteButton.className = 'chat-option-btn';
				deleteButton.innerHTML = '<i class="fas fa-trash-alt"></i>';
				deleteButton.onclick = () => handleDelete(message.id, message.userId, message.username);
				optionsMenu.appendChild(deleteButton);
			}
			return optionsMenu;
		};

		const handleDelete = (messageId, originalUserId, originalUsername) => {
			showConfirmationModal("Nachricht wirklich löschen?", () => {
				socket.send(JSON.stringify({ type: 'delete_message', payload: { messageId, originalUserId, originalUsername } }));
			});
		};

		const handleSoftDelete = (payload) => {
			const bubbleElement = document.getElementById(`chat-bubble-${payload.messageId}`);
			const containerElement = document.getElementById(`message-container-${payload.messageId}`);
			if (bubbleElement && containerElement) {
				containerElement.querySelector('.chat-options')?.remove(); // Remove edit/delete buttons

				let deletedText;
				if (payload.originalUsername === payload.deletedByUsername) {
					deletedText = `Nachricht von ${payload.originalUsername} gelöscht`;
				} else {
					deletedText = `Nachricht von ${payload.originalUsername} wurde von ${payload.deletedByUsername} gelöscht`;
				}
				bubbleElement.innerHTML = `<span class="chat-deleted-info">${deletedText}</span>`;
				bubbleElement.classList.add('deleted');
			}
		};

		const handleEdit = (messageId) => {
			const textElement = document.getElementById(`message-text-${messageId}`);
			const currentText = textElement.textContent;
			const editInput = document.createElement('input');
			editInput.type = 'text';
			editInput.value = currentText;
			editInput.className = 'chat-edit-input';

			editInput.onkeydown = (keyboardEvent) => {
				if (keyboardEvent.key === 'Enter') {
					if (editInput.value.trim() && editInput.value !== currentText) {
						socket.send(JSON.stringify({ type: 'update_message', payload: { messageId, newText: editInput.value } }));
					}
					textElement.style.display = 'block';
					editInput.replaceWith(textElement);
				} else if (keyboardEvent.key === 'Escape') {
					textElement.style.display = 'block';
					editInput.replaceWith(textElement);
				}
			};

			textElement.style.display = 'none';
			textElement.parentElement.insertBefore(editInput, textElement);
			editInput.focus();
		};

		const fetchMessages = () => {
			fetch(`${contextPath}/api/event-chat?eventId=${eventId}`)
				.then(response => response.json())
				.then(messages => {
					chatBox.innerHTML = '';
					if (messages && messages.length > 0) messages.forEach(appendMessage);
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