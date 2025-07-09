<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Eventverwaltung" />
</c:import>

<c:set var="userPermissions" value="${sessionScope.user.permissions}" />
<c:set var="hasMasterAccess"
	value="${userPermissions.contains('ACCESS_ADMIN_PANEL')}" />

<h1>
	<i class="fas fa-calendar-check"></i> Eventverwaltung
</h1>
<p>Hier können Sie Events erstellen, bearbeiten, Personal zuweisen
	und den Status verwalten.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="table-controls">
	<c:if
		test="${hasMasterAccess or userPermissions.contains('EVENT_CREATE')}">
		<button type="button" class="btn btn-success" id="new-event-btn">
			<i class="fas fa-plus"></i> Neues Event anlegen
		</button>
	</c:if>
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
				<c:set var="isLeader"
					value="${sessionScope.user.id == event.leaderUserId}" />
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}"><c:out
								value="${event.name}" /></a></td>
					<td><c:out value="${event.formattedEventDateTimeRange}" /></td>
					<td><span
						class="status-badge ${event.status == 'LAUFEND' ? 'status-warn' : (event.status == 'ABGESCHLOSSEN' or event.status == 'ABGESAGT') ? 'status-info' : 'status-ok'}"><c:out
								value="${event.status}" /></span></td>
					<td style="display: flex; gap: 5px; flex-wrap: wrap;"><c:if
							test="${hasMasterAccess or userPermissions.contains('EVENT_UPDATE') or isLeader}">
							<button type="button" class="btn btn-small edit-event-btn"
								data-event-id="${event.id}">Bearbeiten</button>
						</c:if> <c:if test="${event.status != 'ABGESCHLOSSEN'}">
							<c:if
								test="${hasMasterAccess or userPermissions.contains('EVENT_MANAGE_ASSIGNMENTS') or isLeader}">
								<button type="button"
									class="btn btn-small btn-success assign-users-btn"
									data-event-id="${event.id}"
									data-event-name="${fn:escapeXml(event.name)}">Zuweisen</button>
							</c:if>
							<c:if
								test="${(hasMasterAccess or userPermissions.contains('EVENT_UPDATE') or isLeader) and (event.status == 'GEPLANT' || event.status == 'KOMPLETT')}">
								<form
									action="${pageContext.request.contextPath}/admin/veranstaltungen"
									method="post" style="display: inline;" class="js-confirm-form"
									data-confirm-message="Event '${fn:escapeXml(event.name)}' wirklich starten? Der Chat wird aktiviert.">
									<input type="hidden" name="csrfToken"
										value="${sessionScope.csrfToken}"> <input
										type="hidden" name="action" value="updateStatus"><input
										type="hidden" name="id" value="${event.id}"><input
										type="hidden" name="newStatus" value="LAUFEND">
									<button type="submit" class="btn btn-small btn-warning">Starten</button>
								</form>
							</c:if>
							<c:if
								test="${(hasMasterAccess or userPermissions.contains('EVENT_UPDATE') or isLeader) and event.status == 'LAUFEND'}">
								<form
									action="${pageContext.request.contextPath}/admin/veranstaltungen"
									method="post" style="display: inline;" class="js-confirm-form"
									data-confirm-message="Event '${fn:escapeXml(event.name)}' wirklich abschließen?">
									<input type="hidden" name="csrfToken"
										value="${sessionScope.csrfToken}"> <input
										type="hidden" name="action" value="updateStatus"><input
										type="hidden" name="id" value="${event.id}"><input
										type="hidden" name="newStatus" value="ABGESCHLOSSEN">
									<button type="submit" class="btn btn-small"
										style="background-color: var(--text-muted-color);">Abschließen</button>
								</form>
							</c:if>
						</c:if> <c:if
							test="${hasMasterAccess or userPermissions.contains('EVENT_DELETE')}">
							<form
								action="${pageContext.request.contextPath}/admin/veranstaltungen"
								method="post" class="inline-form js-confirm-form"
								data-confirm-message="Soll das Event '${fn:escapeXml(event.name)}' wirklich endgültig gelöscht werden?">
								<input type="hidden" name="csrfToken"
									value="${sessionScope.csrfToken}"> <input type="hidden"
									name="action" value="delete"><input type="hidden"
									name="id" value="${event.id}">
								<button type="submit" class="btn btn-small btn-danger">Löschen</button>
							</form>
						</c:if></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list searchable-table">
	<c:forEach var="event" items="${eventList}">
		<c:set var="isLeader"
			value="${sessionScope.user.id == event.leaderUserId}" />
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
				<span>Status:</span> <span><span
					class="status-badge ${event.status == 'LAUFEND' ? 'status-warn' : (event.status == 'ABGESCHLOSSEN' or event.status == 'ABGESAGT') ? 'status-info' : 'status-ok'}"><c:out
							value="${event.status}" /></span></span>
			</div>
			<div class="card-actions">
				<c:if
					test="${hasMasterAccess or userPermissions.contains('EVENT_UPDATE') or isLeader}">
					<button type="button" class="btn btn-small edit-event-btn"
						data-event-id="${event.id}">Bearbeiten</button>
				</c:if>
				<c:if test="${event.status != 'ABGESCHLOSSEN'}">
					<c:if
						test="${hasMasterAccess or userPermissions.contains('EVENT_MANAGE_ASSIGNMENTS') or isLeader}">
						<button type="button"
							class="btn btn-small btn-success assign-users-btn"
							data-event-id="${event.id}"
							data-event-name="${fn:escapeXml(event.name)}">Zuweisen</button>
					</c:if>
					<c:if
						test="${(hasMasterAccess or userPermissions.contains('EVENT_UPDATE') or isLeader) and (event.status == 'GEPLANT' || event.status == 'KOMPLETT')}">
						<form
							action="${pageContext.request.contextPath}/admin/veranstaltungen"
							method="post" style="display: inline;" class="js-confirm-form"
							data-confirm-message="Event '${fn:escapeXml(event.name)}' wirklich starten? Der Chat wird aktiviert.">
							<input type="hidden" name="csrfToken"
								value="${sessionScope.csrfToken}"> <input type="hidden"
								name="action" value="updateStatus"><input type="hidden"
								name="id" value="${event.id}"><input type="hidden"
								name="newStatus" value="LAUFEND">
							<button type="submit" class="btn btn-small btn-warning">Starten</button>
						</form>
					</c:if>
					<c:if
						test="${(hasMasterAccess or userPermissions.contains('EVENT_UPDATE') or isLeader) and event.status == 'LAUFEND'}">
						<form
							action="${pageContext.request.contextPath}/admin/veranstaltungen"
							method="post" style="display: inline;" class="js-confirm-form"
							data-confirm-message="Event '${fn:escapeXml(event.name)}' wirklich abschließen?">
							<input type="hidden" name="csrfToken"
								value="${sessionScope.csrfToken}"> <input type="hidden"
								name="action" value="updateStatus"><input type="hidden"
								name="id" value="${event.id}"><input type="hidden"
								name="newStatus" value="ABGESCHLOSSEN">
							<button type="submit" class="btn btn-small"
								style="background-color: var(--text-muted-color);">Abschließen</button>
						</form>
					</c:if>
				</c:if>
				<c:if
					test="${hasMasterAccess or userPermissions.contains('EVENT_DELETE')}">
					<form
						action="${pageContext.request.contextPath}/admin/veranstaltungen"
						method="post" class="inline-form js-confirm-form"
						data-confirm-message="Soll das Event '${fn:escapeXml(event.name)}' wirklich endgültig gelöscht werden?">
						<input type="hidden" name="csrfToken"
							value="${sessionScope.csrfToken}"> <input type="hidden"
							name="action" value="delete"><input type="hidden"
							name="id" value="${event.id}">
						<button type="submit" class="btn btn-small btn-danger">Löschen</button>
					</form>
				</c:if>
			</div>
		</div>
	</c:forEach>
</div>

<%-- Assume event_modals.jspf contains the file input and now needs the warning message --%>
<%-- This is a hypothetical change, as the file isn't provided, but it's crucial for the JS fix --%>
<%--
    <!-- Inside event_modals.jspf -->
    <div class="form-group">
        <label for="attachment">Anhang</label>
        <input type="file" name="attachment" id="attachment" class="file-input" data-max-size="41943040"> <%-- 40MB --%>
<small class="file-size-warning" style="color: red; display: none;">Datei
	ist zu groß! (Max. 40MB)</small>
</div>
--%>
<jsp:include page="/WEB-INF/jspf/event_modals.jspf" />

<script id="allCoursesData" type="application/json">${allCoursesJson}</script>
<script id="allItemsData" type="application/json">${allItemsJson}</script>
<script id="allKitsData" type="application/json">${allKitsJson}</script>

<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_events_list.js"></script>