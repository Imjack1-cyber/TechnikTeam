<%-- src/main/webapp/views/admin/admin_feedback.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Feedback Board" />
</c:import>

<%-- The <style> block has been removed and its contents moved to style4.css --%>

<h1>
	<i class="fas fa-columns"></i> Feedback Board
</h1>
<p>Verwalten Sie hier alle eingereichten Feedbacks. Sie können die
	Karten per Drag & Drop zwischen den Spalten verschieben, um den Status
	zu ändern.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<%-- The board is now an empty container that will be filled by JavaScript --%>
<div class="feedback-board" id="feedback-board-container">
	<p>Loading board...</p>
</div>

<!-- Modal for Feedback Details -->
<div class="modal-overlay" id="feedback-details-modal">
	<div class="modal-content" style="max-width: 700px;">
		<button class="modal-close-btn" type="button" aria-label="Schließen"
			data-modal-close>×</button>
		<h3 id="feedback-modal-title">Feedback Details</h3>
		<form id="feedback-details-form">
			<input type="hidden" name="submissionId" id="feedback-modal-id">
			<div class="form-group">
				<label>Originaltitel</label>
				<p id="feedback-modal-original-subject" style="font-weight: bold;"></p>
			</div>
			<div class="form-group">
				<label for="feedback-modal-display-title">Anzeigetitel
					(optional, für Admins)</label> <input type="text"
					id="feedback-modal-display-title" name="displayTitle">
			</div>
			<div class="form-group">
				<label>Inhalt</label>
				<div id="feedback-modal-content" class="markdown-content"
					style="white-space: pre-wrap; background-color: var(--bg-color); padding: 1rem; border-radius: var(--border-radius);"></div>
			</div>
			<div class="form-group">
				<label for="feedback-modal-status">Status</label> <select
					name="status" id="feedback-modal-status">
					<option value="NEW">NEW</option>
					<option value="VIEWED">VIEWED</option>
					<option value="PLANNED">PLANNED</option>
					<option value="REJECTED">REJECTED</option>
					<option value="COMPLETED">COMPLETED</option>
				</select>
			</div>
			<div style="display: flex; justify-content: space-between;">
				<button type="submit" class="btn btn-success">
					<i class="fas fa-save"></i> Änderungen speichern
				</button>
				<button type="button" id="feedback-modal-delete-btn" data-id=""
					class="btn btn-danger-outline">
					<i class="fas fa-trash"></i> Löschen
				</button>
			</div>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/webjars/sortablejs/1.15.2/Sortable.min.js"></script>
<script
	src="${pageContext.request.contextPath}/js/admin/admin_feedback.js"></script>