<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
admin_meeting_list.jsp

This JSP displays a list of meetings for a specific parent course. An admin
can edit or delete each meeting. Creating and editing meetings is now handled via modals.

    It is served by: AdminMeetingServlet (doGet).

    Expected attributes:
        'parentCourse' (de.technikteam.model.Course): The course whose meetings are being listed.
        'meetings' (List<de.technikteam.model.Meeting>): The list of meetings for this course.
        'allUsers' (List<de.technikteam.model.User>): For the leader dropdown in the modal.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Meetings für ${parentCourse.name}" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>
	Meetings für "
	<c:out value="${parentCourse.name}" />
	"
</h1>
<a href="${pageContext.request.contextPath}/admin/courses"
	style="margin-bottom: 1rem; display: inline-block;"> « Zurück zu
	allen Vorlagen </a>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">
		<c:out value="${sessionScope.successMessage}" />
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">
		<c:out value="${sessionScope.errorMessage}" />
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>
<div class="table-controls">
	<button type="button" class="btn" id="new-meeting-btn">Neues
		Meeting planen</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<c:if test="${empty meetings}">
	<div class="card">
		<p>Für diesen Lehrgang wurden noch keine Meetings geplant.</p>
	</div>
</c:if>
<!-- MOBILE LAYOUT -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="meeting" items="${meetings}">
		<div class="list-item-card"
			data-searchable-content="<c:out value='${meeting.name}'/> <c:out value='${meeting.leaderUsername}'/>">
			<h3 class="card-title">
				<c:out value="${meeting.name}" />
			</h3>
			<div class="card-row">
				<span>Datum:</span> <span><c:out
						value="${meeting.formattedMeetingDateTimeRange}" /></span>
			</div>
			<div class="card-row">
				<span>Leitung:</span> <span><c:out
						value="${empty meeting.leaderUsername ? 'N/A' : meeting.leaderUsername}" /></span>
			</div>
			<div class="card-actions">
				<button type="button" class="btn btn-small edit-meeting-btn"
					data-meeting-id="${meeting.id}">Bearbeiten</button>
				<form action="${pageContext.request.contextPath}/admin/meetings"
					method="post" class="inline-form js-confirm-form"
					data-confirm-message="Meeting '${fn:escapeXml(meeting.name)}' wirklich löschen?">
					<input type="hidden" name="action" value="delete"> <input
						type="hidden" name="courseId" value="${parentCourse.id}">
					<input type="hidden" name="meetingId" value="${meeting.id}">
					<button type="submit" class="btn btn-small btn-danger">Löschen</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>
<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Meeting-Name</th>
				<th class="sortable" data-sort-type="string">Datum & Uhrzeit</th>
				<th class="sortable" data-sort-type="string">Leitung</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="meeting" items="${meetings}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/meetingDetails?id=${meeting.id}"><c:out
								value="${meeting.name}" /></a></td>
					<td><c:out value="${meeting.formattedMeetingDateTimeRange}" /></td>
					<td><c:out
							value="${empty meeting.leaderUsername ? 'N/A' : meeting.leaderUsername}" /></td>
					<td style="display: flex; gap: 0.5rem;">
						<button type="button" class="btn btn-small edit-meeting-btn"
							data-meeting-id="${meeting.id}">Bearbeiten & Anhänge</button>
						<form action="${pageContext.request.contextPath}/admin/meetings"
							method="post" class="inline-form js-confirm-form"
							data-confirm-message="Meeting '${fn:escapeXml(meeting.name)}' wirklich löschen?">
							<input type="hidden" name="action" value="delete"> <input
								type="hidden" name="courseId" value="${parentCourse.id}">
							<input type="hidden" name="meetingId" value="${meeting.id}">
							<button type="submit" class="btn btn-small btn-danger">Löschen</button>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>
<!-- MODAL FOR CREATE/EDIT MEETING -->
<div class="modal-overlay" id="meeting-modal">
	<div class="modal-content" style="max-width: 700px;">
		<button class="modal-close-btn">×</button>
		<h3 id="meeting-modal-title">Meeting</h3>
		<form id="meeting-modal-form"
			action="${pageContext.request.contextPath}/admin/meetings"
			method="post" enctype="multipart/form-data">

			<input type="hidden" name="action" id="meeting-action"> <input
				type="hidden" name="courseId" value="${parentCourse.id}"> <input
				type="hidden" name="id" id="meeting-id">

			<div class="form-group">
				<label for="name-modal">Name des Meetings (z.B. Teil 1)</label> <input
					type="text" id="name-modal" name="name" required>
			</div>
			<div class="responsive-form-grid">
				<div class="form-group">
					<label for="meetingDateTime-modal">Beginn</label> <input
						type="datetime-local" id="meetingDateTime-modal"
						name="meetingDateTime" required>
				</div>
				<div class="form-group">
					<label for="endDateTime-modal">Ende (optional)</label> <input
						type="datetime-local" id="endDateTime-modal" name="endDateTime">
				</div>
			</div>
			<div class="responsive-form-grid">
				<div class="form-group">
					<label for="location-modal">Ort</label> <input type="text"
						id="location-modal" name="location">
				</div>
				<div class="form-group">
					<label for="leader-modal">Leitende Person</label> <select
						name="leaderUserId" id="leader-modal">
						<option value="">(Keine)</option>
						<c:forEach var="user" items="${allUsers}">
							<option value="${user.id}"><c:out
									value="${user.username}" /></option>
						</c:forEach>
					</select>
				</div>
			</div>
			<div class="form-group">
				<label for="description-modal">Beschreibung</label>
				<textarea id="description-modal" name="description" rows="3"></textarea>
			</div>

			<div class="card"
				style="margin-top: 1rem; padding: 1rem; background-color: var(--bg-color);">
				<h4 class="card-title" style="border: none; padding: 0;">Anhänge</h4>
				<ul id="modal-attachments-list" class="details-list"
					style="margin-bottom: 1rem;"></ul>
				<div class="form-group">
					<label for="attachment-modal">Neuen Anhang hochladen</label> <input
						type="file" name="attachment" id="attachment-modal"
						class="file-input" data-max-size="20971520"> <small
						class="file-size-warning" style="color: red; display: none;">Datei
						ist zu groß! (Max. 20 MB)</small>
				</div>
				<div class="form-group">
					<label for="requiredRole-modal">Sichtbar für</label> <select
						name="requiredRole" id="requiredRole-modal">
						<option value="NUTZER" selected>Alle Nutzer</option>
						<option value="ADMIN">Nur Admins</option>
					</select>
				</div>
			</div>
			<button type="submit" class="btn" style="margin-top: 1.5rem;">Speichern</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<script>
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
                    attachments.forEach(att => {
                        const li = document.createElement('li');
                        li.id = `attachment-item-${att.id}`;
                        li.innerHTML = `<a href="${contextPath}/download?file=${att.filepath}" target="_blank">${att.filename}</a> (Rolle: ${att.requiredRole})`;
                        const removeBtn = document.createElement('button');
						removeBtn.type = 'button';
						removeBtn.className = 'btn btn-small btn-danger-outline';
						removeBtn.innerHTML = '&times;';
						removeBtn.onclick = () => {
							showConfirmationModal(`Anhang '${att.filename}' wirklich löschen?`, () => {
								const deleteForm = document.createElement('form');
								deleteForm.method = 'post';
								deleteForm.action = `${contextPath}/admin/meetings`;
								deleteForm.innerHTML = `
									<input type="hidden" name="action" value="deleteAttachment">
									<input type="hidden" name="attachmentId" value="${att.id}">
									<input type="hidden" name="courseId" value="${meeting.courseId}">
								`;
								document.body.appendChild(deleteForm);
								deleteForm.submit();
							});
						};
						li.appendChild(removeBtn);
                        attachmentsList.appendChild(li);
                    });
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
</script>