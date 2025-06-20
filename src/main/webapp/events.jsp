<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Veranstaltungen"/></c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Anstehende Veranstaltungen</h1>
<%-- Feedback-Nachrichten --%>
<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>

<div class="card">
    <table class="styled-table">
        <thead>
            <tr><th>Veranstaltung</th><th>Datum & Uhrzeit</th><th>Dein Status</th><th>Aktion</th></tr>
        </thead>
        <tbody>
            <c:forEach var="event" items="${events}">
                <tr>
                    <td data-label="Veranstaltung">
                        <a href="${pageContext.request.contextPath}/eventDetails?id=${event.id}">${event.name}</a>
                        <c:if test="${event.status == 'KOMPLETT'}"><span class="status-badge">Team steht!</span></c:if>
                    </td>
                    <td data-label="Datum & Uhrzeit"><java-time:format value="${event.eventDateTime}" pattern="dd.MM.yyyy HH:mm" /> Uhr</td>
                    <td data-label="Dein Status">
                        <c:choose>
                            <c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}"><span class="status-angemeldet">Anwesend</span></c:when>
                            <c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}"><span class="status-abgemeldet">Abwesend</span></c:when>
                            <c:otherwise>Offen</c:otherwise>
                        </c:choose>
                    </td>
                    <td data-label="Aktion">
                        <form action="${pageContext.request.contextPath}/event-action" method="post" style="display: flex; gap: 5px;">
                            <input type="hidden" name="eventId" value="${event.id}">
                            <c:if test="${event.userAttendanceStatus != 'ANGEMELDET'}"><button type="submit" name="action" value="signup" class="btn-small">Anwesend</button></c:if>
                            <c:if test="${event.userAttendanceStatus != 'ABGEMELDET'}"><button type="submit" name="action" value="signoff" class="btn-small btn-danger">Abwesend</button></c:if>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />