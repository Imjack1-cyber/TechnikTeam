<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="../../jspf/main_header.jspf">
	<c:param name="pageTitle"
		value="Benutzerdetails: ${userToView.username}" />
</c:import>

<h1>
	Benutzerdetails:
	<c:out value="${userToView.username}" />
</h1>
<a href="${pageContext.request.contextPath}/admin/users"
	style="display: inline-block; margin-bottom: 1rem;"> « Zurück zur
	Benutzerliste </a>

<c:import url="../../jspf/message_banner.jspf" />

<div class="responsive-dashboard-grid">
	<div class="card">
		<h2 class="card-title">Stammdaten</h2>
		<ul class="details-list">
			<li><strong>Benutzername:</strong> <c:out
					value="${userToView.username}" /></li>
			<li><strong>Rolle:</strong> <c:out
					value="${userToView.roleName}" /></li>
			<li><strong>Jahrgang:</strong> <c:out
					value="${userToView.classYear}" /></li>
			<li><strong>Klasse:</strong> <c:out
					value="${userToView.className}" /></li>
			<li><strong>E-Mail:</strong> <c:out
					value="${not empty userToView.email ? userToView.email : 'Nicht hinterlegt'}" /></li>
			<li><strong>Registriert seit:</strong> <c:out
					value="${userToView.formattedCreatedAt}" /> Uhr</li>
		</ul>
	</div>

	<div class="card">
		<h2 class="card-title">Event-Teilnahmehistorie</h2>
		<div class="table-wrapper"
			style="max-height: 450px; overflow-y: auto;">
			<table class="data-table">
				<thead>
					<tr>
						<th>Event</th>
						<th>Datum</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<c:if test="${empty eventHistory}">
						<tr>
							<td colspan="3" style="text-align: center;">Keine
								Event-Historie vorhanden.</td>
						</tr>
					</c:if>
					<c:forEach var="event" items="${eventHistory}">
						<tr>
							<td><a
								href="${pageContext.request.contextPath}/events/details?id=${event.id}"><c:out
										value="${event.name}" /></a></td>
							<td><c:out value="${event.formattedEventDateTime}" /> Uhr</td>
							<td><c:out value="${event.userAttendanceStatus}" /></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</div>

<style>
.details-list {
	list-style-type: none;
	padding-left: 0;
}

.details-list li {
	padding: 0.75rem 0;
	border-bottom: 1px solid var(--border-color);
	display: flex;
	justify-content: space-between;
}

.details-list li:last-child {
	border-bottom: none;
}
</style>

<c:import url="../../jspf/main_footer.jspf" />