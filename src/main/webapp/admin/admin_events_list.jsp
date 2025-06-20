<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Veranstaltungsverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Veranstaltungsverwaltung</h1>
<a href="${pageContext.request.contextPath}/admin/events?action=new" class="btn" style="margin-bottom: 1rem;">Neue Veranstaltung anlegen</a>
<div class="card">
    <table class="styled-table">
        <thead>
            <tr><th>Name</th><th>Datum & Uhrzeit</th><th>Status</th><th>Aktionen</th></tr>
        </thead>
        <tbody>
            <c:forEach var="event" items="${eventList}">
                <tr>
                    <td data-label="Name">${event.name}</td>
                    <td data-label="Datum & Uhrzeit"><java-time:format value="${event.eventDateTime}" pattern="dd.MM.yyyy HH:mm" /> Uhr</td>
                    <td data-label="Status">${event.status}</td>
                    <td data-label="Aktionen">
                        <a href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}" class="btn-small">Bearbeiten</a>
                        <a href="${pageContext.request.contextPath}/admin/events?action=assign&id=${event.id}" class="btn-small" style="background-color: var(--success-color);">Teilnehmer</a>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
<c:import url="/WEB-INF/jspf/footer.jspf" />