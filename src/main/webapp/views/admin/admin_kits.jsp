<%-- src/main/webapp/views/admin/admin_kits.jsp --%>
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
	<button type="button" class="btn btn-success" id="new-kit-btn"
		data-modal-target="kit-modal">
		<i class="fas fa-plus"></i> Neues Kit anlegen
	</button>
</div>

<%-- This is now a shell container to be filled by JavaScript --%>
<div class="card" id="kits-container">
	<p>Lade Kits...</p>
</div>

<!-- Modal for Create/Edit Kit Metadata -->
<div class="modal-overlay" id="kit-modal">
	<div class="modal-content">
		<button class="modal-close-btn" type="button" aria-label="Schließen"
			data-modal-close>×</button>
		<h3>Kit verwalten</h3>
		<form id="kit-modal-form">
			<input type="hidden" name="action" id="kit-modal-action"> <input
				type="hidden" name="id" id="kit-modal-id">
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

<%-- Embed data for the client-side script --%>
<script id="allItemsData" type="application/json">
    ${allItemsJson}
</script>
<script id="allSelectableItemsData" type="application/json">
    ${allItemsJson}
</script>


<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/admin/admin_kits.js"></script>