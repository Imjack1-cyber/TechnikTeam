<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Qualifikations-Matrix" />
	<c:param name="navType" value="admin" />
</c:import>

<h1>
	<i class="fas fa-th-list"></i> Qualifikations-Matrix (Modular)
</h1>
<p>Klicken Sie auf eine Zelle, um die Teilnahme an einem Meeting zu
	bearbeiten.</p>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">
		<i class="fas fa-check-circle"></i> ${sessionScope.successMessage}
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">
		<i class="fas fa-exclamation-triangle"></i>
		${sessionScope.errorMessage}
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="table-wrapper">
	<table class="data-table">
		<thead>
			<tr>
				<th rowspan="2"
					style="vertical-align: middle; position: sticky; left: 0; z-index: 10; background-color: var(--surface-color);">Nutzer
					/ Lehrgang ↓</th>
				<c:forEach var="course" items="${allCourses}">
					<th colspan="${meetingsByCourse[course.id].size()}"
						style="text-align: center;"><a
						href="${pageContext.request.contextPath}/admin/courses?action=edit&id=${course.id}"
						title="Vorlage '${course.name}' bearbeiten">${course.abbreviation}</a>
					</th>
				</c:forEach>
			</tr>
			<tr>
				<c:forEach var="course" items="${allCourses}">
					<c:forEach var="meeting" items="${meetingsByCourse[course.id]}">
						<th style="text-align: center;"><a
							href="${pageContext.request.contextPath}/admin/meetings?action=edit&courseId=${course.id}&meetingId=${meeting.id}"
							title="Meeting '${meeting.name}' bearbeiten">${meeting.name}</a>
						</th>
					</c:forEach>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="user" items="${allUsers}">
				<tr>
					<td
						style="font-weight: 500; position: sticky; left: 0; background-color: var(--surface-color); z-index: 5;">
						<a
						href="${pageContext.request.contextPath}/admin/users?action=details&id=${user.id}">${user.username}</a>
					</td>
					<c:forEach var="course" items="${allCourses}">
						<c:forEach var="meeting" items="${meetingsByCourse[course.id]}">
							<c:set var="attendanceKey" value="${user.id}-${meeting.id}" />
							<c:set var="attendance" value="${attendanceMap[attendanceKey]}" />
							<td class="qual-cell" data-user-id="${user.id}"
								data-user-name="${user.username}"
								data-meeting-id="${meeting.id}"
								data-meeting-name="${course.name} - ${meeting.name}"
								data-attended="${not empty attendance && attendance.attended}"
								data-remarks="${not empty attendance ? attendance.remarks : ''}"
								style="text-align: center; font-weight: bold; cursor: pointer;"
								title="Klicken zum Bearbeiten"><c:if
									test="${not empty attendance && attendance.attended}">
									<span style="color: var(--success-color);">✔</span>
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
			<button type="submit" class="btn">
				<i class="fas fa-save"></i> Speichern
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />

<script>
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
</script>