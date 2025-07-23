<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Mein Profil" />
</c:import>

<h1>Mein Profil</h1>
<p>Hier finden Sie eine Übersicht Ihrer Daten, Qualifikationen und
	Aktivitäten.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="responsive-dashboard-grid">
	<div class="card">
		<h2 class="card-title">Stammdaten</h2>
		<ul class="details-list">
			<li><strong>Benutzername:</strong> <span><c:out
						value="${sessionScope.user.username}" /></span></li>
			<li><strong>Rolle:</strong> <span><c:out
						value="${sessionScope.user.roleName}" /></span></li>
			<li><strong>Jahrgang:</strong> <span><c:out
						value="${sessionScope.user.classYear}" /></span></li>
			<li><strong>Klasse:</strong> <span><c:out
						value="${sessionScope.user.className}" /></span></li>
			<li><strong>E-Mail:</strong> <span><c:out
						value="${not empty sessionScope.user.email ? sessionScope.user.email : 'Nicht hinterlegt'}" /></span></li>
			<li style="align-items: center; gap: 1rem;"><strong>Chat-Farbe:</strong>
				<form action="${pageContext.request.contextPath}/profil"
					method="post"
					style="display: flex; align-items: center; gap: 0.5rem;">
					<input type="hidden" name="csrfToken"
						value="${sessionScope.csrfToken}"> <input type="color"
						name="chatColor"
						value="<c:out value='${not empty sessionScope.user.chatColor ? sessionScope.user.chatColor : "#E9ECEF"}'/>"
						title="Wähle deine Chat-Farbe">
					<button type="submit" class="btn btn-small">Speichern</button>
				</form></li>
			<li><a href="${pageContext.request.contextPath}/passwort"
				class="btn btn-secondary">Passwort ändern</a></li>
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

	<div class="card" style="grid-column: 1/-1;">
		<h2 class="card-title">Meine Abzeichen</h2>
		<c:if test="${empty achievements}">
			<p>Du hast noch keine Abzeichen verdient. Nimm an Events teil, um
				sie freizuschalten!</p>
		</c:if>
		<div style="display: flex; flex-wrap: wrap; gap: 1rem;">
			<c:forEach var="ach" items="${achievements}">
				<div class="card"
					style="flex: 1; min-width: 250px; text-align: center;">
					<i class="fas ${ach.iconClass}"
						style="font-size: 3rem; color: var(--primary-color); margin-bottom: 1rem;"></i>
					<h4 style="margin: 0;">
						<c:out value="${ach.name}" />
					</h4>
					<p style="color: var(--text-muted-color); font-size: 0.9rem;">
						<c:out value="${ach.description}" />
					</p>
					<small>Verdient am: <c:out value="${ach.formattedEarnedAt}" /></small>
				</div>
			</c:forEach>
		</div>
	</div>

</div>

<div class="card">
	<h2 class="card-title">Meine Event-Historie</h2>

	<!-- Desktop Table View -->
	<div class="desktop-table-wrapper">
		<div class="table-wrapper"
			style="max-height: 500px; overflow-y: auto;">
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
								href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}"><c:out
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

	<!-- Mobile Card View -->
	<div class="mobile-card-list">
		<c:if test="${empty eventHistory}">
			<div class="list-item-card">
				<p>Keine Event-Historie vorhanden.</p>
			</div>
		</c:if>
		<c:forEach var="event" items="${eventHistory}">
			<div class="list-item-card">
				<h3 class="card-title">
					<a
						href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}"><c:out
							value="${event.name}" /></a>
				</h3>
				<div class="card-row">
					<span>Datum:</span> <strong><c:out
							value="${event.formattedEventDateTime}" /> Uhr</strong>
				</div>
				<div class="card-row">
					<span>Dein Status:</span> <strong><c:out
							value="${event.userAttendanceStatus}" /></strong>
				</div>
				<div class="card-actions">
					<c:if
						test="${event.status == 'ABGESCHLOSSEN' && event.userAttendanceStatus == 'ZUGEWIESEN'}">
						<a
							href="${pageContext.request.contextPath}/feedback?action=submit&eventId=${event.id}"
							class="btn btn-small">Feedback geben</a>
					</c:if>
				</div>
			</div>
		</c:forEach>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />