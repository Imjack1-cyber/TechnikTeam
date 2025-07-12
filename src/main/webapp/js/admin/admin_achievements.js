document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const modal = document.getElementById('achievement-modal');
	if (!modal) return;

	const form = document.getElementById('achievement-modal-form');
	const title = document.getElementById('achievement-modal-title');
	const actionInput = document.getElementById('achievement-modal-action');
	const idInput = document.getElementById('achievement-modal-id');
	const nameInput = document.getElementById('name-modal');
	const iconInput = document.getElementById('icon-modal');
	const descInput = document.getElementById('description-modal');
	const closeModalBtn = modal.querySelector('.modal-close-btn');

	// --- Key Builder Elements for Creating New Achievements ---
	const keyBuilderGroup = document.getElementById('key-builder-group');
	const keyTypeSelect = document.getElementById('key-type-select');
	const keySubtypeGroups = document.querySelectorAll('.key-subtype-group');
	const keyNumberGroup = document.getElementById('key-number-group');
	const keyValueInput = document.getElementById('key-value-input');
	const keyCourseGroup = document.getElementById('key-course-group');
	const keyCourseSelect = document.getElementById('key-course-select');
	const keyPreview = document.getElementById('generated-key-preview');
	const hiddenKeyInput = document.getElementById('achievement-key-hidden');

	/**
	 * Dynamically builds the programmatic key for the achievement based on user selections.
	 * This ensures a consistent key format.
	 */
	const updateKey = () => {
		const type = keyTypeSelect.value;
		let generatedKey = '';

		// Hide all subtype groups initially for a clean slate
		keySubtypeGroups.forEach(group => group.style.display = 'none');

		if (type === 'EVENT_PARTICIPANT' || type === 'EVENT_LEADER') {
			keyNumberGroup.style.display = 'block';
			const value = keyValueInput.value || '1';
			generatedKey = `${type}_${value}`;
		} else if (type === 'QUALIFICATION') {
			keyCourseGroup.style.display = 'block';
			const value = keyCourseSelect.value;
			if (value) {
				generatedKey = `${type}_${value}`;
			}
		}

		keyPreview.textContent = generatedKey || '-- Bitte eine Art wählen --';
		hiddenKeyInput.value = generatedKey;
	};

	keyTypeSelect.addEventListener('change', updateKey);
	keyValueInput.addEventListener('input', updateKey);
	keyCourseSelect.addEventListener('change', updateKey);

	const closeModal = () => modal.classList.remove('active');
	const openModal = () => modal.classList.add('active');

	// --- Event Listeners for Buttons ---
	document.getElementById('new-achievement-btn').addEventListener('click', () => {
		form.reset();
		title.textContent = 'Neuen Erfolg anlegen';
		actionInput.value = 'create';
		idInput.value = '';
		keyBuilderGroup.style.display = 'block'; // Show key builder for new achievements
		updateKey(); // Initialize/reset the key builder state
		openModal();
	});

	document.querySelectorAll('.edit-achievement-btn').forEach(btn => {
		btn.addEventListener('click', async () => {
			const id = btn.dataset.id;
			try {
				const response = await fetch(`${contextPath}/admin/achievements?action=getAchievementData&id=${id}`);
				if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
				const data = await response.json();

				form.reset();
				title.textContent = 'Erfolg bearbeiten';
				actionInput.value = 'update';
				idInput.value = data.id;
				nameInput.value = data.name || '';
				iconInput.value = data.iconClass || 'fa-award';
				descInput.value = data.description || '';
				keyBuilderGroup.style.display = 'none'; // Hide key builder on edit, as the key is immutable

				openModal();
			} catch (error) {
				console.error('Failed to open edit modal:', error);
				alert("Fehler: Die Daten für diesen Erfolg konnten nicht geladen werden.");
			}
		});
	});

	// --- Confirmation for Delete Forms ---
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	// --- Modal Closing Listeners ---
	if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
	modal.addEventListener('click', (e) => {
		if (e.target === modal) closeModal();
	});
	document.addEventListener('keydown', (e) => {
		if (e.key === 'Escape' && modal.classList.contains('active')) closeModal();
	});
});