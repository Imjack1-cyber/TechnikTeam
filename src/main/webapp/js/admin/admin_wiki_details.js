document.addEventListener('DOMContentLoaded', () => {
	const editorContainer = document.getElementById('editor-page-container');
	if (!editorContainer) return;

	const editor = document.getElementById('editor');
	const preview = document.getElementById('markdown-preview');
	const toggle = document.getElementById('mode-toggle');

	// Initial render of the preview pane from the reliable textarea source
	if (preview && editor && typeof marked !== 'undefined') {
		const initialContent = editor.value;
		preview.innerHTML = marked.parse(initialContent, { sanitize: true });
	}

	if (toggle) {
		// Set initial toggle state based on which pane is visible by default (view mode)
		toggle.checked = false;
		editor.style.display = 'none';
		preview.style.display = 'block';


		toggle.addEventListener('change', () => {
			if (toggle.checked) { // Switch to Edit mode
				editor.style.display = 'block';
				preview.style.display = 'none';
			} else { // Switch to View mode
				// Re-render markdown from the (potentially modified) textarea
				if (typeof marked !== 'undefined') {
					preview.innerHTML = marked.parse(editor.value || '', { sanitize: true });
				}
				editor.style.display = 'none';
				preview.style.display = 'block';
			}
		});
	}
});