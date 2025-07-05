document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	const kitModal = document.getElementById('kit-modal');
	if (kitModal) {
		const form = kitModal.querySelector('form');
		const title = kitModal.querySelector('h3');
		const actionInput = form.querySelector('input[name="action"]');
		const idInput = form.querySelector('input[name="id"]');
		const nameInput = form.querySelector('#name-modal');
		const descInput = form.querySelector('#description-modal');
		const locationInput = form.querySelector('#location-modal');
		const closeModalBtn = kitModal.querySelector('.modal-close-btn');

		const openModal = () => kitModal.classList.add('active');
		const closeModal = () => kitModal.classList.remove('active');

		const newKitBtn = document.getElementById('new-kit-btn');
		if (newKitBtn) {
			newKitBtn.addEventListener('click', () => {
				form.reset();
				title.textContent = 'Neues Kit anlegen';
				actionInput.value = 'create';
				idInput.value = '';
				locationInput.parentElement.style.display = 'block';
				openModal();
			});
		}

		document.querySelectorAll('.edit-kit-btn').forEach(btn => {
			btn.addEventListener('click', () => {
				form.reset();
				title.textContent = 'Kit bearbeiten';
				actionInput.value = 'update';
				idInput.value = btn.dataset.kitId;
				nameInput.value = btn.dataset.kitName;
				descInput.value = btn.dataset.kitDesc;
				locationInput.value = btn.dataset.kitLocation || '';
				locationInput.parentElement.style.display = 'block';
				openModal();
			});
		});

		if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
		kitModal.addEventListener('click', e => { if (e.target === kitModal) closeModal(); });
	}

	const allItems = JSON.parse(document.getElementById('allItemsData').textContent);

	const updateQuantityMax = (selectElement) => {
		const quantityInput = selectElement.nextElementSibling;
		const selectedItemId = parseInt(selectElement.value, 10);
		const selectedItem = allItems.find(item => item.id === selectedItemId);
		if (selectedItem) {
			quantityInput.max = selectedItem.availableQuantity;
			quantityInput.title = `Maximal verfügbar: ${selectedItem.availableQuantity}`;
		} else {
			quantityInput.removeAttribute('max');
			quantityInput.title = '';
		}
	};

	document.querySelectorAll('.kit-header').forEach(header => {
		header.addEventListener('click', () => {
			const content = header.nextElementSibling;
			const icon = header.querySelector('.toggle-icon');
			const isOpening = content.style.display !== 'block';

			content.style.display = isOpening ? 'block' : 'none';
			icon.classList.toggle('fa-chevron-down', !isOpening);
			icon.classList.toggle('fa-chevron-up', isOpening);

			if (isOpening) {
				content.querySelectorAll('select[name="itemIds"]').forEach(updateQuantityMax);
			}
		});
	});

	const createItemRow = () => {
		const row = document.createElement('div');
		row.className = 'dynamic-row';

		const select = document.createElement('select');
		select.name = 'itemIds';
		select.className = 'form-group';
		select.innerHTML = '<option value="">-- Artikel auswählen --</option>' +
			allItems.map(item => `<option value="${item.id}">${item.name}</option>`).join('');

		const quantityInput = document.createElement('input');
		quantityInput.type = 'number';
		quantityInput.name = 'quantities';
		quantityInput.value = '1';
		quantityInput.min = '1';
		quantityInput.className = 'form-group';
		quantityInput.style.maxWidth = '100px';

		const removeBtn = document.createElement('button');
		removeBtn.type = 'button';
		removeBtn.className = 'btn btn-small btn-danger btn-remove-kit-item-row';
		removeBtn.title = 'Zeile entfernen';
		removeBtn.innerHTML = '×';

		row.appendChild(select);
		row.appendChild(quantityInput);
		row.appendChild(removeBtn);

		return row;
	};

	document.body.addEventListener('click', e => {
		const addBtn = e.target.closest('.btn-add-kit-item-row');
		const removeBtn = e.target.closest('.btn-remove-kit-item-row');

		if (!addBtn && !removeBtn) {
			return;
		}

		e.preventDefault();

		if (addBtn) {
			const containerId = addBtn.dataset.containerId;
			const container = document.getElementById(containerId);
			if (container) {
				const noItemsMsg = container.querySelector('.no-items-message');
				if (noItemsMsg) noItemsMsg.remove();
				container.appendChild(createItemRow());
			}
		} else if (removeBtn) {
			const row = removeBtn.closest('.dynamic-row');
			const container = row.parentElement;

			// Show confirmation modal before removing the row
			showConfirmationModal("Diesen Artikel wirklich aus dem Kit entfernen?", () => {
				row.remove();

				if (container && container.children.length === 0) {
					const p = document.createElement('p');
					p.className = 'no-items-message';
					p.textContent = 'Dieses Kit ist leer. Fügen Sie einen Artikel hinzu.';
					container.appendChild(p);
				}
			});
		}
	});

	document.body.addEventListener('change', e => {
		if (e.target.matches('select[name="itemIds"]')) {
			updateQuantityMax(e.target);
		}
	});
});