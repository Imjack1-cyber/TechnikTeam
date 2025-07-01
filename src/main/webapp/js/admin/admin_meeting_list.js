document.addEventListener('DOMContentLoaded', () => {
	const contextPath = "${pageContext.request.contextPath}";
    // Custom confirmation for delete forms
    document.querySelectorAll('.js-confirm-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
            showConfirmationModal(message, () => this.submit());
        });
    });

    // Modal Logic
    const modal = document.getElementById('meeting-modal');
    if (!modal) return;

    const form = document.getElementById('meeting-modal-form');
    const modalTitle = document.getElementById('meeting-modal-title');
    const actionInput = document.getElementById('meeting-action');
    const idInput = document.getElementById('meeting-id');
    const attachmentsList = document.getElementById('modal-attachments-list');
    const closeModalBtn = modal.querySelector('.modal-close-btn');

    const openModal = () => modal.classList.add('active');
    const closeModal = () => modal.classList.remove('active');

    const resetModal = () => {
        form.reset();
        attachmentsList.innerHTML = '';
    };

    // Open "Create" Modal
    document.getElementById('new-meeting-btn').addEventListener('click', () => {
        resetModal();
        modalTitle.textContent = "Neues Meeting planen";
        actionInput.value = "create";
        idInput.value = "";
        openModal();
    });

    // Open "Edit" Modal
    document.querySelectorAll('.edit-meeting-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const meetingId = btn.dataset.meetingId;
            try {
                // CORRECTED: Fetch from the correct servlet URL
                const response = await fetch(`${contextPath}/admin/meetings?action=getMeetingData&id=${meetingId}`);
                if (!response.ok) throw new Error('Meeting data could not be fetched.');
                const data = await response.json();
                const meeting = data.meetingData;
                const attachments = data.attachmentsData;

                resetModal();
                modalTitle.textContent = "Meeting bearbeiten";
                actionInput.value = "update";
                idInput.value = meeting.id;
                form.querySelector('#name-modal').value = meeting.name || '';
                form.querySelector('#location-modal').value = meeting.location || '';
                form.querySelector('#meetingDateTime-modal').value = meeting.meetingDateTime ? meeting.meetingDateTime.substring(0, 16) : '';
                form.querySelector('#endDateTime-modal').value = meeting.endDateTime ? meeting.endDateTime.substring(0, 16) : '';
                form.querySelector('#leader-modal').value = meeting.leaderUserId || '';
                form.querySelector('#description-modal').value = meeting.description || '';

                if (attachments && attachments.length > 0) {
                    attachments.forEach(att => addAttachmentRow(att, meeting.courseId));
                } else {
                    attachmentsList.innerHTML = '<li>Keine Anhänge vorhanden.</li>';
                }

                openModal();
            } catch (error) {
                console.error('Error fetching meeting data:', error);
                alert('Fehler beim Laden der Meeting-Daten.');
            }
        });
    });
	
	const addAttachmentRow = (attachment, courseId) => {
		const li = document.createElement('li');
		li.id = `attachment-item-${attachment.id}`;
		li.innerHTML = `<a href="${contextPath}/download?file=${attachment.filepath}" target="_blank">${attachment.filename}</a> (Rolle: ${attachment.requiredRole})`;
		const removeBtn = document.createElement('button');
		removeBtn.type = 'button';
		removeBtn.className = 'btn btn-small btn-danger-outline';
		removeBtn.innerHTML = '&times;';
		removeBtn.onclick = () => {
			showConfirmationModal(`Anhang '${attachment.filename}' wirklich löschen?`, () => {
				const deleteForm = document.createElement('form');
				deleteForm.method = 'post';
                // CORRECTED: Form action should point to the correct servlet URL
				deleteForm.action = `${contextPath}/admin/meetings`;
				deleteForm.innerHTML = `
					<input type="hidden" name="action" value="deleteAttachment">
					<input type="hidden" name="attachmentId" value="${attachment.id}">
					<input type="hidden" name="courseId" value="${courseId}">
				`;
				document.body.appendChild(deleteForm);
				deleteForm.submit();
			});
		};
		li.appendChild(removeBtn);
		attachmentsList.appendChild(li);
	};

    closeModalBtn.addEventListener('click', closeModal);
    modal.addEventListener('click', e => { if (e.target === modal) closeModal(); });
    document.addEventListener('keydown', e => { if (e.key === 'Escape' && modal.classList.contains('active')) closeModal(); });
	
	document.querySelectorAll('.file-input').forEach(input => {
		input.addEventListener('change', (e) => {
			const file = e.target.files[0];
			const maxSize = parseInt(e.target.dataset.maxSize, 10);
			const warningElement = e.target.nextElementSibling;
			if (file && file.size > maxSize) {
				warningElement.style.display = 'block';
				e.target.value = '';
			} else {
				warningElement.style.display = 'none';
			}
		});
	});
});