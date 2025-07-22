document.addEventListener('DOMContentLoaded', () => {
	const defectModal = document.getElementById('defect-modal');
	if (!defectModal) return;

	const modalTitle = document.getElementById('defect-modal-title');
	const itemIdInput = document.getElementById('defect-item-id');
	const defectQtyInput = document.getElementById('defective_quantity');
	const reasonInput = document.getElementById('defect_reason');

	/**
	 * Attaches an event listener to all "Status bearbeiten" buttons to open the defect modal.
	 */
	document.querySelectorAll('.defect-modal-btn').forEach(btn => {
		btn.addEventListener('click', () => {
			// Populate the modal with data from the clicked button's data attributes
			modalTitle.textContent = `Defekt-Status für "${btn.dataset.itemName}" bearbeiten`;
			itemIdInput.value = btn.dataset.itemId;
			defectQtyInput.value = btn.dataset.currentDefectQty;
			defectQtyInput.max = btn.dataset.maxQty; // Set max value to prevent invalid input
			reasonInput.value = btn.dataset.currentReason;
		});
	});
});