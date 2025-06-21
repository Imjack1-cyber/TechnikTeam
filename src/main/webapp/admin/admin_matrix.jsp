<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Qualifikations-Matrix" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Qualifikations-Matrix (Modular)</h1>
<p>Klicken Sie auf eine Zelle, um die Teilnahme an einem Meeting zu
	bearbeiten.</p>
<%-- ... Feedback Messages ... --%>

<!-- MOBILE LAYOUT -->
<div class="mobile-matrix-wrapper card">
	<table class="mobile-matrix-table">
		<thead>
			<tr>
				<th>Nutzer</th>
				<c:forEach var="course" items="${allCourses}">
					<c:forEach var="meeting" items="${meetingsByCourse[course.id]}">
						<th>${course.abbreviation}<br />${meeting.name}</th>
					</c:forEach>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="user" items="${allUsers}">
				<tr>
					<td>${user.username}</td>
					<c:forEach var="course" items="${allCourses}">
						<c:forEach var="meeting" items="${meetingsByCourse[course.id]}">
							<c:set var="attendance"
								value="${attendanceMap[user.id.toString().concat('-').concat(meeting.id)]}" />

							<%-- Data for the modal is attached directly to each cell --%>
							<td class="qual-cell" data-user-id="${user.id}"
								data-user-name="${user.username}"
								data-meeting-id="${meeting.id}"
								data-meeting-name="${course.name} - ${meeting.name}"
								data-attended="${not empty attendance && attendance.attended}"
								data-remarks="${not empty attendance ? attendance.remarks : ''}">

								<c:if test="${not empty attendance && attendance.attended}">
									<span class="text-success">✔</span>
								</c:if> <c:if test="${empty attendance || !attendance.attended}">-</c:if>
							</td>
						</c:forEach>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table matrix-table">
		<thead>
			<tr>
				<th rowspan="2" style="vertical-align: middle;">Nutzer /
					Lehrgang ↓</th>
				<c:forEach var="course" items="${allCourses}">
					<th colspan="${meetingsByCourse[course.id].size()}"
						style="text-align: center;"><a
						href="${pageContext.request.contextPath}/admin/courses?action=edit&id=${course.id}">${course.abbreviation}</a></th>
				</c:forEach>
			</tr>
			<tr>
				<c:forEach var="course" items="${allCourses}">
					<c:forEach var="meeting" items="${meetingsByCourse[course.id]}">
						<th style="text-align: center;"><a
							href="${pageContext.request.contextPath}/admin/meetings?action=edit&courseId=${course.id}&meetingId=${meeting.id}">${meeting.name}</a></th>
					</c:forEach>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="user" items="${allUsers}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/admin/users?action=details&id=${user.id}">${user.username}</a></td>
					<c:forEach var="course" items="${allCourses}">
						<c:forEach var="meeting" items="${meetingsByCourse[course.id]}">
							<c:set var="attendance"
								value="${attendanceMap[user.id.toString().concat('-').concat(meeting.id)]}" />

							<%-- Data for the modal is attached directly to each cell --%>
							<td class="qual-cell" data-user-id="${user.id}"
								data-user-name="${user.username}"
								data-meeting-id="${meeting.id}"
								data-meeting-name="${course.name} - ${meeting.name}"
								data-attended="${not empty attendance && attendance.attended}"
								data-remarks="${not empty attendance ? attendance.remarks : ''}"
								style="text-align: center; font-weight: bold; cursor: pointer;"
								title="Klicken zum Bearbeiten"><c:if
									test="${not empty attendance && attendance.attended}">
									<span class="text-success">✔</span>
								</c:if> <c:if test="${empty attendance || !attendance.attended}">-</c:if>
							</td>
						</c:forEach>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- MODAL HTML STRUCTURE -->
<div class="modal-overlay" id="attendance-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3>Teilnahme bearbeiten</h3>
		<p id="modal-title" style="font-weight: bold; margin-bottom: 1rem;"></p>
		<form action="${pageContext.request.contextPath}/admin/attendance"
			method="post">
			<input type="hidden" name="action" value="updateMeetingAttendance">
			<input type="hidden" name="returnTo" value="matrix"> <input
				type="hidden" name="userId" id="modal-user-id"> <input
				type="hidden" name="meetingId" id="modal-meeting-id">
			<div class="form-group"
				style="display: flex; align-items: center; gap: 1rem;">
				<label for="modal-attended" style="margin-bottom: 0;">Teilgenommen:</label>
				<input type="checkbox" id="modal-attended" name="attended"
					value="true" style="width: auto; height: 1.5rem;">
			</div>
			<div class="form-group">
				<label for="modal-remarks">Bemerkungen:</label>
				<textarea name="remarks" id="modal-remarks" rows="3"></textarea>
			</div>
			<button type="submit" class="btn">Speichern</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />

<%-- ======================================================================== --%>
<%-- NEW, SIMPLIFIED, AND ROBUST JAVASCRIPT LOGIC                           --%>
<%-- The old "data island" script has been completely removed.              --%>
<%-- ======================================================================== --%>
<script>
document.addEventListener('DOMContentLoaded', () => {
    const modalOverlay = document.getElementById('attendance-modal');
    if (!modalOverlay) return;
    
    // Select all modal form elements once for efficiency
    const modalTitle = document.getElementById('modal-title');
    const modalUserId = document.getElementById('modal-user-id');
    const modalMeetingId = document.getElementById('modal-meeting-id');
    const modalAttended = document.getElementById('modal-attended');
    const modalRemarks = document.getElementById('modal-remarks');
    const closeBtn = modalOverlay.querySelector('.modal-close-btn');

    const openModal = (cell) => {
        // Read data directly from the clicked cell's "data-*" attributes.
        // The browser automatically makes these available in the `dataset` object.
        const userData = cell.dataset;

        // Populate the modal with the data from the cell's attributes. This will now work.
        modalTitle.textContent = `Nutzer: ${userData.userName} | Meeting: ${userData.meetingName}`;
        modalUserId.value = userData.userId;
        modalMeetingId.value = userData.meetingId;
        modalRemarks.value = userData.remarks;
        
        // The data attribute gives a string "true" or "false". 
        // We must compare it to the string "true" to set the checkbox correctly.
        modalAttended.checked = (userData.attended === 'true');
        
        modalOverlay.classList.add('active');
    };

    const closeModal = () => {
        modalOverlay.classList.remove('active');
    };

    // Attach the click event listener to every cell with the "qual-cell" class.
    document.querySelectorAll('.qual-cell').forEach(cell => {
        cell.addEventListener('click', () => openModal(cell));
    });

    // Event listeners for closing the modal
    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    modalOverlay.addEventListener('click', (event) => { if (event.target === modalOverlay) closeModal(); });
    document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && modalOverlay.classList.contains('active')) closeModal(); });
});
</script>