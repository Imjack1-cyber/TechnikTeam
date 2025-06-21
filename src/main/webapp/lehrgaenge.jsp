<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lehrgänge" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Anstehende Lehrgänge & Meetings</h1>
<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<c:if test="${empty meetings}">
	<div class="card">
		<p>Derzeit stehen keine Lehrgänge oder Meetings an.</p>
	</div>
</c:if>

<!-- MOBILE LAYOUT -->
<div class="mobile-card-list">
	<c:forEach var="meeting" items="${meetings}">
		<div class="list-item-card">
			<h3 class="card-title">
				<a
					href="${pageContext.request.contextPath}/meetingDetails?id=${meeting.id}">${meeting.name}</a>
			</h3>
			<div class="card-row">
				<span>Gehört zu:</span> <span>${meeting.parentCourseName}</span>
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
	<table class="desktop-table">
		<thead>
			<tr>
				<th>Meeting</th>
				<th>Gehört zu Kurs</th>
				<th>Datum & Uhrzeit</th>
				<th>Dein Status</th>
				<th>Aktion</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="meeting" items="${meetings}">
				<tr>
					<td>
						<%-- THE FIX: Link to meetingDetails and use meeting.id --%> <a
						href="${pageContext.request.contextPath}/meetingDetails?id=${meeting.id}">${meeting.name}</a>
					</td>
					<td>${meeting.parentCourseName}</td>
					<td>${meeting.meetingDateTime}</td>
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

<c:import url="/WEB-INF/jspf/footer.jspf" />