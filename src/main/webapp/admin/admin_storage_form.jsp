<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
admin_storage_form.jsp

This JSP provides the form for an administrator to edit an
existing inventory item. It includes fields for all item properties, such
as name, location details, quantities, and an optional image upload.
Creating new items is handled by a modal on admin_storage_list.jsp.

    It is served by: AdminStorageServlet (doGet, action=edit).

    It submits to: AdminStorageServlet (doPost, action=update).

    Expected attributes:

        'storageItem' (de.technikteam.model.StorageItem): The item to edit.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lagerartikel bearbeiten" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>
	<i class="fas fa-edit"></i> Lagerartikel bearbeiten
</h1>
<div class="form-center-wrapper">
	<div class="card">
		<c:if test="${not empty sessionScope.errorMessage}">
			<p class="error-message">${sessionScope.errorMessage}</p>
			<c:remove var="errorMessage" scope="session" />
		</c:if>
		<form action="${pageContext.request.contextPath}/admin/storage"
			method="post" enctype="multipart/form-data">

			<input type="hidden" name="action" value="update"> <input
				type="hidden" name="id" value="${storageItem.id}">

			<div class="form-group">
				<label for="name">Artikelname</label> <input type="text" id="name"
					name="name" value="${storageItem.name}" required>
			</div>

			<div class="form-group">
				<label for="location">Ort (z.B. Erdgeschoss, Obergeschoss,
					Lagercontainer)</label> <input type="text" id="location" name="location"
					value="${storageItem.location}" required>
			</div>

			<div class="form-group">
				<label for="cabinet">Schrank</label> <input type="text" id="cabinet"
					name="cabinet" value="${storageItem.cabinet}">
			</div>

			<div class="form-group">
				<label for="shelf">Regal</label> <input type="text" id="shelf"
					name="shelf" value="${storageItem.shelf}">
			</div>

			<div class="form-group">
				<label for="compartment">Fach / Kiste</label> <input type="text"
					id="compartment" name="compartment"
					value="${storageItem.compartment}">
			</div>

			<div class="form-group">
				<label for="quantity">Aktuelle Anzahl</label> <input type="number"
					id="quantity" name="quantity" value="${storageItem.quantity}"
					required min="0">
			</div>

			<div class="form-group">
				<label for="maxQuantity">Maximale Anzahl (0 für unbegrenzt)</label>
				<input type="number" id="maxQuantity" name="maxQuantity"
					value="${storageItem.maxQuantity}" required min="0">
			</div>

			<div class="form-group">
				<label for="imagePath">Aktuelles Bild</label>
				<c:if test="${not empty storageItem.imagePath}">
					<img
						src="${pageContext.request.contextPath}/image?file=${storageItem.imagePath}"
						alt="Aktuelles Bild"
						style="max-width: 200px; height: auto; border-radius: 8px; margin-top: 0.5rem;">
				</c:if>
				<c:if test="${empty storageItem.imagePath}">
					<p style="color: var(--text-muted-color);">Kein Bild vorhanden.</p>
				</c:if>
			</div>

			<div class="form-group">
				<label for="imageFile">Neues Bild hochladen (optional,
					überschreibt das alte Bild)</label> <input type="file" id="imageFile"
					name="imageFile"
					accept="image/jpeg, image/png, image/gif, image/webp">
			</div>

			<div style="display: flex; gap: 1rem;">
				<button type="submit" class="btn">
					<i class="fas fa-save"></i> Speichern
				</button>
				<a href="${pageContext.request.contextPath}/admin/storage"
					class="btn" style="background-color: var(--text-muted-color);">Abbrechen</a>
			</div>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />