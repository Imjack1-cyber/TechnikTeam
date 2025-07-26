<%-- src/main/webapp/views/admin/admin_log.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Admin Log" />
</c:import>
<h1>Admin Aktions-Protokoll</h1>
<div class="table-controls">
	<div class="form-group" style="margin-bottom: 0; flex-grow: 1;">
		<input type="search" id="table-filter"
			placeholder="Nach Details, Name oder Aktion filtern..."
			style="width: 100%;" aria-label="Protokoll filtern">
	</div>
</div>
<div class="desktop-table-wrapper">
	<table class="data-table searchable-table">
		<thead>
			<tr>
				<th>Wann</th>
				<th>Wer</th>
				<th>Aktionstyp</th>
				<th>Details</th>
			</tr>
		</thead>
		<tbody id="log-table-body">
			<tr>
				<td colspan="4" style="text-align: center;">Lade Logs...</td>
			</tr>
		</tbody>
	</table>
</div>
<div class="mobile-card-list searchable-table" id="log-mobile-list">
	<div class="card">
		<p>Lade Logs...</p>
	</div>
</div>
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/admin/admin_log.js"></script>