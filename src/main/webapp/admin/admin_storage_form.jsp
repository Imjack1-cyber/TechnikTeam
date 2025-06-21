<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lagerartikel bearbeiten"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<!--  
admin_storage_form.jsp: The form for creating and editing inventory/storage items.

    Served by: AdminStorageServlet (doGet with action=new|edit).

    Submits to: AdminStorageServlet (doPost with action=create|update).

    Dependencies: Includes header.jspf, admin_navigation.jspf, footer.jspf.
-->

<h1><c:out value="${empty storageItem ? 'Neuen Lagerartikel anlegen' : 'Lagerartikel bearbeiten'}"/></h1>

<div class="card form-container" style="max-width: 800px;">
    <%-- 
      CRITICAL FIX: The enctype="multipart/form-data" is required for file uploads.
      Without it, the server will throw an InvalidContentTypeException.
    --%>
    <form action="${pageContext.request.contextPath}/admin/storage" method="post" enctype="multipart/form-data">
        
        <input type="hidden" name="action" value="${empty storageItem ? 'create' : 'update'}">
        <c:if test="${not empty storageItem}"><input type="hidden" name="id" value="${storageItem.id}"></c:if>

        <div class="form-group">
            <label for="name">Artikelname</label>
            <input type="text" id="name" name="name" value="${storageItem.name}" required>
        </div>
        
        <div class="form-group">
            <label for="location">Ort (z.B. Erdgeschoss, Obergeschoss)</label>
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
            <label for="compartment">Fach / Kiste</label>
            <input type="text" id="compartment" name="compartment" value="${storageItem.compartment}">
        </div>

        <div class="form-group">
            <label for="quantity">Anzahl</label>
            <input type="number" id="quantity" name="quantity" value="${storageItem.quantity}" required min="0">
        </div>
        
        <div class="form-group">
            <label for="imagePath">Aktueller Bildpfad (wird bei neuem Upload überschrieben)</label>
            <input type="text" id="imagePath" name="imagePath" value="${storageItem.imagePath}" readonly>
        </div>
        <div class="form-group">
            <label for="imageFile">Neues Bild hochladen (optional)</label>
            <input type="file" id="imageFile" name="imageFile" accept="image/jpeg, image/png, image/gif, image/webp">
        </div>

        <button type="submit" class="btn">Speichern</button>
        <a href="${pageContext.request.contextPath}/admin/storage" class="btn" style="background-color: #6c757d;">Abbrechen</a>
    </form>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />