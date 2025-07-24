document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	const handleRequestAction = async (form) => {
		try {
			// FIX: Use URLSearchParams to ensure the correct Content-Type for the servlet.
			const formData = new URLSearchParams(new FormData(form));

			const response = await fetch(form.action, {
				method: 'POST',
				body: formData
			});
			const result = await response.json();

			if (response.ok && result.success) {
				showToast(result.message, 'success');
				const row = document.querySelector(`tr[data-request-id='${result.data.requestId}']`);
				if (row) {
					row.style.transition = 'opacity 0.5s';
					row.style.opacity = '0';
					setTimeout(() => row.remove(), 500);
				}
			} else {
				showToast(result.message || 'Ein Fehler ist aufgetreten.', 'danger');
			}
		} catch (error) {
			console.error('Error processing request action:', error);
			showToast('Ein Netzwerkfehler ist aufgetreten.', 'danger');
		}
	};

	document.querySelectorAll('.js-approve-request-form').forEach(form => {
		form.addEventListener('submit', (e) => {
			e.preventDefault();
			showConfirmationModal('Diese Änderungen wirklich genehmigen und anwenden?', () => handleRequestAction(form));
		});
	});

	document.querySelectorAll('.js-deny-request-form').forEach(form => {
		form.addEventListener('submit', (e) => {
			e.preventDefault();
			showConfirmationModal('Diese Änderungsanfrage wirklich ablehnen?', () => handleRequestAction(form));
		});
	});
});