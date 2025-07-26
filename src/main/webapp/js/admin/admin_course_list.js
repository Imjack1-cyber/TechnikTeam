// src/main/webapp/js/admin/admin_course_list.js
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = document.body.dataset.contextPath || '';
    const tableBody = document.querySelector('.searchable-table tbody');
    const mobileList = document.querySelector('.mobile-card-list.searchable-table');
    const courseModal = document.getElementById('course-modal');

    if (!tableBody || !mobileList || !courseModal) return;

    // --- API Abstraction for v1 ---
    const api = {
        getAll: () => fetch(`${contextPath}/api/v1/courses`).then(res => res.json()),
        getOne: (id) => fetch(`${contextPath}/api/v1/courses/${id}`).then(res => res.json()),
        create: (data) => fetch(`${contextPath}/api/v1/courses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(res => res.json()),
        update: (id, data) => fetch(`${contextPath}/api/v1/courses/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(res => res.json()),
        delete: (id) => fetch(`${contextPath}/api/v1/courses/${id}`, { method: 'DELETE' }).then(res => res.json())
    };

    // --- Modal Element References ---
    const form = document.getElementById('course-modal-form');
    const title = document.getElementById('course-modal-title');
    const actionInput = document.getElementById('course-modal-action');
    const idInput = document.getElementById('course-modal-id');
    const nameInput = document.getElementById('name-modal');
    const abbrInput = document.getElementById('abbreviation-modal');
    const descInput = document.getElementById('description-modal');

    /**
     * Renders both the desktop table and mobile card view from course data.
     * @param {Array} courses - Array of course objects.
     */
    const renderTable = (courses) => {
        tableBody.innerHTML = '';
        mobileList.innerHTML = '';

        if (!courses || courses.length === 0) {
            const noDataRow = `<tr><td colspan="3" style="text-align: center;">Es wurden noch keine Lehrgangs-Vorlagen erstellt.</td></tr>`;
            const noDataCard = `<div class="card"><p>Es wurden noch keine Lehrgangs-Vorlagen erstellt.</p></div>`;
            tableBody.innerHTML = noDataRow;
            mobileList.innerHTML = noDataCard;
            return;
        }

        courses.forEach(course => {
            const actionsHtml = `
                <a href="${contextPath}/admin/meetings?courseId=${course.id}" class="btn btn-small">
                    <i class="fas fa-calendar-day"></i> Meetings
                </a>
                <button type="button" class="btn btn-small btn-secondary edit-course-btn" data-id="${course.id}">
                    <i class="fas fa-edit"></i> Bearbeiten
                </button>
                <button type="button" class="btn btn-small btn-danger delete-course-btn" data-id="${course.id}" data-name="${escape(course.name)}">
                    <i class="fas fa-trash"></i> Löschen
                </button>`;
            
            // Desktop Row
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${escape(course.name)}</td>
                <td>${escape(course.abbreviation)}</td>
                <td style="display: flex; gap: 0.5rem; flex-wrap: wrap;">${actionsHtml}</td>`;
            tableBody.appendChild(row);

            // Mobile Card
            const card = document.createElement('div');
            card.className = 'list-item-card';
            card.innerHTML = `
                <h3 class="card-title">${escape(course.name)}</h3>
                <div class="card-row">
                    <span>Abkürzung:</span> <strong>${escape(course.abbreviation)}</strong>
                </div>
                <div class="card-actions">${actionsHtml}</div>`;
            mobileList.appendChild(card);
        });
    };

    /**
     * Fetches all courses from the API and triggers rendering.
     */
    const loadCourses = async () => {
        try {
            const result = await api.getAll();
            if (result.success) {
                renderTable(result.data);
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error("Failed to load courses:", error);
            tableBody.innerHTML = `<tr><td colspan="3" class="error-message">Fehler beim Laden der Daten.</td></tr>`;
        }
    };
    
    // --- Modal Logic ---
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const courseId = idInput.value;
        const isUpdate = !!courseId;

        const data = {
            name: nameInput.value,
            abbreviation: abbrInput.value,
            description: descInput.value
        };

        try {
            const result = isUpdate ? await api.update(courseId, data) : await api.create(data);
            if (result.success) {
                showToast(result.message, 'success');
                courseModal.classList.remove('active');
                loadCourses();
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            showToast(error.message || 'Speichern fehlgeschlagen.', 'danger');
        }
    });

    // --- Event Delegation for table/list actions ---
    document.body.addEventListener('click', async (e) => {
        const editBtn = e.target.closest('.edit-course-btn');
        const deleteBtn = e.target.closest('.delete-course-btn');
        const newBtn = e.target.closest('#new-course-btn');

        if (newBtn) {
            form.reset();
			title.textContent = "Neue Lehrgangs-Vorlage anlegen";
			actionInput.value = "create";
			idInput.value = "";
            courseModal.classList.add('active');
        }

        if (editBtn) {
            const courseId = editBtn.dataset.id;
            try {
                const result = await api.getOne(courseId);
                if (!result.success) throw new Error(result.message);
                
                const data = result.data;
                form.reset();
                title.textContent = "Lehrgangs-Vorlage bearbeiten";
                actionInput.value = "update";
                idInput.value = data.id;
                nameInput.value = data.name || '';
                abbrInput.value = data.abbreviation || '';
                descInput.value = data.description || '';
                courseModal.classList.add('active');
            } catch (error) {
                console.error("Failed to open edit modal:", error);
                showToast("Fehler beim Laden der Vorlagen-Daten.", 'danger');
            }
        }

        if (deleteBtn) {
            const courseId = deleteBtn.dataset.id;
            const courseName = unescape(deleteBtn.dataset.name);
            showConfirmationModal(`Vorlage '${courseName}' wirklich löschen? Alle zugehörigen Meetings und Qualifikationen werden auch gelöscht!`, async () => {
                try {
                    const result = await api.delete(courseId);
                    if (result.success) {
                        showToast(result.message, 'success');
                        loadCourses();
                    } else {
                        throw new Error(result.message);
                    }
                } catch(error) {
                    showToast(error.message || 'Löschen fehlgeschlagen.', 'danger');
                }
            });
        }
    });

    // Simple helper to escape HTML entities
    const escape = (str) => {
        if (!str) return '';
        return str.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>').replace(/"/g, '"').replace(/'/g, ''');
    };

	// Initial Load
	loadCourses();
});