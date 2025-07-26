<%-- src/main/webapp/views/public/lager.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
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

<div class="searchable-table" id="storage-container">
	<div class="card">
		<p>Lade Lagerdaten...</p>
	</div>
</div>

<%@ include file="/WEB-INF/jspf/storage_modals.jspf"%>
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/public/lager.js"></script>