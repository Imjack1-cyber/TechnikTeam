<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
admin_storage_list.jsp

This JSP displays a list of all inventory items for administrators.
It provides actions to edit an item, delete it, or generate a QR code.
Creating and editing items are handled via a modal dialog on this page.

    It is served by: AdminStorageServlet (doGet).

    Expected attributes:

        'storageList' (List<de.technikteam.model.StorageItem>): A flat list of all inventory items.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lagerverwaltung" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Lagerverwaltung</h1>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">
		<c:out value="${sessionScope.successMessage}" />
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">
		<c:out value="${sessionScope.errorMessage}" />
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<div class="table-controls">
	<button type="button" class="btn" id="new-item-btn">Neuen
		Artikel anlegen</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<c:if test="${empty storageList}">
	<div class="card">
		<p>Es sind noch keine Lagerartikel erfasst worden.</p>
	</div>
</c:if>
<!-- MOBILE LAYOUT -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="item" items="${storageList}">
		<div class="list-item-card"
			data-searchable-content="<c:out value='${item.name}'/> <c:out value='${item.location}'/> <c:out value='${item.cabinet}'/>">
			<h3 class="card-title">
				<c:out value="${item.name}" />
			</h3>
			<div class="card-row">
				<span>Ort:</span> <span><c:out value="${item.location}" /></span>
			</div>
			<div class="card-row">
				<span>Anzahl:</span> <span><c:out value="${item.quantity}" /></span>
			</div>
			<div class="card-actions">
				<button type="button" class="btn btn-small edit-item-btn"
					data-id="${item.id}">Bearbeiten</button>
				<a
					href="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/storage-item?id=${item.id}"
					target="_blank" class="btn btn-small btn-success">QR-Code</a>
				<form action="${pageContext.request.contextPath}/admin/storage"
					method="post" class="js-confirm-form"
					data-confirm-message="Artikel '${fn:escapeXml(item.name)}' wirklich löschen?">
					<input type="hidden" name="action" value="delete"> <input
						type="hidden" name="id" value="${item.id}">
					<button type="submit" class="btn btn-small btn-danger">Löschen</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>
<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name</th>
				<th class="sortable" data-sort-type="string">Ort</th>
				<th class="sortable" data-sort-type="string">Schrank</th>
				<th class="sortable" data-sort-type="number">Anzahl</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="item" items="${storageList}">
				<tr>
					<td><a
						href="${pageContext.request.contextPath}/storage-item?id=${item.id}"><c:out
								value="${item.name}" /></a></td>
					<td><c:out value="${item.location}" /></td>
					<td><c:out value="${item.cabinet}" /></td>
					<td><c:out value="${item.quantity}" /></td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
						<button type="button" class="btn btn-small edit-item-btn"
							data-id="${item.id}">Bearbeiten</button> <a
						href="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/storage-item?id=${item.id}"
						target="_blank" class="btn btn-small btn-success">QR-Code</a>
						<form action="${pageContext.request.contextPath}/admin/storage"
							method="post" class="js-confirm-form"
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
<!-- MODAL FOR NEW/EDIT STORAGE ITEM -->
<div class="modal-overlay" id="item-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3 id="item-modal-title">Lagerartikel</h3>
		<form id="item-modal-form"
			action="${pageContext.request.contextPath}/admin/storage"
			method="post" enctype="multipart/form-data">

			<input type="hidden" name="action" id="item-modal-action"> <input
				type="hidden" name="id" id="item-modal-id">

			<div class="form-group">
				<label for="name-modal">Artikelname</label> <input type="text"
					id="name-modal" name="name" required>
			</div>
			<div class="form-group">
				<label for="location-modal">Ort</label> <input type="text"
					id="location-modal" name="location" required>
			</div>
			<div class="form-group">
				<label for="cabinet-modal">Schrank</label> <input type="text"
					id="cabinet-modal" name="cabinet">
			</div>
			<div class="form-group">
				<label for="shelf-modal">Regal</label> <input type="text"
					id="shelf-modal" name="shelf">
			</div>
			<div class="form-group">
				<label for="compartment-modal">Fach / Kiste</label> <input
					type="text" id="compartment-modal" name="compartment">
			</div>
			<div class="form-group">
				<label for="quantity-modal">Aktuelle Anzahl</label> <input
					type="number" id="quantity-modal" name="quantity" value="0"
					required min="0">
			</div>
			<div class="form-group">
				<label for="maxQuantity-modal">Maximale Anzahl (0 für
					unbegrenzt)</label> <input type="number" id="maxQuantity-modal"
					name="maxQuantity" value="0" required min="0">
			</div>
			<div class="form-group">
				<label for="imageFile-modal">Bild (optional)</label> <input
					type="file" id="imageFile-modal" name="imageFile"
					accept="image/jpeg, image/png, image/gif, image/webp">
			</div>
			<button type="submit" class="btn">
				<i class="fas fa-save"></i> Speichern
			</button>
		</form>
	</div>

</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = "${pageContext.request.contextPath}";
    // Custom confirmation for delete forms
    document.querySelectorAll('.js-confirm-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
            showConfirmationModal(message, () => this.submit());
        });
    });

    // Modal Logic
    const modal = document.getElementById('item-modal');
    if (!modal) return;

    const form = document.getElementById('item-modal-form');
    const title = document.getElementById('item-modal-title');
    const actionInput = document.getElementById('item-modal-action');
    const idInput = document.getElementById('item-modal-id');

    const openModalBtn = document.getElementById('new-item-btn');
    const closeModalBtn = modal.querySelector('.modal-close-btn');

    const openCreateModal = () => {
        form.reset();
        title.textContent = 'Neuen Lagerartikel anlegen';
        actionInput.value = 'create';
        idInput.value = '';
        modal.classList.add('active');
    };

    const openEditModal = async (btn) => {
        form.reset();
        const itemId = btn.dataset.id;
        try {
            const response = await fetch(`${contextPath}/admin/storage?action=getItemData&id=${itemId}`);
            if (!response.ok) throw new Error('Could not fetch item data.');
            const itemData = await response.json();

            title.textContent = 'Lagerartikel bearbeiten';
            actionInput.value = 'update';
            idInput.value = itemData.id;
            form.querySelector('#name-modal').value = itemData.name || '';
            form.querySelector('#location-modal').value = itemData.location || '';
            form.querySelector('#cabinet-modal').value = itemData.cabinet || '';
            form.querySelector('#shelf-modal').value = itemData.shelf || '';
            form.querySelector('#compartment-modal').value = itemData.compartment || '';
            form.querySelector('#quantity-modal').value = itemData.quantity || 0;
            form.querySelector('#maxQuantity-modal').value = itemData.maxQuantity || 0;
            modal.classList.add('active');

        } catch (error) {
            console.error("Failed to open edit modal:", error);
            alert("Fehler beim Laden der Artikeldaten.");
        }
    };

    const closeModal = () => modal.classList.remove('active');

    openModalBtn.addEventListener('click', openCreateModal);
    document.querySelectorAll('.edit-item-btn').forEach(btn => {
        btn.addEventListener('click', () => openEditModal(btn));
    });

    closeModalBtn.addEventListener('click', closeModal);
    modal.addEventListener('click', e => {
        if (e.target === modal) closeModal();
    });
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && modal.classList.contains('active')) closeModal();
    });
});
</script>