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
		<p>Derzeit stehen keine Veranstaltungen an.</p>
	</div>
</c:if>

<!-- Desktop Table View -->
<div class="desktop-table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Veranstaltung</th>
				<th class="sortable" data-sort-type="date">Datum & Uhrzeit</th>
				<th class="sortable" data-sort-type="string">Event-Status</th>
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
					<td><span
						class="status-badge ${event.status == 'LAUFEND' ? 'status-warn' : (event.status == 'ABGESCHLOSSEN' or event.status == 'ABGESAGT') ? 'status-info' : 'status-ok'}"><c:out
								value="${event.status}" /></span></td>
					<td><c:choose>
							<c:when test="${event.userAttendanceStatus == 'ZUGEWIESEN'}">
								<strong class="text-success"><c:out value="Zugewiesen" /></strong>
							</c:when>
							<c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}">
								<span class="text-success"><c:out value="Angemeldet" /></span>
							</c:when>
							<c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}">
								<span class="text-danger"><c:out value="Abgemeldet" /></span>
							</c:when>
							<c:otherwise>
								<c:out value="Offen" />
							</c:otherwise>
						</c:choose></td>
					<td>
						<div style="display: flex; gap: 0.5rem;">
							<c:if
								test="${event.userAttendanceStatus == 'OFFEN' or event.userAttendanceStatus == 'ABGEMELDET'}">
								<c:choose>
									<c:when test="${event.userQualified}">
										<button type="button"
											class="btn btn-small btn-success signup-btn"
											data-event-id="${event.id}"
											data-event-name="${fn:escapeXml(event.name)}">Anmelden</button>
									</c:when>
									<c:otherwise>
										<button type="button" class="btn btn-small btn-success"
											disabled
											title="Du erfüllst die Anforderungen für dieses Event nicht.">Anmelden</button>
									</c:otherwise>
								</c:choose>
							</c:if>
							<c:if test="${event.userAttendanceStatus == 'ANGEMELDET'}">
								<form action="${pageContext.request.contextPath}/event-action"
									method="post" class="js-signoff-form"
									data-event-id="${event.id}" data-event-status="${event.status}"
									data-confirm-message="Wirklich vom Event '${fn:escapeXml(event.name)}' abmelden?">
									<input type="hidden" name="csrfToken"
										value="${sessionScope.csrfToken}"> <input
										type="hidden" name="eventId" value="${event.id}"> <input
										type="hidden" name="action" value="signoff">
									<button type="submit" class="btn btn-small btn-danger">Abmelden</button>
								</form>
							</c:if>
						</div>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list searchable-table">
	<c:forEach var="event" items="${events}">
		<div class="list-item-card">
			<h3 class="card-title">
				<a
					href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}"><c:out
						value="${event.name}" /></a>
			</h3>
			<div class="card-row">
				<span>Zeitraum:</span> <strong><c:out
						value="${event.formattedEventDateTimeRange}" /></strong>
			</div>
			<div class="card-row">
				<span>Event-Status:</span> <span><span
					class="status-badge ${event.status == 'LAUFEND' ? 'status-warn' : (event.status == 'ABGESCHLOSSEN' or event.status == 'ABGESAGT') ? 'status-info' : 'status-ok'}"><c:out
							value="${event.status}" /></span></span>
			</div>
			<div class="card-row">
				<span>Dein Status:</span> <strong> <c:choose>
						<c:when test="${event.userAttendanceStatus == 'ZUGEWIESEN'}">
							<span class="text-success">Zugewiesen</span>
						</c:when>
						<c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}">
							<span class="text-success">Angemeldet</span>
						</c:when>
						<c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}">
							<span class="text-danger">Abgemeldet</span>
						</c:when>
						<c:otherwise>Offen</c:otherwise>
					</c:choose>
				</strong>
			</div>
			<div class="card-actions">
				<c:if
					test="${event.userAttendanceStatus == 'OFFEN' or event.userAttendanceStatus == 'ABGEMELDET'}">
					<c:choose>
						<c:when test="${event.userQualified}">
							<button type="button"
								class="btn btn-small btn-success signup-btn"
								data-event-id="${event.id}"
								data-event-name="${fn:escapeXml(event.name)}">Anmelden</button>
						</c:when>
						<c:otherwise>
							<button type="button" class="btn btn-small btn-success" disabled
								title="Du erfüllst die Anforderungen für dieses Event nicht.">Anmelden</button>
						</c:otherwise>
					</c:choose>
				</c:if>
				<c:if test="${event.userAttendanceStatus == 'ANGEMELDET'}">
					<form action="${pageContext.request.contextPath}/event-action"
						method="post" class="js-signoff-form" data-event-id="${event.id}"
						data-event-status="${event.status}"
						data-confirm-message="Wirklich vom Event '${fn:escapeXml(event.name)}' abmelden?">
						<input type="hidden" name="csrfToken"
							value="${sessionScope.csrfToken}"> <input type="hidden"
							name="eventId" value="${event.id}"> <input type="hidden"
							name="action" value="signoff">
						<button type="submit" class="btn btn-small btn-danger">Abmelden</button>
					</form>
				</c:if>
			</div>
		</div>
	</c:forEach>
</div>

<!-- Signup Modal -->
<div class="modal-overlay" id="signup-modal">
	<div class="modal-content">
		<button type="button" class="modal-close-btn" aria-label="Schließen">×</button>
		<h3 id="signup-modal-title">Anmeldung</h3>
		<form id="signup-form"
			action="${pageContext.request.contextPath}/event-action"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" value="signup"> <input type="hidden"
				name="eventId" id="signup-event-id">
			<div id="custom-fields-container"></div>
			<button type="submit" class="btn btn-success"
				style="margin-top: 1rem;">Anmeldung bestätigen</button>
		</form>
	</div>
</div>

<!-- Sign-off with Reason Modal -->
<div class="modal-overlay" id="signoff-reason-modal">
	<div class="modal-content" style="max-width: 500px;">
		<button type="button" class="modal-close-btn" aria-label="Schließen">×</button>
		<h3>Abmeldung vom laufenden Event</h3>
		<p>Da dieses Event bereits läuft, ist eine Begründung für die
			Abmeldung erforderlich. Der Event-Leiter wird benachrichtigt.</p>
		<form id="signoff-reason-form"
			action="${pageContext.request.contextPath}/event-action"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" value="signOffWithReason"> <input
				type="hidden" name="eventId" id="signoff-event-id">
			<div class="form-group">
				<label for="signoff-reason">Begründung</label>
				<textarea id="signoff-reason" name="reason" rows="4" required></textarea>
			</div>
			<button type="submit" class="btn btn-danger">Abmeldung
				bestätigen</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/public/events.js"></script>