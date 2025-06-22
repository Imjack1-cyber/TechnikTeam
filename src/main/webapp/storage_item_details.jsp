<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  storage_item_details.jsp
  
  This is the public-facing detail page for a single inventory item. It is
  designed to be easily accessible, often via a QR code scan. It displays the
  item's name, an image if available, and its location details.
  
  - It is served by: StorageItemDetailsServlet.
  - Expected attributes:
    - 'item' (de.technikteam.model.StorageItem): The inventory item to display.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Artikeldetails" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Artikeldetails</h1>
<p>Dies ist die Detailansicht für einen Artikel aus dem Lager, oft
	aufgerufen über einen QR-Code.</p>

<div class="card" style="max-width: 600px; margin: 1rem auto;">
	<h2 class="card-title">${item.name}</h2>

	<c:if test="${not empty item.imagePath}">
		<img
			src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
			alt="Bild von ${item.name}"
			style="width: 100%; max-width: 400px; height: auto; border-radius: 8px; margin: 0 auto 1.5rem; display: block;">
	</c:if>

	<ul class="details-list">
		<li><strong>Aktuelle Anzahl:</strong> ${item.quantity}</li>
		<li><strong>Ort:</strong> ${item.location}</li>
		<li><strong>Schrank:</strong> ${not empty item.cabinet ? item.cabinet : 'N/A'}</li>
		<li><strong>Regal:</strong> ${not empty item.shelf ? item.shelf : 'N/A'}</li>
		<li><strong>Fach/Kiste:</strong> ${not empty item.compartment ? item.compartment : 'N/A'}</li>
	</ul>

	<div style="margin-top: 2rem;">
		<a href="${pageContext.request.contextPath}/lager" class="btn">Zurück
			zur Lagerübersicht</a>
	</div>
</div>

<style>
.details-list {
	list-style-type: none;
	padding-left: 0;
}

.details-list li {
	padding: 0.75rem 0;
	border-bottom: 1px solid var(--border-color);
	display: flex;
	justify-content: space-between;
}

.details-list li:last-child {
	border-bottom: none;
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />