document.addEventListener('DOMContentLoaded', () => {
	const editor = document.getElementById('editor');
	const statusIndicator = document.getElementById('status-indicator');
	const contextPath = document.body.dataset.contextPath || '';
	const apiUrl = `${contextPath}/api/document`;
	let debounceTimer;

	const saveContent = () => {
		clearTimeout(debounceTimer);
		debounceTimer = setTimeout(() => {
			const content = editor.value;
			statusIndicator.textContent = 'Speichere...';
			fetch(apiUrl, {
				method: 'POST',
				headers: { 'Content-Type': 'text/plain; charset=utf-8' },
				body: content
			}).then(response => {
				if (response.ok) {
					statusIndicator.textContent = 'Gespeichert';
				} else {
					statusIndicator.textContent = 'Fehler beim Speichern!';
					statusIndicator.style.color = 'var(--danger-color)';
				}
			}).catch(err => {
				statusIndicator.textContent = 'Netzwerkfehler!';
				statusIndicator.style.color = 'var(--danger-color)';
			});
		}, 500);
	};

	const fetchContent = () => {
		fetch(apiUrl)
			.then(response => {
				if (!response.ok) throw new Error("Server response not OK");
				return response.text();
			})
			.then(newContent => {
				if (document.activeElement !== editor && editor.value !== newContent) {
					const cursorPos = editor.selectionStart;
					editor.value = newContent;
					editor.selectionStart = editor.selectionEnd = cursorPos;
				}
			}).catch(err => {
				console.error("Error fetching document content:", err);
				statusIndicator.textContent = 'Fehler beim Laden!';
				statusIndicator.style.color = 'var(--danger-color)';
			});
	};

	editor.addEventListener('input', saveContent);

	const pollInterval = setInterval(fetchContent, 3000);
	fetchContent();

	window.addEventListener('beforeunload', () => {
		clearInterval(pollInterval);
	});
});