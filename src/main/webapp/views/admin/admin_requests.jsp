<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Anträge" />
</c:import>

<h1>
	<i class="fas fa-inbox"></i> Offene Anträge
</h1>
<p>Hier sehen Sie alle ausstehenden Anträge von Benutzern auf
	Änderung ihrer Profildaten.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="card">
	<div class="table-wrapper">
		<table class="data-table">
			<thead>
				<tr>
					<th>Antragssteller</th>
					<th>Beantragt am</th>
					<th>Beantragte Änderungen</th>
					<th style="width: 200px;">Aktionen</th>
				</tr>
			</thead>
			<tbody>
				<c:if test="${empty pendingRequests}">
					<tr>
						<td colspan="4" style="text-align: center;">Derzeit liegen
							keine offenen Anträge vor.</td>
					</tr>
				</c:if>
				<c:forEach var="req" items="${pendingRequests}">
					<tr data-request-id="${req.id}">
						<td><c:out value="${req.username}" /></td>
						<td><c:out value="${req.requestedAt}" /></td>
						<td><pre>
								<code>
									<c:out value="${req.requestedChanges}" />
								</code>
							</pre></td>
						<td>
							<div style="display: flex; gap: 0.5rem;">
								<form
									action="${pageContext.request.contextPath}/admin/action/request?action=approve"
									method="POST" class="js-approve-request-form">
									<input type="hidden" name="csrfToken"
										value="${sessionScope.csrfToken}"> <input
										type="hidden" name="requestId" value="${req.id}">
									<button type="submit" class="btn btn-small btn-success">Genehmigen</button>
								</form>
								<form
									action="${pageContext.request.contextPath}/admin/action/request?action=deny"
									method="POST" class="js-deny-request-form">
									<input type="hidden" name="csrfToken"
										value="${sessionScope.csrfToken}"> <input
										type="hidden" name="requestId" value="${req.id}">
									<button type="submit" class="btn btn-small btn-danger">Ablehnen</button>
								</form>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_requests.js"></script>