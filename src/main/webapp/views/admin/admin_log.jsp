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

<!-- Desktop Table View -->
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
		<tbody>
			<c:forEach var="log" items="${logs}">
				<tr>
					<td><c:out value="${log.formattedActionTimestamp}" /> Uhr</td>
					<td><c:out value="${log.adminUsername}" /></td>
					<td><c:out value="${log.actionType}" /></td>
					<td style="white-space: normal;"><c:out value="${log.details}" /></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list searchable-table">
	<c:if test="${empty logs}">
		<div class="card">
			<p>Keine Log-Einträge gefunden.</p>
		</div>
	</c:if>
	<c:forEach var="log" items="${logs}">
		<div class="list-item-card">
			<h3 class="card-title" style="word-break: break-all;">
				<c:out value="${log.actionType}" />
			</h3>
			<div class="card-row">
				<span>Wer:</span> <strong><c:out
						value="${log.adminUsername}" /></strong>
			</div>
			<div class="card-row">
				<span>Wann:</span> <strong><c:out
						value="${log.formattedActionTimestamp}" /> Uhr</strong>
			</div>
			<div class="card-row"
				style="flex-direction: column; align-items: flex-start;">
				<span style="font-weight: 500;">Details:</span>
				<p style="margin-top: 0.25rem; font-size: 0.9em; width: 100%;">
					<c:out value="${log.details}" />
				</p>
			</div>
		</div>
	</c:forEach>
</div>


<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />