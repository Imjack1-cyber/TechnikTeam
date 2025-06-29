<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="../../jspf/main_header.jspf">
	<c:param name="pageTitle" value="Mein Profil" />
</c:import>

<h1>Mein Profil</h1>
<p>Hier finden Sie eine Übersicht Ihrer Daten, Qualifikationen und
	Aktivitäten.</p>

<c:import url="../../jspf/message_banner.jspf" />

<div class="responsive-dashboard-grid">
	<div class="card">
		<h2 class="card-title">Stammdaten</h2>
		<ul class="details-list">
			<li><strong>Benutzername:</strong> <c:out
					value="${sessionScope.user.username}" /></li>
			<li><strong>Rolle:</strong> <c:out
					value="${sessionScope.user.roleName}" /></li>
			<li><strong>Jahrgang:</strong> <c:out
					value="${sessionScope.user.classYear}" /></li>
			<li><strong>Klasse:</strong> <c:out
					value="${sessionScope.user.className}" /></li>
			<li><strong>E-Mail:</strong> <c:out
					value="${not empty sessionScope.user.email ? sessionScope.user.email : 'Nicht hinterlegt'}" /></li>
		</ul>
	</div>

	<div class="card">
		<h2 class="card-title">Meine Qualifikationen</h2>
		<div class="table-wrapper"
			style="max-height: 400px; overflow-y: auto;">
			<table class="data-table">
				<thead>
					<tr>
						<th>Lehrgang</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<c:if test="${empty qualifications}">
						<tr>
							<td colspan="2">Keine Qualifikationen erworben.</td>
						</tr>
					</c:if>
					<c:forEach var="qual" items="${qualifications}">
						<tr>
							<td><c:out value="${qual.courseName}" /></td>
							<td><c:out value="${qual.status}" /></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</div>

<div class="card">
	<h2 class="card-title">Meine Event-Historie</h2>
	<div class="table-wrapper" style="max-height: 500px; overflow-y: auto;">
		<table class="data-table">
			<thead>
				<tr>
					<th>Event</th>
					<th>Datum</th>
					<th>Dein Status</th>
					<th>Feedback</th>
				</tr>
			</thead>
			<tbody>
				<c:if test="${empty eventHistory}">
					<tr>
						<td colspan="4">Keine Event-Historie vorhanden.</td>
					</tr>
				</c:if>
				<c:forEach var="event" items="${eventHistory}">
					<tr>
						<td><a
							href="${pageContext.request.contextPath}/events/details?id=${event.id}"><c:out
									value="${event.name}" /></a></td>
						<td><c:out value="${event.formattedEventDateTime}" /> Uhr</td>
						<td><c:out value="${event.userAttendanceStatus}" /></td>
						<td><c:if
								test="${event.status == 'ABGESCHLOSSEN' && event.userAttendanceStatus == 'ZUGEWIESEN'}">
								<a
									href="${pageContext.request.contextPath}/feedback?action=submit&eventId=${event.id}"
									class="btn btn-small">Feedback geben</a>
							</c:if></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<c:import url="../../jspf/main_footer.jspf" />