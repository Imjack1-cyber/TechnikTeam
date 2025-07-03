<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Veranstaltungen" />
</c:import>

<h1>Anstehende Veranstaltungen</h1>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

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

<div class="table-wrapper">
	<table class="data-table sortable-table searchable-table">
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
						href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}"><c:out
								value="${event.name}" /></a></td>
					<td data-sort-value="${event.eventDateTime}"><c:out
							value="${event.formattedEventDateTimeRange}" /></td>
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
							<div style="display: flex; gap: 0.5rem;">
								<c:if
									test="${event.userAttendanceStatus == 'OFFEN' or event.userAttendanceStatus == 'ABGEMELDET'}">
									<button type="button"
										class="btn btn-small btn-success signup-btn"
										data-event-id="${event.id}"
										data-event-name="${fn:escapeXml(event.name)}">Anmelden</button>
								</c:if>
								<c:if test="${event.userAttendanceStatus == 'ANGEMELDET'}">
									<form action="${pageContext.request.contextPath}/event-action"
										method="post" class="js-confirm-form"
										data-confirm-message="Wirklich vom Event '${fn:escapeXml(event.name)}' abmelden?">
										<input type="hidden" name="eventId" value="${event.id}">
										<button type="submit" name="action" value="signoff"
											class="btn btn-small btn-danger">Abmelden</button>
									</form>
								</c:if>
							</div>
						</c:if></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Signup Modal -->
<div class="modal-overlay" id="signup-modal">
	<div class="modal-content">
		<button type="button" class="modal-close-btn" aria-label="Schließen">×</button>
		<h3 id="signup-modal-title">Anmeldung</h3>
		<form id="signup-form"
			action="${pageContext.request.contextPath}/event-action"
			method="post">
			<input type="hidden" name="action" value="signup"> <input
				type="hidden" name="eventId" id="signup-event-id">
			<div id="custom-fields-container"></div>
			<button type="submit" class="btn btn-success"
				style="margin-top: 1rem;">Anmeldung bestätigen</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/public/events.js"></script>