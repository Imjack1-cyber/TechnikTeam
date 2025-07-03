document.addEventListener('DOMContentLoaded', () => {
    const transactionModal = document.getElementById('transaction-modal');
    if (transactionModal) {
        const modalTitle = document.getElementById('transaction-modal-title');
        const modalItemId = document.getElementById('transaction-item-id');
        const closeModalBtn = transactionModal.querySelector('.modal-close-btn');

        const openModal = (btn) => {
            modalTitle.textContent = `${btn.dataset.itemName}: Entnehmen / Einräumen`;
            modalItemId.value = btn.dataset.itemId;
            transactionModal.classList.add('active');
        };

        const closeModal = () => transactionModal.classList.remove('active');
        document.querySelectorAll('.transaction-btn').forEach(btn => btn.addEventListener('click', () => openModal(btn)));
        closeModalBtn.addEventListener('click', closeModal);
        transactionModal.addEventListener('click', e => { if (e.target === transactionModal) closeModal(); });
    }
});