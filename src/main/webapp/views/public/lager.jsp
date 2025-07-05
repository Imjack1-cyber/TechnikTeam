<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Lager" />
</c:import>

<h1>
	<i class="fas fa-boxes"></i> Lagerübersicht
</h1>
<p>Hier finden Sie eine Übersicht aller erfassten Artikel im Lager.
	Klicken Sie auf einen Artikelnamen für Details und Verlauf.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

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
						<th class="sortable" data-sort-type="string">Schrank</th>
						<th class="sortable" data-sort-type="string">Fach</th>
						<th>Status</th>
						<th>Bestand</th>
						<th>Aktion</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="item" items="${locationEntry.value}">
						<tr
							class="${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
							<td class="item-name-cell"><a
								href="<c:url value='/lager/details?id=${item.id}'/>"
								title="Details für ${item.name} ansehen"><c:out
										value="${item.name}" /></a> <c:if
									test="${not empty item.imagePath}">
									<button class="camera-btn lightbox-trigger"
										data-src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
										title="Bild anzeigen">
										<i class="fas fa-camera"></i>
									</button>
								</c:if></td>
							<td><c:out
									value="${not empty item.cabinet ? item.cabinet : '-'}" /></td>
							<td><c:out
									value="${not empty item.compartment ? item.compartment : '-'}" /></td>
							<td><c:choose>
									<c:when test="${item.status == 'CHECKED_OUT'}">
										<span class="status-badge status-warn">Entnommen</span>
										<span class="item-status-details">an:
											${item.currentHolderUsername}</span>
									</c:when>
									<c:when test="${item.status == 'MAINTENANCE'}">
										<span class="status-badge status-info">Wartung</span>
									</c:when>
									<c:otherwise>
										<span class="status-badge status-ok">Im Lager</span>
									</c:otherwise>
								</c:choose></td>
							<td><span class="inventory-details">${item.availableQuantity}
									/ ${item.quantity}</span> <c:if test="${item.defectiveQuantity > 0}">
									<span class="inventory-details text-danger">(${item.defectiveQuantity}
										defekt)</span>
								</c:if></td>
							<td>
								<button class="btn btn-small transaction-btn"
									data-item-id="${item.id}"
									data-item-name="${fn:escapeXml(item.name)}"
									data-max-qty="${item.availableQuantity}"
									data-current-qty="${item.quantity}"
									data-total-max-qty="${item.maxQuantity}">Aktion</button>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</c:forEach>

<!-- Lightbox Modal -->
<div id="lightbox" class="lightbox-overlay">
	<span class="lightbox-close" title="Schließen">×</span> <img
		class="lightbox-content" id="lightbox-image" alt="Großansicht">
</div>

<%@ include file="/WEB-INF/jspf/storage_modals.jspf"%>
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/public/lager.js"></script>