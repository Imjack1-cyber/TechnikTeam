<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%-- HEADER MUST BE FIRST --%>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lagerverwaltung"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf"/>

<h1>Lagerverwaltung</h1>
<%-- Feedback messages --%>
<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><p class="error-message">${sessionScope.errorMessage}</p><c:remove var="errorMessage" scope="session" /></c:if>

<a href="${pageContext.request.contextPath}/admin/storage?action=new" class="btn" style="margin-bottom: 1.5rem;">Neuen Artikel anlegen</a>

<!-- MOBILE LAYOUT -->
<div class="mobile-card-list">
    <c:forEach var="item" items="${storageList}">
        <div class="list-item-card">
            <h3 class="card-title">${item.name}</h3>
            <div class="card-row"><span>Ort:</span> <span>${item.location}</span></div>
            <div class="card-row"><span>Anzahl:</span> <span>${item.quantity}</span></div>
            <div class="card-actions">
                <a href="${pageContext.request.contextPath}/admin/storage?action=edit&id=${item.id}" class="btn btn-small">Bearbeiten</a>
                <form action="${pageContext.request.contextPath}/admin/storage" method="post" style="display:inline;">
                    <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${item.id}">
                    <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Artikel \'${item.name}\' wirklich löschen?')">Löschen</button>
                </form>
            </div>
        </div>
    </c:forEach>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
    <table class="desktop-table">
        <thead>
            <tr><th>Name</th><th>Ort</th><th>Schrank</th><th>Anzahl</th><th>Aktionen</th></tr>
        </thead>
        <tbody>
            <c:forEach var="item" items="${storageList}">
                <tr>
                    <td>${item.name}</td>
                    <td>${item.location}</td>
                    <td>${item.cabinet}</td>
                    <td>${item.quantity}</td>
                    <td style="display: flex; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/admin/storage?action=edit&id=${item.id}" class="btn btn-small">Bearbeiten</a>
                        <form action="${pageContext.request.contextPath}/admin/storage" method="post" style="display:inline;">
                            <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${item.id}">
                            <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('Artikel \'${item.name}\' wirklich löschen?')">Löschen</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<%-- FOOTER MUST BE LAST --%>
<c:import url="/WEB-INF/jspf/footer.jspf" />