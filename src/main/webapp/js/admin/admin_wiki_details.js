document.addEventListener('DOMContentLoaded', () => {
	const editorContainer = document.getElementById('editor-page-container');
	if (!editorContainer) return;

	const contextPath = document.body.dataset.contextPath || '';
	const csrfToken = document.body.dataset.csrfToken;
	const wikiId = editorContainer.dataset.wikiId;

	const editor = document.getElementById('editor');
	const preview = document.getElementById('markdown-preview');
	const toggle = document.getElementById('mode-toggle');
	const saveBtn = document.getElementById('save-wiki-btn');
	const statusIndicator = document.getElementById('save-status-indicator');

	// Function to render markdown content
	const renderMarkdown = (content) => {
		if (preview && typeof marked !== 'undefined') {
			preview.innerHTML = marked.parse(content || '', { sanitize: true });
		}
	};

	// --- Status Indicator Logic ---
	const showStatus = (state, message) => {
		if (statusIndicator) {
			statusIndicator.style.display = 'inline-block';
			statusIndicator.className = `status-badge status-${state}`;
			statusIndicator.textContent = message;
			// Fade out after a few seconds unless it's an error
			if (state !== 'danger') {
				setTimeout(() => {
					statusIndicator.style.display = 'none';
				}, 3000);
			}
		}
	};

	// --- Save Logic ---
	const saveContent = async () => {
		showStatus('warn', 'Speichern...');
		try {
			const formData = new URLSearchParams();
			formData.append('csrfToken', csrfToken);
			formData.append('wikiId', wikiId);
			formData.append('content', editor.value);

			const response = await fetch(`${contextPath}/admin/action/wiki?action=update`, {
				method: 'POST',
				body: formData
			});
			const result = await response.json();

			if (response.ok && result.success) {
				showStatus('ok', 'Gespeichert');
				showToast(result.message, 'success');
			} else {
				throw new Error(result.message || 'Unknown error');
			}
		} catch (error) {
			console.error('Error saving wiki content:', error);
			showStatus('danger', 'Fehler!');
			showToast(`Fehler beim Speichern: ${error.message}`, 'danger');
		}
	};

	// --- Event Listeners ---
	if (saveBtn) {
		saveBtn.addEventListener('click', saveContent);
	}

	// Initial render of the preview pane from the reliable textarea source
	renderMarkdown(editor.value);
	editor.style.display = 'none';
	preview.style.display = 'block';

	if (toggle) {
		toggle.checked = false; // Start in view mode
		toggle.addEventListener('change', () => {
			const isEditMode = toggle.checked;
			editor.style.display = isEditMode ? 'block' : 'none';
			preview.style.display = isEditMode ? 'none' : 'block';
			saveBtn.style.display = isEditMode ? 'inline-flex' : 'none';

			if (!isEditMode) {
				// When switching back to view mode, re-render from editor
				renderMarkdown(editor.value);
			}
		});
	}
});