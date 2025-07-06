document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	const modalOverlay = document.getElementById('course-modal');
	if (!modalOverlay) return;

	const form = document.getElementById('course-modal-form');
	const title = document.getElementById('course-modal-title');
	const actionInput = document.getElementById('course-modal-action');
	const idInput = document.getElementById('course-modal-id');
	const nameInput = document.getElementById('name-modal');
	const abbrInput = document.getElementById('abbreviation-modal');
	const descInput = document.getElementById('description-modal');
	const closeModalBtn = modalOverlay.querySelector('.modal-close-btn');

	const closeModal = () => modalOverlay.classList.remove('active');

	const openCreateModal = () => {
		form.reset();
		title.textContent = "Neue Lehrgangs-Vorlage anlegen";
		actionInput.value = "create";
		idInput.value = "";
		modalOverlay.classList.add('active');
	};

	const openEditModal = async (btn) => {
		form.reset();
		title.textContent = "Lehrgangs-Vorlage bearbeiten";
		actionInput.value = "update";
		const courseId = btn.dataset.id;
		idInput.value = courseId;

		try {
			const response = await fetch(`${contextPath}/admin/lehrgaenge?action=getCourseData&id=${courseId}`);
			if (!response.ok) throw new Error('Could not fetch course data');
			const data = await response.json();

			nameInput.value = data.name || '';
			abbrInput.value = data.abbreviation || '';
			descInput.value = data.description || '';

			modalOverlay.classList.add('active');
		} catch (error) {
			console.error("Failed to open edit modal:", error);
			alert("Fehler beim Laden der Vorlagen-Daten.");
		}
	};

	const newCourseBtn = document.getElementById('new-course-btn');
	if (newCourseBtn) {
		newCourseBtn.addEventListener('click', openCreateModal);
	}

	document.querySelectorAll('.edit-course-btn').forEach(btn => {
		btn.addEventListener('click', () => openEditModal(btn));
	});

	if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
	modalOverlay.addEventListener('click', (e) => {
		if (e.target === modalOverlay) closeModal();
	});
	document.addEventListener('keydown', (e) => {
		if (e.key === 'Escape' && modalOverlay.classList.contains('active')) closeModal();
	});
});