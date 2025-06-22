<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
admin_meeting_form.jsp

This JSP provides a multipart form for an administrator to edit
a specific meeting. It includes fields for meeting details and allows for
uploading and deleting file attachments. Creating a new meeting is handled
by a modal on admin_meeting_list.jsp.

    It is served by: AdminMeetingServlet (doGet, action=edit).

    It submits to: AdminMeetingServlet (doPost, action=update).

    Expected attributes:

        'parentCourse' (Course): The parent course of this meeting.

        'meeting' (Meeting): The meeting object to edit.

        'attachments' (List<MeetingAttachment>): Existing attachments for the meeting.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Meeting bearbeiten" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Meeting bearbeiten</h1>
<a
	href="${pageContext.request.contextPath}/admin/meetings?courseId=${parentCourse.id}"
	style="display: inline-block; margin-bottom: 1rem;"> « Zurück zur
	Meeting-Liste </a>

<c:if test="

        
notemptysessionScope.successMessage">
	<pclass="success−message">notemptysessionScope.successMessage"><pclass="success−message">



	{sessionScope.successMessage}
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="

        
notemptysessionScope.errorMessage">
	<pclass="error−message">notemptysessionScope.errorMessage"><pclass="error−message">



	{sessionScope.errorMessage}
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>
<div class="card" style="max-width: 700px; margin: 1rem auto;">
	<form action="${pageContext.request.contextPath}/admin/meetings"
		method="post" enctype="multipart/form-data">
		<input type="hidden" name="action" value="update"> <input
			type="hidden" name="courseId" value="${parentCourse.id}"> <input
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
			<label for="leader">Leitende Person</label> <input type="text"
				id="leader" name="leader" value="${meeting.leader}">
		</div>

		<div class="form-group">
			<label for="description">Beschreibung (spezifisch für dieses
				Meeting)</label>
			<textarea id="description" name="description" rows="4">${meeting.description}</textarea>
		</div>

		<%-- File Upload Section --%>
		<div class="card" style="background-color: var(--bg-color);">
			<h3 class="card-title" style="border: none; padding: 0;">Anhänge
				verwalten</h3>

			<c:if test="${not empty attachments}">
				<ul style="list-style: none; padding-left: 0; margin-bottom: 1rem;">
					<c:forEach var="att" items="${attachments}">
						<li
							style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0; border-bottom: 1px solid var(--border-color);">
							<span><a
								href="${pageContext.request.contextPath}/download?file=${att.filepath}">${att.filename}</a></span>
							<%-- This form is NOT multipart, so it submits to the else block in doPost --%>
							<form action="${pageContext.request.contextPath}/admin/meetings"
								method="post"
								onsubmit="return confirm('Anhang \'${att.filename}\' wirklich löschen?')">
								<input type="hidden" name="action" value="deleteAttachment">
								<input type="hidden" name="attachmentId" value="${att.id}">
								<input type="hidden" name="meetingId" value="${meeting.id}">
								<input type="hidden" name="courseId" value="${parentCourse.id}">
								<button type="submit" class="btn btn-small btn-danger">X</button>
							</form>
						</li>
					</c:forEach>
				</ul>
			</c:if>

			<h4 style="margin-top: 1rem;">Neuen Anhang hochladen</h4>
			<div class="form-group">
				<label for="attachment">Datei</label> <input type="file"
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
		</div>

		<div style="display: flex; gap: 1rem; margin-top: 1.5rem;">
			<button type="submit" class="btn">Änderungen speichern</button>
			<a
				href="${pageContext.request.contextPath}/admin/meetings?courseId=${parentCourse.id}"
				class="btn" style="background-color: var(--text-muted-color);">Abbrechen</a>
		</div>
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
</script>
<c:import url="/WEB-INF/jspf/footer.jspf" />