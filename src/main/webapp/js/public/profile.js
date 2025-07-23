document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const profileForm = document.getElementById('profile-form');
	if (!profileForm) return;

	const editBtn = document.getElementById('edit-profile-btn');
	const submitBtn = document.getElementById('submit-profile-btn');
	const cancelBtn = document.getElementById('cancel-edit-btn');

	const editableFields = profileForm.querySelectorAll('.editable-field');
	const originalValues = {};

	const toggleEditMode = (isEditing) => {
		editableFields.forEach(field => {
			field.readOnly = !isEditing;
			if (!isEditing) {
				field.style.backgroundColor = '';
				field.style.border = '1px solid transparent';
			} else {
				field.style.backgroundColor = 'var(--bg-color)';
				field.style.border = '1px solid var(--border-color)';
			}
		});

		// Defensive check: only manipulate buttons if they exist
		if (editBtn) editBtn.style.display = isEditing ? 'none' : 'inline-flex';
		if (submitBtn) submitBtn.style.display = isEditing ? 'inline-flex' : 'none';
		if (cancelBtn) cancelBtn.style.display = isEditing ? 'inline-flex' : 'none';

		if (isEditing) {
			editableFields.forEach(input => {
				originalValues[input.name] = input.value;
			});
		}
	};

	// Initialize view mode
	toggleEditMode(false);
	profileForm.querySelector('input[name="username"]').style.border = '1px solid transparent';

	if (editBtn) {
		editBtn.addEventListener('click', () => toggleEditMode(true));
	}

	if (cancelBtn) {
		cancelBtn.addEventListener('click', () => {
			editableFields.forEach(input => {
				input.value = originalValues[input.name];
			});
			toggleEditMode(false);
		});
	}

	profileForm.addEventListener('submit', async (e) => {
		e.preventDefault();

		const body = new URLSearchParams(new FormData(profileForm));
		const actionUrl = profileForm.getAttribute('action');

		try {
			const response = await fetch(actionUrl, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded',
				},
				body: body
			});

			const contentType = response.headers.get("content-type");
			if (contentType && contentType.includes("application/json")) {
				const result = await response.json();
				if (response.ok && result.success) {
					showToast(result.message, 'success');
					toggleEditMode(false);
					setTimeout(() => window.location.reload(), 1500);
				} else {
					showToast(result.message || 'Ein Fehler ist aufgetreten.', 'danger');
				}
			} else {
				const text = await response.text();
				console.error("Server returned non-JSON response:", text);
				throw new Error("Server did not return a valid JSON response. Status: " + response.status);
			}
		} catch (error) {
			console.error('Error submitting profile change request:', error);
			showToast('Ein Netzwerkfehler oder Serverfehler ist aufgetreten.', 'danger');
		}
	});
});