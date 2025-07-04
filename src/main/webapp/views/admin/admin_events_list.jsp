<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Eventverwaltung" />
</c:import>

<h1>
	<i class="fas fa-calendar-check"></i> Eventverwaltung
</h1>
<p>Hier können Sie Events erstellen, bearbeiten, Personal zuweisen
	und den Status verwalten.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="table-controls">
	<button type="button" class="btn btn-success" id="new-event-btn">
		<i class="fas fa-plus"></i> Neues Event anlegen
	</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter" placeholder="Events filtern..."
			aria-label="Tabelle filtern">
	</div>
</div>

<c:if test="${empty eventList}">
	<div class="card">
		<p>Keine Events gefunden.</p>
	</div>
</c:if>

<!-- Mobile Card View -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="event" items="${eventList}">
		<div class="list-item-card"
			data-searchable-content="${event.name} ${event.status}">
			<h3 class="card-title">
				<a
					href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}">${event.name}</a>
			</h3>
			<div class="card-row">
				<span>Zeitraum:</span> <span>${event.formattedEventDateTimeRange}</span>
			</div>
			<div class="card-row">
				<span>Status:</span> <span><span
					class="status-badge ${event.status == 'LAUFEND' ? 'status-warn' : (event.status == 'ABGESCHLOSSEN' or event.status == 'ABGESAGT') ? 'status-info' : 'status-ok'}">${event.status}</span></span>
			</div>
			<div class="card-actions">
				<button type="button" class="btn btn-small edit-event-btn"
					data-event-id="${event.id}">
					<i class="fas fa-edit"></i> Bearbeiten
				</button>
				<c:if test="${event.status != 'ABGESCHLOSSEN'}">
					<button type="button"
						class="btn btn-small btn-success assign-users-btn"
						data-event-id="${event.id}"
						data-event-name="${fn:escapeXml(event.name)}">
						<i class="fas fa-users"></i> Zuweisen
					</button>
				</c:if>
				<form
					action="${pageContext.request.contextPath}/admin/veranstaltungen"
					method="post" class="js-confirm-form"
					data-confirm-message="Soll das Event '${fn:escapeXml(event.name)}' wirklich endgültig gelöscht werden?">
					<input type="hidden" name="action" value="delete"><input
						type="hidden" name="id" value="${event.id}">
					<button type="submit" class="btn btn-small btn-danger">
						<i class="fas fa-trash"></i> Löschen
					</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>


<!-- Desktop Table View -->
<div class="desktop-table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name</th>
				<th class="sortable" data-sort-type="string">Zeitraum</th>
				<th class="sortable" data-sort-type="string">Status</th>
				<th style="min-width: 450px;">Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="event" items="${eventList}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}">${event.name}</a></td>
					<td>${event.formattedEventDateTimeRange}</td>
					<td><span
						class="status-badge ${event.status == 'LAUFEND' ? 'status-warn' : (event.status == 'ABGESCHLOSSEN' or event.status == 'ABGESAGT') ? 'status-info' : 'status-ok'}">${event.status}</span></td>
					<td style="display: flex; gap: 5px; flex-wrap: wrap;">
						<button type="button" class="btn btn-small edit-event-btn"
							data-event-id="${event.id}">Bearbeiten</button> <c:if
							test="${event.status != 'ABGESCHLOSSEN'}">
							<button type="button"
								class="btn btn-small btn-success assign-users-btn"
								data-event-id="${event.id}"
								data-event-name="${fn:escapeXml(event.name)}">Zuweisen</button>
							<c:if
								test="${event.status == 'GEPLANT' || event.status == 'KOMPLETT'}">
								<form
									action="${pageContext.request.contextPath}/admin/veranstaltungen"
									method="post" style="display: inline;" class="js-confirm-form"
									data-confirm-message="Event '${fn:escapeXml(event.name)}' wirklich starten? Der Chat wird aktiviert.">
									<input type="hidden" name="action" value="updateStatus"><input
										type="hidden" name="id" value="${event.id}"><input
										type="hidden" name="newStatus" value="LAUFEND">
									<button type="submit" class="btn btn-small btn-warning">Starten</button>
								</form>
							</c:if>
							<c:if test="${event.status == 'LAUFEND'}">
								<form
									action="${pageContext.request.contextPath}/admin/veranstaltungen"
									method="post" style="display: inline;" class="js-confirm-form"
									data-confirm-message="Event '${fn:escapeXml(event.name)}' wirklich abschließen?">
									<input type="hidden" name="action" value="updateStatus"><input
										type="hidden" name="id" value="${event.id}"><input
										type="hidden" name="newStatus" value="ABGESCHLOSSEN">
									<button type="submit" class="btn btn-small"
										style="background-color: var(--text-muted-color);">Abschließen</button>
								</form>
							</c:if>
						</c:if>
						<form
							action="${pageContext.request.contextPath}/admin/veranstaltungen"
							method="post" class="inline-form js-confirm-form"
							data-confirm-message="Soll das Event '${fn:escapeXml(event.name)}' wirklich endgültig gelöscht werden?">
							<input type="hidden" name="action" value="delete"><input
								type="hidden" name="id" value="${event.id}">
							<button type="submit" class="btn btn-small btn-danger">Löschen</button>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<jsp:include page="/WEB-INF/jspf/event_modals.jspf" />

<script id="allCoursesData" type="application/json">[<c:forEach var="c" items="${allCourses}" varStatus="loop">{"id":${c.id},"name":"<c:out value="${c.name}"/>"}<c:if test="${not loop.last}">,</c:if></c:forEach>]</script>
<script id="allItemsData" type="application/json">[<c:forEach var="i" items="${allItems}" varStatus="loop">{"id":${i.id},"name":"<c:out value="${i.name} (verfügbar: ${i.availableQuantity})"/>"}<c:if test="${not loop.last}">,</c:if></c:forEach>]</script>

<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_events_list.js"></script>