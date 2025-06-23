<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%--
admin_meeting_edit.jsp

This JSP provides a multipart form for an administrator to edit
a specific meeting. It includes fields for meeting details and allows for
uploading and deleting file attachments.

    It is served by: AdminMeetingServlet (doGet, action=edit).
    It submits to: AdminMeetingServlet (doPost, action=update).
    Expected attributes:
        'meeting' (Meeting): The meeting object to edit.
        'attachments' (List<MeetingAttachment>): Existing attachments for the meeting.
        'allUsers' (List<User>): For the leader dropdown.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Meeting bearbeiten" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Meeting bearbeiten</h1>
<a
	href="${pageContext.request.contextPath}/admin/meetings?courseId=${meeting.courseId}"
	style="display: inline-block; margin-bottom: 1rem;"> « Zurück zur
	Meeting-Liste </a>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>
<div class="card" style="max-width: 700px; margin: 1rem auto;">
	<form action="${pageContext.request.contextPath}/admin/meetings"
		method="post" enctype="multipart/form-data">

		<input type="hidden" name="action" value="update"> <input
			type="hidden" name="courseId" value="${meeting.courseId}"> <input
			type="hidden" name="meetingId" value="${meeting.id}">

		<div class="form-group">
			<label for="name">Name des Meetings (z.B. Teil 1, Modul A:
				Ton)</label> <input type="text" id="name" name="name"
				value="${meeting.name}" required>
		</div>

		<div class="responsive-form-grid">
			<div class="form-group">
				<label for="meetingDateTime">Beginn</label> <input
					type="datetime-local" id="meetingDateTime" name="meetingDateTime"
					value="${meeting.meetingDateTime}" required>
			</div>
			<div class="form-group">
				<label for="endDateTime">Ende (optional)</label> <input
					type="datetime-local" id="endDateTime" name="endDateTime"
					value="${meeting.endDateTime}">
			</div>
		</div>

		<div class="form-group">
			<label for="leaderUserId">Leitende Person</label> <select
				name="leaderUserId" id="leaderUserId">
				<option value="">(Keine)</option>
				<c:forEach var="user" items="${allUsers}">
					<option value="${user.id}"
						${meeting.leaderUserId == user.id ? 'selected' : ''}>${user.username}</option>
				</c:forEach>
			</select>
		</div>

		<div class="form-group">
			<label for="description">Beschreibung (spezifisch für dieses
				Meeting)</label>
			<textarea id="description" name="description" rows="4">${meeting.description}</textarea>
		</div>

		<hr style="margin: 2rem 0;">

		<%-- Attachment Section --%>
		<h3>Anhänge verwalten</h3>
		<c:if test="${not empty attachments}">
			<ul class="details-list" style="margin-bottom: 1rem;">
				<c:forEach var="att" items="${attachments}">
					<li><span><a
							href="${pageContext.request.contextPath}/download?file=${att.filepath}">${att.filename}</a>
							(Rolle: ${att.requiredRole})</span>
						<form action="${pageContext.request.contextPath}/admin/meetings"
							method="post" class="js-confirm-form"
							data-confirm-message="Anhang '${att.filename}' wirklich löschen?">
							<input type="hidden" name="action" value="deleteAttachment">
							<input type="hidden" name="attachmentId" value="${att.id}">
							<button type="submit" class="btn btn-small btn-danger-outline">X</button>
						</form></li>
				</c:forEach>
			</ul>
		</c:if>

		<h4 style="margin-top: 1rem;">Neuen Anhang hochladen</h4>
		<div class="form-group">
			<label for="attachment">Datei auswählen</label> <input type="file"
				name="attachment" id="attachment" class="file-input"
				data-max-size="20971520"> <small class="file-size-warning"
				style="color: red; display: none;">Datei ist zu groß! (Max.
				20 MB)</small>
		</div>
		<div class="form-group">
			<label for="requiredRole">Sichtbar für</label> <select
				name="requiredRole" id="requiredRole">
				<option value="NUTZER" selected>Alle Nutzer</option>
				<option value="ADMIN">Nur Admins</option>
			</select>
		</div>

		<button type="submit" class="btn">Änderungen speichern &
			Anhang hochladen</button>
	</form>
</div>

<script>
// File size validation script
document.querySelectorAll('.file-input').forEach(input => {
	input.addEventListener('change', (e) => {
		const file = e.target.files[0];
		const maxSize = parseInt(e.target.dataset.maxSize, 10);
		const warningElement = e.target.nextElementSibling;

		if (file && file.size > maxSize) {
			warningElement.style.display = 'block';
			e.target.value = ''; // Clear the invalid file selection
		} else {
			warningElement.style.display = 'none';
		}
	});
});
// Custom confirmation for delete forms
document.querySelectorAll('.js-confirm-form').forEach(form => {
	form.addEventListener('submit', function(e) {
		e.preventDefault();
		const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
		showConfirmationModal(message, () => this.submit());
	});
});
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />