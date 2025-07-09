<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Lagerverwaltung" />
</c:import>

<h1>
	<i class="fas fa-warehouse"></i> Lagerverwaltung
</h1>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="table-controls">
	<button type="button" class="btn btn-success" id="new-item-btn">
		<i class="fas fa-plus"></i> Neuen Artikel anlegen
	</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Artikel filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<!-- Desktop Table View -->
<div class="desktop-table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name</th>
				<th class="sortable" data-sort-type="string">Ort</th>
				<th class="sortable" data-sort-type="string">Schrank</th>
				<th class="sortable" data-sort-type="string">Fach</th>
				<th class="sortable" data-sort-type="number">Verfügbar</th>
				<th>Status</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="item" items="${storageList}">
				<tr
					class="${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
					<td class="item-name-cell"><a
						href="<c:url value='/lager/details?id=${item.id}'/>"><c:out
								value="${item.name}" /></a> <c:if
							test="${not empty item.imagePath}">
							<button class="camera-btn lightbox-trigger"
								data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
								title="Bild anzeigen">
								<i class="fas fa-camera"></i>
							</button>
						</c:if></td>
					<td><c:out value="${item.location}" /></td>
					<td><c:out
							value="${not empty item.cabinet ? item.cabinet : '-'}" /></td>
					<td><c:out
							value="${not empty item.compartment ? item.compartment : '-'}" /></td>
					<td>${item.availableQuantity}/${item.maxQuantity}<c:if
							test="${item.defectiveQuantity > 0}">
							<span class="text-danger">(${item.defectiveQuantity} def.)</span>
						</c:if>
					</td>
					<td><span
						class="status-badge ${item.status == 'IN_STORAGE' ? 'status-ok' : (item.status == 'CHECKED_OUT' ? 'status-danger' : 'status-warn')}"><c:out
								value="${item.status}" /></span></td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
						<button type="button" class="btn btn-small edit-item-btn"
							data-fetch-url="<c:url value='/admin/lager?action=getItemData&id=${item.id}'/>">Bearbeiten</button>

						<c:set var="absoluteActionUrl"
							value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/lager/aktionen?id=${item.id}" />
						<c:set var="qrApiUrl"
							value="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${fn:escapeXml(absoluteActionUrl)}" />
						<a href="${qrApiUrl}" target="_blank"
						class="btn btn-small btn-secondary">QR-Code</a>

						<button class="btn btn-small btn-warning defect-modal-btn"
							data-item-id="${item.id}"
							data-item-name="${fn:escapeXml(item.name)}"
							data-max-qty="${item.quantity}"
							data-current-defect-qty="${item.defectiveQuantity}"
							data-current-reason="${fn:escapeXml(item.defectReason)}">Defekt</button>

						<button class="btn btn-small btn-info maintenance-modal-btn"
							data-item-id="${item.id}"
							data-item-name="${fn:escapeXml(item.name)}"
							data-current-status="${item.status}">Wartung</button>

						<form action="${pageContext.request.contextPath}/admin/lager"
							method="post" class="js-confirm-form"
							data-confirm-message="Artikel '${fn:escapeXml(item.name)}' wirklich löschen?">
							<input type="hidden" name="csrfToken"
								value="${sessionScope.csrfToken}"> <input type="hidden"
								name="action" value="delete"> <input type="hidden"
								name="id" value="${item.id}">
							<button type="submit" class="btn btn-small btn-danger">Löschen</button>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list searchable-table">
	<c:if test="${empty storageList}">
		<div class="card">
			<p>Keine Artikel gefunden.</p>
		</div>
	</c:if>
	<c:forEach var="item" items="${storageList}">
		<div
			class="list-item-card ${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
			<h3 class="card-title">
				<a href="<c:url value='/lager/details?id=${item.id}'/>"><c:out
						value="${item.name}" /></a>
				<c:if test="${not empty item.imagePath}">
					<button class="camera-btn lightbox-trigger"
						data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
						title="Bild anzeigen">
						<i class="fas fa-camera"></i>
					</button>
				</c:if>
			</h3>
			<div class="card-row">
				<span>Ort:</span> <strong><c:out value="${item.location}" />,
					<c:out value="${item.cabinet}" />, <c:out
						value="${item.compartment}" /></strong>
			</div>
			<div class="card-row">
				<span>Bestand:</span> <strong>${item.availableQuantity} /
					${item.maxQuantity} <c:if test="${item.defectiveQuantity > 0}">
						<span class="text-danger">(${item.defectiveQuantity} def.)</span>
					</c:if>
				</strong>
			</div>
			<div class="card-row">
				<span>Status:</span> <span><span
					class="status-badge ${item.status == 'IN_STORAGE' ? 'status-ok' : (item.status == 'CHECKED_OUT' ? 'status-danger' : 'status-warn')}"><c:out
							value="${item.status}" /></span></span>
			</div>
			<div class="card-actions">
				<button type="button" class="btn btn-small edit-item-btn"
					data-fetch-url="<c:url value='/admin/lager?action=getItemData&id=${item.id}'/>">Bearbeiten</button>
				<c:if test="${item.defectiveQuantity > 0}">
					<button class="btn btn-small btn-success repair-modal-btn"
						data-item-id="${item.id}"
						data-item-name="${fn:escapeXml(item.name)}"
						data-max-repair-qty="${item.defectiveQuantity}">Repariert</button>
				</c:if>
				<button class="btn btn-small btn-warning defect-modal-btn"
					data-item-id="${item.id}"
					data-item-name="${fn:escapeXml(item.name)}"
					data-max-qty="${item.quantity}"
					data-current-defect-qty="${item.defectiveQuantity}"
					data-current-reason="${fn:escapeXml(item.defectReason)}">Defekt</button>
				<button class="btn btn-small btn-info maintenance-modal-btn"
					data-item-id="${item.id}"
					data-item-name="${fn:escapeXml(item.name)}"
					data-current-status="${item.status}">Wartung</button>
				<form action="${pageContext.request.contextPath}/admin/lager"
					method="post" class="js-confirm-form"
					data-confirm-message="Artikel '${fn:escapeXml(item.name)}' wirklich löschen?">
					<input type="hidden" name="csrfToken"
						value="${sessionScope.csrfToken}"> <input type="hidden"
						name="action" value="delete"> <input type="hidden"
						name="id" value="${item.id}">
					<button type="submit" class="btn btn-small btn-danger">Löschen</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>

<div id="lightbox" class="lightbox-overlay">
	<span class="lightbox-close" title="Schließen">×</span> <img
		class="lightbox-content" id="lightbox-image" alt="Großansicht">
</div>

<jsp:include page="/WEB-INF/jspf/storage_modals.jspf" />

<!-- Repair Modal -->
<div class="modal-overlay" id="repair-modal">
	<div class="modal-content">
		<button type="button" class="modal-close-btn" aria-label="Schließen">×</button>
		<h3 id="repair-modal-title">Artikel als repariert markieren</h3>
		<form action="${pageContext.request.contextPath}/admin/lager"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" value="repair"> <input type="hidden" name="id"
				id="repair-item-id">
			<div class="form-group">
				<label for="repaired_quantity">Anzahl reparierter Artikel</label> <input
					type="number" name="repaired_quantity" id="repaired_quantity"
					value="1" min="1" required>
			</div>
			<div class="form-group">
				<label for="repair_notes">Notiz (optional)</label>
				<textarea name="repair_notes" id="repair_notes" rows="3"
					placeholder="z.B. Kabel neu gelötet"></textarea>
			</div>
			<button type="submit" class="btn btn-success">
				<i class="fas fa-check-circle"></i> Als repariert buchen
			</button>
		</form>
	</div>
</div>


<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_storage_list.js"></script>