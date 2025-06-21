<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lager" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Lagerübersicht</h1>

<c:if test="${empty storageData}">
	<div class="card">
		<p>Derzeit sind keine Artikel im Lager erfasst.</p>
	</div>
</c:if>

<c:forEach var="locationEntry" items="${storageData}">
	<h2>${locationEntry.key}</h2>

	<!-- MOBILE LAYOUT -->
	<div class="mobile-card-list">
		<c:forEach var="item" items="${locationEntry.value}">
			<div class="list-item-card">
				<h3 class="card-title">${item.name}</h3>
				<div class="card-row">
					<span>Anzahl:</span> <span>${item.quantity}</span>
				</div>
				<div class="card-row">
					<span>Ort:</span> <span>${item.cabinet} / ${item.shelf}</span>
				</div>
				<div class="card-actions">
					<c:if test="${not empty item.imagePath}">
						<a href="#" class="btn btn-small lightbox-trigger"
							data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}">Bild
							anzeigen</a>
					</c:if>
				</div>
			</div>
		</c:forEach>
	</div>

	<!-- DESKTOP LAYOUT -->
	<div class="desktop-table-wrapper">
		<table class="desktop-table">
			<thead>
				<tr>
					<th>Gerät</th>
					<th>Schrank</th>
					<th>Regal</th>
					<th>Anzahl</th>
					<th>Bild</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${locationEntry.value}">
					<tr>
						<td>${item.name}</td>
						<td>${item.cabinet}</td>
						<td>${item.shelf}</td>
						<td>${item.quantity}</td>
						<td><c:if test="${not empty item.imagePath}">
								<a href="#" class="lightbox-trigger"
									data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}">
									<img
									src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
									alt="${item.name}" width="50"
									style="border-radius: 4px; vertical-align: middle; cursor: pointer;">
								</a>
							</c:if></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</c:forEach>

<!-- Lightbox structure, placed once at the end of the page -->
<div class="lightbox-overlay" id="lightbox">
	<img src="" alt="Vergrößerte Ansicht">
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />

<%-- This script must be on this page to handle the lightbox logic --%>
<script>
document.addEventListener('DOMContentLoaded', () => {
    const lightbox = document.getElementById('lightbox');
    if (!lightbox) return;

    const lightboxImage = lightbox.querySelector('img');
    const triggers = document.querySelectorAll('.lightbox-trigger');

    triggers.forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            const imgSrc = trigger.dataset.src;
            if (imgSrc) {
                lightboxImage.setAttribute('src', imgSrc);
                lightbox.classList.add('active');
            }
        });
    });

    // Close the lightbox when the overlay itself is clicked
    lightbox.addEventListener('click', () => {
        lightbox.classList.remove('active');
    });
});
</script>