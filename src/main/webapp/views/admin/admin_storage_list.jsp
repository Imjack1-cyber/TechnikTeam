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
				<th>Bild</th>
				<th class="sortable" data-sort-type="string">Ort</th>
				<th class="sortable" data-sort-type="number">Verfügbar</th>
				<th class="sortable" data-sort-type="number">Defekt</th>
				<th>Status</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="item" items="${storageList}">
				<tr
					class="${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
					<td><a href="<c:url value='/lager/details?id=${item.id}'/>"><c:out
								value="${item.name}" /></a></td>
					<td style="text-align: center;"><c:if
							test="${not empty item.imagePath}">
							<a href="#" class="lightbox-trigger"
								data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
								title="Bild anzeigen"> <img
								src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
								alt="Vorschaubild"
								style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;">
							</a>
						</c:if></td>
					<td><c:out value="${item.location}" /></td>
					<td><c:out value="${item.availableQuantity}" /></td>
					<td><c:out value="${item.defectiveQuantity}" /></td>
					<td><span
						class="status-badge ${item.status == 'IN_STORAGE' ? 'status-ok' : (item.status == 'CHECKED_OUT' ? 'status-danger' : 'status-warn')}"><c:out
								value="${item.status}" /></span></td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
						<button type="button" class="btn btn-small edit-item-btn"
							data-fetch-url="<c:url value='/admin/lager?action=getItemData&id=${item.id}'/>">Bearbeiten</button>

						<c:url var="actionUrl" value="/lager/aktionen">
							<c:param name="id" value="${item.id}" />
						</c:url> <c:url var="qrApiUrl"
							value="https://api.qrserver.com/v1/create-qr-code/">
							<c:param name="size" value="200x200" />
							<c:param name="data"
								value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${actionUrl}" />
						</c:url> <a href="${qrApiUrl}" target="_blank"
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

						<form action="<c:url value='/admin/lager'/>" method="post"
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

<%-- Lightbox is now created globally via main.js --%>

<jsp:include page="/WEB-INF/jspf/storage_modals.jspf" />
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_storage_list.js"></script>