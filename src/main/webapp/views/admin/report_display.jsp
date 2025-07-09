<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Bericht: ${reportTitle}" />
</c:import>

<h1>${reportTitle}</h1>
<div class="table-controls">
	<a href="<c:url value='/admin/berichte'/>" class="btn btn-secondary"><i
		class="fas fa-arrow-left"></i> Zurück zur Berichtsübersicht</a> <a
		href="?report=${param.report}&export=csv" class="btn btn-success"><i
		class="fas fa-file-csv"></i> Als CSV exportieren</a>
</div>

<div class="table-wrapper">
	<c:if test="${empty reportData}">
		<p style="text-align: center; padding: 2rem;">Keine Daten für
			diesen Bericht verfügbar.</p>
	</c:if>
	<c:if test="${not empty reportData}">
		<table class="data-table">
			<c:choose>
				<c:when test="${param.report == 'event_participation'}">
					<thead>
						<tr>
							<th>Event-Name</th>
							<th>Zugewiesene Teilnehmer</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="row" items="${reportData}">
							<tr>
								<td><c:out value="${row.event_name}" /></td>
								<td><c:out value="${row.participant_count}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</c:when>

				<c:when test="${param.report == 'inventory_usage'}">
					<thead>
						<tr>
							<th>Artikelname</th>
							<th>Gesamtmenge entnommen</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="row" items="${reportData}">
							<tr>
								<td><c:out value="${row.item_name}" /></td>
								<td><c:out value="${row.total_quantity_checked_out}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</c:when>

				<c:when test="${param.report == 'user_activity'}">
					<thead>
						<tr>
							<th>Benutzername</th>
							<th>Anmeldungen (Events)</th>
							<th>Teilnahmen (Meetings)</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="row" items="${reportData}">
							<tr>
								<td><c:out value="${row.username}" /></td>
								<td><c:out value="${row.events_signed_up}" /></td>
								<td><c:out value="${row.meetings_attended}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</c:when>

				<c:otherwise>
					<thead>
						<tr>
							<c:forEach var="header" items="${reportData[0].keySet()}">
								<th><c:out value="${fn:replace(header, '_', ' ')}" /></th>
							</c:forEach>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="row" items="${reportData}">
							<tr>
								<c:forEach var="key" items="${reportData[0].keySet()}">
									<td><c:out value="${row[key]}" /></td>
								</c:forEach>
							</tr>
						</c:forEach>
					</tbody>
				</c:otherwise>
			</c:choose>
		</table>
	</c:if>

</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />