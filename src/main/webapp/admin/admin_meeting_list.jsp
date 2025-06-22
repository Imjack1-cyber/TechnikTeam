<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
admin_meeting_list.jsp

This JSP displays a list of meetings for a specific parent course. An admin
can edit or delete each meeting. Creating and editing meetings is now handled via modals.

    It is served by: AdminMeetingServlet (doGet).

    Expected attributes:

        'parentCourse' (de.technikteam.model.Course): The course whose meetings are being listed.

        'meetings' (List<de.technikteam.model.Meeting>): The list of meetings for this course.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Meetings für ${parentCourse.name}" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Meetings für "${parentCourse.name}"</h1>
<a href="${pageContext.request.contextPath}/admin/courses"
	style="margin-bottom: 1rem; display: inline-block;"> &laquo; Zurück
	zu allen Vorlagen </a>

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
			data-searchable-content="${meeting.name} ${meeting.leader}">
			<h3 class="card-title">${meeting.name}</h3>
			<div class="card-row">
				<span>Datum:</span> <span>${meeting.formattedMeetingDateTimeRange}</span>
			</div>
			<div class="card-row">
				<span>Leitung:</span> <span>${empty meeting.leader ? 'N/A' : meeting.leader}</span>
			</div>
			<div class="card-actions">
				<button type="button" class="btn btn-small edit-meeting-btn"
					data-id="${meeting.id}" data-name="${meeting.name}"
					data-meeting-date-time="${meeting.meetingDateTime}"
					data-end-date-time="${meeting.endDateTime}"
					data-leader="${meeting.leader}"
					data-description="${meeting.description}">Bearbeiten</button>
				<form action="${pageContext.request.contextPath}/admin/meetings"
					method="post" style="display: inline;">
					<input type="hidden" name="action" value="delete"> <input
						type="hidden" name="courseId" value="${parentCourse.id}">
					<input type="hidden" name="meetingId" value="${meeting.id}">
					<button type="submit" class="btn btn-small btn-danger"
						onclick="return confirm('Meeting \'${meeting.name}\' wirklich löschen?')">Löschen</button>
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
					<td>${meeting.name}</td>
					<td>${meeting.formattedMeetingDateTimeRange}</td>
					<td>${empty meeting.leader ? 'N/A' : meeting.leader}</td>
					<td style="display: flex; gap: 0.5rem;">
						<button type="button" class="btn btn-small edit-meeting-btn"
							data-id="${meeting.id}" data-name="${meeting.name}"
							data-meeting-date-time="${meeting.meetingDateTime}"
							data-end-date-time="${meeting.endDateTime}"
							data-leader="${meeting.leader}"
							data-description="${meeting.description}">Bearbeiten</button>
						<form action="${pageContext.request.contextPath}/admin/meetings"
							method="post" style="display: inline;">
							<input type="hidden" name="action" value="delete"> <input
								type="hidden" name="courseId" value="${parentCourse.id}">
							<input type="hidden" name="meetingId" value="${meeting.id}">
							<button type="submit" class="btn btn-small btn-danger"
								onclick="return confirm('Meeting \'${meeting.name}\' wirklich löschen?')">Löschen</button>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>
<!-- MODAL FOR NEW/EDIT MEETING -->
<div class="modal-overlay" id="meeting-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3 id="meeting-modal-title">Meeting</h3>
		<form id="meeting-modal-form"
			action="${pageContext.request.contextPath}/admin/meetings"
			method="post" enctype="multipart/form-data">

			<input type="hidden" name="action" id="meeting-modal-action">
			<input type="hidden" name="courseId" value="${parentCourse.id}">
			<input type="hidden" name="meetingId" id="meeting-modal-id">

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
				<label for="leader-modal">Leitende Person</label> <input type="text"
					id="leader-modal" name="leader">
			</div>
			<div class="form-group">
				<label for="description-modal">Beschreibung</label>
				<textarea id="description-modal" name="description" rows="3"></textarea>
			</div>
			<button type="submit" class="btn">Speichern</button>
		</form>
	</div>

</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
const modal = document.getElementById('meeting-modal');
if (!modal) return;

const form = document.getElementById('meeting-modal-form');
const title = document.getElementById('meeting-modal-title');
const actionInput = document.getElementById('meeting-modal-action');
const idInput = document.getElementById('meeting-modal-id');
const nameInput = document.getElementById('name-modal');
const startInput = document.getElementById('meetingDateTime-modal');
const endInput = document.getElementById('endDateTime-modal');
const leaderInput = document.getElementById('leader-modal');
const descInput = document.getElementById('description-modal');

const closeModalBtn = modal.querySelector('.modal-close-btn');

const closeModal = () => modal.classList.remove('active');

const openCreateModal = () => {
form.reset();
title.textContent = 'Neues Meeting für "${parentCourse.name}" planen';
actionInput.value = 'create';
idInput.value = '';
modal.classList.add('active');
};

const openEditModal = (btn) => {
form.reset();
const data = btn.dataset;
title.textContent = "Meeting bearbeiten";
actionInput.value = 'update';
idInput.value = data.id;
nameInput.value = data.name;
startInput.value = data.meetingDateTime || '';
endInput.value = data.endDateTime || '';
leaderInput.value = data.leader;
descInput.value = data.description;
modal.classList.add('active');
};

document.getElementById('new-meeting-btn').addEventListener('click', openCreateModal);
document.querySelectorAll('.edit-meeting-btn').forEach(btn => {
btn.addEventListener('click', () => openEditModal(btn));
});

closeModalBtn.addEventListener('click', closeModal);
modal.addEventListener('click', e => { if (e.target === modal) closeModal(); });
document.addEventListener('keydown', e => { if (e.key === 'Escape' && modal.classList.contains('active')) closeModal(); });
});
</script>