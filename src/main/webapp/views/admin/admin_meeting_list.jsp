<%-- src/main/webapp/views/admin/admin_meeting_list.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Meetings für ${parentCourse.name}" />
</c:import>

<%-- Pass courseId to JavaScript via data-attribute on the body --%>
<body data-course-id="${parentCourse.id}">

	<h1>
		Meetings für "
		<c:out value="${parentCourse.name}" />
		"
	</h1>
	<a href="${pageContext.request.contextPath}/admin/lehrgaenge"
		style="margin-bottom: 1rem; display: inline-block;"> <i
		class="fas fa-arrow-left"></i> Zurück zu allen Vorlagen
	</a>

	<c:import url="/WEB-INF/jspf/message_banner.jspf" />

	<div class="table-controls">
		<button type="button" class="btn btn-success" id="new-meeting-btn">
			<i class="fas fa-plus"></i> Neues Meeting planen
		</button>
		<div class="form-group" style="margin-bottom: 0;">
			<input type="search" id="table-filter"
				placeholder="Meetings filtern..." aria-label="Tabelle filtern">
		</div>
	</div>

	<!-- Desktop Table View (Shell) -->
	<div class="desktop-table-wrapper">
		<table class="data-table sortable-table searchable-table">
			<thead>
				<tr>
					<th class="sortable" data-sort-type="string">Meeting-Name</th>
					<th class="sortable" data-sort-type="string">Datum & Uhrzeit</th>
					<th class="sortable" data-sort-type="string">Leitung</th>
					<th>Aktionen</th>
				</tr>
			</thead>
			<tbody>
				<%-- Content rendered by admin_meeting_list.js --%>
				<tr>
					<td colspan="4" style="text-align: center;">Lade Meetings...</td>
				</tr>
			</tbody>
		</table>
	</div>

	<!-- Mobile Card View (Shell) -->
	<div class="mobile-card-list searchable-table">
		<%-- Content rendered by admin_meeting_list.js --%>
		<div class="card">
			<p>Lade Meetings...</p>
		</div>
	</div>


	<!-- Modal for creating/editing a meeting -->
	<div class="modal-overlay" id="meeting-modal">
		<div class="modal-content" style="max-width: 700px;">
			<button type="button" class="modal-close-btn" aria-label="Schließen"
				data-modal-close>×</button>
			<h3 id="meeting-modal-title">Meeting</h3>
			<form id="meeting-modal-form" enctype="multipart/form-data">
				<%-- Removed action/method, now handled by JS --%>
				<input type="hidden" name="action" id="meeting-action"> <input
					type="hidden" name="courseId" value="${parentCourse.id}"> <input
					type="hidden" name="id" id="meeting-id">
				<div class="form-group">
					<label for="name-modal">Name des Meetings</label> <input
						type="text" id="name-modal" name="name" required>
				</div>
				<div class="responsive-dashboard-grid">
					<div class="form-group">
						<label for="meetingDateTime-modal">Beginn</label> <input
							type="datetime-local" id="meetingDateTime-modal"
							name="meetingDateTime" required>
					</div>
					<div class="form-group">
						<label for="endDateTime-modal">Ende (optional)</label> <input
							type="datetime-local" id="endDateTime-modal" name="endDateTime">
					</div>
				</div>
				<div class="responsive-dashboard-grid">
					<div class="form-group">
						<label for="location-modal">Ort</label><input type="text"
							id="location-modal" name="location">
					</div>
					<div class="form-group">
						<label for="leader-modal">Leitende Person</label> <select
							name="leaderUserId" id="leader-modal">
							<option value="">(Keine)</option>
							<c:forEach var="user" items="${allUsers}">
								<option value="${user.id}"><c:out
										value="${user.username}" /></option>
							</c:forEach>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label for="description-modal">Beschreibung</label>
					<textarea id="description-modal" name="description" rows="3"></textarea>
				</div>
				<div class="card"
					style="margin-top: 1rem; padding: 1rem; background-color: var(--bg-color);">
					<h4 class="card-title" style="border: none; padding: 0;">Anhänge</h4>
					<ul id="modal-attachments-list" class="details-list"
						style="margin-bottom: 1rem;"></ul>
					<div class="form-group">
						<label for="attachment-modal">Neuen Anhang hochladen</label> <input
							type="file" name="attachment" id="attachment-modal"
							class="file-input" data-max-size="20971520"> <small
							class="file-size-warning" style="color: red; display: none;">Datei
							ist zu groß! (Max. 20 MB)</small>
					</div>
					<div class="form-group">
						<label for="requiredRole-modal">Sichtbar für</label> <select
							name="requiredRole" id="requiredRole-modal">
							<option value="NUTZER" selected>Alle Nutzer</option>
							<option value="ADMIN">Nur Admins</option>
						</select>
					</div>
				</div>
				<button type="submit" class="btn" style="margin-top: 1.5rem;">
					<i class="fas fa-save"></i> Speichern
				</button>
			</form>
		</div>
	</div>

	<c:import url="/WEB-INF/jspf/table_scripts.jspf" />
	<c:import url="/WEB-INF/jspf/main_footer.jspf" />
	<script
		src="${pageContext.request.contextPath}/js/admin/admin_meeting_list.js"></script>