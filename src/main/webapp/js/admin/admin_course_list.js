document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

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

		document.getElementById('new-course-btn')?.addEventListener('click', () => {
			form.reset();
			title.textContent = "Neue Lehrgangs-Vorlage anlegen";
			actionInput.value = "create";
			idInput.value = "";
		});

		document.querySelectorAll('.edit-course-btn').forEach(btn => {
			btn.addEventListener('click', async (e) => {
				form.reset();
				title.textContent = "Lehrgangs-Vorlage bearbeiten";
				actionInput.value = "update";
				const courseId = e.currentTarget.dataset.id;
				idInput.value = courseId;

				try {
					const response = await fetch(`${contextPath}/admin/lehrgaenge?action=getCourseData&id=${courseId}`);
					if (!response.ok) throw new Error('Could not fetch course data');
					const data = await response.json();

					nameInput.value = data.name || '';
					abbrInput.value = data.abbreviation || '';
					descInput.value = data.description || '';
				} catch (error) {
					console.error("Failed to open edit modal:", error);
					alert("Fehler beim Laden der Vorlagen-Daten.");
				}
			});
		});
	}

	// --- Grant Qualifications Modal Logic ---
	const grantQualsModal = document.getElementById('grant-quals-modal');
	if (grantQualsModal) {
		const modalTitle = document.getElementById('grant-quals-modal-title');
		const courseIdInput = document.getElementById('grant-quals-course-id');

		document.querySelectorAll('.grant-quals-btn').forEach(btn => {
			btn.addEventListener('click', (e) => {
				const button = e.currentTarget;
				modalTitle.textContent = `Qualifikationen für "${button.dataset.courseName}" vergeben`;
				courseIdInput.value = button.dataset.courseId;
			});
		});
	}
});