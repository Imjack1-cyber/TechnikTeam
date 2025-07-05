document.addEventListener('DOMContentLoaded', () => {
	// Transaction Modal Logic
	const transactionModal = document.getElementById('transaction-modal');
	if (transactionModal) {
		const modalTitle = document.getElementById('transaction-modal-title');
		const modalItemId = document.getElementById('transaction-item-id');
		const quantityInput = document.getElementById('transaction-quantity');
		const checkoutButton = transactionModal.querySelector('button[value="checkout"]');
		const checkinButton = transactionModal.querySelector('button[value="checkin"]');
		const closeModalBtn = transactionModal.querySelector('.modal-close-btn');

		const openModal = (btn) => {
			modalTitle.textContent = `${btn.dataset.itemName}: Entnehmen / Einräumen`;
			modalItemId.value = btn.dataset.itemId;

			const availableQty = parseInt(btn.dataset.maxQty, 10);
			const currentQty = parseInt(btn.dataset.currentQty, 10);
			const totalMaxQty = parseInt(btn.dataset.totalMaxQty, 10);
			const availableSpace = totalMaxQty > 0 ? totalMaxQty - currentQty : 9999;

			// Set initial state for checkout validation
			quantityInput.max = availableQty;
			quantityInput.title = `Maximal entnehmbar: ${availableQty}`;
			quantityInput.value = 1;

			checkoutButton.disabled = (availableQty <= 0);
			checkinButton.disabled = (totalMaxQty > 0 && currentQty >= totalMaxQty);

			// DYNAMICALLY UPDATE MAX ATTRIBUTE ON HOVER
			checkoutButton.onmouseover = () => {
				quantityInput.max = availableQty;
				quantityInput.title = `Maximal entnehmbar: ${availableQty}`;
			};

			checkinButton.onmouseover = () => {
				quantityInput.max = availableSpace > 0 ? availableSpace : 9999; // Allow large number if no max
				quantityInput.title = `Maximal einräumbar: ${availableSpace}`;
			};

			transactionModal.classList.add('active');
		};

		const closeModal = () => transactionModal.classList.remove('active');
		document.querySelectorAll('.transaction-btn').forEach(btn => btn.addEventListener('click', () => openModal(btn)));
		closeModalBtn.addEventListener('click', closeModal);
		transactionModal.addEventListener('click', e => { if (e.target === transactionModal) closeModal(); });
	}

	// Lightbox Logic
	const lightbox = document.getElementById('lightbox');
	if (lightbox) {
		const lightboxImage = document.getElementById('lightbox-image');
		const closeBtn = lightbox.querySelector('.lightbox-close');

		document.querySelectorAll('.lightbox-trigger').forEach(trigger => {
			trigger.addEventListener('click', (e) => {
				e.preventDefault();
				lightboxImage.src = trigger.dataset.src;
				lightbox.style.display = 'flex';
			});
		});

		const closeLightbox = () => {
			lightbox.style.display = 'none';
			lightboxImage.src = '';
		};

		if (closeBtn) closeBtn.addEventListener('click', closeLightbox);
		lightbox.addEventListener('click', (e) => {
			if (e.target === lightbox) {
				closeLightbox();
			}
		});
		document.addEventListener('keydown', (e) => {
			if (e.key === 'Escape' && lightbox.style.display === 'flex') {
				closeLightbox();
			}
		});
	}
});