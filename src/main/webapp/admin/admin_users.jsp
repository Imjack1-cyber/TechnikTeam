<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Benutzerverwaltung"/>
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Benutzerverwaltung</h1>

<!-- Display success or error messages from session -->
<c:if test="${not empty sessionScope.successMessage}">
    <p class="success-message">${sessionScope.successMessage}</p>
    <c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
    <p class="error-message">${sessionScope.errorMessage}</p>
    <c:remove var="errorMessage" scope="session" />
</c:if>

<!-- Section to display existing users -->
<div class="card">
    <h2 class="card-title">Alle Benutzer</h2>
    <table class="styled-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Benutzername</th>
                <th>Rolle</th>
                <th>Aktionen</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${userList}">
                <tr>
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.role}</td>
                    <td>
                        <!-- KORREKTUR: Dieser Link führt jetzt zur neuen Detail- und Verwaltungsseite -->
                        <a href="${pageContext.request.contextPath}/admin/users?action=details&id=${user.id}" class="btn-small">Details / Bearbeiten</a>
                        
                        <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display:inline;">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="userId" value="${user.id}">
                            <button type="submit" class="btn-small btn-danger" onclick="return confirm('Benutzer ${user.username} wirklich löschen?')">Löschen</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<!-- Section to add a new user -->
<div class="card" style="margin-top: 2rem;">
    <h2 class="card-title">Neuen Benutzer anlegen</h2>
    <form action="${pageContext.request.contextPath}/admin/users" method="post" class="user-form">
        <input type="hidden" name="action" value="create">
        <div class="form-group">
            <label for="username">Benutzername</label>
            <input type="text" id="username" name="username" required>
        </div>
        <div class="form-group">
            <label for="password">Passwort</label>
            <input type="password" id="password" name="password" required>
        </div>
        <div class="form-group">
            <label for="role">Rolle</label>
            <select id="role" name="role">
                <option value="NUTZER">Nutzer</option>
                <option value="ADMIN">Admin</option>
            </select>
        </div>
        <button type="submit" class="btn">Benutzer erstellen</button>
    </form>
</div>

<style>
.user-form { display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end; }
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />