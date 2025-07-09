<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Qualifikations-Matrix" />
</c:import>

<h1>
	<i class="fas fa-th-list"></i> Qualifikations-Matrix
</h1>
<p>Klicken Sie auf eine Zelle, um die Teilnahme an einem Meeting zu
	bearbeiten. Die Kopfzeile und die Benutzerleiste bleiben beim Scrollen
	fixiert.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="table-wrapper">
	<table class="data-table">
		<thead>
			<tr>
				<th rowspan="2" class="sticky-header sticky-col"
					style="vertical-align: middle; left: 0; z-index: 15;">Nutzer /
					Lehrgang ↓</th>
				<c:forEach var="course" items="${allCourses}">
					<th colspan="${fn:length(meetingsByCourse[course.id])}"
						class="sticky-header" style="text-align: center;"><a
						href="${pageContext.request.contextPath}/admin/lehrgaenge"
						title="Vorlagen verwalten">${course.abbreviation}</a></th>
				</c:forEach>
			</tr>
			<tr>
				<c:forEach var="course" items="${allCourses}">
					<c:forEach var="meeting" items="${meetingsByCourse[course.id]}">
						<th class="sticky-header"
							style="text-align: center; min-width: 120px;"><a
							href="${pageContext.request.contextPath}/admin/meetings?courseId=${course.id}"
							title="Meetings für '${course.name}' verwalten">${meeting.name}</a>
						</th>
					</c:forEach>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="user" items="${allUsers}">
				<tr>
					<td class="sticky-col" style="font-weight: 500; left: 0;"><a
						href="${pageContext.request.contextPath}/admin/mitglieder?action=details&id=${user.id}">${user.username}</a>
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
								title="Klicken zum Bearbeiten"><c:choose>
									<c:when test="${not empty attendance && attendance.attended}">
										<span style="font-size: 1.2rem;">✔</span>
									</c:when>
									<c:otherwise>
										<span class="text-muted">-</span>
									</c:otherwise>
								</c:choose></td>
						</c:forEach>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Attendance Modal -->
<div class="modal-overlay" id="attendance-modal">
	<div class="modal-content">
		<button type="button" class="modal-close-btn" aria-label="Schließen">×</button>
		<h3>Teilnahme bearbeiten</h3>
		<p id="modal-title" style="font-weight: bold; margin-bottom: 1rem;"></p>
		<form action="${pageContext.request.contextPath}/admin/teilnahme"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="returnTo" value="matrix"> <input type="hidden"
				name="userId" id="modal-user-id"> <input type="hidden"
				name="meetingId" id="modal-meeting-id">
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

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_matrix.js"></script>