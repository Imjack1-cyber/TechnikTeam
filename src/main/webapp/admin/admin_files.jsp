<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Dateiverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Dateiverwaltung</h1>

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
                <c:forEach var="cat" items="${allCategories}">
                    <option value="${cat.id}">${cat.name}</option>
                </c:forEach>
            </select>
        </div>
        <button type="submit" class="btn">Hochladen</button>
    </form>
</div>

<div class="card" style="margin-top: 2rem;">
    <h2 class="card-title">Neue Kategorie erstellen</h2>
    <form action="${pageContext.request.contextPath}/admin/categories/create" method="post" class="user-form">
        <div class="form-group">
            <label for="categoryName">Name der neuen Kategorie</label>
            <input type="text" name="categoryName" id="categoryName" required>
        </div>
        <button type="submit" class="btn">Kategorie erstellen</button>
    </form>
</div>

<%-- Fügen Sie diesen neuen Block in admin_files.jsp ein --%>
<div class="card" style="margin-top: 2rem;">
    <h2 class="card-title">Kategorien verwalten</h2>
    <table class="styled-table">
        <thead><tr><th>Name</th><th>Aktionen</th></tr></thead>
        <tbody>
            <c:forEach var="cat" items="${allCategories}">
                <tr>
                    <form action="${pageContext.request.contextPath}/admin/categories/update" method="post">
                        <td><input type="text" name="categoryName" value="${cat.name}" required></td>
                        <td>
                            <input type="hidden" name="categoryId" value="${cat.id}">
                            <button type="submit" class="btn-small">Umbenennen</button>
                    </form>
                    <form action="${pageContext.request.contextPath}/admin/categories/delete" method="post" style="display:inline;">
                        <input type="hidden" name="categoryId" value="${cat.id}">
                        <button type="submit" class="btn-small btn-danger" onclick="return confirm('Kategorie wirklich löschen?')">Löschen</button>
                    </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<div class="card" style="margin-top: 2rem;">
    <h2 class="card-title">Vorhandene Dateien</h2>
    <table class="styled-table">
        <thead><tr><th>Dateiname</th><th>Kategorie</th><th>Aktionen</th></tr></thead>
        <tbody>
            <c:forEach var="categoryEntry" items="${groupedFiles}">
                <c:if test="${not empty categoryEntry.value}">
                    <tr style="background-color: var(--secondary-color); font-weight: bold;"><td colspan="3">${categoryEntry.key}</td></tr>
                    <c:forEach var="file" items="${categoryEntry.value}">
                        <tr>
                            <td><a href="${pageContext.request.contextPath}/download?file=${file.filepath}" target="_blank">${file.filename}</a></td>
                            <td>${file.categoryName}</td>
                            <td>
                                <form action="${pageContext.request.contextPath}/admin/files" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="delete"><input type="hidden" name="fileId" value="${file.id}">
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