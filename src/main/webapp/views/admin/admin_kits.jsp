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
					<c:set var="absoluteActionUrl"
						value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/pack-kit?kitId=${kit.id}" />
					<c:set var="qrApiUrl"
						value="https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${fn:escapeXml(absoluteActionUrl)}" />
					<a href="${qrApiUrl}" target="_blank" class="btn btn-small">QR-Code</a>

					<button type="button"
						class="btn btn-small btn-secondary edit-kit-btn"
						data-kit-id="${kit.id}" data-kit-name="${fn:escapeXml(kit.name)}"
						data-kit-desc="${fn:escapeXml(kit.description)}"
						data-kit-location="${fn:escapeXml(kit.location)}">Bearbeiten</button>
					<form action="${pageContext.request.contextPath}/admin/kits"
						method="post" class="js-confirm-form"
						data-confirm-message="Kit '${fn:escapeXml(kit.name)}' wirklich löschen?">
						<input type="hidden" name="csrfToken"
							value="${sessionScope.csrfToken}"> <input type="hidden"
							name="action" value="delete"> <input type="hidden"
							name="id" value="${kit.id}">
						<button type="submit" class="btn btn-small btn-danger">Löschen</button>
					</form>
				</div>
			</div>
			<div class="kit-content"
				style="display: none; padding-left: 2rem; margin-top: 1rem;">

				<form action="${pageContext.request.contextPath}/admin/kits"
					method="post">
					<input type="hidden" name="csrfToken"
						value="${sessionScope.csrfToken}"> <input type="hidden"
						name="action" value="updateKitItems"> <input type="hidden"
						name="kitId" value="${kit.id}">

					<h4>Inhalt bearbeiten</h4>
					<div id="kit-items-container-${kit.id}" class="kit-items-container">
						<c:if test="${empty kit.items}">
							<p class="no-items-message">Dieses Kit ist leer. Fügen Sie
								einen Artikel hinzu.</p>
						</c:if>
						<c:forEach var="item" items="${kit.items}">
							<div class="dynamic-row">
								<select name="itemIds" class="form-group">
									<c:forEach var="storageItem" items="${allItems}">
										<option value="${storageItem.id}"
											${storageItem.id == item.itemId ? 'selected' : ''}>
											<c:out value="${storageItem.name}" />
										</option>
									</c:forEach>
								</select> <input type="number" name="quantities" value="${item.quantity}"
									min="1" class="form-group" style="max-width: 100px;">
								<button type="button"
									class="btn btn-small btn-danger btn-remove-kit-item-row"
									title="Zeile entfernen">×</button>
							</div>
						</c:forEach>
					</div>

					<div
						style="margin-top: 1rem; display: flex; justify-content: space-between; align-items: center;">
						<button type="button" class="btn btn-small btn-add-kit-item-row"
							data-container-id="kit-items-container-${kit.id}">
							<i class="fas fa-plus"></i> Zeile hinzufügen
						</button>
						<button type="submit" class="btn btn-success">
							<i class="fas fa-save"></i> Kit-Inhalt speichern
						</button>
					</div>
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
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" value=""> <input type="hidden" name="id"
				value="">
			<div class="form-group">
				<label for="name-modal">Name des Kits</label> <input type="text"
					id="name-modal" name="name" required>
			</div>
			<div class="form-group">
				<label for="description-modal">Beschreibung</label>
				<textarea id="description-modal" name="description" rows="3"></textarea>
			</div>
			<div class="form-group">
				<label for="location-modal">Physischer Standort des Kits</label> <input
					type="text" id="location-modal" name="location"
					placeholder="z.B. Lager, Schrank 3, Fach A">
			</div>
			<button type="submit" class="btn">Speichern</button>
		</form>
	</div>
</div>

<script id="allItemsData" type="application/json">
    ${allItemsJson}
</script>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/admin/admin_kits.js"></script>