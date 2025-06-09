<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time" %>

<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Events"/></c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Anstehende Events</h1>

<!-- Erfolgs- und Fehlermeldungen -->
<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><p class="error-message">${sessionScope.errorMessage}</p><c:remove var="errorMessage" scope="session" /></c:if>

<div class="card">
    <table class="styled-table">
        <thead>
            <tr>
                <th>Event</th>
                <th>Datum &amp; Uhrzeit</th>
                <th>Dein Status</th>
                <th>Aktion</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="event" items="${events}">
                <tr class="event-row ${event.status == 'KOMPLETT' ? 'event-complete' : ''}">
                    <td>
                        <a href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a>
                        <c:if test="${event.status == 'KOMPLETT'}">
                            <span class="status-badge">Team steht!</span>
                        </c:if>
                    </td>
                    <td><java-time:format value="${event.eventDateTime}" pattern="dd.MM.yyyy HH:mm" /> Uhr</td>
                    
                    <%-- Angepasste Statusanzeige --%>
                    <td>
                        <c:choose>
                            <c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}"><span class="status-angemeldet">Anwesend</span></c:when>
                            <c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}"><span class="status-abgemeldet">Abwesend</span></c:when>
                            <c:otherwise><span style="color: grey;">Offen</span></c:otherwise>
                        </c:choose>
                    </td>
                    
                    <%-- Angepasste Aktions-Buttons --%>
                    <td>
                        <form action="${pageContext.request.contextPath}/event-action" method="post" style="display: flex; gap: 5px;">
                            <input type="hidden" name="eventId" value="${event.id}">
                            
                            <%-- Zeige "Anwesend"-Button, wenn nicht bereits anwesend --%>
                            <c:if test="${event.userAttendanceStatus != 'ANGEMELDET'}">
                                <button type="submit" name="action" value="signup" class="btn-small">Anwesend</button>
                            </c:if>
                            
                            <%-- Zeige "Abwesend"-Button, wenn nicht bereits abwesend --%>
                            <c:if test="${event.userAttendanceStatus != 'ABGEMELDET'}">
                                <button type="submit" name="action" value="signoff" class="btn-small btn-danger">Abwesend</button>
                            </c:if>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty events}">
                <tr><td colspan="4" style="text-align: center;">Aktuell stehen keine Events an, für die du qualifiziert bist.</td></tr>
            </c:if>
        </tbody>
    </table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />