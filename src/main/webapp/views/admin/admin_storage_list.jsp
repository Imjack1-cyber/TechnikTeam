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

<div class="table-wrapper">
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
					<td>${item.availableQuantity} / ${item.quantity} <c:if
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
	<span class="lightbox-close" title="Schließen">×</span> <img
		class="lightbox-content" id="lightbox-image" alt="Großansicht">
</div>

<jsp:include page="/WEB-INF/jspf/storage_modals.jspf" />
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_storage_list.js"></script>