<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
lager.jsp
This is the main inventory page for all users. It displays a list of all
storage items, grouped by their physical location. It provides actions for
users to check items in or out via a modal dialog. It also features a
lightbox for viewing item images and client-side filtering and sorting.
    It is served by: StorageServlet.
    It submits to: StorageTransactionServlet (from the transaction modal).
    Expected attributes:
        'storageData' (Map<String, List<de.technikteam.model.StorageItem>>): Items grouped by location.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lager" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />
<h1>Lagerübersicht</h1>
<p>Hier finden Sie eine Übersicht aller erfassten Artikel im Lager.
	Klicken Sie auf einen Artikelnamen für Details und Historie.</p>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">
		<i class="fas fa-check-circle"></i>
		<c:out value="${sessionScope.successMessage}" />
	</p>
	<%-- This removes the message from the session so it doesn't appear again on the next page load. --%>
	<c:remove var="successMessage" scope="session" />
</c:if>

<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">
		<i class="fas fa-exclamation-triangle"></i>
		<c:out value="${sessionScope.errorMessage}" />
	</p>
	<%-- This removes the message from the session so it doesn't appear again on the next page load. --%>
	<c:remove var="errorMessage" scope="session" />
</c:if>
<div class="table-controls">
	<div class="form-group" style="margin-bottom: 0; flex-grow: 1;">
		<input type="search" id="table-filter"
			placeholder="Alle Artikel filtern..." style="width: 100%;"
			aria-label="Lager filtern">
	</div>
</div>

<c:if test="${empty storageData}">
	<div class="card">
		<p>Derzeit sind keine Artikel im Lager erfasst.</p>
	</div>
</c:if>

<c:forEach var="locationEntry" items="${storageData}">
	<div class="location-group">
		<h2>
			<i class="fas fa-map-marker-alt"></i>
			<c:out value="${locationEntry.key}" />
		</h2>

		<!-- MOBILE LAYOUT -->
		<div class="mobile-card-list searchable-list">
			<c:forEach var="item" items="${locationEntry.value}">
				<div class="list-item-card"
					data-searchable-content="<c:out value='${item.name}'/> <c:out value='${item.cabinet}'/> <c:out value='${item.shelf}'/>">
					<h3 class="card-title">
						<a href="#" class="item-details-trigger" data-item-id="${item.id}"><c:out
								value="${item.name}" /></a>
					</h3>
					<div class="card-row">
						<span>Anzahl:</span> <span><c:out value="${item.quantity}" />
							<span class="status-badge ${item.availabilityStatusCssClass}"><c:out
									value="${item.availabilityStatus}" /></span></span>
					</div>
					<div class="card-row">
						<span>Ort:</span> <span><c:out value="${item.cabinet}" />
							/ <c:out value="${item.shelf}" /></span>
					</div>
					<div class="card-actions">
						<button class="btn btn-small btn-success transaction-btn"
							data-item-id="${item.id}"
							data-item-name="<c:out value="${item.name}"/>"
							data-type="checkin">
							<i class="fas fa-plus"></i> Einräumen
						</button>
						<button class="btn btn-small btn-danger transaction-btn"
							data-item-id="${item.id}"
							data-item-name="<c:out value="${item.name}"/>"
							data-type="checkout">
							<i class="fas fa-minus"></i> Entnehmen
						</button>
						<c:if test="${not empty item.imagePath}">
							<a href="#" class="btn btn-small btn-secondary lightbox-trigger"
								data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}"><i
								class="fas fa-image"></i> Bild</a>
						</c:if>
					</div>
				</div>
			</c:forEach>
		</div>

		<!-- DESKTOP LAYOUT -->
		<div class="desktop-table-wrapper">
			<table class="desktop-table sortable-table searchable-table">
				<thead>
					<tr>
						<th class="sortable" data-sort-type="string">Gerät</th>
						<th class="sortable" data-sort-type="string">Schrank</th>
						<th class="sortable" data-sort-type="string">Regal</th>
						<th class="sortable" data-sort-type="number">Anzahl</th>
						<th class="sortable" data-sort-type="string">Status</th>
						<th>Bild</th>
						<th>Aktion</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="item" items="${locationEntry.value}">
						<tr>
							<td><a href="#" class="item-details-trigger"
								data-item-id="${item.id}"><c:out value="${item.name}" /></a></td>
							<td><c:out value="${item.cabinet}" /></td>
							<td><c:out value="${item.shelf}" /></td>
							<td><c:out value="${item.quantity}" /></td>
							<td><span
								class="status-badge ${item.availabilityStatusCssClass}"><c:out
										value="${item.availabilityStatus}" /></span></td>
							<td><c:if test="${not empty item.imagePath}">
									<a href="#" class="lightbox-trigger"
										data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}">
										<img
										src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
										alt="<c:out value='${item.name}'/>" width="60"
										style="border-radius: 4px; vertical-align: middle; cursor: pointer; aspect-ratio: 4/3; object-fit: cover;">
									</a>
								</c:if></td>
							<td style="display: flex; gap: 0.5rem;">
								<button class="btn btn-small btn-success transaction-btn"
									data-item-id="${item.id}"
									data-item-name="<c:out value="${item.name}"/>"
									data-type="checkin">
									<i class="fas fa-plus"></i> Einräumen
								</button>
								<button class="btn btn-small btn-danger transaction-btn"
									data-item-id="${item.id}"
									data-item-name="<c:out value="${item.name}"/>"
									data-type="checkout">
									<i class="fas fa-minus"></i> Entnehmen
								</button>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>

</c:forEach>
<!-- Lightbox structure, placed once at the end of the page -->
<div class="lightbox-overlay" id="lightbox">
	<img src="" alt="Vergrößerte Ansicht">
</div>
<!-- Transaction Modal -->
<div class="modal-overlay" id="transaction-modal">
	<div class="modal-content" style="max-width: 450px;">
		<button class="modal-close-btn">×</button>
		<h3 id="transaction-modal-title">Artikel bewegen</h3>
		<form action="${pageContext.request.contextPath}/storage-transaction"
			method="post">
			<input type="hidden" name="itemId" id="transaction-item-id">
			<input type="hidden" name="type" id="transaction-type"> <input
				type="hidden" name="redirectUrl"
				value="${pageContext.request.contextPath}/lager">
			<div class="form-group">
				<label for="transaction-quantity">Anzahl</label> <input
					type="number" name="quantity" id="transaction-quantity" required
					min="1" value="1">
			</div>
			<div class="form-group">
				<label for="transaction-notes">Notiz (Grund, Event, etc.)</label>
				<textarea name="notes" id="transaction-notes" rows="2"
					placeholder="z.B. für Theater-AG, Reparatur..."></textarea>
			</div>
			<button type="submit" class="btn">Bestätigen</button>
		</form>
	</div>
</div>

<!-- Item Details & History Modal -->
<div class="modal-overlay" id="item-details-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3 id="details-modal-title">Artikeldetails</h3>
		<div class="card"
			style="border-top: none; box-shadow: none; padding: 0;">
			<h4 style="border: none; padding: 0; margin-bottom: 1rem;">Nutzungsverlauf</h4>
			<div id="item-history-container"
				style="max-height: 300px; overflow-y: auto;">
				<p>Lade Verlauf...</p>
			</div>
		</div>
	</div>
</div>


<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = "${pageContext.request.contextPath}";

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

    // Transaction Modal Logic
    const transactionModal = document.getElementById('transaction-modal');
    if (transactionModal) {
        const modalTitle = document.getElementById('transaction-modal-title');
        const modalItemId = document.getElementById('transaction-item-id');
        const modalType = document.getElementById('transaction-type');
        const closeModalBtn = transactionModal.querySelector('.modal-close-btn');

        const openModal = (btn) => {
            const type = btn.dataset.type;
            const actionText = type === 'checkin' ? 'Einräumen' : 'Entnehmen';
            modalTitle.textContent = `${btn.dataset.itemName} ${actionText}`;
            modalItemId.value = btn.dataset.itemId;
            modalType.value = type;
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

    // Item Details & History Modal Logic
    const detailsModal = document.getElementById('item-details-modal');
    if(detailsModal) {
        const detailsTitle = document.getElementById('details-modal-title');
        const historyContainer = document.getElementById('item-history-container');
        const closeDetailsBtn = detailsModal.querySelector('.modal-close-btn');

        const fetchAndShowHistory = async (itemId, itemName) => {
            detailsTitle.textContent = `Verlauf für: ${itemName}`;
            historyContainer.innerHTML = '<p>Lade Verlauf...</p>';
            detailsModal.classList.add('active');
            try {
                const response = await fetch(`${contextPath}/api/storage-history?itemId=${itemId}`);
                if (!response.ok) throw new Error('Network response was not ok');
                const history = await response.json();
                
                let html = '<ul class="details-list">';
                if (history.length > 0) {
                    history.forEach(entry => {
                        const changeClass = entry.quantityChange > 0 ? 'text-success' : 'text-danger';
                        const changeSign = entry.quantityChange > 0 ? '+' : '';
                        html += `<li>
                                    <div>
                                        <strong class="${changeClass}">${changeSign}${entry.quantityChange} Stück</strong>
                                        von <strong>${entry.username}</strong>
                                        <br>
                                        <small>${entry.notes || 'Keine Notiz'}</small>
                                    </div>
                                    <small>${new Date(entry.transactionTimestamp).toLocaleString('de-DE')}</small>
                                 </li>`;
                    });
                } else {
                    html += '<li>Kein Verlauf für diesen Artikel vorhanden.</li>';
                }
                html += '</ul>';
                historyContainer.innerHTML = html;

            } catch (error) {
                historyContainer.innerHTML = '<p class="error-message">Verlauf konnte nicht geladen werden.</p>';
                console.error('Fetch error:', error);
            }
        };
        
        document.querySelectorAll('.item-details-trigger').forEach(trigger => {
            trigger.addEventListener('click', (e) => {
                e.preventDefault();
                const itemId = trigger.dataset.itemId;
                const itemName = trigger.textContent;
                fetchAndShowHistory(itemId, itemName);
            });
        });

        closeDetailsBtn.addEventListener('click', () => detailsModal.classList.remove('active'));
        detailsModal.addEventListener('click', e => {
            if (e.target === detailsModal) detailsModal.classList.remove('active');
        });
    }

});
</script>