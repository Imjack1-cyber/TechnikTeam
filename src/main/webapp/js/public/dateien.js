document.addEventListener('DOMContentLoaded', () => {
	const uploadModal = document.getElementById('upload-version-modal');
	if (!uploadModal) return;

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

	closeModalBtn.addEventListener('click', closeModal);
	uploadModal.addEventListener('click', (e) => {
		if (e.target === uploadModal) closeModal();
	});
});