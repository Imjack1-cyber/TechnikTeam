<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lehrgang bearbeiten"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1><c:out value="${empty course ? 'Neuen Lehrgang anlegen' : 'Lehrgang bearbeiten'}"/></h1>

<div class="card form-container">
    <form action="${pageContext.request.contextPath}/admin/courses" method="post">
        
        <input type="hidden" name="action" value="${empty course ? 'create' : 'update'}">
        <c:if test="${not empty course}"><input type="hidden" name="id" value="${course.id}"></c:if>

        <div class="form-group">
            <label for="name">Name des Lehrgangs</label>
            <input type="text" id="name" name="name" value="${course.name}" required>
        </div>
        <div class="form-group">
            <label for="type">Typ (z.B. Technik, Sicherheit)</label>
            <input type="text" id="type" name="type" value="${course.type}" required>
        </div>
        <div class="form-group">
            <label for="abbreviation">Abkürzung (für Matrix-Ansicht)</label>
            <input type="text" id="abbreviation" name="abbreviation" value="${course.abbreviation}" maxlength="10">
        </div>
        <div class="form-group">
            <label for="courseDateTime">Datum & Uhrzeit</label>
            <input type="datetime-local" id="courseDateTime" name="courseDateTime" value="${course.courseDateTime}" required>
        </div>
        <div class="form-group">
            <label for="leader">Leitende Person</label>
            <input type="text" id="leader" name="leader" value="${course.leader}">
        </div>
        <div class="form-group">
            <label for="description">Beschreibung</label>
            <textarea id="description" name="description" rows="4">${course.description}</textarea>
        </div>

        <button type="submit" class="btn">Speichern</button>
        <a href="${pageContext.request.contextPath}/admin/courses" class="btn" style="background-color: #6c757d;">Abbrechen</a>
    </form>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />