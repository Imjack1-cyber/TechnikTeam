<%-- src/main/webapp/views/public/lehrgaenge.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Lehrgänge" />
</c:import>
<h1>Anstehende Lehrgänge & Meetings</h1>
<c:import url="/WEB-INF/jspf/message_banner.jspf" />
<div class="table-controls">
	<div class="form-group" style="margin-bottom: 0; flex-grow: 1;">
		<input type="search" id="table-filter"
			placeholder="Lehrgänge filtern..." aria-label="Lehrgänge filtern">
	</div>
</div>
<div class="desktop-table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Meeting</th>
				<th class="sortable" data-sort-type="string">Gehört zu Kurs</th>
				<th class="sortable" data-sort-type="date">Datum & Uhrzeit</th>
				<th class="sortable" data-sort-type="string">Dein Status</th>
				<th>Aktion</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td colspan="5" style="text-align: center;">Lade Lehrgänge...</td>
			</tr>
		</tbody>
	</table>
</div>
<div class="mobile-card-list searchable-table">
	<div class="card">
		<p>Lade Lehrgänge...</p>
	</div>
</div>
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/public/lehrgaenge.js"></script>