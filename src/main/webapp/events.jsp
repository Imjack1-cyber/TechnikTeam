<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<head>
	<link rel="stylesheet" href="css/style.css">
</head>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Events"/>
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Anstehende Events</h1>

<div class="event-list">
    <c:forEach var="event" items="${events}">
        <div class="card event-card ${event.status == 'KOMPLETT' ? 'event-complete' : ''}">
            <h3>
                ${event.name}
                <c:if test="${event.status == 'KOMPLETT'}">
                    <span class="status-badge">Team steht!</span>
                </c:if>
            </h3>
            <p><strong>Datum:</strong> <fmt:formatDate value="${event.eventDateTime}" type="both" dateStyle="long" timeStyle="short" /></p>
            <p><strong>Deine Teilnahme:</strong> 
                <c:choose>
                    <c:when test="${event.userAttendanceStatus == 'ANGEMELDET'}"><span class="status-angemeldet">Angemeldet</span></c:when>
                    <c:when test="${event.userAttendanceStatus == 'ABGEMELDET'}"><span class="status-abgemeldet">Abgemeldet</span></c:when>
                    <c:otherwise>Nicht gemeldet</c:otherwise>
                </c:choose>
            </p>
            
            <div class="event-actions">
                <button class="btn-small btn-details">Details anzeigen</button>
                <form action="${pageContext.request.contextPath}/event-action" method="post" style="display:inline-block; margin-left:10px;">
                    <input type="hidden" name="eventId" value="${event.id}">
                    <c:if test="${event.userAttendanceStatus != 'ANGEMELDET'}"><button type="submit" name="action" value="signup" class="btn-small">Anmelden</button></c:if>
                    <c:if test="${event.userAttendanceStatus == 'ANGEMELDET'}"><button type="submit" name="action" value="signoff" class="btn-small btn-danger">Abmelden</button></c:if>
                </form>
            </div>
            
            <div class="event-details" style="display: none;">
                <h4>Beschreibung</h4>
                <p>${empty event.description ? 'Keine Beschreibung vorhanden.' : event.description}</p>
                
                <c:if test="${not empty event.skillRequirements}">
                    <h4>Benötigter Personalbedarf</h4>
                    <ul><c:forEach var="req" items="${event.skillRequirements}"><li>${req.skillName}: ${req.requiredPersons} Person(en)</li></c:forEach></ul>
                </c:if>
                
                <c:if test="${event.status == 'KOMPLETT' && not empty event.assignedAttendees}">
                    <h4>Zugewiesene Teilnehmer</h4>
                    <ul><c:forEach var="attendee" items="${event.assignedAttendees}"><li>${attendee.username}</li></c:forEach></ul>
                </c:if>
            </div>
        </div>
    </c:forEach>
    <c:if test="${empty events}"><p>Aktuell stehen keine Events an, für die du qualifiziert bist.</p></c:if>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />