<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Datei- & Kategorienverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf"/>

<h1>Datei- & Kategorienverwaltung</h1>
<%-- ... Feedback messages ... --%>

<div class="dashboard-grid">
    <div class="card">
        <h2>Dateien</h2>
        <c:forEach var="categoryEntry" items="${groupedFiles}">
            <div style="margin-bottom: 1.5rem;">
                <h3 style="color: var(--text-color); border: none; padding: 0;">${categoryEntry.key}</h3>
                <ul style="list-style: none; padding-left: 0;">
                    <c:forEach var="file" items="${categoryEntry.value}">
                        <li style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0; border-bottom: 1px solid var(--border-color);">
                            <span><a href="${pageContext.request.contextPath}/download?file=${file.filepath}">${file.filename}</a></span>
                            <form action="${pageContext.request.contextPath}/admin/files" method="post">
                                <input type="hidden" name="action" value="delete"><input type="hidden" name="fileId" value="${file.id}">
                                <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Datei \'${file.filename}\' wirklich löschen?')">X</button>
                            </form>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </c:forEach>
    </div>

    <div class="card">
        <h2>Neue Datei hochladen</h2>
        <form action="${pageContext.request.contextPath}/admin/files" method="post" enctype="multipart/form-data">
            <div class="form-group"><label>Datei auswählen</label><input type="file" name="file" required></div>
            <div class="form-group"><label>Kategorie</label><select name="categoryId" required><c:forEach var="cat" items="${allCategories}"><option value="${cat.id}">${cat.name}</option></c:forEach></select></div>
            <button type="submit" class="btn">Hochladen</button>
        </form>
    </div>
</div>

<div class="card">
    <h2>Kategorien verwalten</h2>
    <div class="desktop-table-wrapper">
        <table class="desktop-table">
            <thead>
                <tr><th>Kategoriename</th><th style="width: 250px;">Aktionen</th></tr>
            </thead>
            <tbody>
                <c:forEach var="cat" items="${allCategories}">
                    <tr>
                        <form action="${pageContext.request.contextPath}/admin/categories/update" method="post">
                            <td><input type="text" name="categoryName" value="${cat.name}" required class="form-group" style="margin: 0; padding: 0.5rem;"></td>
                            <td style="display: flex; gap: 0.5rem;">
                                <input type="hidden" name="categoryId" value="${cat.id}">
                                <button type="submit" class="btn btn-small">Umbenennen</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/admin/categories/delete" method="post" style="display:inline;">
                            <input type="hidden" name="categoryId" value="${cat.id}">
                            <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Kategorie \'${cat.name}\' wirklich löschen?')">Löschen</button>
                        </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <form action="${pageContext.request.contextPath}/admin/categories/create" method="post" style="margin-top: 1.5rem;">
        <div class="form-group">
            <label>Neue Kategorie erstellen</label>
            <input type="text" name="categoryName" required>
        </div>
        <button type="submit" class="btn">Erstellen</button>
    </form>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />