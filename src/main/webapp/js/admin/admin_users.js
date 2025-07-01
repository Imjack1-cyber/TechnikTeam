document.addEventListener('DOMContentLoaded', () => {
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(event) {
			event.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	const passwordAlert = document.getElementById('password-reset-alert');
	if (passwordAlert) {
		const passwordElement = passwordAlert.querySelector('strong.copyable-password');
		if(passwordElement) {
			navigator.clipboard.writeText(passwordElement.textContent)
                .then(() => console.log('Password copied to clipboard'))
                .catch(err => console.error('Failed to copy password:', err));
		}
	}

	const modal = document.getElementById('user-modal');
	const form = document.getElementById('user-modal-form');
	const title = document.getElementById('user-modal-title');
	const actionInput = form.querySelector('input[name="action"]');
	const idInput = form.querySelector('input[name="userId"]');
	const usernameInput = form.querySelector('#username-modal');
	const passwordInput = form.querySelector('#password-modal');
	const passwordGroup = form.querySelector('#password-group');
	const roleInput = form.querySelector('#role-modal');
	const classYearInput = form.querySelector('#classYear-modal');
	const classNameInput = form.querySelector('#className-modal');
	const emailInput = form.querySelector('#email-modal');
	const closeModalBtn = modal.querySelector('.modal-close-btn');

	const closeModal = () => modal.classList.remove('active');

    const newUserBtn = document.getElementById('new-user-btn');
    if(newUserBtn) {
        newUserBtn.addEventListener('click', () => {
		    form.reset();
		    title.textContent = "Neuen Benutzer anlegen";
		    actionInput.value = "create";
		    idInput.value = "";
		    passwordInput.required = true;
		    passwordGroup.style.display = 'block';
            roleInput.value = "3"; // Default to NUTZER
		    modal.classList.add('active');
		    usernameInput.focus();
	    });
    }

	document.querySelectorAll('.edit-user-btn').forEach(btn => {
		btn.addEventListener('click', async () => {
			form.reset();
			const fetchUrl = btn.dataset.fetchUrl;
			try {
				const response = await fetch(fetchUrl);
				if (!response.ok) throw new Error('Could not fetch user data');
				const data = await response.json();

				title.textContent = `Benutzer bearbeiten: ${data.username}`;
				actionInput.value = "update";
				idInput.value = data.id;
				usernameInput.value = data.username || '';
				roleInput.value = data.roleId || '3';
				classYearInput.value = data.classYear || '';
				classNameInput.value = data.className || '';
                emailInput.value = data.email || '';
				passwordInput.required = false;
				passwordGroup.style.display = 'none';
				modal.classList.add('active');
			} catch (error) {
				console.error('Failed to open edit modal:', error);
				alert('Benutzerdaten konnten nicht geladen werden.');
			}
		});
	});

	closeModalBtn.addEventListener('click', closeModal);
	modal.addEventListener('click', (event) => { if (event.target === modal) closeModal(); });
	document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && modal.classList.contains('active')) closeModal(); });
});