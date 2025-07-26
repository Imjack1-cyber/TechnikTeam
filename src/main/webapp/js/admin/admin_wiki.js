document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const csrfToken = document.body.dataset.csrfToken;

	const treeContainer = document.getElementById('wiki-tree-container');
	const contentPane = document.getElementById('wiki-content-pane');
	const searchInput = document.getElementById('wiki-search');

	// Mobile sidebar elements
	const wrapper = document.querySelector('.wiki-page-wrapper');
	const sidebarToggle = document.getElementById('wiki-sidebar-toggle');

	// New Page Modal elements
	const addPageModal = document.getElementById('add-wiki-page-modal');
	const addPageBtn = document.getElementById('add-wiki-page-btn');
	const addPageForm = document.getElementById('add-wiki-page-form');
	const addPageCloseBtn = addPageModal?.querySelector('.modal-close-btn');

	if (!treeContainer || !contentPane) return;

	// --- API Abstraction ---
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

	/**
	 * Renders the read-only view of a wiki entry.
	 * @param {object} entry - The wiki entry data object.
	 */
	const renderViewer = (entry) => {
		const renderedHtml = marked.parse(entry.content || '_No content has been written for this document yet._');
		contentPane.innerHTML = `
            <div class="wiki-content-header">
                <h2>${entry.filePath}</h2>
                <div class="wiki-editor-controls">
                    <button class="btn btn-primary btn-small" id="edit-wiki-page-btn" data-id="${entry.id}">
                       <i class="fas fa-pencil-alt"></i> Edit
                    </button>
                    <button class="btn btn-danger-outline btn-small" id="delete-wiki-page-btn" data-id="${entry.id}" data-path="${entry.filePath}">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </div>
            </div>
            <div id="markdown-preview" class="markdown-content">${renderedHtml}</div>`;
	};

	/**
	 * Renders the editor view for a wiki entry.
	 * @param {object} entry - The wiki entry data object.
	 */
	const renderEditor = (entry) => {
		contentPane.innerHTML = `
            <div class="wiki-content-header">
                <h2>Editing: ${entry.filePath}</h2>
                <div class="wiki-editor-controls">
                    <button class="btn btn-success btn-small" id="save-wiki-btn" data-id="${entry.id}">
                        <i class="fas fa-save"></i> Save Changes
                    </button>
                    <button class="btn btn-secondary btn-small" id="cancel-edit-btn" data-id="${entry.id}">
                        <i class="fas fa-times"></i> Cancel
                    </button>
                </div>
            </div>
            <div class="editor-container">
                 <textarea id="wiki-editor" class="form-control">${entry.content || ''}</textarea>
            </div>`;
	};

	/**
	 * Loads wiki content and renders it in viewer mode.
	 * @param {number} id - The ID of the wiki entry.
	 */
	const loadContent = async (id) => {
		contentPane.innerHTML = '<p>Loading content...</p>';
		try {
			const result = await api.getContent(id);
			if (result.success) {
				renderViewer(result.data);
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			contentPane.innerHTML = `<p class="error-message">Could not load document: ${error.message}</p>`;
		}
	};

	/**
	 * Recursively renders the file tree.
	 * @param {object} node - The current node in the tree (a map).
	 * @param {HTMLElement} container - The HTML element to append the rendered tree to.
	 */
	const renderTree = (node, container) => {
		const ul = document.createElement('ul');
		for (const [name, value] of Object.entries(node)) {
			const li = document.createElement('li');
			li.dataset.name = name;
			if (typeof value.id !== 'undefined') { // It's a file (WikiEntry)
				li.innerHTML = `<a href="#" data-id="${value.id}"><i class="fas fa-file-alt fa-fw"></i> ${name}</a>`;
			} else { // It's a directory (Map)
				const details = document.createElement('details');
				const summary = document.createElement('summary');
				summary.textContent = name;
				details.appendChild(summary);
				renderTree(value, details);
				li.appendChild(details);
			}
			ul.appendChild(li);
		}
		container.appendChild(ul);
	};

	const loadTree = async () => {
		try {
			const treeData = await api.getTree();
			treeContainer.innerHTML = '';
			renderTree(treeData, treeContainer);
		} catch (error) {
			console.error('Failed to load wiki tree:', error);
			treeContainer.innerHTML = '<p class="error-message">Could not load wiki navigation.</p>';
		}
	};

	// --- Event Delegation for the whole page ---
	document.body.addEventListener('click', async (e) => {
		// Click on a link in the tree
		const treeLink = e.target.closest('#wiki-tree-container a');
		if (treeLink) {
			e.preventDefault();
			const id = treeLink.dataset.id;
			document.querySelectorAll('#wiki-tree-container a').forEach(a => a.classList.remove('active'));
			treeLink.classList.add('active');
			loadContent(id);
			wrapper.classList.remove('sidebar-open');
			return;
		}

		// Click on the "Edit" button
		const editBtn = e.target.closest('#edit-wiki-page-btn');
		if (editBtn) {
			e.preventDefault();
			const id = editBtn.dataset.id;
			const result = await api.getContent(id);
			if (result.success) {
				renderEditor(result.data);
			} else {
				showToast('Could not load content for editing.', 'danger');
			}
			return;
		}

		// Click on the "Cancel" button in edit mode
		const cancelBtn = e.target.closest('#cancel-edit-btn');
		if (cancelBtn) {
			e.preventDefault();
			const id = cancelBtn.dataset.id;
			loadContent(id); // Simply reload the original content
			return;
		}

		// Click on the "Save" button in edit mode
		const saveBtn = e.target.closest('#save-wiki-btn');
		if (saveBtn) {
			e.preventDefault();
			saveBtn.disabled = true;
			saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';

			const id = saveBtn.dataset.id;
			const editor = document.getElementById('wiki-editor');
			const newContent = editor.value;

			try {
				const result = await api.updatePage(id, newContent);
				if (result.success) {
					showToast('Page updated successfully.', 'success');
					loadContent(id); // Reload in view mode
				} else {
					throw new Error(result.message);
				}
			} catch (error) {
				showToast(`Error saving: ${error.message}`, 'danger');
				saveBtn.disabled = false;
				saveBtn.innerHTML = '<i class="fas fa-save"></i> Save Changes';
			}
			return;
		}

		// Click on the "Delete" button
		const deleteBtn = e.target.closest('#delete-wiki-page-btn');
		if (deleteBtn) {
			e.preventDefault();
			const id = deleteBtn.dataset.id;
			const path = deleteBtn.dataset.path;
			showConfirmationModal(`Delete the page for "${path}" permanently?`, async () => {
				try {
					const result = await api.deletePage(id);
					if (result.success) {
						showToast('Page successfully deleted.', 'success');
						const linkToDelete = treeContainer.querySelector(`a[data-id="${id}"]`);
						linkToDelete?.closest('li').remove();
						contentPane.innerHTML = `<div class="wiki-welcome-pane"><h2>Page Deleted</h2><p>Please select another page from the navigation.</p></div>`;
					} else {
						throw new Error(result.message);
					}
				} catch (error) {
					showToast(`Error deleting page: ${error.message}`, 'danger');
				}
			});
		}
	});

	// --- Search/Filter Logic ---
	searchInput.addEventListener('input', (e) => {
		const searchTerm = e.target.value.toLowerCase();
		treeContainer.querySelectorAll('li').forEach(li => {
			const text = li.dataset.name.toLowerCase();
			const isMatch = text.includes(searchTerm);
			li.style.display = isMatch ? '' : 'none';
			if (isMatch) {
				let parent = li.parentElement;
				while (parent && parent !== treeContainer) {
					if (parent.tagName === 'DETAILS') parent.open = true;
					parent = parent.parentElement;
				}
			}
		});
	});

	// --- Modal & Mobile Sidebar Logic ---
	if (addPageBtn) addPageBtn.addEventListener('click', () => addPageModal.classList.add('active'));
	if (addPageCloseBtn) addPageCloseBtn.addEventListener('click', () => addPageModal.classList.remove('active'));
	if (addPageForm) addPageForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const formData = new URLSearchParams(new FormData(addPageForm));
		formData.append('csrfToken', csrfToken);

		try {
			const result = await api.createPage(formData);
			if (result.success) {
				showToast('New page created successfully.', 'success');
				addPageModal.classList.remove('active');
				loadTree(); // Reload the tree to show the new page
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast(`Error: ${error.message}`, 'danger');
		}
	});

	if (sidebarToggle) sidebarToggle.addEventListener('click', () => {
		wrapper.classList.toggle('sidebar-open');
	});

	// Initial Load
	loadTree();
});