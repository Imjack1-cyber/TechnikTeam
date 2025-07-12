document.addEventListener('DOMContentLoaded', () => {
	const modalOverlay = document.getElementById('attendance-modal');
	if (!modalOverlay) return;

	// Get references to all modal elements once
	const modalTitle = document.getElementById('modal-title');
	const modalUserId = document.getElementById('modal-user-id');
	const modalMeetingId = document.getElementById('modal-meeting-id');
	const modalAttended = document.getElementById('modal-attended');
	const modalRemarks = document.getElementById('modal-remarks');
	const closeBtn = modalOverlay.querySelector('.modal-close-btn');

	/**
	 * Opens the attendance modal and populates it with data from the clicked cell.
	 * @param {HTMLElement} cell The table cell that was clicked.
	 */
	const openModal = (cell) => {
		const userData = cell.dataset;
		modalTitle.textContent = `Nutzer: ${userData.userName} | Meeting: ${userData.meetingName}`;
		modalUserId.value = userData.userId;
		modalMeetingId.value = userData.meetingId;
		modalRemarks.value = userData.remarks;
		// The value from dataset is a string, so we need to compare it to 'true'
		modalAttended.checked = (userData.attended === 'true');
		modalOverlay.classList.add('active');
	};

	/**
	 * Closes the attendance modal.
	 */
	const closeModal = () => modalOverlay.classList.remove('active');

	// Attach click listener to all qualification cells to open the modal
	document.querySelectorAll('.qual-cell').forEach(cell => {
		cell.addEventListener('click', (e) => openModal(e.currentTarget));
	});

	// --- Modal Closing Listeners ---
	if (closeBtn) closeBtn.addEventListener('click', closeModal);

	// Close modal if the overlay background is clicked
	modalOverlay.addEventListener('click', (event) => {
		if (event.target === modalOverlay) closeModal();
	});

	// Close modal on Escape key press
	document.addEventListener('keydown', (event) => {
		if (event.key === 'Escape' && modalOverlay.classList.contains('active')) {
			closeModal();
		}
	});
});