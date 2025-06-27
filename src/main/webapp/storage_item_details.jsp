<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Artikeldetails: ${item.name}" />
</c:import>

<h1>
	<i class="fas fa-cube"></i> Artikeldetails
</h1>

<div class="dashboard-grid"
	style="grid-template-columns: 1fr 2fr; align-items: flex-start;">

	<!-- Left Column: Image and Main Data -->
	<div class="card">
		<h2 class="card-title">${item.name}</h2>
		<c:if test="${not empty item.imagePath}">
			<a href="#" class="lightbox-trigger"> <img
				src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
				alt="${item.name}"
				style="width: 100%; border-radius: var(--border-radius); margin-bottom: 1rem; cursor: zoom-in;">
			</a>
		</c:if>
		<ul class="details-list">
			<li><strong>Status:</strong> <span
				class="status-badge ${item.availabilityStatusCssClass}">${item.availabilityStatus}</span></li>
			<li><strong>Verfügbar:</strong> ${item.availableQuantity} /
				${item.quantity}</li>
			<li><strong>Defekt:</strong> ${item.defectiveQuantity}</li>
			<li><strong>Ort:</strong> ${item.location}</li>
			<li><strong>Schrank:</strong> ${not empty item.cabinet ? item.cabinet : 'N/A'}</li>
			<li><strong>Fach:</strong> ${not empty item.compartment ? item.compartment : 'N/A'}</li>
		</ul>
		<div style="margin-top: 2rem;">
			<a href="<c:url value='/lager'/>" class="btn btn-small"><i
				class="fas fa-arrow-left"></i> Zur Lagerübersicht</a>
		</div>
	</div>

	<!-- Right Column: Transaction History -->
	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-history"></i> Verlauf / Chronik
		</h2>
		<div class="table-wrapper" style="max-height: 60vh; overflow-y: auto;">
			<table class="data-table">
				<thead>
					<tr>
						<th>Wann</th>
						<th>Aktion</th>
						<th>Wer</th>
						<th>Notiz</th>
					</tr>
				</thead>
				<tbody>
					<c:if test="${empty history}">
						<tr>
							<td colspan="4" style="text-align: center;">Kein Verlauf für
								diesen Artikel vorhanden.</td>
						</tr>
					</c:if>
					<c:forEach var="entry" items="${history}">
						<tr>
							<td>${entry.transactionTimestampLocaleString}</td>
							<td><span
								class="status-badge ${entry.quantityChange > 0 ? 'status-ok' : 'status-danger'}">
									${entry.quantityChange > 0 ? '+' : ''}${entry.quantityChange} </span>
							</td>
							<td>${entry.username}</td>
							<td>${not empty entry.notes ? entry.notes : '-'}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- Lightbox structure -->
<div id="lightbox" class="lightbox-overlay">
	<span class="lightbox-close">×</span> <img class="lightbox-content"
		id="lightbox-image">
</div>

<style>
.lightbox-overlay {
	display: none;
	position: fixed;
	z-index: 3000;
	padding-top: 100px;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	overflow: auto;
	background-color: rgba(0, 0, 0, 0.9);
}

.lightbox-content {
	margin: auto;
	display: block;
	width: 80%;
	max-width: 900px;
}

.lightbox-close {
	position: absolute;
	top: 15px;
	right: 35px;
	color: #f1f1f1;
	font-size: 40px;
	font-weight: bold;
	transition: 0.3s;
	cursor: pointer;
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />

<script>
document.addEventListener('DOMContentLoaded', () => {
    const lightbox = document.getElementById('lightbox');
    const lightboxImage = document.getElementById('lightbox-image');
    const closeBtn = document.querySelector('.lightbox-close');

    document.querySelectorAll('.lightbox-trigger').forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            lightbox.style.display = 'block';
            lightboxImage.src = trigger.querySelector('img').src;
        });
    });

    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            lightbox.style.display = 'none';
        });
    }
});
</script>