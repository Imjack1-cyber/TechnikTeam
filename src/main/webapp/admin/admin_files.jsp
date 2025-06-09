<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Dateiverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Dateiverwaltung</h1>
<p>Hier können Sie neue Dateien hochladen und bestehende verwalten.</p>

<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><p class="error-message">${sessionScope.errorMessage}</p><c:remove var="errorMessage" scope="session" /></c:if>

<!-- Upload Form -->
<div class="card">
    <h2 class="card-title">Neue Datei hochladen</h2>
    <form action="${pageContext.request.contextPath}/admin/files" method="post" enctype="multipart/form-data">
        <div class="form-group">
            <label for="file">Datei auswählen</label>
            <input type="file" name="file" id="file" required>
        </div>
        <div class="form-group">
            <label for="categoryId">Kategorie</label>
            <select name="categoryId" id="categoryId" required>
                <option value="">-- Bitte wählen --</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.id}">${cat.name}</option>
                </c:forEach>
            </select>
        </div>
        <button type="submit" class="btn">Hochladen</button>
    </form>
    <p style="margin-top:1rem;"><a href="${pageContext.request.contextPath}/admin/file-categories" class="btn-small">Kategorien verwalten</a></p>
</div>

<!-- Existing Files Table -->
<div class="card" style="margin-top: 2rem;">
    <h2 class="card-title">Vorhandene Dateien</h2>
    <table class="styled-table">
        <thead>
            <tr><th>Dateiname</th><th>Kategorie</th><th>Hochgeladen am</th><th>Aktionen</th></tr>
        </thead>
        <tbody>
            <c:forEach var="categoryEntry" items="${groupedFiles}">
                <c:if test="${not empty categoryEntry.value}">
                    <tr style="background-color: var(--secondary-color); font-weight: bold;"><td colspan="4">${categoryEntry.key}</td></tr>
                    <c:forEach var="file" items="${categoryEntry.value}">
                        <tr>
                            <td><a href="${pageContext.request.contextPath}/download?file=${file.filepath}" target="_blank">${file.filename}</a></td>
                            <td>${categoryEntry.key}</td>
                            <td><java-time:format value="${file.uploadedAt}" pattern="dd.MM.yyyy HH:mm"/></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/admin/files" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="fileId" value="${file.id}">
                                    <button type="submit" class="btn-small btn-danger" onclick="return confirm('Datei \'${file.filename}\' wirklich löschen?')">Löschen</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>
            </c:forEach>
        </tbody>
    </table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />