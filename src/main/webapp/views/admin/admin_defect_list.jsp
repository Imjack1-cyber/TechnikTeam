<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Defekte Artikel" />
</c:import>

<h1>
	<i class="fas fa-wrench"></i> Defekte Artikel verwalten
</h1>
<p>Hier sind alle Artikel gelistet, von denen mindestens ein
	Exemplar als defekt markiert wurde.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<!-- Desktop Table View -->
<div class="desktop-table-wrapper">
	<table class="data-table">
		<thead>
			<tr>
				<th>Name</th>
				<th>Defekt / Gesamt</th>
				<th>Grund</th>
				<th>Aktion</th>
			</tr>
		</thead>
		<tbody>
			<c:if test="${empty defectiveItems}">
				<tr>
					<td colspan="4" style="text-align: center;">Aktuell sind keine
						Artikel als defekt gemeldet.</td>
				</tr>
			</c:if>
			<c:forEach var="item" items="${defectiveItems}">
				<tr>
					<td><a href="<c:url value='/lager/details?id=${item.id}'/>"><c:out
								value="${item.name}" /></a></td>
					<td><c:out value="${item.defectiveQuantity}" /> / <c:out
							value="${item.quantity}" /></td>
					<td><c:out value="${item.defectReason}" /></td>
					<td>
						<button class="btn btn-small btn-warning defect-modal-btn"
							data-item-id="${item.id}"
							data-item-name="${fn:escapeXml(item.name)}"
							data-max-qty="${item.quantity}"
							data-current-defect-qty="${item.defectiveQuantity}"
							data-current-reason="${fn:escapeXml(item.defectReason)}">Status
							bearbeiten</button>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list">
	<c:if test="${empty defectiveItems}">
		<div class="card">
			<p>Aktuell sind keine Artikel als defekt gemeldet.</p>
		</div>
	</c:if>
	<c:forEach var="item" items="${defectiveItems}">
		<div class="list-item-card">
			<h3 class="card-title">
				<a href="<c:url value='/lager/details?id=${item.id}'/>"><c:out
						value="${item.name}" /></a>
			</h3>
			<div class="card-row">
				<span>Defekt / Gesamt:</span> <strong><c:out
						value="${item.defectiveQuantity}" /> / <c:out
						value="${item.quantity}" /></strong>
			</div>
			<div class="card-row"
				style="flex-direction: column; align-items: flex-start;">
				<span>Grund:</span>
				<p style="margin-top: 0.25rem; font-size: 0.9em; width: 100%;">
					<c:out
						value="${not empty item.defectReason ? item.defectReason : 'Kein Grund angegeben.'}" />
				</p>
			</div>
			<div class="card-actions">
				<button class="btn btn-small btn-warning defect-modal-btn"
					data-item-id="${item.id}"
					data-item-name="${fn:escapeXml(item.name)}"
					data-max-qty="${item.quantity}"
					data-current-defect-qty="${item.defectiveQuantity}"
					data-current-reason="${fn:escapeXml(item.defectReason)}">Status
					bearbeiten</button>
			</div>
		</div>
	</c:forEach>
</div>


<!-- Modal for updating defect status -->
<div class="modal-overlay" id="defect-modal">
	<div class="modal-content">
		<button type="button" class="modal-close-btn" aria-label="Schließen">×</button>
		<h3 id="defect-modal-title">Defekt-Status bearbeiten</h3>
		<form action="${pageContext.request.contextPath}/admin/lager"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" value="updateDefect"> <input type="hidden"
				name="id" id="defect-item-id"> <input type="hidden"
				name="returnTo" value="defekte">
			<div class="form-group">
				<label for="defective_quantity">Anzahl defekter Artikel</label> <input
					type="number" name="defective_quantity" id="defective_quantity"
					min="0" required>
			</div>
			<div class="form-group">
				<label for="defect_reason">Grund (optional)</label>
				<textarea name="defect_reason" id="defect_reason" rows="3"></textarea>
			</div>
			<button type="submit" class="btn">
				<i class="fas fa-save"></i> Speichern
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_defect_list.js"></script>