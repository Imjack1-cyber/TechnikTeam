<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%-- The <java-time> taglib is no longer needed, so we can remove it. --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Eventverwaltung" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<!-- 
admin_events_list.jsp: Displays a table of all events with options to edit, delete, or assign users.

    Served by: AdminEventServlet (doGet with action=list).

    Submits to: AdminEventServlet (doPost with action=delete) for deletion.

    Dependencies: Includes header.jspf, admin_navigation.jspf, footer.jspf. Links to admin_event_form.jsp (via action=edit) and admin_event_assign.jsp (via action=assign).
-->

<h1>Eventverwaltung</h1>

<%-- Display feedback messages --%>
<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<a href="${pageContext.request.contextPath}/admin/events?action=new" class="btn" style="margin-bottom: 1.5rem;">Neues Event anlegen</a>

<!-- MOBILE LAYOUT: CARD LIST -->
<div class="mobile-card-list">
    <c:forEach var="event" items="${eventList}">
        <div class="list-item-card">
            <h3 class="card-title">${event.name}</h3>
            <div class="card-row"><span>Datum:</span> <span>${event.formattedEventDateTime} Uhr</span></div>
            <div class="card-row"><span>Status:</span> <span>${event.status}</span></div>
            <div class="card-actions">
                <a href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}" class="btn btn-small">Bearbeiten</a>
                <a href="${pageContext.request.contextPath}/admin/events?action=assign&id=${event.id}" class="btn btn-small" style="background-color: var(--success-color);">Zuweisen</a>
                <form action="${pageContext.request.contextPath}/admin/events" method="post" style="display:inline;">
                    <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${event.id}">
                    <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Soll das Event \'${event.name}\' wirklich endgültig gelöscht werden?')">Löschen</button>
                </form>
            </div>
        </div>
    </c:forEach>
</div>

<!-- DESKTOP LAYOUT: TABLE -->
<table class="desktop-table">
    <thead>
        <tr><th>Name</th><th>Datum & Uhrzeit</th><th>Status</th><th>Aktionen</th></tr>
    </thead>
    <tbody>
        <c:forEach var="event" items="${eventList}">
            <tr>
                <td>${event.name}</td>
                <td>${event.formattedEventDateTime} Uhr</td>
                <td>${event.status}</td>
                <td style="display: flex; gap: 5px; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}" class="btn btn-small">Bearbeiten</a>
                    <a href="${pageContext.request.contextPath}/admin/events?action=assign&id=${event.id}" class="btn btn-small" style="background-color: var(--success-color);">Zuweisen</a>
                    <form action="${pageContext.request.contextPath}/admin/events" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${event.id}">
                        <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Soll das Event \'${event.name}\' wirklich endgültig gelöscht werden?')">Löschen</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<c:import url="/WEB-INF/jspf/footer.jspf" />