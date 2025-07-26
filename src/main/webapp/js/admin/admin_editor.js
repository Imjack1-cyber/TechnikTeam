document.addEventListener('DOMContentLoaded', () => {
	const editorContainer = document.getElementById('editor-page-container');
	if (!editorContainer) return;

	const fileId = editorContainer.dataset.fileId;
	const editor = document.getElementById('editor');
	const preview = document.getElementById('markdown-preview');
	const toggle = document.getElementById('mode-toggle');
	const statusIndicator = document.getElementById('save-status-indicator');

	let socket;
	let debounceTimer;

	const renderMarkdown = (content) => {
		if (typeof marked !== 'undefined') {
			preview.innerHTML = marked.parse(content, { sanitize: true });
		}
	};

	const showStatus = (state, message) => {
		if (statusIndicator) {
			statusIndicator.style.display = 'inline-block';
			statusIndicator.className = `status-badge status-${state}`;
			statusIndicator.textContent = message;
		}
	};

	const connect = () => {
		const websocketProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
		const websocketUrl = `${websocketProtocol}//${window.location.host}${document.body.dataset.contextPath}/ws/editor/${fileId}`;

		socket = new WebSocket(websocketUrl);

		socket.onopen = () => {
			console.log('WebSocket connection established.');
			showStatus('ok', 'Verbunden');
		};

		socket.onmessage = (event) => {
			const data = JSON.parse(event.data);
			if (data.type === 'content_update') {
				console.log('Received content update from another client.');

				const cursorStart = editor.selectionStart;
				const cursorEnd = editor.selectionEnd;

				editor.value = data.payload;
				renderMarkdown(data.payload);

				editor.setSelectionRange(cursorStart, cursorEnd);
			}
		};

		socket.onclose = () => {
			console.warn('WebSocket connection closed.');
			showStatus('danger', 'Getrennt');
			setTimeout(connect, 5000); 
		};

		socket.onerror = (error) => {
			console.error('WebSocket error:', error);
			showStatus('danger', 'Verbindungsfehler');
		};
	};


	if (editor.readOnly) {
		renderMarkdown(editor.value);
		return;
	}

	connect();
	renderMarkdown(editor.value);

	const sendContentUpdate = () => {
		if (socket && socket.readyState === WebSocket.OPEN) {
			const payload = {
				type: 'content_update',
				payload: editor.value
			};
			socket.send(JSON.stringify(payload));
			showStatus('warn', 'Speichern...');
			setTimeout(() => {
				if (statusIndicator.textContent === 'Speichern...') {
					showStatus('ok', 'Gespeichert');
				}
			}, 1000);
		}
	};

	editor.addEventListener('input', () => {
		renderMarkdown(editor.value);
		clearTimeout(debounceTimer);
		debounceTimer = setTimeout(sendContentUpdate, 500); 
	});

	if (toggle) {
		toggle.addEventListener('change', () => {
			if (toggle.checked) { 
				editor.style.display = 'block';
				preview.style.display = 'none';
			} else { 
				renderMarkdown(editor.value);
				editor.style.display = 'none';
				preview.style.display = 'block';
			}
		});
	}
});