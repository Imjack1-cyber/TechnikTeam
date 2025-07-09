document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const eventId = document.body.dataset.eventId || '';
	const currentUserId = document.body.dataset.userId || '';
	const isAdmin = document.body.dataset.isAdmin === 'true';

	const taskModal = document.getElementById('task-modal');
	if (taskModal) {
		const allUsers = JSON.parse(document.getElementById('allUsersData')?.textContent || '[]');
		const allItems = JSON.parse(document.getElementById('allItemsData')?.textContent || '[]');
		const allKits = JSON.parse(document.getElementById('allKitsData')?.textContent || '[]');
		const allTasks = JSON.parse(document.getElementById('allTasksData')?.textContent || '[]');

		const form = document.getElementById('task-modal-form');
		const title = document.getElementById('task-modal-title');
		const taskIdInput = document.getElementById('task-id-modal');
		const descInput = document.getElementById('task-description-modal');
		const detailsInput = document.getElementById('task-details-modal');
		const orderInput = document.getElementById('task-display-order-modal');
		const statusGroup = document.getElementById('task-status-group');
		const statusInput = document.getElementById('task-status-modal');
		const deleteBtn = document.getElementById('delete-task-btn');

		const assignmentTypeRadios = form.querySelectorAll('input[name="assignmentType"]');
		const directFields = document.getElementById('direct-assignment-fields');
		const poolFields = document.getElementById('pool-assignment-fields');
		const requiredPersonsInput = document.getElementById('task-required-persons-modal');
		const userCheckboxesContainer = document.getElementById('task-user-checkboxes');

		const itemsContainer = document.getElementById('task-items-container');
		const kitsContainer = document.getElementById('task-kits-container');

		const createRow = (container, onRemove) => {
			const row = document.createElement('div');
			row.className = 'dynamic-row';
			const removeBtn = document.createElement('button');
			removeBtn.type = 'button';
			removeBtn.className = 'btn-small btn-danger';
			removeBtn.innerHTML = '×';
			removeBtn.onclick = () => onRemove(row);
			row.appendChild(removeBtn);
			container.appendChild(row);
			return row;
		};

		const addItemRow = (item = { id: '', quantity: 1 }) => {
			const row = createRow(itemsContainer, r => r.remove());
			const select = document.createElement('select');
			select.name = 'itemIds';
			select.className = 'form-group';
			select.innerHTML = '<option value="">-- Material --</option>' + allItems.map(i => `<option value="${i.id}" data-max-qty="${i.availableQuantity}">${i.name}</option>`).join('');
			select.value = item.id;

			const input = document.createElement('input');
			input.type = 'number';
			input.name = 'itemQuantities';
			input.value = item.quantity;
			input.min = '1';
			input.className = 'form-group';
			input.style.maxWidth = '100px';

			select.addEventListener('change', () => {
				const selectedOption = select.options[select.selectedIndex];
				const maxQty = selectedOption.dataset.maxQty;
				input.max = maxQty || '';
				if (maxQty) input.title = `Maximal verfügbar: ${maxQty}`;
			});

			row.prepend(select, input);
		};

		const addKitRow = (kit = { id: '' }) => {
			const row = createRow(kitsContainer, r => r.remove());
			const select = document.createElement('select');
			select.name = 'kitIds';
			select.className = 'form-group';
			select.innerHTML = '<option value="">-- Kit --</option>' + allKits.map(k => `<option value="${k.id}">${k.name}</option>`).join('');
			select.value = kit.id;
			row.prepend(select);
		};

		const openModal = () => taskModal.classList.add('active');
		const closeModal = () => taskModal.classList.remove('active');

		const resetModal = () => {
			form.reset();
			taskIdInput.value = '';
			itemsContainer.innerHTML = '';
			kitsContainer.innerHTML = '';
			userCheckboxesContainer.innerHTML = '';
			statusGroup.style.display = 'none';
			deleteBtn.style.display = 'none';
			directFields.style.display = 'block';
			poolFields.style.display = 'none';
			form.querySelector('input[name="assignmentType"][value="direct"]').checked = true;
		};

		document.getElementById('new-task-btn')?.addEventListener('click', () => {
			resetModal();
			title.textContent = 'Neue Aufgabe erstellen';
			allUsers.forEach(user => {
				userCheckboxesContainer.innerHTML += `<label><input type="checkbox" name="userIds" value="${user.id}"> ${user.username}</label>`;
			});
			openModal();
		});

		document.querySelectorAll('.edit-task-btn').forEach(btn => {
			btn.addEventListener('click', () => {
				const taskId = parseInt(btn.dataset.taskId, 10);
				const task = allTasks.find(t => t.id === taskId);
				if (!task) return;

				resetModal();
				title.textContent = 'Aufgabe bearbeiten';
				statusGroup.style.display = 'block';
				deleteBtn.style.display = 'inline-block';

				taskIdInput.value = task.id;
				descInput.value = task.description;
				detailsInput.value = task.details || '';
				orderInput.value = task.displayOrder;
				statusInput.value = task.status;

				if (task.requiredPersons > 0) {
					form.querySelector('input[name="assignmentType"][value="pool"]').checked = true;
					poolFields.style.display = 'block';
					directFields.style.display = 'none';
					requiredPersonsInput.value = task.requiredPersons;
				} else {
					const assignedIds = new Set(task.assignedUsers.map(u => u.id));
					allUsers.forEach(user => {
						const isChecked = assignedIds.has(user.id) ? 'checked' : '';
						userCheckboxesContainer.innerHTML += `<label><input type="checkbox" name="userIds" value="${user.id}" ${isChecked}> ${user.username}</label>`;
					});
				}

				task.requiredItems?.forEach(item => addItemRow({ id: item.id, quantity: item.quantity }));
				task.requiredKits?.forEach(kit => addKitRow({ id: kit.id }));
				openModal();
			});
		});

		assignmentTypeRadios.forEach(radio => {
			radio.addEventListener('change', () => {
				directFields.style.display = radio.value === 'direct' ? 'block' : 'none';
				poolFields.style.display = radio.value === 'pool' ? 'block' : 'none';
			});
		});

		deleteBtn.addEventListener('click', () => {
			showConfirmationModal('Diese Aufgabe wirklich löschen?', () => {
				const csrfToken = form.querySelector('input[name="csrfToken"]').value;
				const deleteForm = document.createElement('form');
				deleteForm.method = 'post';
				deleteForm.action = `${contextPath}/task-action`;
				deleteForm.innerHTML = `
					<input type="hidden" name="action" value="delete">
					<input type="hidden" name="taskId" value="${taskIdInput.value}">
					<input type="hidden" name="eventId" value="${eventId}">
					<input type="hidden" name="csrfToken" value="${csrfToken}">`;
				document.body.appendChild(deleteForm);
				deleteForm.submit();
			});
		});

		document.body.addEventListener('click', e => {
			const addItemBtn = e.target.closest('#add-task-item-btn');
			const addKitBtn = e.target.closest('#add-task-kit-btn');

			if (addItemBtn) {
				addItemRow();
			}
			if (addKitBtn) {
				addKitRow();
			}
		});

		taskModal.querySelector('.modal-close-btn').addEventListener('click', closeModal);
	}

	const taskListContainer = document.getElementById('task-list-container');
	if (taskListContainer) {
		taskListContainer.addEventListener('click', (e) => {
			const markDoneBtn = e.target.closest('.mark-task-done-btn');
			if (markDoneBtn) {
				const taskId = markDoneBtn.dataset.taskId;
				const csrfToken = document.body.dataset.csrfToken;
				const params = new URLSearchParams();
				params.append('action', 'updateStatus');
				params.append('taskId', taskId);
				params.append('status', 'ERLEDIGT');
				if (csrfToken) {
					params.append('csrfToken', csrfToken);
				}

				fetch(`${contextPath}/task-action`, {
					method: 'POST',
					body: params
				})
					.then(response => {
						if (response.ok) window.location.reload();
						else alert('Fehler beim Aktualisieren der Aufgabe.');
					})
					.catch(error => {
						console.error("Error updating task status:", error);
						alert('Netzwerkfehler beim Aktualisieren der Aufgabe.');
					});
			}
		});
	}

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
			const deletedByEl = document.createElement('span');
			deletedByEl.textContent = message.deletedByUsername;

			const originalUserEl = document.createElement('span');
			originalUserEl.textContent = message.username;

			const infoSpan = document.createElement('span');
			infoSpan.className = 'chat-deleted-info';

			if (message.username === message.deletedByUsername) {
				infoSpan.textContent = `Nachricht wurde von ${originalUserEl.textContent} gelöscht`;
			} else {
				infoSpan.textContent = `Nachricht von ${originalUserEl.textContent} wurde von ${deletedByEl.textContent} gelöscht`;
			}
			bubbleElement.appendChild(infoSpan);
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
				containerElement.querySelector('.chat-options')?.remove();

				const deletedByEl = document.createElement('span');
				deletedByEl.textContent = payload.deletedByUsername;

				const originalUserEl = document.createElement('span');
				originalUserEl.textContent = payload.originalUsername;

				const infoSpan = document.createElement('span');
				infoSpan.className = 'chat-deleted-info';

				if (payload.originalUsername === payload.deletedByUsername) {
					infoSpan.textContent = `Nachricht von ${originalUserEl.textContent} gelöscht`;
				} else {
					infoSpan.textContent = `Nachricht von ${originalUserEl.textContent} wurde von ${deletedByEl.textContent} gelöscht`;
				}

				bubbleElement.innerHTML = ''; // Clear existing content
				bubbleElement.appendChild(infoSpan);
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