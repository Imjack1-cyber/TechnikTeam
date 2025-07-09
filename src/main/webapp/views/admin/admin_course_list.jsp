<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Lehrgangs-Vorlagen" />
</c:import>

<h1>Lehrgangs-Vorlagen verwalten</h1>
<p>Dies sind die übergeordneten Lehrgänge. Einzelne Termine
	(Meetings) werden für jede Vorlage separat verwaltet.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="table-controls">
	<button type="button" id="new-course-btn" class="btn btn-success">
		<i class="fas fa-plus"></i> Neue Vorlage
	</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Vorlagen filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<!-- Desktop Table View -->
<div class="desktop-table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name der Vorlage</th>
				<th class="sortable" data-sort-type="string">Abkürzung (für
					Matrix)</th>
				<th style="min-width: 350px;">Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:if test="${empty courseList}">
				<tr>
					<td colspan="3" style="text-align: center;">Es wurden noch
						keine Lehrgangs-Vorlagen erstellt.</td>
				</tr>
			</c:if>
			<c:forEach var="course" items="${courseList}">
				<tr>
					<td><c:out value="${course.name}" /></td>
					<td><c:out value="${course.abbreviation}" /></td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;"><a
						href="${pageContext.request.contextPath}/admin/meetings?courseId=${course.id}"
						class="btn btn-small"> <i class="fas fa-calendar-day"></i>
							Meetings
					</a>
						<button type="button"
							class="btn btn-small btn-secondary edit-course-btn"
							data-id="${course.id}">
							<i class="fas fa-edit"></i> Bearbeiten
						</button>
						<form action="${pageContext.request.contextPath}/admin/lehrgaenge"
							method="post" class="js-confirm-form"
							data-confirm-message="Vorlage '${fn:escapeXml(course.name)}' wirklich löschen? Alle zugehörigen Meetings und Qualifikationen werden auch gelöscht!">
							<input type="hidden" name="csrfToken"
								value="${sessionScope.csrfToken}"> <input type="hidden"
								name="action" value="delete"> <input type="hidden"
								name="id" value="${course.id}">
							<button type="submit" class="btn btn-small btn-danger">
								<i class="fas fa-trash"></i> Löschen
							</button>
						</form></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list searchable-table">
	<c:if test="${empty courseList}">
		<div class="card">
			<p>Es wurden noch keine Lehrgangs-Vorlagen erstellt.</p>
		</div>
	</c:if>
	<c:forEach var="course" items="${courseList}">
		<div class="list-item-card">
			<h3 class="card-title">
				<c:out value="${course.name}" />
			</h3>
			<div class="card-row">
				<span>Abkürzung:</span> <strong><c:out
						value="${course.abbreviation}" /></strong>
			</div>
			<div class="card-actions">
				<a
					href="${pageContext.request.contextPath}/admin/meetings?courseId=${course.id}"
					class="btn btn-small"> <i class="fas fa-calendar-day"></i>
					Meetings
				</a>
				<button type="button"
					class="btn btn-small btn-secondary edit-course-btn"
					data-id="${course.id}">
					<i class="fas fa-edit"></i> Bearbeiten
				</button>
				<form action="${pageContext.request.contextPath}/admin/lehrgaenge"
					method="post" class="js-confirm-form"
					data-confirm-message="Vorlage '${fn:escapeXml(course.name)}' wirklich löschen? Alle zugehörigen Meetings und Qualifikationen werden auch gelöscht!">
					<input type="hidden" name="csrfToken"
						value="${sessionScope.csrfToken}"> <input type="hidden"
						name="action" value="delete"> <input type="hidden"
						name="id" value="${course.id}">
					<button type="submit" class="btn btn-small btn-danger">
						<i class="fas fa-trash"></i> Löschen
					</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>

<!-- MODAL FOR CREATE/EDIT COURSE -->
<div class="modal-overlay" id="course-modal">
	<div class="modal-content">
		<button class="modal-close-btn" type="button" aria-label="Schließen">×</button>
		<h3 id="course-modal-title">Lehrgangs-Vorlage</h3>
		<form id="course-modal-form"
			action="${pageContext.request.contextPath}/admin/lehrgaenge"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" id="course-modal-action"> <input type="hidden"
				name="id" id="course-modal-id">
			<div class="form-group">
				<label for="name-modal">Name der Vorlage</label> <input type="text"
					id="name-modal" name="name" required>
			</div>
			<div class="form-group">
				<label for="abbreviation-modal">Abkürzung (max. 10 Zeichen)</label>
				<input type="text" id="abbreviation-modal" name="abbreviation"
					maxlength="10" required>
			</div>
			<div class="form-group">
				<label for="description-modal">Allgemeine Beschreibung</label>
				<textarea id="description-modal" name="description" rows="4"></textarea>
			</div>
			<button type="submit" class="btn">
				<i class="fas fa-save"></i> Vorlage Speichern
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_course_list.js"></script>