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

	<div class="card">
		<h2 class="card-title">${item.name}</h2>
		<c:if test="${not empty item.imagePath}">
			<a href="#" class="lightbox-trigger"><img
				src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
				alt="${item.name}"
				style="width: 100%; border-radius: var(--border-radius); margin-bottom: 1rem; cursor: zoom-in;"></a>
		</c:if>
		<ul class="details-list">
			<li><strong>Allg. Status:</strong> <span
				class="status-badge ${item.availabilityStatusCssClass}">${item.availabilityStatus}</span></li>
			<li><strong>Verfügbar:</strong> ${item.availableQuantity} /
				${item.quantity}</li>
			<li><strong>Defekt:</strong> ${item.defectiveQuantity}</li>
			<li><strong>Tracking-Status:</strong> <c:choose>
					<c:when test="${item.status == 'CHECKED_OUT'}">
						<span class="status-badge status-warn">Entnommen</span>
					</c:when>
					<c:when test="${item.status == 'MAINTENANCE'}">
						<span class="status-badge status-info">Wartung</span>
					</c:when>
					<c:otherwise>
						<span class="status-badge status-ok">Im Lager</span>
					</c:otherwise>
				</c:choose></li>
			<c:if test="${not empty item.currentHolderUsername}">
				<li><strong>Aktueller Inhaber:</strong>
					${item.currentHolderUsername}</li>
			</c:if>
			<li><strong>Ort:</strong> ${item.location}</li>
			<li><strong>Schrank:</strong> ${not empty item.cabinet ? item.cabinet : 'N/A'}</li>
			<li><strong>Fach:</strong> ${not empty item.compartment ? item.compartment : 'N/A'}</li>
		</ul>
		<div style="margin-top: 2rem;">
			<%-- CORRECTED: The link should go to /lager as per the servlet mapping --%>
			<a href="<c:url value='/lager'/>" class="btn btn-small"><i
				class="fas fa-arrow-left"></i> Zur Lagerübersicht</a>
		</div>
	</div>

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
								class="status-badge ${entry.quantityChange > 0 ? 'status-ok' : 'status-danger'}">${entry.quantityChange > 0 ? '+' : ''}${entry.quantityChange}</span></td>
							<td>${entry.username}</td>
							<td>${not empty entry.notes ? entry.notes : '-'}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</div>

<div id="lightbox" class="lightbox-overlay">
	<span class="lightbox-close">×</span><img class="lightbox-content"
		id="lightbox-image">
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />
<script type="text/javascript" src="/js/public/storage_item_details.js"></script>