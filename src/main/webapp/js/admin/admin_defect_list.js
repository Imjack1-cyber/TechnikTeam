document.addEventListener('DOMContentLoaded', () => {
    const defectModal = document.getElementById('defect-modal');
    if (!defectModal) return;

    const modalTitle = document.getElementById('defect-modal-title');
    const itemIdInput = document.getElementById('defect-item-id');
    const defectQtyInput = document.getElementById('defective_quantity');
    const reasonInput = document.getElementById('defect_reason');
    const closeModalBtn = defectModal.querySelector('.modal-close-btn');

    document.querySelectorAll('.defect-modal-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            modalTitle.textContent = `Defekt-Status für "${btn.dataset.itemName}" bearbeiten`;
            itemIdInput.value = btn.dataset.itemId;
            defectQtyInput.value = btn.dataset.currentDefectQty;
            defectQtyInput.max = btn.dataset.maxQty; // Set max based on total quantity
            reasonInput.value = btn.dataset.currentReason;
            defectModal.classList.add('active');
        });
    });

    closeModalBtn.addEventListener('click', () => defectModal.classList.remove('active'));
    defectModal.addEventListener('click', (e) => {
        if (e.target === defectModal) defectModal.classList.remove('active');
    });
});