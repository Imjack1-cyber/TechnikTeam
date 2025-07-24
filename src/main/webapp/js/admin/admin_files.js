document.addEventListener('DOMContentLoaded', () => {

	// --- Upload New Version Modal Logic ---
	const uploadModal = document.getElementById('upload-version-modal');
	if (uploadModal) {
		const modalTitle = document.getElementById('upload-modal-title');
		const modalFileIdInput = document.getElementById('upload-file-id');
		const modalFileNameSpan = document.getElementById('upload-file-name');
		const fileInput = document.getElementById('new-file-version');
		const closeModalBtn = uploadModal.querySelector('.modal-close-btn');

		document.querySelectorAll('.upload-new-version-btn').forEach(btn => {
			btn.addEventListener('click', () => {
				const fileId = btn.dataset.fileId;
				const fileName = btn.dataset.fileName;

				modalFileIdInput.value = fileId;
				modalFileNameSpan.textContent = fileName;
				fileInput.value = ''; // Reset file input
				uploadModal.classList.add('active');
			});
		});

		const closeModal = () => uploadModal.classList.remove('active');
		if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
		uploadModal.addEventListener('click', (e) => {
			if (e.target === uploadModal) closeModal();
		});
	}

	// --- Reassign File Modal Logic ---
	const reassignModal = document.getElementById('reassign-file-modal');
	if (reassignModal) {
		const modalFileIdInput = document.getElementById('reassign-file-id');
		const modalFileNameSpan = document.getElementById('reassign-file-name');
		const closeModalBtn = reassignModal.querySelector('.modal-close-btn');

		document.querySelectorAll('.reassign-file-btn').forEach(btn => {
			btn.addEventListener('click', () => {
				modalFileIdInput.value = btn.dataset.fileId;
				modalFileNameSpan.textContent = btn.dataset.fileName;
				reassignModal.classList.add('active');
			});
		});

		const closeReassignModal = () => reassignModal.classList.remove('active');
		if (closeModalBtn) closeModalBtn.addEventListener('click', closeReassignModal);
		reassignModal.addEventListener('click', (e) => {
			if (e.target === reassignModal) closeReassignModal();
		});
	}

	// --- Generic File Input Size Validation ---
	document.querySelectorAll('.file-input').forEach(input => {
		input.addEventListener('change', (e) => {
			const file = e.target.files[0];
			if (!file) return;

			const maxSize = parseInt(e.target.dataset.maxSize, 10);
			const warningElement = e.target.closest('.form-group').querySelector('.file-size-warning');

			if (file.size > maxSize) {
				if (warningElement) warningElement.style.display = 'block';
				e.target.value = ''; // Clear the invalid selection
			} else {
				if (warningElement) warningElement.style.display = 'none';
			}
		});
	});

});