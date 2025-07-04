document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	// Generic confirmation for all delete forms on this page
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	const kitModal = document.getElementById('kit-modal');
	if (kitModal) {
		const form = kitModal.querySelector('form');
		const title = kitModal.querySelector('h3');
		const actionInput = form.querySelector('input[name="action"]');
		const idInput = form.querySelector('input[name="id"]');
		const nameInput = form.querySelector('#name-modal');
		const descInput = form.querySelector('#description-modal');
		const closeModalBtn = kitModal.querySelector('.modal-close-btn');

		const openModal = () => kitModal.classList.add('active');
		const closeModal = () => kitModal.classList.remove('active');

		// Open "Create" modal
		const newKitBtn = document.getElementById('new-kit-btn');
		if (newKitBtn) {
			newKitBtn.addEventListener('click', () => {
				form.reset();
				title.textContent = 'Neues Kit anlegen';
				actionInput.value = 'create';
				idInput.value = '';
				openModal();
			});
		}

		// Open "Edit" modal
		document.querySelectorAll('.edit-kit-btn').forEach(btn => {
			btn.addEventListener('click', () => {
				form.reset();
				title.textContent = 'Kit bearbeiten';
				actionInput.value = 'update';
				idInput.value = btn.dataset.kitId;
				nameInput.value = btn.dataset.kitName;
				descInput.value = btn.dataset.kitDesc;
				openModal();
			});
		});

		if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
		kitModal.addEventListener('click', e => { if (e.target === kitModal) closeModal(); });
	}

	// Expand/collapse kit details
	document.querySelectorAll('.kit-header').forEach(header => {
		header.addEventListener('click', () => {
			const content = header.nextElementSibling;
			const icon = header.querySelector('.toggle-icon');
			content.style.display = content.style.display === 'block' ? 'none' : 'block';
			icon.classList.toggle('fa-chevron-down');
			icon.classList.toggle('fa-chevron-up');
		});
	});
});