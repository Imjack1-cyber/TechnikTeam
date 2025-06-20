<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lehrgangsverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Lehrgangsverwaltung</h1>
<a href="${pageContext.request.contextPath}/admin/courses?action=new" class="btn" style="margin-bottom: 1rem;">Neuen Lehrgang anlegen</a>
<div class="card">
    <table class="styled-table">
        <thead>
            <tr><th>Name</th><th>Typ</th><th>Datum & Uhrzeit</th><th>Leitung</th><th>Aktionen</th></tr>
        </thead>
        <tbody>
            <c:forEach var="course" items="${courseList}">
                <tr>
                    <td data-label="Name">${course.name}</td>
                    <td data-label="Typ">${course.type}</td>
                    <td data-label="Datum & Uhrzeit"><java-time:format value="${course.courseDateTime}" pattern="dd.MM.yyyy HH:mm" /> Uhr</td>
                    <td data-label="Leitung">${course.leader}</td>
                    <td data-label="Aktionen">
                        <a href="${pageContext.request.contextPath}/admin/courses?action=edit&id=${course.id}" class="btn-small">Bearbeiten</a>
                        <form action="${pageContext.request.contextPath}/admin/courses" method="post" style="display:inline;">
                           <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${course.id}">
                           <button type="submit" class="btn-small btn-danger" onclick="return confirm('Lehrgang wirklich löschen?')">Löschen</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
<c:import url="/WEB-INF/jspf/footer.jspf" />