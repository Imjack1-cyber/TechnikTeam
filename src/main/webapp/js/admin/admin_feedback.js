document.addEventListener('DOMContentLoaded', () => {
	// Handle status updates via AJAX
	document.querySelectorAll('.js-feedback-status-form').forEach(form => {
		form.addEventListener('submit', async (e) => {
			e.preventDefault();

			// CORRECTED: Use URLSearchParams to ensure the correct Content-Type.
			const formData = new URLSearchParams(new FormData(form));
			const actionUrl = form.getAttribute('action');

			try {
				const response = await fetch(actionUrl, {
					method: 'POST',
					headers: {
						'Content-Type': 'application/x-www-form-urlencoded',
					},
					body: formData
				});
				const result = await response.json();
				if (response.ok && result.success) {
					showToast(result.message, 'success');
					// NO reload needed. SSE will handle the UI update.
				} else {
					showToast(result.message || 'Status konnte nicht aktualisiert werden.', 'danger');
				}
			} catch (error) {
				console.error("Failed to parse JSON or network error:", error);
				showToast("Ein Server- oder Netzwerkfehler ist aufgetreten.", "danger");
			}
		});
	});

	// Handle deletions via AJAX with confirmation
	document.querySelectorAll('.js-feedback-delete-form').forEach(form => {
		form.addEventListener('submit', (e) => {
			e.preventDefault();
			showConfirmationModal('Diesen Feedback-Eintrag wirklich endgültig löschen?', async () => {

				// CORRECTED: Use URLSearchParams here as well for consistency and correctness.
				const formData = new URLSearchParams(new FormData(form));
				const actionUrl = form.getAttribute('action');

				try {
					const response = await fetch(actionUrl, {
						method: 'POST',
						headers: {
							'Content-Type': 'application/x-www-form-urlencoded',
						},
						body: formData
					});
					const result = await response.json();
					if (response.ok && result.success) {
						showToast(result.message, 'success');
						// NO DOM removal here. SSE will handle it for all clients at once.
					} else {
						showToast(result.message || 'Eintrag konnte nicht gelöscht werden.', 'danger');
					}
				} catch (error) {
					console.error("Failed to parse JSON or network error:", error);
					showToast("Ein Server- oder Netzwerkfehler ist aufgetreten.", "danger");
				}
			});
		});
	});
});