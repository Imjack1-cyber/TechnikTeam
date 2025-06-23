<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  admin_log.jsp
  
  This JSP displays the administrative audit log. It receives a list of all
  log entries and renders them in a responsive table/card layout. It includes
  a client-side JavaScript search filter that allows the admin to quickly find
  specific log entries by searching across all their fields.
  
  - It is served by: AdminLogServlet.
  - Expected attributes:
    - 'logs' (List<de.technikteam.model.AdminLog>): A list of all log entries, newest first.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Admin Log" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Admin Aktions-Protokoll</h1>

<div class="table-controls">
	<div class="form-group" style="margin-bottom: 0; flex-grow: 1;">
		<input type="search" id="table-filter"
			placeholder="Nach Details, Name oder Aktion filtern..."
			style="width: 100%;" aria-label="Protokoll filtern">
	</div>
</div>


<!-- MOBILE LAYOUT -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="log" items="${logs}">
		<div class="list-item-card"
			data-searchable-content="<c:out value='${log.details}'/> <c:out value='${log.adminUsername}'/> <c:out value='${log.actionType}'/>">
			<p>
				<strong><c:out value="${log.details}" /></strong>
			</p>
			<div class="card-row">
				<span>Wer:</span> <span><c:out value="${log.adminUsername}" /></span>
			</div>
			<div class="card-row">
				<span>Aktionstyp:</span> <span><c:out
						value="${log.actionType}" /></span>
			</div>
			<div class="card-row">
				<span>Wann:</span> <span><c:out
						value="${log.formattedActionTimestamp}" /> Uhr</span>
			</div>
		</div>
	</c:forEach>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table searchable-table">
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

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />