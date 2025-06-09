<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Event-Verwaltung" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<%-- ... header & navigation ... --%>
<h1>Event-Verwaltung</h1>
<a href="${pageContext.request.contextPath}/admin/events?action=new" class="btn" ...>Neues Event erstellen</a>
<div class="card">
    <table class="styled-table">
        <thead><tr><th>Name</th><th>Datum & Uhrzeit</th><th>Status</th><th>Aktionen</th></tr></thead>
        <tbody>
            <c:forEach var="event" items="${eventList}">
                <tr>
                    <td>${event.name}</td>
                    <td><java-time:format value="${event.eventDateTime}" pattern="dd.MM.yyyy HH:mm"/> Uhr</td>
                    <td>${event.status}</td>
                    <td>
                        <a href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}" class="btn-small">Bearbeiten</a>
                        <a href="${pageContext.request.contextPath}/admin/events?action=assign&id=${event.id}" class="btn-small">Teilnehmer</a>
                        
                        <%-- KORREKTES LÖSCH-FORMULAR --%>
                        <form action="${pageContext.request.contextPath}/admin/events" method="post" style="display:inline;">
                           <input type="hidden" name="action" value="delete">
                           <input type="hidden" name="id" value="${event.id}">
                           <button type="submit" class="btn-small btn-danger" onclick="return confirm('Event \'${event.name}\' wirklich löschen? Alle Anmeldungen gehen verloren!')">Löschen</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
<%-- ... footer ... --%>
<c:import url="/WEB-INF/jspf/footer.jspf" />