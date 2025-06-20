<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Event Details"/>
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<div class="details-container">
    
    <%-- Event Title and Status Badge --%>
    <h1>
        <c:out value="${event.name}" />
        <c:if test="${event.status == 'KOMPLETT'}">
            <span class="status-badge">Team steht!</span>
        </c:if>
    </h1>
    
    <%-- Event Date --%>
    <p class="details-subtitle">
        <strong>Datum:</strong> ${event.formattedEventDateTime} Uhr
    </p>
    
    <%-- Card for the Event Description --%>
    <div class="card">
        <h2 class="card-title">Beschreibung</h2>
        <p>${not empty event.description ? event.description : 'Keine Beschreibung für dieses Event vorhanden.'}</p>
    </div>
    
    <%-- Card for Required Skills/Qualifications --%>
    <div class="card">
        <h2 class="card-title">Benötigter Personalbedarf</h2>
        <c:choose>
            <c:when test="${not empty event.skillRequirements}">
                <ul class="details-list">
                    <c:forEach var="req" items="${event.skillRequirements}">
                        <li><strong>${req.courseName}:</strong> ${req.requiredPersons} Person(en) benötigt</li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <p>Für dieses Event werden keine speziellen Qualifikationen benötigt. Alle können sich anmelden.</p>
            </c:otherwise>
        </c:choose>
    </div>
    
    <%-- Admin-Only Section: Display Participants --%>
    <c:if test="${sessionScope.user.role == 'ADMIN'}">
        <div class="card">
            <h2 class="card-title">Teilnehmer-Status</h2>
            
            <c:choose>
                <c:when test="${not empty signedUpUsers}">
                    <ul class="details-list">
                        <c:forEach var="participant" items="${signedUpUsers}">
                            <li>
                                <a href="${pageContext.request.contextPath}/admin/users?action=details&id=${participant.id}">
                                    ${participant.username}
                                </a> - Angemeldet
                            </li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p>Bisher hat sich niemand für dieses Event angemeldet.</p>
                </c:otherwise>
            </c:choose>
            
            <%-- Display the final assigned team if the event is 'KOMPLETT' --%>
            <c:if test="${event.status == 'KOMPLETT' && not empty event.assignedAttendees}">
                <h3 style="margin-top: 1.5rem; border-top: 1px solid var(--border-color); padding-top: 1rem;">Final zugewiesenes Team:</h3>
                <ul class="details-list">
                    <c:forEach var="attendee" items="${event.assignedAttendees}">
                        <li>
                            <a href="${pageContext.request.contextPath}/admin/users?action=details&id=${attendee.id}">
                                ${attendee.username}
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </c:if>
        </div>
    </c:if>
    
    <div style="margin-top: 2rem;">
        <a href="${pageContext.request.contextPath}/events" class="btn">Zurück zur Event-Übersicht</a>
    </div>

</div>

<%-- Specific CSS for this details page --%>
<style>
    .details-container {
        max-width: 800px;
        margin: 0 auto;
    }
    .details-container .card {
        margin-bottom: 1.5rem;
    }
    .details-subtitle {
        font-style: italic;
        color: #6c757d; /* A subtle grey color */
        margin-top: -1rem;
        margin-bottom: 1.5rem;
        font-size: 1.1rem;
    }
    .details-list {
        list-style-type: none;
        padding-left: 0;
    }
    .details-list li {
        padding: 0.5rem 0;
        border-bottom: 1px solid var(--border-color);
    }
    .details-list li:last-child {
        border-bottom: none;
    }
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />