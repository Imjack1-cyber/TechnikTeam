<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="../../jspf/main_header.jspf">
	<c:param name="pageTitle" value="Lagerverwaltung" />
</c:import>

<h1>
	<i class="fas fa-warehouse"></i> Lagerverwaltung
</h1>

<c:import url="../../jspf/message_banner.jspf" />

<div class="table-controls">
	<button type="button" class="btn btn-success" id="new-item-btn">
		<i class="fas fa-plus"></i> Neuen Artikel anlegen
	</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<div class="table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name</th>
				<th>Bild</th>
				<th class="sortable" data-sort-type="string">Ort</th>
				<th class="sortable" data-sort-type="number">Verfügbar</th>
				<th class="sortable" data-sort-type="number">Defekt</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="item" items="${storageList}">
				<tr
					class="${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
					<td><a href="<c:url value='/storage/details?id=${item.id}'/>"><c:out
								value="${item.name}" /></a></td>
					<td style="text-align: center;"><c:if
							test="${not empty item.imagePath}">
							<button class="btn btn-small btn-info lightbox-trigger"
								data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
								title="Bild anzeigen">
								<i class="fas fa-image"></i>
							</button>
						</c:if></td>
					<td><c:out value="${item.location}" /></td>
					<td><c:out value="${item.availableQuantity}" /></td>
					<td><c:out value="${item.defectiveQuantity}" /></td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
						<button type="button"
							class="btn btn-small btn-warning edit-item-btn"
							data-fetch-url="<c:url value='/admin/storage?action=getItemData&id=${item.id}'/>">Bearbeiten</button>
						<c:set var="qrData">
							<c:url value="/storage/actions?id=${item.id}" />
						</c:set> <a
						href="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${fn:escapeXml(qrData)}"
						target="_blank" class="btn btn-small btn-secondary">QR-Code</a>
						<button class="btn btn-small defect-modal-btn"
							data-item-id="${item.id}"
							data-item-name="${fn:escapeXml(item.name)}"
							data-max-qty="${item.quantity}"
							data-current-defect-qty="${item.defectiveQuantity}"
							data-current-reason="${fn:escapeXml(item.defectReason)}">Defekt</button>
						<form action="<c:url value='/admin/storage'/>" method="post"
							class="js-confirm-form"
							data-confirm-message="Artikel '${fn:escapeXml(item.name)}' wirklich löschen?">
							<input type="hidden" name="action" value="delete"> <input
								type="hidden" name="id" value="${item.id}">
							<button type="submit" class="btn btn-small btn-danger">Löschen</button>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<div id="lightbox" class="lightbox-overlay">
	<span class="lightbox-close">×</span><img class="lightbox-content"
		id="lightbox-image">
</div>

<%@ include file="/WEB-INF/jspf/storage_modals.jspf"%>
<c:import url="../../jspf/table_scripts.jspf" />
<c:import url="../../jspf/main_footer.jspf" />

<script>
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.js-confirm-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
            showConfirmationModal(message, () => this.submit());
        });
    });

    // --- Lightbox Logic ---
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
        lightbox.addEventListener('click', (e) => { if(e.target === lightbox) { closeLightbox(); } });
        document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && lightbox.style.display === 'block') closeLightbox(); });
    }

    // --- Edit/Create Modal Logic ---
    const itemModal = document.getElementById('item-modal');
    if (itemModal) {
        const form = itemModal.querySelector('form');
        const title = itemModal.querySelector('h3');
        const actionInput = form.querySelector('input[name="action"]');
        const idInput = form.querySelector('input[name="id"]');
        const closeModalBtn = itemModal.querySelector('.modal-close-btn');

        document.getElementById('new-item-btn').addEventListener('click', () => {
            form.reset();
            title.textContent = 'Neuen Lagerartikel anlegen';
            actionInput.value = 'create';
            itemModal.classList.add('active');
        });

        document.querySelectorAll('.edit-item-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                form.reset();
                const fetchUrl = btn.dataset.fetchUrl;
                try {
                    const response = await fetch(fetchUrl);
                    if (!response.ok) throw new Error('Could not fetch item data.');
                    const itemData = await response.json();

                    title.textContent = 'Lagerartikel bearbeiten';
                    actionInput.value = 'update';
                    idInput.value = itemData.id;
                    form.querySelector('#name-modal').value = itemData.name || '';
                    form.querySelector('#location-modal').value = itemData.location || '';
                    form.querySelector('#cabinet-modal').value = itemData.cabinet || '';
                    form.querySelector('#compartment-modal').value = itemData.compartment || '';
                    form.querySelector('#quantity-modal').value = itemData.quantity;
                    form.querySelector('#maxQuantity-modal').value = itemData.maxQuantity;
                    form.querySelector('#weight_kg-modal').value = itemData.weightKg || '';
                    form.querySelector('#price_eur-modal').value = itemData.priceEur || '';
                    itemModal.classList.add('active');
                } catch (error) {
                    console.error("Failed to open edit modal:", error);
                    alert("Fehler beim Laden der Artikeldaten.");
                }
            });
        });
        closeModalBtn.addEventListener('click', () => itemModal.classList.remove('active'));
    }

    // --- Defect Modal Logic ---
    const defectModal = document.getElementById('defect-modal');
    if (defectModal) {
        const modalTitle = defectModal.querySelector('h3');
        const itemIdInput = defectModal.querySelector('#defect-item-id');
        const defectQtyInput = defectModal.querySelector('#defective_quantity');
        const reasonInput = defectModal.querySelector('#defect_reason');
        const closeModalBtn = defectModal.querySelector('.modal-close-btn');

        document.querySelectorAll('.defect-modal-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                modalTitle.textContent = `Defekt-Status für "${btn.dataset.itemName}" bearbeiten`;
                itemIdInput.value = btn.dataset.itemId;
                defectQtyInput.value = btn.dataset.currentDefectQty;
                defectQtyInput.max = btn.dataset.maxQty;
                reasonInput.value = btn.dataset.currentReason;
                defectModal.classList.add('active');
            });
        });
        closeModalBtn.addEventListener('click', () => defectModal.classList.remove('active'));
    }
});
</script>