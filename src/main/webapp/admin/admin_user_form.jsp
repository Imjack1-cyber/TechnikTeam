<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Benutzer bearbeiten"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Benutzer bearbeiten</h1>
<div class="card">
    <form action="${pageContext.request.contextPath}/admin/users" method="post" class="user-form">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="userId" value="${userToEdit.id}">
        <div class="form-group">
            <label>Benutzername</label>
            <input type="text" name="username" value="${userToEdit.username}" required>
        </div>
        <div class="form-group">
            <label>Rolle</label>
            <select name="role">
                <option value="NUTZER" ${userToEdit.role == 'NUTZER' ? 'selected' : ''}>Nutzer</option>
                <option value="ADMIN" ${userToEdit.role == 'ADMIN' ? 'selected' : ''}>Admin</option>
            </select>
        </div>
        <%-- Password change could be a separate form/logic --%>
        <button type="submit" class="btn">Änderungen speichern</button>
    </form>
</div>
<c:import url="/WEB-INF/jspf/footer.jspf" />