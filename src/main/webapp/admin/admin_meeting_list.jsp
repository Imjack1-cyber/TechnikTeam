<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Meetings für ${parentCourse.name}"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf"/>

<h1>Meetings für "${parentCourse.name}"</h1>
<a href="${pageContext.request.contextPath}/admin/courses" style="margin-bottom: 1rem; display: inline-block;">« Zurück zu allen Vorlagen</a>
<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>

<a href="${pageContext.request.contextPath}/admin/meetings?action=new&courseId=${parentCourse.id}" class="btn" style="margin-bottom: 1.5rem;">Neues Meeting planen</a>

<!-- MOBILE LAYOUT -->
<div class="mobile-card-list">
    <c:forEach var="meeting" items="${meetings}">
        <div class="list-item-card">
            <h3 class="card-title">${meeting.name}</h3>
            <div class="card-row"><span>Datum:</span> <span>${meeting.meetingDateTime}</span></div>
            <div class="card-row"><span>Leitung:</span> <span>${empty meeting.leader ? 'N/A' : meeting.leader}</span></div>
            <div class="card-actions">
                <a href="${pageContext.request.contextPath}/admin/meetings?action=edit&courseId=${parentCourse.id}&meetingId=${meeting.id}" class="btn btn-small">Bearbeiten</a>
                <form action="${pageContext.request.contextPath}/admin/meetings" method="post" style="display:inline;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="courseId" value="${parentCourse.id}">
                    <input type="hidden" name="meetingId" value="${meeting.id}">
                    <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Meeting \'${meeting.name}\' wirklich löschen?')">Löschen</button>
                </form>
            </div>
        </div>
    </c:forEach>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
    <table class="desktop-table">
        <thead>
            <tr><th>Meeting-Name</th><th>Datum & Uhrzeit</th><th>Leitung</th><th>Aktionen</th></tr>
        </thead>
        <tbody>
            <c:forEach var="meeting" items="${meetings}">
                <tr>
                    <td>${meeting.name}</td>
                    <td>${meeting.meetingDateTime}</td>
                    <td>${empty meeting.leader ? 'N/A' : meeting.leader}</td>
                    <td style="display: flex; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/admin/meetings?action=edit&courseId=${parentCourse.id}&meetingId=${meeting.id}" class="btn btn-small">Bearbeiten</a>
                        <form action="${pageContext.request.contextPath}/admin/meetings" method="post" style="display:inline;">
                           <input type="hidden" name="action" value="delete">
                           <input type="hidden" name="courseId" value="${parentCourse.id}">
                           <input type="hidden" name="meetingId" value="${meeting.id}">
                           <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Meeting \'${meeting.name}\' wirklich löschen?')">Löschen</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
             <c:if test="${empty meetings}">
                <tr><td colspan="4">Für diesen Lehrgang wurden noch keine Meetings geplant.</td></tr>
            </c:if>
        </tbody>
    </table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />