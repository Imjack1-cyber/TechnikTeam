document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const csrfToken = document.body.dataset.csrfToken;

	const columns = document.querySelectorAll('.feedback-list');
	if (!columns.length || typeof Sortable === 'undefined') {
		if (typeof Sortable === 'undefined') {
			console.error("Sortable.js library not loaded. Drag & drop functionality will not be available.");
		}
		return;
	}

	const modal = document.getElementById('feedback-details-modal');
	const modalForm = document.getElementById('feedback-details-form');
	const modalIdInput = document.getElementById('feedback-modal-id');
	const modalOriginalSubject = document.getElementById('feedback-modal-original-subject');
	const modalDisplayTitle = document.getElementById('feedback-modal-display-title');
	const modalContent = document.getElementById('feedback-modal-content');
	const modalStatus = document.getElementById('feedback-modal-status');

	const api = {
		async getDetails(id) {
			const response = await fetch(`${contextPath}/admin/action/feedback?action=getDetails`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: new URLSearchParams({ submissionId: id, csrfToken: csrfToken })
			});
			if (!response.ok) throw new Error('Could not fetch feedback details.');
			return response.json();
		},
		async update(formData) {
			const response = await fetch(`${contextPath}/admin/action/feedback?action=updateStatus`, {
				method: 'POST',
				body: new URLSearchParams(formData)
			});
			return response.json();
		},
		async reorder(data) {
			const response = await fetch(`${contextPath}/admin/action/feedback?action=reorder`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: new URLSearchParams({
					csrfToken: csrfToken,
					reorderData: JSON.stringify(data)
				})
			});
			return response.json();
		}
	};

	document.querySelector('.feedback-board').addEventListener('click', async (e) => {
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

			modal.classList.add('active');

		} catch (error) {
			console.error(error);
			showToast('Details konnten nicht geladen werden.', 'danger');
		}
	});

	modalForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const formData = new FormData(modalForm);
		formData.append('csrfToken', csrfToken);

		try {
			const result = await api.update(formData);
			if (result.success) {
				showToast('Änderungen gespeichert.', 'success');
				modal.classList.remove('active');
				window.location.reload(); // Reload to reflect changes
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast(error.message || 'Fehler beim Speichern.', 'danger');
		}
	});

	modal.querySelector('.modal-close-btn').addEventListener('click', () => modal.classList.remove('active'));
	modal.addEventListener('click', (e) => { if (e.target === modal) modal.classList.remove('active'); });

	const handleReorder = async (evt) => {
		const item = evt.item;
		const toList = evt.to;
		const newStatus = toList.dataset.statusId;
		const submissionId = item.dataset.id;

		item.dataset.status = newStatus;

		const reorderData = {
			submissionId: parseInt(submissionId, 10),
			newStatus: newStatus,
		};

		try {
			const result = await api.reorder(reorderData);
			if (result.success) {
				showToast(result.message, 'success');
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error('Error saving new order:', error);
			showToast(error.message || 'Sortierung konnte nicht gespeichert werden.', 'danger');
		}
	};

	columns.forEach(col => {
		new Sortable(col, {
			group: 'feedback',
			animation: 150,
			ghostClass: 'sortable-ghost',
			onEnd: handleReorder
		});
	});
});