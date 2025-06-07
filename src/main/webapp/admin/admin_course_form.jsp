<%-- Pfad: src/main/webapp/admin/admin_course_form.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lehrgang bearbeiten"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1><c:out value="${empty course ? 'Neuen Lehrgang anlegen' : 'Lehrgang bearbeiten'}"/></h1>
<div class="card form-container">
    <form action="${pageContext.request.contextPath}/admin/courses" method="post">
        <input type="hidden" name="id" value="${course.id}">
        <div class="form-group"><label>Name</label><input type="text" name="name" value="${course.name}" required></div>
        <div class="form-group"><label>Typ</label><input type="text" name="type" value="${course.type}" required></div>
        <div class="form-group"><label>Datum/Zeit</label><input type="datetime-local" name="courseDateTime" value="${course.courseDateTime}" required></div>
        <div class="form-group"><label>Leitende Person</label><input type="text" name="leader" value="${course.leader}"></div>
        <div class="form-group"><label>Beschreibung</label><textarea name="description" rows="4">${course.description}</textarea></div>
        <button type="submit" class="btn">Speichern</button>
    </form>
</div>
<c:import url="/WEB-INF/jspf/footer.jspf" />