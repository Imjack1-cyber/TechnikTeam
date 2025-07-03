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
	<button type="button" class="btn" id="new-event-btn">
		<i class="fas fa-plus"></i> Neues Event anlegen
	</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<div class="table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name</th>
				<th class="sortable" data-sort-type="string">Zeitraum</th>
				<th class="sortable" data-sort-type="string">Status</th>
				<th style="width: 450px;">Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:if test="${empty eventList}">
				<tr>
					<td colspan="4" style="text-align: center;">Keine Events
						gefunden.</td>
				</tr>
			</c:if>
			<c:forEach var="event" items="${eventList}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}">${event.name}</a></td>
					<td>${event.formattedEventDateTimeRange}</td>
					<td><span
						class="status-badge ${event.status == 'KOMPLETT' or event.status == 'ZUGEWIESEN' ? 'status-ok' : event.status == 'LAUFEND' ? 'status-warn' : event.status == 'ABGESCHLOSSEN' ? 'status-info' : 'status-info'}">${event.status}</span></td>
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

<%@ include file="/WEB-INF/jspf/event_modals.jspf"%>
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script type="text/javascript" src="/js/admin/admin_events_list.js"></script>