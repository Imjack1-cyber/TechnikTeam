<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  events.jsp
  
  This is the main event listing page for a regular user. It displays a list
  of all upcoming events for which the user is qualified. For each event, it
  shows the user's specific status (e.g., "Zugewiesen", "Angemeldet", "Offen")
  and provides appropriate actions (like "Anmelden" or "Abmelden"). It now
  includes client-side sorting and filtering.
  
  - It is served by: EventServlet.
  - It submits to: EventActionServlet.
  - Expected attributes:
    - 'events' (List<de.technikteam.model.Event>): A list of events relevant to the user.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Veranstaltungen" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Anstehende Veranstaltungen</h1>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="table-controls">
	<div class="form-group" style="margin-bottom: 0; flex-grow: 1;">
		<input type="search" id="table-filter" placeholder="Events filtern..."
			style="width: 100%;" aria-label="Events filtern">
	</div>
</div>


<c:if test="${empty events}">
	<div class="card">
		<p>Für dich stehen derzeit keine Veranstaltungen an, für die du
			qualifiziert bist.</p>
	</div>
</c:if>

<!-- MOBILE LAYOUT -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="event" items="${events}">
		<div class="list-item-card"
			data-searchable-content="${event.name} ${event.userAttendanceStatus}">
			<h3 class="card-title">
				<a
					href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a>
			</h3>
			<div class="card-row">
				<span>Datum:</span> <span>${event.formattedEventDateTimeRange}</span>
			</div>
			<div class="card-row">
				<span>Dein Status:</span> <span> <c:choose>
						<c:when test="${event.userAttendanceStatus == 'ZUGEWIESEN'}">
							<strong class="text-success">Zugewiesen</strong>
						</c:when>
						<c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}">
							<span class="text-success">Angemeldet</span>
						</c:when>
						<c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}">
							<span class="text-danger">Abgemeldet</span>
						</c:when>
						<c:otherwise>Offen</c:otherwise>
					</c:choose>
				</span>
			</div>
			<c:if test="${event.userAttendanceStatus != 'ZUGEWIESEN'}">
				<div class="card-actions">
					<form action="${pageContext.request.contextPath}/event-action"
						method="post" style="display: flex; gap: 0.5rem;">
						<input type="hidden" name="eventId" value="${event.id}">
						<c:if
							test="${event.userAttendanceStatus == 'OFFEN' or event.userAttendanceStatus == 'ABGEMELDET'}">
							<button type="submit" name="action" value="signup"
								class="btn btn-small btn-success">Anmelden</button>
						</c:if>
						<c:if test="${event.userAttendanceStatus == 'ANGEMELDET'}">
							<button type="submit" name="action" value="signoff"
								class="btn btn-small btn-danger">Abmelden</button>
						</c:if>
					</form>
				</div>
			</c:if>
		</div>
	</c:forEach>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Veranstaltung</th>
				<th class="sortable" data-sort-type="date">Datum & Uhrzeit</th>
				<th class="sortable" data-sort-type="string">Dein Status</th>
				<th>Aktion</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="event" items="${events}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a></td>
					<td data-sort-value="${event.eventDateTime}">${event.formattedEventDateTimeRange}</td>
					<td><c:choose>
							<c:when test="${event.userAttendanceStatus == 'ZUGEWIESEN'}">
								<strong class="text-success">Zugewiesen</strong>
							</c:when>
							<c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}">
								<span class="text-success">Angemeldet</span>
							</c:when>
							<c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}">
								<span class="text-danger">Abgemeldet</span>
							</c:when>
							<c:otherwise>Offen</c:otherwise>
						</c:choose></td>
					<td><c:if test="${event.userAttendanceStatus != 'ZUGEWIESEN'}">
							<form action="${pageContext.request.contextPath}/event-action"
								method="post" style="display: flex; gap: 0.5rem;">
								<input type="hidden" name="eventId" value="${event.id}">
								<c:if
									test="${event.userAttendanceStatus == 'OFFEN' or event.userAttendanceStatus == 'ABGEMELDET'}">
									<button type="submit" name="action" value="signup"
										class="btn btn-small btn-success">Anmelden</button>
								</c:if>
								<c:if test="${event.userAttendanceStatus == 'ANGEMELDET'}">
									<button type="submit" name="action" value="signoff"
										class="btn btn-small btn-danger">Abmelden</button>
								</c:if>
							</form>
						</c:if></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />