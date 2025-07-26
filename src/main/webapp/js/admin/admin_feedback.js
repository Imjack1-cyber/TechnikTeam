// src/main/webapp/js/admin/admin_feedback.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const boardContainer = document.getElementById('feedback-board-container');

	if (!boardContainer || typeof Sortable === 'undefined') {
		if (typeof Sortable === 'undefined') {
			console.error("Sortable.js library not loaded. Drag & drop functionality will not be available.");
		}
		return;
	}

	// --- Modal Element References ---
	const modal = document.getElementById('feedback-details-modal');
	const modalForm = document.getElementById('feedback-details-form');
	const modalIdInput = document.getElementById('feedback-modal-id');
	const modalOriginalSubject = document.getElementById('feedback-modal-original-subject');
	const modalDisplayTitle = document.getElementById('feedback-modal-display-title');
	const modalContent = document.getElementById('feedback-modal-content');
	const modalStatus = document.getElementById('feedback-modal-status');
	const deleteBtn = document.getElementById('feedback-modal-delete-btn');

	// --- API Abstraction for v1 ---
	const api = {
		getBoard: () => fetch(`${contextPath}/api/v1/feedback`).then(res => res.json()),
		getDetails: (id) => fetch(`${contextPath}/api/v1/feedback/${id}`).then(res => res.json()),
		update: (id, data) => fetch(`${contextPath}/api/v1/feedback/${id}`, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json()),
		reorder: (data) => fetch(`${contextPath}/api/v1/feedback/reorder`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json()),
		delete: (id) => fetch(`${contextPath}/api/v1/feedback/${id}`, {
			method: 'DELETE'
		}).then(res => res.json())
	};

	/**
	 * Creates a single feedback card element.
	 * @param {object} submission - The feedback submission data.
	 * @returns {HTMLElement} The created card element.
	 */
	const createCardElement = (submission) => {
		const card = document.createElement('div');
		card.className = 'feedback-card-item';
		card.dataset.id = submission.id;
		card.innerHTML = `
            <strong class="subject">${submission.displayTitle || submission.subject}</strong>
            <p class="content-preview">${submission.content}</p>
            <p class="meta">
                Von: <strong>${submission.username}</strong> am ${new Date(submission.submittedAt).toLocaleDateString('de-DE')}
            </p>`;
		return card;
	};

	/**
	 * Renders the entire feedback board from API data.
	 * @param {object} data - The full board state from the API.
	 */
	const renderBoard = (data) => {
		boardContainer.innerHTML = ''; // Clear existing board
		data.statusOrder.forEach(status => {
			const column = document.createElement('div');
			column.className = 'feedback-column';

			const title = document.createElement('h2');
			title.textContent = status;

			const list = document.createElement('div');
			list.className = 'feedback-list';
			list.dataset.statusId = status;

			const submissions = data.groupedSubmissions[status] || [];
			submissions.forEach(submission => {
				list.appendChild(createCardElement(submission));
			});

			column.appendChild(title);
			column.appendChild(list);
			boardContainer.appendChild(column);
		});

		// Re-initialize SortableJS on the new elements
		initializeSortable();
	};

	/**
	 * Fetches board data from the API and renders it.
	 */
	const loadBoard = async () => {
		boardContainer.innerHTML = '<p>Loading board...</p>';
		try {
			const result = await api.getBoard();
			if (result.success) {
				renderBoard(result.data);
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error('Failed to load feedback board:', error);
			boardContainer.innerHTML = `<p class="error-message">Could not load board: ${error.message}</p>`;
		}
	};

	/**
	 * Handles the reordering after a drag & drop action.
	 */
	const handleReorder = async (evt) => {
		const item = evt.item;
		const toList = evt.to;
		const newStatus = toList.dataset.statusId;
		const submissionId = item.dataset.id;

		const reorderData = {
			submissionId: parseInt(submissionId, 10),
			newStatus: newStatus,
			orderedIds: Array.from(toList.children).map(child => parseInt(child.dataset.id, 10))
		};

		try {
			const result = await api.reorder(reorderData);
			if (result.success) {
				showToast('Board updated successfully.', 'success');
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error('Error saving new order:', error);
			showToast(error.message || 'Could not save new order.', 'danger');
			loadBoard(); // Revert to server state on error
		}
	};

	/**
	 * Initializes the SortableJS library on all columns.
	 */
	const initializeSortable = () => {
		document.querySelectorAll('.feedback-list').forEach(col => {
			new Sortable(col, {
				group: 'feedback',
				animation: 150,
				ghostClass: 'sortable-ghost',
				onEnd: handleReorder
			});
		});
	};

	// --- Event Listeners ---

	// Open Details Modal
	boardContainer.addEventListener('click', async (e) => {
		const card = e.target.closest('.feedback-card-item');
		if (!card) return;

		try {
			const submissionId = card.dataset.id;
			const result = await api.getDetails(submissionId);
			if (!result.success) throw new Error(result.message);

			const submission = result.data;
			modalIdInput.value = submission.id;
			modalOriginalSubject.textContent = submission.subject;
			modalDisplayTitle.value = submission.displayTitle || '';
			modalContent.textContent = submission.content;
			if (window.renderMarkdown) window.renderMarkdown(modalContent);
			modalStatus.value = submission.status;
			deleteBtn.dataset.id = submission.id;

			modal.classList.add('active');
		} catch (error) {
			console.error(error);
			showToast('Details could not be loaded.', 'danger');
		}
	});

	// Submit Modal Form (Update)
	modalForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const submissionId = modalIdInput.value;
		const data = {
			status: modalStatus.value,
			displayTitle: modalDisplayTitle.value
		};

		try {
			const result = await api.update(submissionId, data);
			if (result.success) {
				showToast('Changes saved.', 'success');
				modal.classList.remove('active');
				loadBoard(); // Refresh the board to show changes
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast(error.message || 'Error saving changes.', 'danger');
		}
	});

	// Delete Button in Modal
	deleteBtn.addEventListener('click', () => {
		const submissionId = deleteBtn.dataset.id;
		showConfirmationModal('Delete this feedback permanently?', async () => {
			try {
				const result = await api.delete(submissionId);
				if (result.success) {
					showToast('Feedback deleted.', 'success');
					modal.classList.remove('active');
					loadBoard(); // Refresh board
				} else {
					throw new Error(result.message);
				}
			} catch (error) {
				showToast(error.message || 'Error deleting feedback.', 'danger');
			}
		});
	});

	// --- Initial Load ---
	loadBoard();
});