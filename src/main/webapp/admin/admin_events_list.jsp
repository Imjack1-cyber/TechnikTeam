<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%-- The <java-time> taglib is no longer needed, so we can remove it. --%>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Eventverwaltung"/>
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

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

<a href="${pageContext.request.contextPath}/admin/events?action=new" class="btn" style="margin-bottom: 1rem;">Neues Event anlegen</a>

<div class="card">
    <table class="styled-table">
        <thead>
            <tr>
                <th>Name</th>
                <th>Datum & Uhrzeit</th>
                <th>Status</th>
                <th>Aktionen</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty eventList}">
                    <c:forEach var="event" items="${eventList}">
                        <tr>
                            <td data-label="Name">${event.name}</td>
                            <td data-label="Datum & Uhrzeit">
                                <%-- THE FIX IS HERE: Call our new getter method --%>
                                ${event.formattedEventDateTime} Uhr
                            </td>
                            <td data-label="Status">${event.status}</td>
                            <td data-label="Aktionen" class="action-buttons">
                                <a href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}" class="btn-small">Bearbeiten</a>
                                <a href="${pageContext.request.contextPath}/admin/events?action=assign&id=${event.id}" class="btn-small" style="background-color: var(--success-color);">Zuweisen</a>
                                <form action="${pageContext.request.contextPath}/admin/events" method="post" style="display:inline;">
                                   <input type="hidden" name="action" value="delete">
                                   <input type="hidden" name="id" value="${event.id}">
                                   <button type="submit" class="btn-small btn-danger" onclick="return confirm('Soll das Event \'${event.name}\' wirklich endgültig gelöscht werden?')">Löschen</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td colspan="4" style="text-align: center;">Keine Events gefunden.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<style>
    .action-buttons {
        display: flex;
        flex-wrap: wrap;
        gap: 5px;
    }
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />