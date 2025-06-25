<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Artikeldetails" />
	<c:param name="navType" value="user" />
</c:import>

<h1>Artikeldetails</h1>

<div class="card" style="max-width: 600px; margin: 1rem auto;">
	<h2 class="card-title">
		<c:out value="${item.name}" />
	</h2>

	<c:if test="${not empty item.imagePath}">
		<a href="#" class="lightbox-trigger"
			data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}">
			<img
			src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
			alt="Bild von ${item.name}"
			style="width: 100%; max-width: 400px; height: auto; border-radius: 8px; margin: 0 auto 1.5rem; display: block; cursor: pointer;">
		</a>
	</c:if>

	<ul style="list-style: none; padding: 0;">
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Status:</strong>
			<span class="status-badge ${item.availabilityStatusCssClass}">
				<c:out value="${item.availabilityStatus}" />
		</span></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Aktuelle
				Anzahl:</strong> <c:out value="${item.quantity}" /></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Ort:</strong>
			<c:out value="${item.location}" /></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Schrank:</strong>
			<c:out value="${not empty item.cabinet ? item.cabinet : 'N/A'}" /></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Regal:</strong>
			<c:out value="${not empty item.shelf ? item.shelf : 'N/A'}" /></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Fach/Kiste:</strong>
			<c:out
				value="${not empty item.compartment ? item.compartment : 'N/A'}" /></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Gewicht:</strong>
			<c:if test="${item.weightKg > 0}">
				<fmt:formatNumber value="${item.weightKg}" type="number"
					minFractionDigits="2" maxFractionDigits="2" /> kg</c:if> <c:if
				test="${item.weightKg <= 0}">N/A</c:if></li>
		<li
			style="padding: 0.75rem 0; display: flex; justify-content: space-between;"><strong>Preis:</strong>
			<c:if test="${item.priceEur > 0}">
				<fmt:formatNumber value="${item.priceEur}" type="currency"
					currencySymbol="€" />
			</c:if> <c:if test="${item.priceEur <= 0}">N/A</c:if></li>
	</ul>

	<div style="margin-top: 2rem;">
		<a href="${pageContext.request.contextPath}/lager" class="btn">Zurück
			zur Lagerübersicht</a>
	</div>
</div>

<!-- Lightbox structure -->
<div class="lightbox-overlay" id="lightbox">
	<img src="" alt="Vergrößerte Ansicht">
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />

<script>
document.addEventListener('DOMContentLoaded', () => {
    // Lightbox Logic
    const lightbox = document.getElementById('lightbox');
    if (lightbox) {
        const lightboxImage = lightbox.querySelector('img');
        document.querySelectorAll('.lightbox-trigger').forEach(trigger => {
            trigger.addEventListener('click', (e) => {
                e.preventDefault();
                lightboxImage.setAttribute('src', trigger.dataset.src);
                lightbox.classList.add('active');
            });
        });
        lightbox.addEventListener('click', () => lightbox.classList.remove('active'));
    }
});
</script>