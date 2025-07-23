document.addEventListener('DOMContentLoaded', () => {
	// Handle status updates via AJAX
	document.querySelectorAll('.js-feedback-status-form').forEach(form => {
		form.addEventListener('submit', async (e) => {
			e.preventDefault();
			const formData = new FormData(form);
			const response = await fetch(form.action, { method: 'POST', body: formData });
			const result = await response.json();
			if (response.ok && result.success) {
				showToast(result.message, 'success');
				// NO reload needed. SSE will handle the UI update.
			} else {
				showToast(result.message || 'Status konnte nicht aktualisiert werden.', 'danger');
			}
		});
	});

	// Handle deletions via AJAX with confirmation
	document.querySelectorAll('.js-feedback-delete-form').forEach(form => {
		form.addEventListener('submit', (e) => {
			e.preventDefault();
			showConfirmationModal('Diesen Feedback-Eintrag wirklich endgültig löschen?', async () => {
				const formData = new FormData(form);
				const response = await fetch(form.action, { method: 'POST', body: formData });
				const result = await response.json();
				if (response.ok && result.success) {
					showToast(result.message, 'success');
					// NO DOM removal here. SSE will handle it for all clients at once.
				} else {
					showToast(result.message || 'Eintrag konnte nicht gelöscht werden.', 'danger');
				}
			});
		});
	});
});