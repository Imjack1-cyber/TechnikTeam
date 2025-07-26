<%-- src/main/webapp/views/admin/admin_course_list.jsp --%>
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

<!-- Desktop Table View (Shell) -->
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
			<%-- Content will be rendered here by JavaScript --%>
			<tr>
				<td colspan="3" style="text-align: center;">Lade Daten...</td>
			</tr>
		</tbody>
	</table>
</div>

<!-- Mobile Card View (Shell) -->
<div class="mobile-card-list searchable-table">
	<%-- Content will be rendered here by JavaScript --%>
	<div class="card">
		<p>Lade Daten...</p>
	</div>
</div>

<!-- MODAL FOR CREATE/EDIT COURSE (Unchanged) -->
<div class="modal-overlay" id="course-modal">
	<div class="modal-content">
		<button class="modal-close-btn" type="button" aria-label="Schließen"
			data-modal-close>×</button>
		<h3 id="course-modal-title">Lehrgangs-Vorlage</h3>
		<form id="course-modal-form">
			<%-- Removed action/method --%>
			<input type="hidden" name="action" id="course-modal-action">
			<input type="hidden" name="id" id="course-modal-id">
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