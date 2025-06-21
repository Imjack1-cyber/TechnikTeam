<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%-- HEADER MUST BE FIRST --%>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Benutzerverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf"/>

<h1>Benutzerverwaltung</h1>
<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><p class="error-message">${sessionScope.errorMessage}</p><c:remove var="errorMessage" scope="session" /></c:if>

<a href="#new-user-form" class="btn" style="margin-bottom: 1.5rem;">Neuen Benutzer anlegen</a>

<!-- MOBILE LAYOUT -->
<div class="mobile-card-list">
    <c:forEach var="user" items="${userList}">
        <div class="list-item-card">
            <h3 class="card-title">${user.username}</h3>
            <div class="card-row"><span>ID:</span> <span>${user.id}</span></div>
            <div class="card-row"><span>Rolle:</span> <span>${user.role}</span></div>
            <div class="card-actions">
                <a href="${pageContext.request.contextPath}/admin/users?action=details&id=${user.id}" class="btn btn-small">Details</a>
                <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display:inline;">
                    <input type="hidden" name="action" value="delete"><input type="hidden" name="userId" value="${user.id}">
                    <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Benutzer ${user.username} wirklich löschen?')">Löschen</button>
                </form>
            </div>
        </div>
    </c:forEach>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
    <table class="desktop-table">
        <thead>
            <tr><th>ID</th><th>Benutzername</th><th>Rolle</th><th>Aktionen</th></tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${userList}">
                <tr>
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.role}</td>
                    <td style="display: flex; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/admin/users?action=details&id=${user.id}" class="btn btn-small">Details</a>
                        <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display:inline;">
                            <input type="hidden" name="action" value="delete"><input type="hidden" name="userId" value="${user.id}">
                            <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Benutzer ${user.username} wirklich löschen?')">Löschen</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<div class="card" id="new-user-form" style="margin-top: 2rem;">
    <h2>Neuen Benutzer anlegen</h2>
    <form action="${pageContext.request.contextPath}/admin/users" method="post">
        <input type="hidden" name="action" value="create">
        <div class="form-group"><label>Benutzername</label><input type="text" name="username" required></div>
        <div class="form-group"><label>Passwort</label><input type="password" name="password" required></div>
        <div class="form-group"><label>Rolle</label><select name="role"><option value="NUTZER">Nutzer</option><option value="ADMIN">Admin</option></select></div>
        <button type="submit" class="btn">Benutzer erstellen</button>
    </form>
</div>

<%-- FOOTER MUST BE LAST --%>
<c:import url="/WEB-INF/jspf/footer.jspf" />