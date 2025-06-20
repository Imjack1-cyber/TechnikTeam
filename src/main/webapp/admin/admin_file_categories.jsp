<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Dateikategorien verwalten"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Dateikategorien verwalten</h1>
<div class="card" style="display:flex; gap: 2rem;">
    <div>
        <h2 class="card-title">Neue Kategorie erstellen</h2>
        <form action="" method="post">
            <input type="hidden" name="action" value="create">
            <div class="form-group">
                <label>Kategoriename</label>
                <input type="text" name="name" required>
            </div>
            <button type="submit" class="btn">Erstellen</button>
        </form>
    </div>
    <div>
        <h2 class="card-title">Bestehende Kategorien</h2>
        <ul>
            <c:forEach var="cat" items="${categories}">
                <li style="display:flex; justify-content: space-between; align-items: center; width: 300px;">
                    ${cat.name}
                    <form action="" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${cat.id}">
                        <button type="submit" class="btn-small btn-danger" onclick="return confirm('Kategorie wirklich löschen?')">X</button>
                    </form>
                </li>
            </c:forEach>
        </ul>
    </div>
</div>
<c:import url="/WEB-INF/jspf/footer.jspf" />