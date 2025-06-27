<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Lager"/>
</c:import>

<h1><i class="fas fa-boxes"></i> Lagerübersicht</h1>
<p>Hier finden Sie eine Übersicht aller erfassten Artikel im Lager.</p>

<%-- Session Messages --%>
<c:if test="${not empty sessionScope.successMessage}"><p class="success-message"><c:out value="${sessionScope.successMessage}"/></p><c:remove var="successMessage" scope="session"/></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><p class="error-message"><c:out value="${sessionScope.errorMessage}"/></p><c:remove var="errorMessage" scope="session"/></c:if>

<div class="table-controls">
	<div class="form-group" style="margin-bottom: 0; flex-grow: 1;">
		<input type="search" id="table-filter" placeholder="Alle Artikel filtern..." aria-label="Lager filtern">
	</div>
</div>

<c:if test="${empty storageData}">
	<div class="card"><p>Derzeit sind keine Artikel im Lager erfasst.</p></div>
</c:if>

<c:forEach var="locationEntry" items="${storageData}">
	<div class="card">
		<h2><i class="fas fa-map-marker-alt"></i> <c:out value="${locationEntry.key}" /></h2>
        <div class="table-wrapper">
            <table class="data-table searchable-table">
                <thead>
                    <tr>
                        <th>Gerät</th>
                        <th>Verfügbar</th>
                        <th>Defekt</th>
                        <th>Status</th>
                        <th>Aktion</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${locationEntry.value}">
                        <tr class="${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
                            <td><a href="#" class="item-details-trigger" data-item-id="${item.id}"><c:out value="${item.name}" /></a></td>
                            <td>${item.availableQuantity} / ${item.quantity}</td>
                            <td>${item.defectiveQuantity}</td>
                            <td><span class="status-badge ${item.availabilityStatusCssClass}"><c:out value="${item.availabilityStatus}" /></span></td>
                            <td>
                                <button class="btn btn-small transaction-btn"
                                    data-item-id="${item.id}"
                                    data-item-name="${fn:escapeXml(item.name)}"
                                    data-max-qty="${item.availableQuantity}" ${item.availableQuantity <= 0 ? 'disabled' : ''}>
                                    Entnehmen/Einräumen
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
	</div>
</c:forEach>
<script>
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = "${'${pageContext.request.contextPath}'}"; // Escaped for JS
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
                                    <small>${entry.transactionTimestampLocaleString}</small> 
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
<c:import url="/WEB-INF/jspf/footer.jspf" />