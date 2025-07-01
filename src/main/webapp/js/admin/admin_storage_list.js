document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.js-confirm-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
            showConfirmationModal(message, () => this.submit());
        });
    });

    // --- Lightbox Logic ---
    const lightbox = document.getElementById('lightbox');
    if (lightbox) {
        const lightboxImage = lightbox.querySelector('img');
        const closeBtn = lightbox.querySelector('.lightbox-close');

        document.querySelectorAll('.lightbox-trigger').forEach(trigger => {
            trigger.addEventListener('click', (e) => {
                e.preventDefault();
                lightbox.style.display = 'block';
                lightboxImage.src = trigger.dataset.src;
            });
        });

        const closeLightbox = () => { lightbox.style.display = 'none'; };
        if (closeBtn) closeBtn.addEventListener('click', closeLightbox);
        lightbox.addEventListener('click', (e) => { if(e.target === lightbox) { closeLightbox(); } });
        document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && lightbox.style.display === 'block') closeLightbox(); });
    }

    // --- Edit/Create Modal Logic ---
    const itemModal = document.getElementById('item-modal');
    if (itemModal) {
        const form = itemModal.querySelector('form');
        const title = itemModal.querySelector('h3');
        const actionInput = form.querySelector('input[name="action"]');
        const idInput = form.querySelector('input[name="id"]');
        const closeModalBtn = itemModal.querySelector('.modal-close-btn');

        document.getElementById('new-item-btn').addEventListener('click', () => {
            form.reset();
            title.textContent = 'Neuen Lagerartikel anlegen';
            actionInput.value = 'create';
            itemModal.classList.add('active');
        });

        document.querySelectorAll('.edit-item-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                form.reset();
                const fetchUrl = btn.dataset.fetchUrl;
                try {
                    const response = await fetch(fetchUrl);
                    if (!response.ok) throw new Error('Could not fetch item data.');
                    const itemData = await response.json();

                    title.textContent = 'Lagerartikel bearbeiten';
                    actionInput.value = 'update';
                    idInput.value = itemData.id;
                    form.querySelector('#name-modal').value = itemData.name || '';
                    form.querySelector('#location-modal').value = itemData.location || '';
                    form.querySelector('#cabinet-modal').value = itemData.cabinet || '';
                    form.querySelector('#compartment-modal').value = itemData.compartment || '';
                    form.querySelector('#quantity-modal').value = itemData.quantity;
                    form.querySelector('#maxQuantity-modal').value = itemData.maxQuantity;
                    form.querySelector('#weight_kg-modal').value = itemData.weightKg || '';
                    form.querySelector('#price_eur-modal').value = itemData.priceEur || '';
                    itemModal.classList.add('active');
                } catch (error) {
                    console.error("Failed to open edit modal:", error);
                    alert("Fehler beim Laden der Artikeldaten.");
                }
            });
        });
        closeModalBtn.addEventListener('click', () => itemModal.classList.remove('active'));
    }

    // --- Defect Modal Logic ---
    const defectModal = document.getElementById('defect-modal');
    if (defectModal) {
        const modalTitle = defectModal.querySelector('h3');
        const itemIdInput = defectModal.querySelector('#defect-item-id');
        const defectQtyInput = defectModal.querySelector('#defective_quantity');
        const reasonInput = defectModal.querySelector('#defect_reason');
        const closeModalBtn = defectModal.querySelector('.modal-close-btn');

        document.querySelectorAll('.defect-modal-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                modalTitle.textContent = `Defekt-Status für "${btn.dataset.itemName}" bearbeiten`;
                itemIdInput.value = btn.dataset.itemId;
                defectQtyInput.value = btn.dataset.currentDefectQty;
                defectQtyInput.max = btn.dataset.maxQty;
                reasonInput.value = btn.dataset.currentReason;
                defectModal.classList.add('active');
            });
        });
        closeModalBtn.addEventListener('click', () => defectModal.classList.remove('active'));
    }
});