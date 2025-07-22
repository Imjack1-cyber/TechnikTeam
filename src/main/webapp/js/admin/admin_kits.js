document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	// --- Kit Create/Edit Modal ---
	const kitModal = document.getElementById('kit-modal');
	if (kitModal) {
		const form = kitModal.querySelector('form');
		const title = kitModal.querySelector('h3');
		const actionInput = form.querySelector('input[name="action"]');
		const idInput = form.querySelector('input[name="id"]');
		const nameInput = form.querySelector('#name-modal');
		const descInput = form.querySelector('#description-modal');
		const locationInput = form.querySelector('#location-modal');

		document.getElementById('new-kit-btn')?.addEventListener('click', () => {
			form.reset();
			title.textContent = 'Neues Kit anlegen';
			actionInput.value = 'create';
			idInput.value = '';
		});

		document.querySelectorAll('.edit-kit-btn').forEach(btn => {
			btn.addEventListener('click', () => {
				form.reset();
				title.textContent = 'Kit bearbeiten';
				actionInput.value = 'update';
				idInput.value = btn.dataset.kitId;
				nameInput.value = btn.dataset.kitName;
				descInput.value = btn.dataset.kitDesc;
				locationInput.value = btn.dataset.kitLocation || '';
			});
		});
	}

	// --- Kit Content Management (Accordion & Dynamic Rows) ---
	const allItems = JSON.parse(document.getElementById('allItemsData').textContent || '[]');

	// Toggle accordion for each kit
	document.querySelectorAll('.kit-header').forEach(header => {
		header.addEventListener('click', () => {
			const content = header.nextElementSibling;
			const icon = header.querySelector('.toggle-icon');
			const isOpening = content.style.display !== 'block';

			content.style.display = isOpening ? 'block' : 'none';
			icon.classList.toggle('fa-chevron-down', !isOpening);
			icon.classList.toggle('fa-chevron-up', isOpening);
		});
	});

	/**
	 * Creates a new DOM element for an item row within a kit's content form.
	 * @param {object} item - The item to pre-populate the row with.
	 * @returns {HTMLDivElement} The new row element.
	 */
	const createItemRow = (item = { id: '', quantity: 1 }) => {
		const row = document.createElement('div');
		row.className = 'dynamic-row';

		const select = document.createElement('select');
		select.name = 'itemIds';
		select.className = 'form-group';
		select.innerHTML = '<option value="">-- Artikel auswählen --</option>' +
			allItems.map(i => `<option value="${i.id}">${i.name}</option>`).join('');
		select.value = item.id;

		const quantityInput = document.createElement('input');
		quantityInput.type = 'number';
		quantityInput.name = 'quantities';
		quantityInput.value = item.quantity;
		quantityInput.min = '1';
		quantityInput.className = 'form-group';
		quantityInput.style.maxWidth = '100px';
		quantityInput.required = true;

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

	// Event delegation for adding/removing item rows
	document.body.addEventListener('click', e => {
		const addBtn = e.target.closest('.btn-add-kit-item-row');
		const removeBtn = e.target.closest('.btn-remove-kit-item-row');

		if (addBtn) {
			e.preventDefault();
			const container = document.getElementById(addBtn.dataset.containerId);
			if (container) {
				const noItemsMsg = container.querySelector('.no-items-message');
				if (noItemsMsg) noItemsMsg.remove();
				container.appendChild(createItemRow());
			}
		} else if (removeBtn) {
			e.preventDefault();
			const row = removeBtn.closest('.dynamic-row');
			const container = row.parentElement;
			showConfirmationModal("Diesen Artikel wirklich aus dem Kit entfernen?", () => {
				row.remove();
				// If the container is now empty, show the placeholder message again.
				if (container && !container.querySelector('.dynamic-row')) {
					const p = document.createElement('p');
					p.className = 'no-items-message';
					p.textContent = 'Dieses Kit ist leer. Fügen Sie einen Artikel hinzu.';
					container.appendChild(p);
				}
			});
		}
	});
});