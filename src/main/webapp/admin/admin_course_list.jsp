<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%-- Header must be included first to prevent Quirks Mode and CSS path errors --%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lehrgangs-Vorlagen" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Lehrgangs-Vorlagen verwalten</h1>
<p>Dies sind die übergeordneten Lehrgänge. Einzelne Termine
	(Meetings) werden für jede Vorlage separat verwaltet.</p>
<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">${sessionScope.successMessage}</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">${sessionScope.errorMessage}</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>

<a href="${pageContext.request.contextPath}/admin/courses?action=new"
	class="btn" style="margin-bottom: 1.5rem;">Neue Lehrgangs-Vorlage
	anlegen</a>

<!-- MOBILE LAYOUT: A list of cards, one for each course template -->
<div class="mobile-card-list">
	<c:forEach var="course" items="${courseList}">
		<div class="list-item-card">
			<h3 class="card-title">${course.name}</h3>
			<div class="card-row">
				<span>Abkürzung:</span> <span>${course.abbreviation}</span>
			</div>
			<div class="card-actions">
				<a
					href="${pageContext.request.contextPath}/admin/meetings?courseId=${course.id}"
					class="btn btn-small btn-success">Meetings verwalten</a> <a
					href="${pageContext.request.contextPath}/admin/courses?action=edit&id=${course.id}"
					class="btn btn-small">Vorlage bearbeiten</a>
				<form action="${pageContext.request.contextPath}/admin/courses"
					method="post" style="display: inline;">
					<input type="hidden" name="action" value="delete"> <input
						type="hidden" name="id" value="${course.id}">
					<button type="submit" class="btn btn-small btn-danger"
						onclick="return confirm('Vorlage \'${course.name}\' wirklich löschen? Alle zugehörigen Meetings werden auch gelöscht!')">Löschen</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>

<!-- DESKTOP LAYOUT: A bordered table -->
<div class="desktop-table-wrapper">
	<table class="desktop-table">
		<thead>
			<tr>
				<th>Name der Vorlage</th>
				<th>Abkürzung (für Matrix)</th>
				<th style="width: 350px;">Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="course" items="${courseList}">
				<tr>
					<td>${course.name}</td>
					<td>${course.abbreviation}</td>
					<td style="display: flex; gap: 0.5rem;"><a
						href="${pageContext.request.contextPath}/admin/meetings?courseId=${course.id}"
						class="btn btn-small btn-success">Meetings verwalten</a> <a
						href="${pageContext.request.contextPath}/admin/courses?action=edit&id=${course.id}"
						class="btn btn-small">Vorlage bearbeiten</a>
						<form action="${pageContext.request.contextPath}/admin/courses"
							method="post" style="display: inline;">
							<input type="hidden" name="action" value="delete"> <input
								type="hidden" name="id" value="${course.id}">
							<button type="submit" class="btn btn-small btn-danger"
								onclick="return confirm('Vorlage \'${course.name}\' wirklich löschen? Alle zugehörigen Meetings werden auch gelöscht!')">Löschen</button>
						</form></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<%-- Footer must be included last --%>
<c:import url="/WEB-INF/jspf/footer.jspf" />