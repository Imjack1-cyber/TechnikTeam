<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  lehrgaenge.jsp
  
  This is the main page for users to view and interact with upcoming course
  meetings. It displays a list of all scheduled meetings, showing the user's
  current attendance status for each. It provides buttons for the user to
  sign up for or sign off from each meeting. Now includes client-side
  filtering and sorting.
  
  - It is served by: MeetingServlet.
  - It submits to: MeetingActionServlet.
  - Expected attributes:
    - 'meetings' (List<de.technikteam.model.Meeting>): A list of all upcoming meetings.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lehrgänge" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

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

<!-- MOBILE LAYOUT -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="meeting" items="${meetings}">
		<div class="list-item-card"
			data-searchable-content="<c:out value='${meeting.name}'/> <c:out value='${meeting.parentCourseName}'/> <c:out value='${meeting.userAttendanceStatus}'/>">
			<h3 class="card-title">
				<a
					href="${pageContext.request.contextPath}/meetingDetails?id=${meeting.id}"><c:out
						value="${meeting.name}" /></a>
			</h3>
			<div class="card-row">
				<span>Gehört zu:</span> <span><c:out
						value="${meeting.parentCourseName}" /></span>
			</div>
			<div class="card-row">
				<span>Dein Status:</span> <span> <c:choose>
						<c:when test="${meeting.userAttendanceStatus == 'ANGEMELDET'}">
							<span class="text-success">Angemeldet</span>
						</c:when>
						<c:when test="${meeting.userAttendanceStatus == 'ABGEMELDET'}">
							<span class="text-danger">Abgemeldet</span>
						</c:when>
						<c:otherwise>Offen</c:otherwise>
					</c:choose>
				</span>
			</div>
			<div class="card-actions">
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
			</div>
		</div>
	</c:forEach>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table sortable-table searchable-table">
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
								<span class="text-success">Angemeldet</span>
							</c:when>
							<c:when test="${meeting.userAttendanceStatus == 'ABGEMELDET'}">
								<span class="text-danger">Abgemeldet</span>
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