document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const csrfToken = document.body.dataset.csrfToken;
	const treeContainer = document.getElementById('wiki-tree-container');
	const contentPane = document.getElementById('wiki-content-pane');
	const searchInput = document.getElementById('wiki-search');

	// Layout elements for mobile
	const wrapper = document.querySelector('.wiki-page-wrapper');
	const sidebarToggle = document.getElementById('wiki-sidebar-toggle');

	// New Page Modal elements
	const addPageModal = document.getElementById('add-wiki-page-modal');
	const addPageBtn = document.getElementById('add-wiki-page-btn');
	const addPageForm = document.getElementById('add-wiki-page-form');
	const addPageCloseBtn = addPageModal.querySelector('.modal-close-btn');

	let debounceTimer;

	if (!treeContainer || !contentPane) return;

	const api = {
		getTree: () => fetch(`${contextPath}/api/admin/wiki`).then(res => res.json()),
		getContent: (id) => fetch(`${contextPath}/admin/action/wiki?action=getContent`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ id, csrfToken })
		}).then(res => res.json()),
		createPage: (formData) => fetch(`${contextPath}/admin/action/wiki?action=create`, {
			method: 'POST',
			body: formData
		}).then(res => res.json()),
		deletePage: (id) => fetch(`${contextPath}/admin/action/wiki?action=delete`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ wikiId: id, csrfToken })
		}).then(res => res.json()),
		updatePage: (id, content) => fetch(`${contextPath}/admin/action/wiki?action=update`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ wikiId: id, content, csrfToken })
		}).then(res => res.json())
	};

	const saveTreeState = () => {
		const openDirectories = [];
		treeContainer.querySelectorAll('details[open]').forEach(details => {
			let path = [];
			let current = details;
			while (current && current !== treeContainer) {
				if (current.tagName === 'LI') {
					path.unshift(current.dataset.name);
				}
				current = current.parentElement;
			}
			openDirectories.push(path.join('/'));
		});
		sessionStorage.setItem('wikiTreeState', JSON.stringify(openDirectories));
	};

	const restoreTreeState = () => {
		const openDirectories = JSON.parse(sessionStorage.getItem('wikiTreeState') || '[]');
		if (openDirectories.length === 0) return;

		treeContainer.querySelectorAll('details').forEach(details => {
			let path = [];
			let current = details;
			while (current && current !== treeContainer) {
				if (current.tagName === 'LI') {
					path.unshift(current.dataset.name);
				}
				current = current.parentElement;
			}
			if (openDirectories.includes(path.join('/'))) {
				details.open = true;
			}
		});
	};

	const renderTree = (node, container) => {
		const ul = document.createElement('ul');
		Object.entries(node).forEach(([name, value]) => {
			const li = document.createElement('li');
			li.dataset.name = name;
			if (typeof value.id !== 'undefined') { // It's a file (WikiEntry)
				li.innerHTML = `<a href="#" data-id="${value.id}"><i class="fas fa-file-alt fa-fw"></i> ${name}</a>`;
			} else { // It's a directory (Map)
				const details = document.createElement('details');
				const summary = document.createElement('summary');
				summary.innerHTML = `<i class="fas fa-folder fa-fw"></i> ${name}`;
				details.appendChild(summary);
				renderTree(value, details);
				li.appendChild(details);
			}
			ul.appendChild(li);
		});
		container.appendChild(ul);
	};

	const loadContent = async (id) => {
		contentPane.innerHTML = '<p>Lade Inhalt...</p>';
		try {
			const result = await api.getContent(id);
			if (result.success) {
				const entry = result.data;
				const renderedHtml = marked.parse(entry.content || 'Für dieses Dokument wurde noch kein Inhalt verfasst.', { sanitize: true });
				contentPane.innerHTML = `
                    <div class="wiki-content-header">
                        <h2>${entry.filePath}</h2>
                        <div class="wiki-editor-controls">
                            <span id="save-status-indicator" class="status-badge" style="display: none;"></span>
                            <div class="mode-switcher">
                                <span>Ansehen</span>
                                <label class="toggle-switch">
                                    <input type="checkbox" id="mode-toggle">
                                    <span class="slider"></span>
                                </label>
                                <span>Bearbeiten</span>
                            </div>
                            <button class="btn btn-danger-outline btn-small" id="delete-wiki-page-btn" data-id="${entry.id}" data-path="${entry.filePath}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                    <div class="editor-container">
                        <textarea id="editor" style="display: none;">${entry.content || ''}</textarea>
                        <div id="markdown-preview" class="markdown-content">${renderedHtml}</div>
                    </div>`;
				setupEditor(entry.id);
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error('Failed to load content:', error);
			contentPane.innerHTML = `<p class="error-message">Dokumenteninhalt konnte nicht geladen werden: ${error.message}</p>`;
		}
	};

	const setupEditor = (wikiId) => {
		const editor = document.getElementById('editor');
		const preview = document.getElementById('markdown-preview');
		const toggle = document.getElementById('mode-toggle');
		const statusIndicator = document.getElementById('save-status-indicator');

		const showStatus = (state, message) => {
			statusIndicator.style.display = 'inline-block';
			statusIndicator.className = `status-badge status-${state}`;
			statusIndicator.textContent = message;
			if (state !== 'danger' && state !== 'warn') {
				setTimeout(() => {
					if (statusIndicator.textContent === message) {
						statusIndicator.style.display = 'none';
					}
				}, 3000);
			}
		};

		const autoSave = async () => {
			showStatus('warn', 'Speichern...');
			try {
				const result = await api.updatePage(wikiId, editor.value);
				if (result.success) {
					showStatus('ok', 'Gespeichert');
				} else {
					throw new Error(result.message);
				}
			} catch (error) {
				console.error("Auto-save failed:", error);
				showStatus('danger', 'Fehler!');
			}
		};

		toggle.addEventListener('change', () => {
			const isEditMode = toggle.checked;
			editor.style.display = isEditMode ? 'block' : 'none';
			preview.style.display = isEditMode ? 'none' : 'block';
			if (!isEditMode) {
				preview.innerHTML = marked.parse(editor.value, { sanitize: true });
			}
		});

		editor.addEventListener('input', () => {
			clearTimeout(debounceTimer);
			showStatus('info', 'Ungespeichert');
			debounceTimer = setTimeout(autoSave, 1500); // Auto-save 1.5s after user stops typing
		});
	};

	// --- Event Delegation for Tree and Content Pane ---
	document.body.addEventListener('click', (e) => {
		const treeLink = e.target.closest('#wiki-tree-container a');
		if (treeLink) {
			e.preventDefault();
			const id = treeLink.dataset.id;
			if (id) {
				document.querySelectorAll('#wiki-tree-container a').forEach(a => a.classList.remove('active'));
				treeLink.classList.add('active');
				loadContent(id);
				wrapper.classList.remove('sidebar-open'); // Close mobile sidebar on selection
			}
			return;
		}

		const deleteBtn = e.target.closest('#delete-wiki-page-btn');
		if (deleteBtn) {
			e.preventDefault();
			const id = deleteBtn.dataset.id;
			const path = deleteBtn.dataset.path;
			showConfirmationModal(`Seite für "${path}" wirklich löschen?`, async () => {
				try {
					const result = await api.deletePage(id);
					if (result.success) {
						showToast(result.message, 'success');
						const linkToDelete = treeContainer.querySelector(`a[data-id="${id}"]`);
						linkToDelete?.closest('li').remove();
						contentPane.innerHTML = `<div class="wiki-welcome-pane"><h2>Seite gelöscht</h2><p>Wählen Sie eine andere Seite aus.</p></div>`;
					} else {
						throw new Error(result.message);
					}
				} catch (error) {
					showToast(`Fehler beim Löschen: ${error.message}`, 'danger');
				}
			});
		}

		const summary = e.target.closest('summary');
		if (summary) {
			setTimeout(saveTreeState, 100);
		}
	});

	searchInput.addEventListener('input', (e) => {
		const searchTerm = e.target.value.toLowerCase();
		treeContainer.querySelectorAll('li').forEach(li => {
			const text = li.dataset.name.toLowerCase();
			const isMatch = text.includes(searchTerm);
			li.style.display = isMatch ? '' : 'none';
			if (isMatch) {
				let parent = li.parentElement;
				while (parent && parent !== treeContainer) {
					if (parent.tagName === 'DETAILS') {
						parent.open = true;
					}
					parent = parent.parentElement;
				}
			}
		});
	});

	const loadTree = () => {
		api.getTree()
			.then(treeData => {
				treeContainer.innerHTML = '';
				renderTree(treeData, treeContainer);
				restoreTreeState();
			})
			.catch(error => {
				console.error('Failed to load wiki tree:', error);
				treeContainer.innerHTML = '<p class="error-message">Could not load wiki navigation.</p>';
			});
	};

	// --- Modal & Mobile Sidebar Logic ---
	addPageBtn.addEventListener('click', () => addPageModal.classList.add('active'));
	addPageCloseBtn.addEventListener('click', () => addPageModal.classList.remove('active'));
	addPageForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const formData = new URLSearchParams(new FormData(addPageForm));
		formData.append('csrfToken', csrfToken);

		try {
			const result = await api.createPage(formData);
			if (result.success) {
				showToast(result.message, 'success');
				addPageModal.classList.remove('active');
				loadTree();
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast(`Fehler: ${error.message}`, 'danger');
		}
	});

	sidebarToggle.addEventListener('click', () => {
		wrapper.classList.toggle('sidebar-open');
	});

	// Initial Load
	loadTree();
});