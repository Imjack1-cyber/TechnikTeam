document.addEventListener('DOMContentLoaded', () => {
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	document.querySelectorAll('.file-input').forEach(input => {
		input.addEventListener('change', (e) => {
			const file = e.target.files[0];
			const maxSize = parseInt(e.target.dataset.maxSize, 10);
			const warningElement = e.target.nextElementSibling;
			if (file && file.size > maxSize) {
				warningElement.style.display = 'block';
				e.target.value = '';
			} else if (warningElement) {
				warningElement.style.display = 'none';
			}
		});
	});
});