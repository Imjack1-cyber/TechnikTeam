<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%-- CORRECTED: Import uses absolute path and correct filename --%>
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
						<th>Status</th>
						<th>Inhaber</th>
						<th>Verfügbar</th>
						<th>Defekt</th>
						<th>Aktion</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="item" items="${locationEntry.value}">
						<tr
							class="${item.defectiveQuantity > 0 ? 'item-status-defect' : ''}">
							<td><a href="<c:url value='/lager/details?id=${item.id}'/>"
								title="Details für ${item.name} ansehen"><c:out
										value="${item.name}" /></a></td>
							<td><c:choose>
									<c:when test="${item.status == 'CHECKED_OUT'}">
										<span class="status-badge status-warn">Entnommen</span>
									</c:when>
									<c:when test="${item.status == 'MAINTENANCE'}">
										<span class="status-badge status-info">Wartung</span>
									</c:when>
									<c:otherwise>
										<span class="status-badge status-ok">Im Lager</span>
									</c:otherwise>
								</c:choose></td>
							<td><c:out
									value="${not empty item.currentHolderUsername ? item.currentHolderUsername : '-'}" /></td>
							<td>${item.availableQuantity}/${item.quantity}</td>
							<td>${item.defectiveQuantity}</td>
							<td>
								<button class="btn btn-small transaction-btn btn-primary"
									data-item-id="${item.id}"
									data-item-name="${fn:escapeXml(item.name)}"
									data-max-qty="${item.availableQuantity}"
									${item.availableQuantity <= 0 && item.status != 'CHECKED_OUT' ? 'disabled' : ''}>
									Aktion</button>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</c:forEach>

<%@ include file="/WEB-INF/jspf/storage_modals.jspf"%>
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script type="text/javascript" src="/js/public/lager.js"></script>