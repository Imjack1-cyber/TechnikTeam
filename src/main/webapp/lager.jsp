<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Lager" />
</c:import>

<h1>
	<i class="fas fa-boxes"></i> Lagerübersicht
</h1>
<p>Hier finden Sie eine Übersicht aller erfassten Artikel im Lager.
	Klicken Sie auf einen Artikelnamen für Details und Verlauf.</p>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="table-controls">
	<div class="form-group" style="margin-bottom: 0; flex-grow: 1;">
		<input type="search" id="table-filter"
			placeholder="Alle Artikel filtern..." aria-label="Lager filtern">
	</div>
</div>

<c:if test="${empty storageData}">
	<div class="card">
		<p>Derzeit sind keine Artikel im Lager erfasst.</p>
	</div>
</c:if>

<c:forEach var="locationEntry" items="${storageData}">
	<div class="card">
		<h2>
			<i class="fas fa-map-marker-alt"></i>
			<c:out value="${locationEntry.key}" />
		</h2>
		<div class="table-wrapper">
			<table class="data-table searchable-table">
				<thead>
					<tr>
						<th>Gerät</th>
						<th>Bild</th>
						<th>Verfügbar</th>
						<th>Defekt</th>
						<th>Status</th>
						<th>Aktion</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="item" items="${locationEntry.value}">
						<tr
							class="${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
							<td><a href="<c:url value='/storage-item?id=${item.id}'/>"
								title="Details für ${item.name} ansehen"><c:out
										value="${item.name}" /></a></td>
							<td style="text-align: center;"><c:if
									test="${not empty item.imagePath}">
									<button class="btn btn-small btn-info lightbox-trigger"
										data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
										title="Bild anzeigen">
										<i class="fas fa-image"></i>
									</button>
								</c:if></td>
							<td>${item.availableQuantity}/ ${item.quantity}</td>
							<td>${item.defectiveQuantity}</td>
							<td><span
								class="status-badge ${item.availabilityStatusCssClass}"><c:out
										value="${item.availabilityStatus}" /></span></td>
							<td>
								<button class="btn btn-small transaction-btn btn-primary"
									data-item-id="${item.id}"
									data-item-name="${fn:escapeXml(item.name)}"
									data-max-qty="${item.availableQuantity}"
									${item.availableQuantity <= 0 ? 'disabled' : ''}>
									Entnehmen/Einräumen</button>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</c:forEach>

<!-- Lightbox structure for image viewing -->
<div id="lightbox" class="lightbox-overlay">
	<span class="lightbox-close">×</span> <img class="lightbox-content"
		id="lightbox-image">
</div>

<%@ include file="/WEB-INF/jspf/storage_modals.jspf"%>
<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />

<script>
document.addEventListener('DOMContentLoaded', () => {
    // Lightbox Logic
    const lightbox = document.getElementById('lightbox');
    if (lightbox) {
        const lightboxImage = lightbox.querySelector('img');
        const closeBtn = lightbox.querySelector('.lightbox-close');

        document.querySelectorAll('.lightbox-trigger').forEach(trigger => {
            trigger.addEventListener('click', (e) => {
                e.preventDefault();
                lightbox.style.display = 'block';
                lightboxImage.src = trigger.dataset.src;
            });
        });

        const closeLightbox = () => { lightbox.style.display = 'none'; };
        if (closeBtn) closeBtn.addEventListener('click', closeLightbox);
        lightbox.addEventListener('click', (e) => { 
            if(e.target === lightbox) { closeLightbox(); }
        });
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && lightbox.style.display === 'block') closeLightbox();
        });
    }

    // Transaction Modal Logic
    const transactionModal = document.getElementById('transaction-modal');
    if (transactionModal) {
        const modalTitle = document.getElementById('transaction-modal-title');
        const modalItemId = document.getElementById('transaction-item-id');
        const closeModalBtn = transactionModal.querySelector('.modal-close-btn');

        const openModal = (btn) => {
            modalTitle.textContent = `${btn.dataset.itemName}: Entnehmen / Einräumen`;
            modalItemId.value = btn.dataset.itemId;
            transactionModal.classList.add('active');
        };

        const closeModal = () => transactionModal.classList.remove('active');

        document.querySelectorAll('.transaction-btn').forEach(btn => {
            btn.addEventListener('click', () => openModal(btn));
        });

        closeModalBtn.addEventListener('click', closeModal);
        transactionModal.addEventListener('click', e => {
            if (e.target === transactionModal) closeModal();
        });
    }
});
</script>