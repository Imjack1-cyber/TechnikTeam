document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const csrfToken = document.body.dataset.csrfToken;
	const apiUrl = `${contextPath}/api/admin/todos`;

	const categoriesContainer = document.getElementById('todo-categories-container');
	const categoryTemplate = document.getElementById('category-template');
	const taskTemplate = document.getElementById('task-template');
	const newCategoryForm = document.getElementById('new-category-form');
	const newCategoryNameInput = document.getElementById('new-category-name');

	// --- API HELPER ---
	const api = {
		async get() {
			const response = await fetch(apiUrl);
			if (!response.ok) throw new Error('Failed to fetch data');
			return response.json();
		},
		async post(action, body) {
			const response = await fetch(`${apiUrl}?action=${action}`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: body
			});
			return response.json();
		},
		async put(payload) {
			const response = await fetch(apiUrl, {
				method: 'PUT',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ ...payload, csrfToken })
			});
			return response.json();
		},
		async delete(resource, id) {
			const response = await fetch(`${apiUrl}?${resource}Id=${id}&csrfToken=${csrfToken}`, {
				method: 'DELETE'
			});
			return response.json();
		}
	};

	// --- RENDERING LOGIC ---
	const renderTask = (task) => {
		const taskClone = taskTemplate.content.cloneNode(true);
		const taskItem = taskClone.querySelector('.task-item');
		const checkbox = taskClone.querySelector('.task-checkbox');
		const contentSpan = taskClone.querySelector('.task-content');

		taskItem.dataset.taskId = task.id;
		contentSpan.textContent = task.content;
		checkbox.checked = task.isCompleted;
		if (task.isCompleted) {
			taskItem.classList.add('completed');
		}

		return taskClone;
	};

	const renderCategory = (category) => {
		const categoryClone = categoryTemplate.content.cloneNode(true);
		const categoryEl = categoryClone.querySelector('.todo-category');
		const title = categoryClone.querySelector('.category-title');
		const taskList = categoryClone.querySelector('.task-list');

		categoryEl.dataset.categoryId = category.id;
		title.textContent = category.name;

		category.tasks.forEach(task => {
			taskList.appendChild(renderTask(task));
		});

		initTaskSortable(taskList);
		return categoryClone;
	};

	const loadAndRender = async () => {
		try {
			const categories = await api.get();
			categoriesContainer.innerHTML = '';
			categories.forEach(cat => {
				categoriesContainer.appendChild(renderCategory(cat));
			});
		} catch (error) {
			console.error(error);
			showToast('Fehler beim Laden der To-Do-Liste.', 'danger');
		}
	};

	// --- EVENT HANDLERS ---
	newCategoryForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const name = newCategoryNameInput.value.trim();
		if (!name) return;

		try {
			const result = await api.post('createCategory', new URLSearchParams({ name, csrfToken }));
			if (result.success) {
				categoriesContainer.appendChild(renderCategory({ ...result.data, tasks: [] }));
				newCategoryNameInput.value = '';
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast('Kategorie konnte nicht erstellt werden.', 'danger');
		}
	});

	categoriesContainer.addEventListener('submit', async (e) => {
		if (e.target.matches('.new-task-form')) {
			e.preventDefault();
			const form = e.target;
			const contentInput = form.querySelector('.new-task-content');
			const content = contentInput.value.trim();
			const categoryId = form.closest('.todo-category').dataset.categoryId;

			if (!content || !categoryId) return;

			try {
				const result = await api.post('createTask', new URLSearchParams({ categoryId, content, csrfToken }));
				if (result.success) {
					form.previousElementSibling.appendChild(renderTask(result.data));
					contentInput.value = '';
				} else {
					throw new Error(result.message);
				}
			} catch (error) {
				showToast('Aufgabe konnte nicht erstellt werden.', 'danger');
			}
		}
	});

	categoriesContainer.addEventListener('click', async (e) => {
		if (e.target.matches('.delete-category-btn, .delete-category-btn *')) {
			const categoryEl = e.target.closest('.todo-category');
			showConfirmationModal('Kategorie und alle Aufgaben darin löschen?', async () => {
				try {
					await api.delete('category', categoryEl.dataset.categoryId);
					categoryEl.remove();
				} catch {
					showToast('Kategorie konnte nicht gelöscht werden.', 'danger');
				}
			});
		}
		if (e.target.matches('.delete-task-btn, .delete-task-btn *')) {
			const taskEl = e.target.closest('.task-item');
			showConfirmationModal('Aufgabe wirklich löschen?', async () => {
				try {
					await api.delete('task', taskEl.dataset.taskId);
					taskEl.remove();
				} catch {
					showToast('Aufgabe konnte nicht gelöscht werden.', 'danger');
				}
			});
		}
	});

	categoriesContainer.addEventListener('change', async (e) => {
		if (e.target.matches('.task-checkbox')) {
			const taskEl = e.target.closest('.task-item');
			taskEl.classList.toggle('completed', e.target.checked);
			try {
				await api.put({
					action: 'updateTask',
					taskId: taskEl.dataset.taskId,
					isCompleted: e.target.checked
				});
			} catch {
				showToast('Status konnte nicht gespeichert werden.', 'danger');
				e.target.checked = !e.target.checked;
				taskEl.classList.toggle('completed', e.target.checked);
			}
		}
	});

	categoriesContainer.addEventListener('dblclick', (e) => {
		if (e.target.matches('.task-content, .category-title')) {
			const element = e.target;
			const originalText = element.textContent;
			const input = document.createElement('input');
			input.type = 'text';
			input.value = originalText;
			input.className = element.className;

			element.replaceWith(input);
			input.focus();

			const saveChanges = async () => {
				const newText = input.value.trim();
				element.textContent = newText || originalText;
				input.replaceWith(element);

				if (newText && newText !== originalText) {
					try {
						if (element.matches('.task-content')) {
							await api.put({ action: 'updateTask', taskId: element.closest('.task-item').dataset.taskId, content: newText });
						}
					} catch {
						showToast('Änderung konnte nicht gespeichert werden.', 'danger');
						element.textContent = originalText;
					}
				}
			};
			input.addEventListener('blur', saveChanges);
			input.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') input.blur(); });
		}
	});

	// --- SORTABLEJS DRAG & DROP LOGIC ---
	const saveOrder = async () => {
		const orderData = {
			categoryOrder: Array.from(categoriesContainer.children).map(c => c.dataset.categoryId)
		};
		document.querySelectorAll('.task-list').forEach(list => {
			const catId = list.closest('.todo-category').dataset.categoryId;
			orderData[`category-${catId}`] = Array.from(list.children).map(task => task.dataset.taskId);
		});

		try {
			await api.put({ action: 'reorder', orderData });
			showToast('Sortierung gespeichert.', 'success');
		} catch {
			showToast('Sortierung konnte nicht gespeichert werden.', 'danger');
			loadAndRender(); // Revert to server state on failure
		}
	};

	const initTaskSortable = (listEl) => {
		new Sortable(listEl, {
			group: 'tasks',
			animation: 150,
			ghostClass: 'sortable-ghost',
			onEnd: saveOrder
		});
	};

	new Sortable(categoriesContainer, {
		animation: 150,
		ghostClass: 'sortable-ghost',
		handle: '.category-header',
		onEnd: saveOrder
	});

	// --- INITIAL LOAD ---
	loadAndRender();
});