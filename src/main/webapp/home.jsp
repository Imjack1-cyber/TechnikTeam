<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<head>
	<link rel="stylesheet" href="css/style.css">
</head>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Home"/>
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Willkommen zurück, ${sessionScope.username}!</h1>
<p>Hier ist eine schnelle Übersicht über anstehende Termine.</p>

<div class="dashboard">
    <div class="card">
        <h2 class="card-title">Anstehende Events</h2>
        <!-- This would be populated by a servlet -->
        <ul>
            <li>Sommerfest Aufbau - 15.08.2024 10:00 - Status: Angemeldet</li>
            <li>Theaterprobe - 02.09.2024 18:30 - Status: Offen</li>
            <!-- Use JSTL forEach to loop through data from servlet -->
            <c:forEach var="event" items="${upcomingEvents}">
                 <li>${event.name} - ${event.date} - Status: ${event.attendanceStatus}</li>
            </c:forEach>
        </ul>
    </div>

    <div class="card">
        <h2 class="card-title">Anstehende Lehrgänge</h2>
        <ul>
            <li>Tonlehrgang - 25.09.2024 17:00 - Leitung: Max Mustermann</li>
            <!-- Use JSTL forEach to loop through data from servlet -->
             <c:forEach var="course" items="${upcomingCourses}">
                 <li>${course.name} (${course.type}) - ${course.date} - Leitung: ${course.leader}</li>
            </c:forEach>
        </ul>
    </div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />