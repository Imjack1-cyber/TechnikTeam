document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.js-confirm-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            showConfirmationModal(this.dataset.confirmMessage || 'Sind Sie sicher?', () => this.submit());
        });
    });

    const signupModal = document.getElementById('signup-modal');
    const signupModalTitle = document.getElementById('signup-modal-title');
    const signupEventIdInput = document.getElementById('signup-event-id');
    const customFieldsContainer = document.getElementById('custom-fields-container');
    const closeModalBtn = signupModal.querySelector('.modal-close-btn');
	const contextPath = document.body.dataset.contextPath || '';

    const openSignupModal = async (btn) => {
        const eventId = btn.dataset.eventId;
        const eventName = btn.dataset.eventName;

        signupModalTitle.textContent = `Anmeldung für: ${eventName}`;
        signupEventIdInput.value = eventId;
        customFieldsContainer.innerHTML = '<p>Lade Anmelde-Optionen...</p>';
        signupModal.classList.add('active');

        try {
            const response = await fetch(`${contextPath}/api/public/event-custom-fields?eventId=${eventId}`);
            if (!response.ok) throw new Error('Could not fetch custom fields for the event.');
            
            const customFields = await response.json();
            
            customFieldsContainer.innerHTML = '';
            if (customFields && customFields.length > 0) {
                customFields.forEach(field => {
                    const fieldGroup = document.createElement('div');
                    fieldGroup.className = 'form-group';
                    let fieldHtml = `<label for="customfield_${field.id}">${field.fieldName}</label>`;
                    if (field.fieldType === 'BOOLEAN') {
                        fieldHtml += `<select name="customfield_${field.id}" id="customfield_${field.id}" class="form-control"><option value="true">Ja</option><option value="false">Nein</option></select>`;
                    } else { 
                        fieldHtml += `<input type="text" name="customfield_${field.id}" id="customfield_${field.id}" class="form-control">`;
                    }
                    fieldGroup.innerHTML = fieldHtml;
                    customFieldsContainer.appendChild(fieldGroup);
                });
            } else {
                 customFieldsContainer.innerHTML = '<p>Für dieses Event sind keine weiteren Angaben nötig.</p>';
            }
        } catch (error) {
            console.error('Failed to load custom fields:', error);
            customFieldsContainer.innerHTML = '<p class="error-message">Fehler beim Laden der Anmelde-Optionen.</p>';
        }
    };
    
    document.querySelectorAll('.signup-btn').forEach(btn => btn.addEventListener('click', () => openSignupModal(btn)));
    closeModalBtn.addEventListener('click', () => signupModal.classList.remove('active'));
    signupModal.addEventListener('click', (e) => {
        if (e.target === signupModal) signupModal.classList.remove('active');
    });
});