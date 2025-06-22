<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%--
admin_user_details.jsp

This JSP displays a detailed view of a single user for an administrator.
It shows the user's master data (username, role, class info) and a table
listing the user's event participation history. The user data itself is
no longer editable on this page; editing is handled by a modal on the main user list.

    It is served by: AdminUserServlet (doGet with action=details).

    Expected attributes:

        'userToView' (de.technikteam.model.User): The user whose details are being viewed.

        'eventHistory' (List<de.technikteam.model.Event>): The user's event history.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Benutzerdetails" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>
	Benutzerdetails:
	<c:out value="${userToView.username}" />
</h1>
<a href="${pageContext.request.contextPath}/admin/users"
	style="display: inline-block; margin-bottom: 1rem;"> &laquo; Zurück
	zur Benutzerliste </a>

<c:if test="

        
notemptysessionScope.successMessage">
	<pclass="success−message">notemptysessionScope.successMessage"><pclass="success−message">



	{sessionScope.successMessage}
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="

        
notemptysessionScope.infoMessage">
	<pclass="info−message">notemptysessionScope.infoMessage"><pclass="info−message">



	{sessionScope.infoMessage}
	</p>
	<c:remove var="infoMessage" scope="session" />
</c:if>
<c:if test="

        
notemptysessionScope.errorMessage">
	<pclass="error−message">notemptysessionScope.errorMessage"><pclass="error−message">



	{sessionScope.errorMessage}
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>
<div class="responsive-dashboard-grid">
	<%-- Master Data --%>
	<div class="card">
		<h2 class="card-title">Stammdaten</h2>
		<ul class="details-list">
			<li><strong>Benutzername:</strong> ${userToView.username}</li>
			<li><strong>Rolle:</strong> ${userToView.role}</li>
			<li><strong>Jahrgang:</strong> ${userToView.classYear}</li>
			<li><strong>Klasse:</strong> ${userToView.className}</li>
			<li><strong>Registriert seit:</strong>
				${userToView.formattedCreatedAt} Uhr</li>
		</ul>
	</div>
	<%-- Event History --%>
	<div class="card">
		<h2 class="card-title">Event-Teilnahmehistorie</h2>
		<div class="desktop-table-wrapper"
			style="box-shadow: none; border: none; max-height: 450px; overflow-y: auto;">
			<table class="desktop-table">
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
							<td colspan="3">Keine Event-Historie vorhanden.</td>
						</tr>
					</c:if>
					<c:forEach var="event" items="${eventHistory}">
						<tr>
							<td><a
								href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a></td>
							<td>${event.formattedEventDateTime}Uhr</td>
							<td>${event.userAttendanceStatus}</td>
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

<c:import url="/WEB-INF/jspf/footer.jspf" />
