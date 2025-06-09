<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lehrgangsverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Lehrgangsverwaltung</h1>
<p>Hier können Sie neue Lehrgänge anlegen und bestehende verwalten.</p>

<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><p class="error-message">${sessionScope.errorMessage}</p><c:remove var="errorMessage" scope="session" /></c:if>

<a href="${pageContext.request.contextPath}/admin/courses?action=new" class="btn" style="margin-bottom: 1rem; display: inline-block;">Neuen Lehrgang anlegen</a>

<div class="card">
    <table class="styled-table">
        <thead>
            <tr>
                <th>Name</th>
                <th>Typ</th>
                <th>Abkürzung</th>
                <th>Datum & Uhrzeit</th>
                <th>Leitung</th>
                <th>Aktionen</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="course" items="${courseList}">
                <tr>
                    <td>${course.name}</td>
                    <td>${course.type}</td>
                    <td>${course.abbreviation}</td>
                    <td><java-time:format value="${course.courseDateTime}" pattern="dd.MM.yyyy HH:mm" /> Uhr</td>
                    <td>${course.leader}</td>
                    <td>
                        <a href="${pageContext.request.contextPath}/admin/courses?action=edit&id=${course.id}" class="btn-small">Bearbeiten</a>
                        <form action="${pageContext.request.contextPath}/admin/courses" method="post" style="display:inline;">
                           <input type="hidden" name="action" value="delete">
                           <input type="hidden" name="id" value="${course.id}">
                           <button type="submit" class="btn-small btn-danger" onclick="return confirm('Lehrgang \'${course.name}\' wirklich löschen?')">Löschen</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />