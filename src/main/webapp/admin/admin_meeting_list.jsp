<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
admin_meeting_list.jsp

This JSP displays a list of meetings for a specific parent course. An admin
can edit or delete each meeting. Creating meetings is now handled via modals,
and editing is done on a separate page to accommodate file uploads.

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
				<a
					href="${pageContext.request.contextPath}/admin/meetings?action=edit&id=${meeting.id}"
					class="btn btn-small">Bearbeiten</a>
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
					<td style="display: flex; gap: 0.5rem;"><a
						href="${pageContext.request.contextPath}/admin/meetings?action=edit&id=${meeting.id}"
						class="btn btn-small">Bearbeiten & Anhänge</a>
						<form action="${pageContext.request.contextPath}/admin/meetings"
							method="post" class="inline-form js-confirm-form"
							data-confirm-message="Meeting '${fn:escapeXml(meeting.name)}' wirklich löschen?">
							<input type="hidden" name="action" value="delete"> <input
								type="hidden" name="courseId" value="${parentCourse.id}">
							<input type="hidden" name="meetingId" value="${meeting.id}">
							<button type="submit" class="btn btn-small btn-danger">Löschen</button>
						</form></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>
<!-- MODAL FOR NEW MEETING -->
<div class="modal-overlay" id="meeting-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3 id="meeting-modal-title">Meeting planen</h3>
		<form id="meeting-modal-form"
			action="${pageContext.request.contextPath}/admin/meetings"
			method="post" enctype="multipart/form-data">

			<input type="hidden" name="action" value="create"> <input
				type="hidden" name="courseId" value="${parentCourse.id}">

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
			<div class="form-group">
				<label for="description-modal">Beschreibung</label>
				<textarea id="description-modal" name="description" rows="3"></textarea>
			</div>
			<button type="submit" class="btn">Speichern</button>
		</form>
	</div>

</div>

<!-- Confirmation Modal Structure -->
<div class="modal-overlay" id="confirmation-modal">
	<div class="modal-content" style="max-width: 450px;">
		<h3 id="confirmation-title">Bestätigung</h3>
		<p id="confirmation-message"
			style="margin: 1.5rem 0; font-size: 1.1rem;"></p>
		<div style="display: flex; justify-content: flex-end; gap: 1rem;">
			<button id="confirmation-btn-cancel" class="btn btn-secondary">Abbrechen</button>
			<button id="confirmation-btn-confirm" class="btn btn-danger">Bestätigen</button>
		</div>
	</div>
</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
	// Custom confirmation for delete forms
	document.querySelectorAll('.js-confirm-form').forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();
			const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => this.submit());
		});
	});

	// Modal Logic for "Create"
	const modal = document.getElementById('meeting-modal');
	if (!modal) return;

	const form = document.getElementById('meeting-modal-form');
	const closeModalBtn = modal.querySelector('.modal-close-btn');

	const closeModal = () => modal.classList.remove('active');

	const openCreateModal = () => {
		form.reset();
		modal.classList.add('active');
	};

	document.getElementById('new-meeting-btn').addEventListener('click', openCreateModal);

	closeModalBtn.addEventListener('click', closeModal);
	modal.addEventListener('click', e => { if (e.target === modal) closeModal(); });
	document.addEventListener('keydown', e => { if (e.key === 'Escape' && modal.classList.contains('active')) closeModal(); });
});
</script>