<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%-- Header must be included first --%>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Meeting-Details"/></c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf"/>

<div style="max-width: 800px; margin: 0 auto;">
    <h1>${meeting.name}</h1>
    <p style="color: var(--text-muted-color); margin-top: -1rem;">
        Teil des Lehrgangs: <strong>${meeting.parentCourseName}</strong>
    </p>

    <div class="card">
        <h2>Details</h2>
        <ul style="list-style: none; padding: 0;">
            <li style="padding: 0.5rem 0;"><strong>Termin:</strong> ${meeting.meetingDateTime}</li>
            <li style="padding: 0.5rem 0;">
                <strong>Leitung:</strong>
                <c:choose>
                    <c:when test="${not empty meeting.leader}">${meeting.leader}</c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </li>
        </ul>
    </div>
    
    <div class="card">
        <h2>Beschreibung</h2>
        <p>
            <c:choose>
                <c:when test="${not empty meeting.description}">${meeting.description}</c:when>
                <c:otherwise>Keine Beschreibung vorhanden.</c:otherwise>
            </c:choose>
        </p>
    </div>
    
    <a href="${pageContext.request.contextPath}/lehrgaenge" class="btn" style="margin-top: 1rem;">« Zurück zur Übersicht</a>
</div>

<%-- Footer must be included last --%>
<c:import url="/WEB-INF/jspf/footer.jspf" />