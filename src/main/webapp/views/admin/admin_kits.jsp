<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Kit-Verwaltung" />
</c:import>

<h1>
	<i class="fas fa-box-open"></i> Kit-Verwaltung
</h1>
<p>Verwalten Sie hier wiederverwendbare Material-Zusammenstellungen
	(Kits oder Koffer).</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="table-controls">
	<button type="button" class="btn btn-success" id="new-kit-btn">
		<i class="fas fa-plus"></i> Neues Kit anlegen
	</button>
</div>

<div class="card">
	<c:if test="${empty kits}">
		<p>Es wurden noch keine Kits erstellt.</p>
	</c:if>
	<c:forEach var="kit" items="${kits}">
		<div class="kit-container"
			style="border-bottom: 1px solid var(--border-color); padding-bottom: 1.5rem; margin-bottom: 1.5rem;">
			<div class="kit-header"
				style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
				<div>
					<h3>
						<i class="fas fa-chevron-down toggle-icon"></i>
						<c:out value="${kit.name}" />
					</h3>
					<p
						style="margin: -0.5rem 0 0 1.75rem; color: var(--text-muted-color);">
						<c:out value="${kit.description}" />
					</p>
				</div>
				<div style="display: flex; gap: 0.5rem;">
					<a
						href="https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/pack-kit?kitId=${kit.id}"
						target="_blank" class="btn btn-small">QR-Code</a>
					<button type="button"
						class="btn btn-small btn-secondary edit-kit-btn"
						data-kit-id="${kit.id}" data-kit-name="${fn:escapeXml(kit.name)}"
						data-kit-desc="${fn:escapeXml(kit.description)}">Bearbeiten</button>
					<form action="${pageContext.request.contextPath}/admin/kits"
						method="post" class="js-confirm-form"
						data-confirm-message="Kit '${fn:escapeXml(kit.name)}' wirklich löschen?">
						<input type="hidden" name="action" value="delete"> <input
							type="hidden" name="id" value="${kit.id}">
						<button type="submit" class="btn btn-small btn-danger">Löschen</button>
					</form>
				</div>
			</div>
			<div class="kit-content"
				style="display: none; padding-left: 2rem; margin-top: 1rem;">
				<c:set var="kitItems" value="${kitDAO.getItemsForKit(kit.id)}"
					scope="request" />
				<h4>Inhalt</h4>
				<ul class="details-list">
					<c:if test="${empty kitItems}">
						<li>Dieses Kit ist leer.</li>
					</c:if>
					<c:forEach var="item" items="${kitItems}">
						<li><span>${item.quantity} x ${item.itemName}</span>
							<form action="${pageContext.request.contextPath}/admin/kits"
								method="post">
								<input type="hidden" name="action" value="removeItem"> <input
									type="hidden" name="kitId" value="${kit.id}"> <input
									type="hidden" name="itemId" value="${item.itemId}">
								<button type="submit" class="btn btn-small btn-danger-outline">&times;</button>
							</form></li>
					</c:forEach>
				</ul>

				<h4 style="margin-top: 1.5rem;">Artikel hinzufügen</h4>
				<form action="${pageContext.request.contextPath}/admin/kits"
					method="post"
					style="display: flex; gap: 1rem; align-items: flex-end;">
					<input type="hidden" name="action" value="addItem"> <input
						type="hidden" name="kitId" value="${kit.id}">
					<div class="form-group" style="flex-grow: 2; margin-bottom: 0;">
						<label>Artikel</label> <select name="itemId" required>
							<option value="">-- Artikel auswählen --</option>
							<c:forEach var="item" items="${allItems}">
								<option value="${item.id}">${item.name}</option>
							</c:forEach>
						</select>
					</div>
					<div class="form-group" style="flex-grow: 1; margin-bottom: 0;">
						<label>Anzahl</label> <input type="number" name="quantity"
							value="1" min="1" required>
					</div>
					<button type="submit" class="btn btn-small">Hinzufügen</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>

<!-- Modal for Create/Edit Kit -->
<div class="modal-overlay" id="kit-modal">
	<div class="modal-content">
		<button class="modal-close-btn" type="button" aria-label="Schließen">×</button>
		<h3>Kit verwalten</h3>
		<form action="${pageContext.request.contextPath}/admin/kits"
			method="post">
			<input type="hidden" name="action" value=""> <input
				type="hidden" name="id" value="">
			<div class="form-group">
				<label for="name-modal">Name des Kits</label> <input type="text"
					id="name-modal" name="name" required>
			</div>
			<div class="form-group">
				<label for="description-modal">Beschreibung</label>
				<textarea id="description-modal" name="description" rows="3"></textarea>
			</div>
			<button type="submit" class="btn">Speichern</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/admin/admin_kits.js"></script>