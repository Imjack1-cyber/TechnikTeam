document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	// --- Confirmation for Delete Forms ---
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	// --- Edit/Create Modal Logic ---
	const courseModal = document.getElementById('course-modal');
	if (courseModal) {
		const form = document.getElementById('course-modal-form');
		const title = document.getElementById('course-modal-title');
		const actionInput = document.getElementById('course-modal-action');
		const idInput = document.getElementById('course-modal-id');
		const nameInput = document.getElementById('name-modal');
		const abbrInput = document.getElementById('abbreviation-modal');
		const descInput = document.getElementById('description-modal');
		const closeModalBtn = courseModal.querySelector('.modal-close-btn');

		const closeModal = () => courseModal.classList.remove('active');

		const openCreateModal = () => {
			form.reset();
			title.textContent = "Neue Lehrgangs-Vorlage anlegen";
			actionInput.value = "create";
			idInput.value = "";
			courseModal.classList.add('active');
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

				courseModal.classList.add('active');
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
			btn.addEventListener('click', (e) => openEditModal(e.currentTarget));
		});

		if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
		courseModal.addEventListener('click', (e) => {
			if (e.target === courseModal) closeModal();
		});
	}

	// --- Grant Qualifications Modal Logic ---
	const grantQualsModal = document.getElementById('grant-quals-modal');
	if (grantQualsModal) {
		const modalTitle = document.getElementById('grant-quals-modal-title');
		const courseIdInput = document.getElementById('grant-quals-course-id');
		const closeBtn = grantQualsModal.querySelector('.modal-close-btn');

		document.querySelectorAll('.grant-quals-btn').forEach(btn => {
			btn.addEventListener('click', (e) => {
				const button = e.currentTarget;
				modalTitle.textContent = `Qualifikationen für "${button.dataset.courseName}" vergeben`;
				courseIdInput.value = button.dataset.courseId;
				grantQualsModal.classList.add('active');
			});
		});

		if (closeBtn) closeBtn.addEventListener('click', () => grantQualsModal.classList.remove('active'));
		grantQualsModal.addEventListener('click', (e) => {
			if (e.target === grantQualsModal) grantQualsModal.classList.remove('active');
		});
	}

	// --- Global Escape Key Listener for Modals ---
	document.addEventListener('keydown', (e) => {
		if (e.key === 'Escape') {
			if (courseModal?.classList.contains('active')) courseModal.classList.remove('active');
			if (grantQualsModal?.classList.contains('active')) grantQualsModal.classList.remove('active');
		}
	});
});