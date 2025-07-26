// src/main/webapp/js/public/lager.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const storageContainer = document.getElementById('storage-container');
	const transactionModal = document.getElementById('transaction-modal');

	if (!storageContainer || !transactionModal) return;

	const api = {
		getStorageData: () => fetch(`${contextPath}/api/v1/public/storage`).then(res => res.json()),
		postTransaction: (data) => fetch(`${contextPath}/api/v1/public/storage/transactions`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json())
	};

	const escape = (str) => {
		if (str === null || typeof str === 'undefined') return '';
		const div = document.createElement('div');
		div.innerText = str;
		return div.innerHTML;
	};

	const renderStorage = (data) => {
		storageContainer.innerHTML = '';
		const { storageData, activeEvents } = data;

		if (!storageData || Object.keys(storageData).length === 0) {
			storageContainer.innerHTML = `<div class="card"><p>Derzeit sind keine Artikel im Lager erfasst.</p></div>`;
			return;
		}

		const eventSelect = document.getElementById('transaction-eventId');
		eventSelect.innerHTML = '<option value="">Kein Event</option>' + activeEvents.map(event => `<option value="${event.id}">${escape(event.name)}</option>`).join('');

		for (const location in storageData) {
			const items = storageData[location];
			const locationCard = document.createElement('div');
			locationCard.className = 'card';

			const tableRows = items.map(item => {
				const statusBadge = item.status === 'CHECKED_OUT' ? `<span class="status-badge status-warn">Entnommen</span><span class="item-status-details">an: ${escape(item.currentHolderUsername)}</span>`
					: item.status === 'MAINTENANCE' ? `<span class="status-badge status-info">Wartung</span>`
						: `<span class="status-badge status-ok">Im Lager</span>`;
				return `
                    <tr class="${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
                        <td class="item-name-cell">
                            <a href="${contextPath}/lager/details?id=${item.id}" title="Details für ${escape(item.name)} ansehen">${escape(item.name)}</a>
                            ${item.imagePath ? `<button class="camera-btn lightbox-trigger" data-src="${contextPath}/image?file=${item.imagePath}" title="Bild anzeigen"><i class="fas fa-camera"></i></button>` : ''}
                        </td>
                        <td>${escape(item.cabinet) || '-'}</td>
                        <td>${escape(item.compartment) || '-'}</td>
                        <td>${statusBadge}</td>
                        <td>
                            <span class="inventory-details">${item.availableQuantity} / ${item.maxQuantity}</span>
                            ${item.defectiveQuantity > 0 ? `<span class="inventory-details text-danger">(${item.defectiveQuantity} defekt)</span>` : ''}
                        </td>
                        <td>
                            <button type="button" class="btn btn-small transaction-btn" data-item-id="${item.id}" data-item-name="${escape(item.name)}" data-max-qty="${item.availableQuantity}" data-current-qty="${item.quantity}" data-total-max-qty="${item.maxQuantity}">Aktion</button>
                        </td>
                    </tr>`;
			}).join('');

			locationCard.innerHTML = `
                <h2><i class="fas fa-map-marker-alt"></i> ${escape(location)}</h2>
                <div class="desktop-table-wrapper">
                    <table class="data-table">
                        <thead><tr><th>Gerät</th><th>Schrank</th><th>Fach</th><th>Status</th><th>Bestand</th><th>Aktion</th></tr></thead>
                        <tbody>${tableRows}</tbody>
                    </table>
                </div>
                <%-- Mobile view can be rendered similarly if needed --%>
            `;
			storageContainer.appendChild(locationCard);
		}
	};

	const loadStorage = async () => {
		try {
			const result = await api.getStorageData();
			if (result.success) {
				renderStorage(result.data);
			} else { throw new Error(result.message); }
		} catch (error) {
			console.error("Failed to load storage data:", error);
			storageContainer.innerHTML = `<div class="card error-message">Lagerdaten konnten nicht geladen werden.</div>`;
		}
	};

	const transactionForm = document.getElementById('transaction-form');
	transactionForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const formData = new FormData(transactionForm);
		const data = {
			itemId: parseInt(formData.get('itemId'), 10),
			quantity: parseInt(formData.get('quantity'), 10),
			type: e.submitter.value, // 'checkout' or 'checkin'
			notes: formData.get('notes'),
			eventId: formData.get('eventId') ? parseInt(formData.get('eventId'), 10) : null
		};

		try {
			const result = await api.postTransaction(data);
			if (result.success) {
				showToast(result.message, 'success');
				transactionModal.classList.remove('active');
				loadStorage(); // Refresh the view
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			showToast(error.message || 'Transaktion fehlgeschlagen.', 'danger');
		}
	});

	document.body.addEventListener('click', e => {
		const transactionBtn = e.target.closest('.transaction-btn');
		if (transactionBtn) {
			const modalTitle = document.getElementById('transaction-modal-title');
			const modalItemId = document.getElementById('transaction-item-id');
			const quantityInput = document.getElementById('transaction-quantity');
			const checkoutButton = transactionModal.querySelector('button[value="checkout"]');
			const checkinButton = transactionModal.querySelector('button[value="checkin"]');

			modalTitle.textContent = `${unescape(transactionBtn.dataset.itemName)}: Entnehmen / Einräumen`;
			modalItemId.value = transactionBtn.dataset.itemId;

			const availableQty = parseInt(transactionBtn.dataset.maxQty, 10);
			const currentQty = parseInt(transactionBtn.dataset.currentQty, 10);
			const totalMaxQty = parseInt(transactionBtn.dataset.totalMaxQty, 10);
			const availableSpace = totalMaxQty > 0 ? totalMaxQty - currentQty : 9999;

			quantityInput.max = availableQty;
			quantityInput.title = `Maximal entnehmbar: ${availableQty}`;
			quantityInput.value = 1;

			checkoutButton.disabled = (availableQty <= 0);
			checkinButton.disabled = (totalMaxQty > 0 && currentQty >= totalMaxQty);
			transactionModal.classList.add('active');
		}

		const lightboxTrigger = e.target.closest('.lightbox-trigger');
		if (lightboxTrigger) {
			e.preventDefault();
			const lightbox = document.getElementById('lightbox');
			const lightboxImage = document.getElementById('lightbox-image');
			lightboxImage.src = lightboxTrigger.dataset.src;
			lightbox.style.display = 'flex';
		}
	});

	loadStorage();
});