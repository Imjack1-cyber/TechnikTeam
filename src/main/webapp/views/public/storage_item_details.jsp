<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle"
		value="Artikeldetails: ${fn:escapeXml(item.name)}" />
</c:import>

<h1>
	<i class="fas fa-cube"></i> Artikeldetails
</h1>

<div class="dashboard-grid"
	style="grid-template-columns: 1fr 2fr; align-items: flex-start;">

	<div class="card">
		<h2 class="card-title">
			<c:out value="${item.name}" />
		</h2>
		<c:if test="${not empty item.imagePath}">
			<a
				href="${pageContext.request.contextPath}/image?file=${item.imagePath}"
				class="lightbox-trigger"> <img
				src="${pageContext.request.contextPath}/image?file=${item.imagePath}"
				alt="<c:out value='${item.name}'/>"
				style="width: 100%; border-radius: var(--border-radius); margin-bottom: 1rem; cursor: zoom-in;">
			</a>
		</c:if>
		<ul class="details-list">
			<li><strong>Allg. Status:</strong> <span
				class="status-badge ${item.availabilityStatusCssClass}"><c:out
						value="${item.availabilityStatus}" /></span></li>
			<li><strong>Verfügbar:</strong> <span><c:out
						value="${item.availableQuantity}" /> / <c:out
						value="${item.quantity}" /></span></li>
			<li><strong>Defekt:</strong> <span><c:out
						value="${item.defectiveQuantity}" /></span></li>
			<li><strong>Tracking-Status:</strong> <span> <c:choose>
						<c:when test="${item.status == 'CHECKED_OUT'}">
							<span class="status-badge status-warn"><c:out
									value="Entnommen" /></span>
						</c:when>
						<c:when test="${item.status == 'MAINTENANCE'}">
							<span class="status-badge status-info"><c:out
									value="Wartung" /></span>
						</c:when>
						<c:otherwise>
							<span class="status-badge status-ok"><c:out
									value="Im Lager" /></span>
						</c:otherwise>
					</c:choose>
			</span></li>
			<c:if test="${not empty item.currentHolderUsername}">
				<li><strong>Aktueller Inhaber:</strong> <span><c:out
							value="${item.currentHolderUsername}" /></span></li>
			</c:if>
			<li><strong>Ort:</strong> <span><c:out
						value="${item.location}" /></span></li>
			<li><strong>Schrank:</strong> <span><c:out
						value="${not empty item.cabinet ? item.cabinet : 'N/A'}" /></span></li>
			<li><strong>Fach:</strong> <span><c:out
						value="${not empty item.compartment ? item.compartment : 'N/A'}" /></span></li>
		</ul>
		<div style="margin-top: 2rem;">
			<a href="<c:url value='/lager'/>" class="btn btn-small"><i
				class="fas fa-arrow-left"></i> Zur Lagerübersicht</a>
		</div>
	</div>

	<div class="card">
		<div class="modal-tabs">
			<button class="modal-tab-button active" data-tab="history-tab">Verlauf</button>
			<button class="modal-tab-button" data-tab="maintenance-tab">Wartungshistorie</button>
		</div>

		<div id="history-tab" class="modal-tab-content active">
			<h2 class="card-title" style="border: none; padding: 0;">Verlauf
				/ Chronik</h2>
			<div class="table-wrapper"
				style="max-height: 60vh; overflow-y: auto;">
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
								<td colspan="4" style="text-align: center;">Kein Verlauf
									für diesen Artikel vorhanden.</td>
							</tr>
						</c:if>
						<c:forEach var="entry" items="${history}">
							<tr>
								<td><c:out
										value="${entry.transactionTimestampLocaleString}" /></td>
								<td><span
									class="status-badge ${entry.quantityChange > 0 ? 'status-ok' : 'status-danger'}">${entry.quantityChange > 0 ? '+' : ''}<c:out
											value="${entry.quantityChange}" /></span></td>
								<td><c:out value="${entry.username}" /></td>
								<td><c:out
										value="${not empty entry.notes ? entry.notes : '-'}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>

		<div id="maintenance-tab" class="modal-tab-content">
			<h2 class="card-title" style="border: none; padding: 0;">Wartungshistorie</h2>
			<div class="table-wrapper"
				style="max-height: 60vh; overflow-y: auto;">
				<table class="data-table">
					<thead>
						<tr>
							<th>Datum</th>
							<th>Aktion</th>
							<th>Bearbeiter</th>
							<th>Notiz</th>
						</tr>
					</thead>
					<tbody>
						<c:if test="${empty maintenanceHistory}">
							<tr>
								<td colspan="4" style="text-align: center;">Keine
									Wartungseinträge für diesen Artikel vorhanden.</td>
							</tr>
						</c:if>
						<c:forEach var="entry" items="${maintenanceHistory}">
							<tr>
								<td><c:out value="${entry.formattedLogDate}" /></td>
								<td><c:out value="${entry.action}" /></td>
								<td><c:out value="${entry.username}" /></td>
								<td><c:out value="${entry.notes}" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>


<div id="lightbox" class="lightbox-overlay">
	<span class="lightbox-close" title="Schließen">×</span> <img
		class="lightbox-content" id="lightbox-image" alt="Großansicht">
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/public/storage_item_details.js"></script>