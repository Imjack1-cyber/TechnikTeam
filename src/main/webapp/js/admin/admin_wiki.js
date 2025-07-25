document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const csrfToken = document.body.dataset.csrfToken;
	const treeContainer = document.getElementById('wiki-tree-container');
	const contentPane = document.getElementById('wiki-content-pane');
	const searchInput = document.getElementById('wiki-search');

	// New Page Modal elements
	const addPageModal = document.getElementById('add-wiki-page-modal');
	const addPageBtn = document.getElementById('add-wiki-page-btn');
	const addPageForm = document.getElementById('add-wiki-page-form');
	const addPageCloseBtn = addPageModal.querySelector('.modal-close-btn');


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
		}).then(res => res.json())
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
		contentPane.innerHTML = '<p>Loading content...</p>';
		try {
			const result = await api.getContent(id);
			if (result.success) {
				const entry = result.data;
				const renderedHtml = marked.parse(entry.content || 'This document has no content yet.', { sanitize: true });
				contentPane.innerHTML = `
                    <div class="wiki-content-header">
                        <h2>${entry.filePath}</h2>
                        <div style="display:flex; gap: 0.5rem;">
                            <a href="${contextPath}/admin/wiki/details?id=${entry.id}" class="btn btn-secondary btn-small">
                                <i class="fas fa-edit"></i> Bearbeiten
                            </a>
                            <button class="btn btn-danger-outline btn-small" id="delete-wiki-page-btn" data-id="${entry.id}" data-path="${entry.filePath}">
                                <i class="fas fa-trash"></i> Löschen
                            </button>
                        </div>
                    </div>
                    <div class="markdown-content">${renderedHtml}</div>`;
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error('Failed to load content:', error);
			contentPane.innerHTML = `<p class="error-message">Could not load documentation content: ${error.message}</p>`;
		}
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
						linkToDelete?.parentElement.remove();
						contentPane.innerHTML = `<p>Seite gelöscht. Wählen Sie eine andere Seite aus.</p>`;
					} else {
						throw new Error(result.message);
					}
				} catch (error) {
					showToast(`Fehler beim Löschen: ${error.message}`, 'danger');
				}
			});
		}
	});

	searchInput.addEventListener('input', (e) => {
		const searchTerm = e.target.value.toLowerCase();
		treeContainer.querySelectorAll('li').forEach(li => {
			const text = li.dataset.name.toLowerCase();
			const isMatch = text.includes(searchTerm);
			li.style.display = isMatch ? '' : 'none';
			// If it's a match, ensure its parent directories are open
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
			})
			.catch(error => {
				console.error('Failed to load wiki tree:', error);
				treeContainer.innerHTML = '<p class="error-message">Could not load wiki navigation.</p>';
			});
	};

	// --- Add Page Modal Logic ---
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
				loadTree(); // Reload the whole tree to show the new page
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast(`Fehler: ${error.message}`, 'danger');
		}
	});

	// Initial Load
	loadTree();
});