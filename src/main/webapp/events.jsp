<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Veranstaltungen" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Anstehende Veranstaltungen</h1>
<%-- ... Feedback messages ... --%>

<c:if test="${empty events}">
	<div class="card">
		<p>Für dich stehen derzeit keine Veranstaltungen an.</p>
	</div>
</c:if>

<!-- MOBILE LAYOUT -->
<div class="mobile-card-list">
	<c:forEach var="event" items="${events}">
		<div class="list-item-card">
			<h3 class="card-title">
				<a
					href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a>
			</h3>
			<div class="card-row">
				<span>Datum:</span> <span>${event.formattedEventDateTime} Uhr</span>
			</div>
			<div class="card-row">
				<span>Dein Status:</span> <span> <c:choose>
						<c:when test="${event.userAttendanceStatus == 'ZUGEWIESEN'}">
							<span class="text-success">Zugewiesen</span>
						</c:when>
						<c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}">
							<span class="text-success">Anwesend</span>
						</c:when>
						<c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}">
							<span class="text-danger">Abwesend</span>
						</c:when>
						<c:otherwise>Offen</c:otherwise>
					</c:choose>
				</span>
			</div>
			<%-- Only show action buttons if user is NOT yet assigned --%>
			<c:if test="${event.userAttendanceStatus != 'ZUGEWIESEN'}">
				<div class="card-actions">
					<form action="${pageContext.request.contextPath}/event-action"
						method="post" style="display: flex; gap: 0.5rem;">
						<input type="hidden" name="eventId" value="${event.id}">
						<c:if test="${event.userAttendanceStatus != 'ANGEMELDET'}">
							<button type="submit" name="action" value="signup"
								class="btn btn-small btn-success">Anwesend</button>
						</c:if>
						<c:if test="${event.userAttendanceStatus == 'ANGEMELDET'}">
							<button type="submit" name="action" value="signoff"
								class="btn btn-small btn-danger">Abwesend</button>
						</c:if>
					</form>
				</div>
			</c:if>
		</div>
	</c:forEach>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table">
		<thead>
			<tr>
				<th>Veranstaltung</th>
				<th>Datum & Uhrzeit</th>
				<th>Dein Status</th>
				<th>Aktion</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="event" items="${events}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a></td>
					<td>${event.formattedEventDateTime}Uhr</td>
					<td><c:choose>
							<c:when test="${event.userAttendanceStatus == 'ZUGEWIESEN'}">
								<strong class="text-success">Zugewiesen</strong>
							</c:when>
							<c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}">
								<span class="text-success">Anwesend</span>
							</c:when>
							<c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}">
								<span class="text-danger">Abwesend</span>
							</c:when>
							<c:otherwise>Offen</c:otherwise>
						</c:choose></td>
					<td>
						<%-- Only show action buttons if user is NOT yet assigned --%> <c:if
							test="${event.userAttendanceStatus != 'ZUGEWIESEN'}">
							<form action="${pageContext.request.contextPath}/event-action"
								method="post" style="display: flex; gap: 0.5rem;">
								<input type="hidden" name="eventId" value="${event.id}">
								<c:if test="${event.userAttendanceStatus != 'ANGEMELDET'}">
									<button type="submit" name="action" value="signup"
										class="btn btn-small btn-success">Anwesend</button>
								</c:if>
								<c:if test="${event.userAttendanceStatus == 'ANGEMELDET'}">
									<button type="submit" name="action" value="signoff"
										class="btn btn-small btn-danger">Abwesend</button>
								</c:if>
							</form>
						</c:if>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />