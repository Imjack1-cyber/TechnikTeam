document.addEventListener('DOMContentLoaded', () => {
    const modalOverlay = document.getElementById('attendance-modal');
    if (!modalOverlay) return;
    
    const modalTitle = document.getElementById('modal-title');
    const modalUserId = document.getElementById('modal-user-id');
    const modalMeetingId = document.getElementById('modal-meeting-id');
    const modalAttended = document.getElementById('modal-attended');
    const modalRemarks = document.getElementById('modal-remarks');
    const closeBtn = modalOverlay.querySelector('.modal-close-btn');

    const openModal = (cell) => {
        const userData = cell.dataset;
        modalTitle.textContent = `Nutzer: ${userData.userName} | Meeting: ${userData.meetingName}`;
        modalUserId.value = userData.userId;
        modalMeetingId.value = userData.meetingId;
        modalRemarks.value = userData.remarks;
        modalAttended.checked = (userData.attended === 'true');
        modalOverlay.classList.add('active');
    };

    const closeModal = () => modalOverlay.classList.remove('active');

    document.querySelectorAll('.qual-cell').forEach(cell => {
        cell.addEventListener('click', () => openModal(cell));
    });

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    modalOverlay.addEventListener('click', (event) => { if (event.target === modalOverlay) closeModal(); });
    document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && modalOverlay.classList.contains('active')) closeModal(); });
});