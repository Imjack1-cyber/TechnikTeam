<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Qualifikations-Matrix" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Qualifikations-Matrix</h1>
<p>Klicken Sie auf eine Zelle (A, X oder -), um den
	Qualifikationsstatus eines Nutzers schnell zu bearbeiten.</p>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="card" style="overflow-x: auto;">
	<table class="styled-table matrix-table">
		<thead>
			<tr>
				<th>Nutzer / Lehrgang ↓</th>
				<c:forEach var="course" items="${allCourses}">
					<th id="course-${course.id}" data-course-name="${course.name}"
						title="${course.name}">${course.abbreviation}</th>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="user" items="${allUsers}">
				<tr id="user-${user.id}" data-user-name="${user.username}">
					<td><a
						href="${pageContext.request.contextPath}/admin/users?action=details&id=${user.id}"
						title="Details für ${user.username} anzeigen">${user.username}</a></td>
					<c:forEach var="course" items="${allCourses}">
						<c:set var="lookupKey" value="${user.id}-${course.id}" />
						<c:set var="qual" value="${qualificationMap[lookupKey]}" />
						<td class="qual-cell" title="Klicken zum Bearbeiten"><c:choose>
								<c:when test="${qual.status == 'ABSOLVIERT'}">A</c:when>
								<c:when test="${qual.status == 'BESUCHT'}">X</c:when>
								<c:otherwise>-</c:otherwise>
							</c:choose></td>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Modal Dialog -->
<div id="qual-modal" class="lightbox" style="display: none;">
	<div class="modal-content">
		<span class="close-btn">×</span>
		<h3>Qualifikation bearbeiten</h3>
		<p id="modal-title" style="font-weight: bold; margin-bottom: 1rem;"></p>
		<form action="${pageContext.request.contextPath}/admin/users"
			method="post">
			<input type="hidden" name="action" value="updateQualification">
			<input type="hidden" name="returnTo" value="matrix"> <input
				type="hidden" name="userId" id="modal-user-id"> <input
				type="hidden" name="courseId" id="modal-course-id">
			<div class="form-group">
				<label>Status:</label><select name="status" id="modal-status"><option
						value="NICHT BESUCHT">Nicht besucht (Eintrag löschen)</option>
					<option value="BESUCHT">Besucht</option>
					<option value="ABSOLVIERT">Absolviert</option></select>
			</div>
			<div class="form-group">
				<label>Abschlussdatum:</label><input type="date"
					name="completionDate" id="modal-date">
			</div>
			<div class="form-group">
				<label>Bemerkungen:</label>
				<textarea name="remarks" id="modal-remarks" rows="3"></textarea>
			</div>
			<button type="submit" class="btn">Speichern</button>
		</form>
	</div>
</div>

<style>
.matrix-table th, .matrix-table td {
	text-align: center;
}

.matrix-table td.qual-cell {
	cursor: pointer;
	font-weight: bold;
}

.matrix-table td.qual-cell:hover {
	background-color: var(--secondary-color);
}

.modal-content {
	background-color: var(--card-bg);
	padding: 25px;
	border-radius: 8px;
	width: 90%;
	max-width: 500px;
	position: relative;
	box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
	text-align: left;
}

.close-btn {
	position: absolute;
	top: 10px;
	right: 20px;
	font-size: 30px;
	cursor: pointer;
	color: #aaa;
}

.close-btn:hover {
	color: var(--text-color);
}
</style>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('qual-modal');
    const modalTitle = document.getElementById('modal-title');
    const modalUserId = document.getElementById('modal-user-id');
    const modalCourseId = document.getElementById('modal-course-id');
    const modalStatus = document.getElementById('modal-status');
    const modalDate = document.getElementById('modal-date');
    const modalRemarks = document.getElementById('modal-remarks');
    const closeBtn = document.querySelector('#qual-modal .close-btn');

    const qualificationMap = {
        <c:forEach var="entry" items="${qualificationMap}" varStatus="loop">
            "${entry.key}": { status: "${entry.value.status}", completionDate: "${entry.value.completionDate}", remarks: "${entry.value.remarks}".replace(/"/g, '\\"').replace(/\r/g, '').replace(/\n/g, '\\n') }${!loop.last ? ',' : ''}
        </c:forEach>
    };

    document.querySelectorAll('.qual-cell').forEach(cell => {
        cell.addEventListener('click', (event) => {
            const clickedCell = event.currentTarget;
            const userRow = clickedCell.closest('tr');
            const table = userRow.closest('table');
            
            const userId = userRow.dataset.userId;
            const userName = userRow.dataset.userName;
            
            const cellIndex = clickedCell.cellIndex;
            const headerCell = table.querySelector(`thead th:nth-child(${cellIndex + 1})`);
            const courseId = headerCell.dataset.courseId;
            const courseName = headerCell.dataset.courseName;

            const lookupKey = `${userId}-${courseId}`;
            const qualData = qualificationMap[lookupKey] || {};

            modalTitle.innerText = `Nutzer: ${userName} | Lehrgang: ${courseName}`;
            modalUserId.value = userId;
            modalCourseId.value = courseId;
            
            modalStatus.value = qualData.status || 'NICHT BESUCHT';
            modalDate.value = (qualData.completionDate && qualData.completionDate !== 'null') ? qualData.completionDate : '';
            modalRemarks.value = (qualData.remarks && qualData.remarks !== 'null') ? qualData.remarks : '';
            
            modal.style.display = 'flex';
        });
    });

    if(closeBtn) { closeBtn.addEventListener('click', () => modal.style.display = 'none'); }
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
});
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />