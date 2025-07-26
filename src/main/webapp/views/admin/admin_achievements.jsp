<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Erfolge & Abzeichen" />
</c:import>

<c:set var="userPermissions" value="${sessionScope.user.permissions}" />
<c:set var="hasMasterAccess"
	value="${userPermissions.contains('ACCESS_ADMIN_PANEL')}" />

<h1>
	<i class="fas fa-award"></i> Erfolge & Abzeichen verwalten
</h1>
<p>Hier können Sie die Bedingungen und das Aussehen für Erfolge
	definieren, die Benutzer verdienen können.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="table-controls">
	<c:if
		test="${hasMasterAccess or userPermissions.contains('ACHIEVEMENT_CREATE')}">
		<button type="button" id="new-achievement-btn" class="btn btn-success"
			data-modal-target="achievement-modal">
			<i class="fas fa-plus"></i> Neuen Erfolg anlegen
		</button>
	</c:if>
</div>

<!-- Desktop Table View -->
<div class="desktop-table-wrapper">
	<table class="data-table">
		<thead>
			<tr>
				<th>Icon</th>
				<th>Name</th>
				<th>Programmatischer Key</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:if test="${empty achievements}">
				<tr>
					<td colspan="4" style="text-align: center;">Keine Erfolge
						definiert.</td>
				</tr>
			</c:if>
			<c:forEach var="ach" items="${achievements}">
				<tr>
					<td style="font-size: 1.5rem; text-align: center;"><i
						class="fas ${ach.iconClass}"></i></td>
					<td><c:out value="${ach.name}" /></td>
					<td><code>
							<c:out value="${ach.achievementKey}" />
						</code></td>
					<td><c:if
							test="${hasMasterAccess or userPermissions.contains('ACHIEVEMENT_UPDATE')}">
							<button type="button"
								class="btn btn-small btn-secondary edit-achievement-btn"
								data-id="${ach.id}" data-modal-target="achievement-modal">
								<i class="fas fa-edit"></i> Bearbeiten
							</button>
						</c:if> <c:if
							test="${hasMasterAccess or userPermissions.contains('ACHIEVEMENT_DELETE')}">
							<form
								action="${pageContext.request.contextPath}/admin/achievements"
								method="post" class="js-confirm-form" style="display: inline;"
								data-confirm-message="Erfolg '${fn:escapeXml(ach.name)}' wirklich löschen?">
								<input type="hidden" name="action" value="delete"> <input
									type="hidden" name="id" value="${ach.id}"> <input
									type="hidden" name="csrfToken"
									value="${sessionScope.csrfToken}">
								<button type="submit" class="btn btn-small btn-danger">
									<i class="fas fa-trash"></i> Löschen
								</button>
							</form>
						</c:if></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Mobile Card View -->
<div class="mobile-card-list">
	<c:if test="${empty achievements}">
		<div class="card">
			<p>Keine Erfolge definiert.</p>
		</div>
	</c:if>
	<c:forEach var="ach" items="${achievements}">
		<div class="list-item-card">
			<h3 class="card-title">
				<i class="fas ${ach.iconClass}"></i>
				<c:out value="${ach.name}" />
			</h3>
			<div class="card-row">
				<span>Key:</span>
				<code>
					<c:out value="${ach.achievementKey}" />
				</code>
			</div>
			<div class="card-actions">
				<c:if
					test="${hasMasterAccess or userPermissions.contains('ACHIEVEMENT_UPDATE')}">
					<button type="button"
						class="btn btn-small btn-secondary edit-achievement-btn"
						data-id="${ach.id}" data-modal-target="achievement-modal">
						<i class="fas fa-edit"></i> Bearbeiten
					</button>
				</c:if>
				<c:if
					test="${hasMasterAccess or userPermissions.contains('ACHIEVEMENT_DELETE')}">
					<form
						action="${pageContext.request.contextPath}/admin/achievements"
						method="post" class="js-confirm-form"
						data-confirm-message="Erfolg '${fn:escapeXml(ach.name)}' wirklich löschen?">
						<input type="hidden" name="action" value="delete"> <input
							type="hidden" name="id" value="${ach.id}"> <input
							type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
						<button type="submit" class="btn btn-small btn-danger">
							<i class="fas fa-trash"></i> Löschen
						</button>
					</form>
				</c:if>
			</div>
		</div>
	</c:forEach>
</div>

<!-- Modal for Create/Edit Achievement -->
<div class="modal-overlay" id="achievement-modal">
	<div class="modal-content">
		<button class="modal-close-btn" type="button" aria-label="Schließen"
			data-modal-close>×</button>
		<h3 id="achievement-modal-title">Erfolg verwalten</h3>
		<form id="achievement-modal-form"
			action="${pageContext.request.contextPath}/admin/achievements"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" id="achievement-modal-action"> <input
				type="hidden" name="id" id="achievement-modal-id"> <input
				type="hidden" name="achievement_key" id="achievement-key-hidden">

			<div class="form-group">
				<label for="name-modal">Name des Erfolgs</label> <input type="text"
					id="name-modal" name="name" required>
			</div>

			<div id="key-builder-group">
				<div class="form-group">
					<label for="key-type-select">Art des Erfolgs (Trigger)</label> <select
						id="key-type-select">
						<option value="">-- Bitte wählen --</option>
						<option value="EVENT_PARTICIPANT">Event-Teilnahme
							(Anzahl)</option>
						<option value="EVENT_LEADER">Event-Leitung (Anzahl)</option>
						<option value="QUALIFICATION">Qualifikation erhalten</option>
					</select>
				</div>
				<div id="key-number-group" class="key-subtype-group"
					style="display: none;">
					<div class="form-group">
						<label for="key-value-input">Erforderliche Anzahl</label> <input
							type="number" id="key-value-input" min="1" value="1"
							placeholder="z.B. 5 für 5 Teilnahmen">
					</div>
				</div>
				<div id="key-course-group" class="key-subtype-group"
					style="display: none;">
					<div class="form-group">
						<label for="key-course-select">Qualifikation</label> <select
							id="key-course-select">
							<option value="">-- Lehrgang wählen --</option>
							<c:forEach var="course" items="${allCourses}">
								<option value="${fn:replace(course.abbreviation, ' ', '_')}">
									<c:out value="${course.name}" />
								</option>
							</c:forEach>
						</select>
					</div>
				</div>
				<p>
					Generierter Key:
					<code id="generated-key-preview">--</code>
				</p>
			</div>

			<div class="form-group">
				<label for="icon-modal"> Icon-Klasse (Font Awesome) <a
					href="https://fontawesome.com/v5/search?m=free" target="_blank"
					title="Icons durchsuchen"> <i class="fas fa-external-link-alt"></i>
				</a>
				</label> <input type="text" id="icon-modal" name="icon_class"
					value="fa-award" required>
			</div>
			<div class="form-group">
				<label for="description-modal">Beschreibung</label>
				<textarea id="description-modal" name="description" rows="3"></textarea>
			</div>

			<button type="submit" class="btn btn-success">
				<i class="fas fa-save"></i> Speichern
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_achievements.js"></script>