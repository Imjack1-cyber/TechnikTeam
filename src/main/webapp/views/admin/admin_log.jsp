<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

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

<div class="table-wrapper">
	<table class="data-table searchable-table">
		<thead>
			<tr>
				<th>Wann</th>
				<th>Wer</th>
				<th>Aktionstyp</th>
				<th>Details</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="log" items="${logs}">
				<tr>
					<td><c:out value="${log.formattedActionTimestamp}" /> Uhr</td>
					<td><c:out value="${log.adminUsername}" /></td>
					<td><c:out value="${log.actionType}" /></td>
					<td><c:out value="${log.details}" /></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />