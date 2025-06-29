<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="../../jspf/header.jspf">
	<c:param name="pageTitle" value="Lehrgänge" />
	<c:param name="navType" value="user" />
</c:import>

<h1>Anstehende Lehrgänge & Meetings</h1>

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
	<div class="form-group" style="margin-bottom: 0; flex-grow: 1;">
		<input type="search" id="table-filter"
			placeholder="Lehrgänge filtern..." style="width: 100%;"
			aria-label="Lehrgänge filtern">
	</div>
</div>

<c:if test="${empty meetings}">
	<div class="card">
		<p>Derzeit stehen keine Lehrgänge oder Meetings an.</p>
	</div>
</c:if>

<div class="table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Meeting</th>
				<th class="sortable" data-sort-type="string">Gehört zu Kurs</th>
				<th class="sortable" data-sort-type="date">Datum & Uhrzeit</th>
				<th class="sortable" data-sort-type="string">Dein Status</th>
				<th>Aktion</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="meeting" items="${meetings}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/meetingDetails?id=${meeting.id}"><c:out
								value="${meeting.name}" /></a></td>
					<td><c:out value="${meeting.parentCourseName}" /></td>
					<td data-sort-value="${meeting.meetingDateTime}"><c:out
							value="${meeting.formattedMeetingDateTimeRange}" /></td>
					<td><c:choose>
							<c:when test="${meeting.userAttendanceStatus == 'ANGEMELDET'}">
								<span style="color: var(--success-color);">Angemeldet</span>
							</c:when>
							<c:when test="${meeting.userAttendanceStatus == 'ABGEMELDET'}">
								<span style="color: var(--danger-color);">Abgemeldet</span>
							</c:when>
							<c:otherwise>Offen</c:otherwise>
						</c:choose></td>
					<td>
						<form action="${pageContext.request.contextPath}/meeting-action"
							method="post" style="display: flex; gap: 0.5rem;">
							<input type="hidden" name="meetingId" value="${meeting.id}">
							<c:if test="${meeting.userAttendanceStatus != 'ANGEMELDET'}">
								<button type="submit" name="action" value="signup"
									class="btn btn-small btn-success">Anmelden</button>
							</c:if>
							<c:if test="${meeting.userAttendanceStatus == 'ANGEMELDET'}">
								<button type="submit" name="action" value="signoff"
									class="btn btn-small btn-danger">Abmelden</button>
							</c:if>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />