<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lagerartikel bearbeiten"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1><c:out value="${empty storageItem ? 'Neuen Lagerartikel anlegen' : 'Lagerartikel bearbeiten'}"/></h1>

<div class="card form-container" style="max-width: 800px;">
    <form action="${pageContext.request.contextPath}/admin/storage" method="post">
        
        <!-- Hidden fields to determine action and ID -->
        <input type="hidden" name="action" value="${empty storageItem ? 'create' : 'update'}">
        <c:if test="${not empty storageItem}">
            <input type="hidden" name="id" value="${storageItem.id}">
        </c:if>

        <div class="form-group">
            <label for="name">Artikelname</label>
            <input type="text" id="name" name="name" value="${storageItem.name}" required>
        </div>
        
        <div class="form-group">
            <label for="location">Ort</label>
            <input type="text" id="location" name="location" value="${storageItem.location}" required>
        </div>

        <div class="form-group">
            <label for="cabinet">Schrank</label>
            <input type="text" id="cabinet" name="cabinet" value="${storageItem.cabinet}">
        </div>

        <div class="form-group">
            <label for="shelf">Regal</label>
            <input type="text" id="shelf" name="shelf" value="${storageItem.shelf}">
        </div>

        <div class="form-group">
            <label for="compartment">Fach</label>
            <input type="text" id="compartment" name="compartment" value="${storageItem.compartment}">
        </div>

        <div class="form-group">
            <label for="quantity">Anzahl</label>
            <input type="number" id="quantity" name="quantity" value="${storageItem.quantity}" required min="0">
        </div>
        
        <div class="form-group">
        <label for="imagePath">Aktueller Bildpfad (nur Text)</label>
        <input type="text" id="imagePath" name="imagePath" value="${storageItem.imagePath}" placeholder="Wird durch Upload überschrieben">
    </div>
    <div class="form-group">
        <label for="imageFile">Neues Bild hochladen (optional)</label>
        <input type="file" id="imageFile" name="imageFile" accept="image/jpeg, image/png, image/gif">
    </div>

    <button type="submit" class="btn">Speichern</button>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />