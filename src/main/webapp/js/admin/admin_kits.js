// src/main/webapp/js/admin/admin_kits.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const kitsContainer = document.getElementById('kits-container');
	const kitModal = document.getElementById('kit-modal');

	if (!kitsContainer || !kitModal) return;

	// --- API Abstraction for v1 ---
	const api = {
		getAll: () => fetch(`${contextPath}/api/v1/kits`).then(res => res.json()),
		create: (data) => fetch(`${contextPath}/api/v1/kits`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json()),
		updateMeta: (id, data) => fetch(`${contextPath}/api/v1/kits/${id}`, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		}).then(res => res.json()),
		updateItems: (id, items) => fetch(`${contextPath}/api/v1/kits/${id}/items`, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(items)
		}).then(res => res.json()),
		delete: (id) => fetch(`${contextPath}/api/v1/kits/${id}`, { method: 'DELETE' }).then(res => res.json())
	};

	// --- Data Initialization from embedded JSON ---
	const allItems = JSON.parse(document.getElementById('allItemsData').textContent || '[]');
	const allSelectableItems = JSON.parse(document.getElementById('allSelectableItemsData').textContent || '[]');

	// --- Modal Element References ---
	const modalForm = kitModal.querySelector('form');
	const modalTitle = kitModal.querySelector('h3');
	const modalActionInput = modalForm.querySelector('input[name="action"]');
	const modalIdInput = modalForm.querySelector('input[name="id"]');
	const modalNameInput = modalForm.querySelector('#name-modal');
	const modalDescInput = modalForm.querySelector('#description-modal');
	const modalLocationInput = modalForm.querySelector('#location-modal');

	/**
	 * Creates a single accordion element for a kit.
	 * @param {object} kit - The kit data object.
	 * @returns {HTMLElement} The created kit container element.
	 */
	const createKitElement = (kit) => {
		const container = document.createElement('div');
		container.className = 'kit-container';
		container.style = "border-bottom: 1px solid var(--border-color); padding-bottom: 1.5rem; margin-bottom: 1.5rem;";

		const absoluteActionUrl = `${window.location.protocol}//${window.location.host}${contextPath}/pack-kit?kitId=${kit.id}`;
		const qrApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(absoluteActionUrl)}`;

		const itemsHtml = (kit.items && kit.items.length > 0)
			? kit.items.map(item => `
                <div class="dynamic-row">
                    <select name="itemIds" class="form-group" required>
                        ${allSelectableItems.map(i => `<option value="${i.id}" ${i.id === item.itemId ? 'selected' : ''}>${escape(i.name)}</option>`).join('')}
                    </select>
                    <input type="number" name="quantities" value="${item.quantity}" min="1" class="form-group" style="max-width: 100px;" required>
                    <button type="button" class="btn btn-small btn-danger btn-remove-kit-item-row" title="Zeile entfernen">×</button>
                </div>`).join('')
			: '<p class="no-items-message">Dieses Kit ist leer. Fügen Sie einen Artikel hinzu.</p>';

		container.innerHTML = `
            <div class="kit-header">
                <div>
                    <h3><i class="fas fa-chevron-down toggle-icon"></i> ${escape(kit.name)}</h3>
                    <p style="margin: -0.5rem 0 0 1.75rem; color: var(--text-muted-color);">${escape(kit.description)}</p>
                </div>
                <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    <a href="${qrApiUrl}" target="_blank" class="btn btn-small">QR-Code</a>
                    <button type="button" class="btn btn-small btn-secondary edit-kit-btn" data-kit-id="${kit.id}" data-kit-name="${escape(kit.name)}" data-kit-desc="${escape(kit.description)}" data-kit-location="${escape(kit.location)}">Bearbeiten</button>
                    <button type="button" class="btn btn-small btn-danger delete-kit-btn" data-kit-id="${kit.id}" data-kit-name="${escape(kit.name)}">Löschen</button>
                </div>
            </div>
            <div class="kit-content" style="display: none; padding-left: 2rem; margin-top: 1rem;">
                <form class="update-kit-items-form" data-kit-id="${kit.id}">
                    <h4>Inhalt bearbeiten</h4>
                    <div id="kit-items-container-${kit.id}" class="kit-items-container">${itemsHtml}</div>
                    <div style="margin-top: 1rem; display: flex; justify-content: space-between; align-items: center;">
                        <button type="button" class="btn btn-small btn-add-kit-item-row" data-container-id="kit-items-container-${kit.id}">
                            <i class="fas fa-plus"></i> Zeile hinzufügen
                        </button>
                        <button type="submit" class="btn btn-success">
                            <i class="fas fa-save"></i> Kit-Inhalt speichern
                        </button>
                    </div>
                </form>
            </div>`;
		return container;
	};

	/**
	 * Renders all kits fetched from the API.
	 * @param {Array} kits - The array of kit objects.
	 */
	const renderKits = (kits) => {
		kitsContainer.innerHTML = '';
		if (!kits || kits.length === 0) {
			kitsContainer.innerHTML = '<div class="card"><p>Es wurden noch keine Kits erstellt.</p></div>';
			return;
		}
		kits.forEach(kit => {
			kitsContainer.appendChild(createKitElement(kit));
		});
	};

	/**
	 * Fetches kit data from the API and renders the view.
	 */
	const loadKits = async () => {
		kitsContainer.innerHTML = '<div class="card"><p>Lade Kits...</p></div>';
		try {
			const result = await api.getAll();
			if (result.success) {
				renderKits(result.data);
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			kitsContainer.innerHTML = `<div class="card error-message">Fehler beim Laden der Kits: ${error.message}</div>`;
		}
	};

	// --- Event Delegation for the entire page ---
	document.body.addEventListener('click', async e => {
		// Kit Header Accordion
		const header = e.target.closest('.kit-header');
		if (header) {
			const content = header.nextElementSibling;
			const icon = header.querySelector('.toggle-icon');
			const isOpening = content.style.display !== 'block';
			content.style.display = isOpening ? 'block' : 'none';
			icon.classList.toggle('fa-chevron-down', !isOpening);
			icon.classList.toggle('fa-chevron-up', isOpening);
		}

		// New Kit Button
		const newBtn = e.target.closest('#new-kit-btn');
		if (newBtn) {
			modalForm.reset();
			modalTitle.textContent = 'Neues Kit anlegen';
			modalActionInput.value = 'create';
			modalIdInput.value = '';
			kitModal.classList.add('active');
		}

		// Edit Kit Metadata Button
		const editBtn = e.target.closest('.edit-kit-btn');
		if (editBtn) {
			modalForm.reset();
			modalTitle.textContent = 'Kit bearbeiten';
			modalActionInput.value = 'update';
			modalIdInput.value = editBtn.dataset.kitId;
			modalNameInput.value = unescape(editBtn.dataset.kitName);
			modalDescInput.value = unescape(editBtn.dataset.kitDesc);
			modalLocationInput.value = unescape(editBtn.dataset.kitLocation);
			kitModal.classList.add('active');
		}

		// Delete Kit Button
		const deleteBtn = e.target.closest('.delete-kit-btn');
		if (deleteBtn) {
			const kitId = deleteBtn.dataset.kitId;
			const kitName = unescape(deleteBtn.dataset.kitName);
			showConfirmationModal(`Kit '${kitName}' wirklich löschen?`, async () => {
				try {
					const result = await api.delete(kitId);
					if (result.success) {
						showToast(result.message, 'success');
						loadKits();
					} else { throw new Error(result.message); }
				} catch (error) {
					showToast(error.message || 'Löschen fehlgeschlagen.', 'danger');
				}
			});
		}

		// Add/Remove Item Rows in Kit Content
		const addRowBtn = e.target.closest('.btn-add-kit-item-row');
		if (addRowBtn) {
			const container = document.getElementById(addRowBtn.dataset.containerId);
			if (container) container.appendChild(createItemRow());
		}
		const removeRowBtn = e.target.closest('.btn-remove-kit-item-row');
		if (removeRowBtn) {
			removeRowBtn.closest('.dynamic-row').remove();
		}
	});

	// --- Form Submissions ---

	// Create/Update Kit Metadata
	modalForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const kitId = modalIdInput.value;
		const isUpdate = !!kitId;
		const data = {
			name: modalNameInput.value,
			description: modalDescInput.value,
			location: modalLocationInput.value
		};

		try {
			const result = isUpdate ? await api.updateMeta(kitId, data) : await api.create(data);
			if (result.success) {
				showToast(result.message, 'success');
				kitModal.classList.remove('active');
				loadKits();
			} else { throw new Error(result.message); }
		} catch (error) {
			showToast(error.message || 'Speichern fehlgeschlagen.', 'danger');
		}
	});

	// Update Kit Items (delegated submit handler)
	document.body.addEventListener('submit', async (e) => {
		if (e.target.matches('.update-kit-items-form')) {
			e.preventDefault();
			const form = e.target;
			const kitId = form.dataset.kitId;

			const items = [];
			form.querySelectorAll('.dynamic-row').forEach(row => {
				const itemId = row.querySelector('select[name="itemIds"]').value;
				const quantity = row.querySelector('input[name="quantities"]').value;
				if (itemId && quantity) {
					items.push({ itemId: parseInt(itemId, 10), quantity: parseInt(quantity, 10) });
				}
			});

			try {
				const result = await api.updateItems(kitId, items);
				if (result.success) {
					showToast(result.message, 'success');
				} else { throw new Error(result.message); }
			} catch (error) {
				showToast(error.message || 'Inhalt konnte nicht gespeichert werden.', 'danger');
			}
		}
	});

	const escape = (str) => {
		if (!str) return '';
		return str.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>').replace(/"/g, '"').replace(/'/g, ''');
    };

	const createItemRow = (item = { id: '', quantity: 1 }) => {
		const row = document.createElement('div');
		row.className = 'dynamic-row';
		const select = document.createElement('select');
		select.name = 'itemIds';
		select.className = 'form-group';
		select.innerHTML = '<option value="">-- Artikel auswählen --</option>' + allSelectableItems.map(i => `<option value="${i.id}">${escape(i.name)}</option>`).join('');
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

	// Initial Load
	loadKits();
});