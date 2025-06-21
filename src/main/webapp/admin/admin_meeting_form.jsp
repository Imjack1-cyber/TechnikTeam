<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="isEditMode" value="${not empty meeting}" />
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="${isEditMode ? 'Meeting bearbeiten' : 'Neues Meeting planen'}"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf"/>

<h1>${isEditMode ? 'Meeting bearbeiten' : 'Neues Meeting für "'.concat(parentCourse.name).concat('" planen')}</h1>

<div class="card form-container" style="max-width: 700px; margin: 2rem auto;">
    <form action="${pageContext.request.contextPath}/admin/meetings" method="post">
        
        <input type="hidden" name="action" value="${isEditMode ? 'update' : 'create'}">
        <input type="hidden" name="courseId" value="${parentCourse.id}">
        <c:if test="${isEditMode}">
            <input type="hidden" name="meetingId" value="${meeting.id}">
        </c:if>
        
        <div class="form-group">
            <label for="name">Name des Meetings (z.B. Teil 1, Modul A: Ton)</label>
            <input type="text" id="name" name="name" value="${meeting.name}" required>
        </div>

        <div class="form-group">
            <label for="meetingDateTime">Datum & Uhrzeit</label>
            <input type="datetime-local" id="meetingDateTime" name="meetingDateTime" value="${meeting.meetingDateTime}" required>
        </div>

        <div class="form-group">
            <label for="leader">Leitende Person</label>
            <input type="text" id="leader" name="leader" value="${meeting.leader}">
        </div>
        
        <div class="form-group">
            <label for="description">Beschreibung (spezifisch für dieses Meeting)</label>
            <textarea id="description" name="description" rows="4">${meeting.description}</textarea>
        </div>

        <div style="display: flex; gap: 1rem; margin-top: 1.5rem;">
            <button type="submit" class="btn">${isEditMode ? 'Änderungen speichern' : 'Meeting planen'}</button>
            <a href="${pageContext.request.contextPath}/admin/meetings?courseId=${parentCourse.id}" class="btn" style="background-color: var(--text-muted-color);">Abbrechen</a>
        </div>
    </form>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />