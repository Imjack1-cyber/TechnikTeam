<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time" %>

<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Benutzerdetails bearbeiten"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Benutzerdetails bearbeiten: <c:out value="${userToEdit.username}"/></h1>
<a href="${pageContext.request.contextPath}/admin/users" style="display: inline-block; margin-bottom: 1rem;">« Zurück zur Benutzerliste</a>

<!-- Display success or error messages -->
<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><p class="error-message">${sessionScope.errorMessage}</p><c:remove var="errorMessage" scope="session" /></c:if>

<div class="card form-container">
    <form action="${pageContext.request.contextPath}/admin/users" method="post">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="userId" value="${userToEdit.id}">
        
        <h2 class="card-title">Stammdaten</h2>
        <div class="form-group">
            <label for="username">Benutzername</label>
            <input type="text" id="username" name="username" value="${userToEdit.username}" required>
        </div>
        <div class="form-group">
            <label for="role">Rolle</label>
            <select name="role" id="role">
                <option value="NUTZER" ${userToEdit.role == 'NUTZER' ? 'selected' : ''}>Nutzer</option>
                <option value="ADMIN" ${userToEdit.role == 'ADMIN' ? 'selected' : ''}>Admin</option>
            </select>
        </div>
        <div class="form-group">
            <label for="classYear">Jahrgang</label>
            <input type="number" id="classYear" name="classYear" value="${userToEdit.classYear}" placeholder="z.B. 2025">
        </div>
        <div class="form-group">
            <label for="className">Klasse</label>
            <input type="text" id="className" name="className" value="${userToEdit.className}" placeholder="z.B. 10b">
        </div>
        <div class="form-group">
            <label>Registriert seit</label>
            <%-- Formatierung des Datums, das jetzt vom DAO geladen wird --%>
            <input type="text" value="<c:if test='${not empty userToEdit.createdAt}'><java-time:format value='${userToEdit.createdAt}' pattern='dd.MM.yyyy HH:mm'/></c:if>" readonly class="readonly-field">
        </div>
        <button type="submit" class="btn">Änderungen speichern</button>
    </form>
</div>

<style>
.form-container { max-width: 700px; margin: auto; }
.readonly-field { background-color: var(--secondary-color); border: 1px solid var(--border-color); cursor: not-allowed; }
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />