<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time" %> <%-- WICHTIG: Diesen Import hinzufügen --%>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Home"/>
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Willkommen zurück, ${sessionScope.user.username}!</h1>
<p>Hier ist eine schnelle Übersicht über anstehende Termine.</p>

<div class="dashboard">
    <div class="card">
        <h2 class="card-title">Anstehende Events</h2>
        <ul>
            <%-- Dieser Teil für Events muss ebenfalls angepasst werden --%>
            <c:forEach var="event" items="${upcomingEvents}">
                 <li>${event.name} - <java-time:format value="${event.eventDateTime}" pattern="dd.MM.yyyy HH:mm"/> Uhr</li>
            </c:forEach>
            <c:if test="${empty upcomingEvents}"><p>Keine anstehenden Events.</p></c:if>
        </ul>
    </div>

    <div class="card">
        <h2 class="card-title">Anstehende Lehrgänge</h2>
        <ul>
            <%-- FIX HIER: Verwende course.courseDateTime und die java-time taglib --%>
            <c:forEach var="course" items="${upcomingCourses}">
                 <li>${course.name} (${course.type}) - <java-time:format value="${course.courseDateTime}" pattern="dd.MM.yyyy"/> - Leitung: ${course.leader}</li>
            </c:forEach>
            <c:if test="${empty upcomingCourses}"><p>Keine anstehenden Lehrgänge.</p></c:if>
        </ul>
    </div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />