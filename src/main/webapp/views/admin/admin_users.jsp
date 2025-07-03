<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Benutzerverwaltung" />
</c:import>

<c:set var="userPermissions" value="${sessionScope.user.permissions}" />

<h1>
	<i class="fas fa-users-cog"></i> Benutzerverwaltung
</h1>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="table-controls">
	<c:if test="${userPermissions.contains('USER_CREATE')}">
		<button type="button" class="btn btn-success" id="new-user-btn">
			<i class="fas fa-user-plus"></i> Neuen Benutzer anlegen
		</button>
	</c:if>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Benutzer filtern..." aria-label="Benutzer filtern">
	</div>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="user" items="${requestScope.userList}">
		<div class="list-item-card"
			data-searchable-content="${user.username} ${user.roleName}">
			<h3 class="card-title">${user.username}</h3>
			<div class="card-row">
				<span>Rolle:</span> <span>${user.roleName}</span>
			</div>
			<div class="card-actions">
				<c:if test="${userPermissions.contains('USER_UPDATE')}">
					<button type="button" class="btn btn-small edit-user-btn"
						data-fetch-url="<c:url value='/admin/mitglieder?action=getUserData&id=${user.id}'/>">Bearbeiten</button>
				</c:if>
				<a
					href="<c:url value='/admin/mitglieder?action=details&id=${user.id}'/>"
					class="btn btn-small">Details</a>
				<c:if
					test="${sessionScope.user.id != user.id and userPermissions.contains('USER_DELETE')}">
					<form action="<c:url value='/admin/mitglieder'/>" method="post"
						class="js-confirm-form"
						data-confirm-message="Benutzer '${fn:escapeXml(user.username)}' wirklich löschen?">
						<input type="hidden" name="action" value="delete"><input
							type="hidden" name="userId" value="${user.id}">
						<button type="submit" class="btn btn-small btn-danger">Löschen</button>
					</form>
				</c:if>
			</div>
		</div>
	</c:forEach>
</div>

<!-- Desktop Table View -->
<div class="desktop-table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="number">ID</th>
				<th class="sortable" data-sort-type="string">Benutzername</th>
				<th class="sortable" data-sort-type="string">Rolle</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="user" items="${requestScope.userList}">
				<tr>
					<td>${user.id}</td>
					<td>${user.username}</td>
					<td>${user.roleName}</td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;"><c:if
							test="${userPermissions.contains('USER_UPDATE')}">
							<button type="button" class="btn btn-small edit-user-btn"
								data-fetch-url="<c:url value='/admin/mitglieder?action=getUserData&id=${user.id}'/>">Bearbeiten</button>
						</c:if> <a
						href="<c:url value='/admin/mitglieder?action=details&id=${user.id}'/>"
						class="btn btn-small">Details</a> <c:if
							test="${sessionScope.user.id != user.id}">
							<c:if test="${userPermissions.contains('USER_PASSWORD_RESET')}">
								<form action="<c:url value='/admin/mitglieder'/>" method="post"
									class="js-confirm-form"
									data-confirm-message="Passwort für '${fn:escapeXml(user.username)}' zurücksetzen? Das neue Passwort wird angezeigt.">
									<input type="hidden" name="action" value="resetPassword"><input
										type="hidden" name="userId" value="${user.id}">
									<button type="submit" class="btn btn-small btn-warning">Passwort
										Reset</button>
								</form>
							</c:if>
							<c:if test="${userPermissions.contains('USER_DELETE')}">
								<form action="<c:url value='/admin/mitglieder'/>" method="post"
									class="js-confirm-form"
									data-confirm-message="Benutzer '${fn:escapeXml(user.username)}' wirklich löschen?">
									<input type="hidden" name="action" value="delete"><input
										type="hidden" name="userId" value="${user.id}">
									<button type="submit" class="btn btn-small btn-danger">Löschen</button>
								</form>
							</c:if>
						</c:if></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<%@ include file="/WEB-INF/jspf/user_modals.jspf"%>
<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/admin/admin_users.js"></script>