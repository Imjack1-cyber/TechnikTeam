<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lehrgangsdetails"/></c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<div class="details-container">
    <h1>${course.name}</h1>
    <p class="details-info">
        <strong>Termin:</strong> <strong>Datum:</strong> ${course.formattedCourseDateTime} Uhr
    </p>
    <p class="details-info">
        <strong>Leitung:</strong> ${not empty course.leader ? course.leader : 'N/A'}
    </p>
    
    <div class="card">
        <h2 class="card-title">Beschreibung</h2>
        <p>${not empty course.description ? course.description : 'Keine Beschreibung vorhanden.'}</p>
    </div>
    
    <c:if test="${sessionScope.user.role == 'ADMIN'}">
        <div class="card">
            <h2 class="card-title">Angemeldete Teilnehmer</h2>
            <c:choose>
                <c:when test="${not empty participants}">
                    <ul>
                        <c:forEach var="p" items="${participants}">
                            <li><a href="${pageContext.request.contextPath}/admin/users?action=details&id=${p.id}">${p.username}</a></li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p>Bisher hat sich niemand für diesen Lehrgang angemeldet.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>

<style>
.details-info { font-style: italic; color: #666; margin-bottom: 0.5rem; }
.details-container h1 { margin-bottom: 0.5rem; }
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />