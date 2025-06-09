<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lagerverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Lagerverwaltung</h1>

<!-- Display success or error messages from session -->
<c:if test="${not empty sessionScope.successMessage}">
    <p class="success-message">${sessionScope.successMessage}</p>
    <c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
    <p class="error-message">${sessionScope.errorMessage}</p>
    <c:remove var="errorMessage" scope="session" />
</c:if>

<a href="${pageContext.request.contextPath}/admin/storage?action=new" class="btn" style="margin-bottom: 1rem; display: inline-block;">Neuen Artikel anlegen</a>

<div class="card">
    <table class="styled-table">
        <thead>
            <tr>
                <th>Name</th>
                <th>Ort</th>
                <th>Schrank</th>
                <th>Regal</th>
                <th>Anzahl</th>
                <th>Aktionen</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="item" items="${storageList}">
                <tr>
                    <td>${item.name}</td>
                    <td>${item.location}</td>
                    <td>${item.cabinet}</td>
                    <td>${item.shelf}</td>
                    <td>${item.quantity}</td>
                    <td>
                        <a href="${pageContext.request.contextPath}/admin/storage?action=edit&id=${item.id}" class="btn-small">Bearbeiten</a>
                        <form action="${pageContext.request.contextPath}/admin/storage" method="post" style="display:inline;">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${item.id}">
                            <button type="submit" class="btn-small btn-danger" onclick="return confirm('Artikel \'${item.name}\' wirklich löschen?')">Löschen</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />