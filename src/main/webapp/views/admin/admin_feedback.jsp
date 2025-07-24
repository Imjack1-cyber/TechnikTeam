<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Feedback Board" />
</c:import>

<style>
.feedback-board {
	display: flex;
	gap: 1.5rem;
	overflow-x: auto;
	padding-bottom: 1rem;
}

.feedback-column {
	flex: 1 0 300px; /* Flex-grow, flex-shrink, flex-basis */
	min-width: 300px;
	background-color: var(--bg-color);
	border-radius: var(--border-radius);
	padding: 1rem;
}

.feedback-column h2 {
	font-size: 1.2rem;
	border-bottom: 2px solid var(--border-color);
	padding-bottom: 0.5rem;
	margin-bottom: 1rem;
}

.feedback-list {
	min-height: 100px;
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.feedback-card-item {
	background-color: var(--surface-color);
	border: 1px solid var(--border-color);
	border-radius: 6px;
	padding: 1rem;
	box-shadow: var(--shadow-sm);
	cursor: pointer;
}

.feedback-card-item:active {
	cursor: grabbing;
}

.feedback-card-item .subject {
	font-weight: 600;
	margin-bottom: 0.5rem;
	display: block;
}

.feedback-card-item .content-preview {
	font-size: 0.9rem;
	color: var(--text-muted-color);
	margin-bottom: 0.75rem;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
	text-overflow: ellipsis;
}

.feedback-card-item .meta {
	font-size: 0.8rem;
	color: var(--text-muted-color);
}

.sortable-ghost {
	opacity: 0.4;
	background: var(--primary-color-light);
}
</style>

<h1>
	<i class="fas fa-columns"></i> Feedback Board
</h1>
<p>Verwalten Sie hier alle eingereichten Feedbacks. Sie können die
	Karten per Drag & Drop zwischen den Spalten verschieben, um den Status
	zu ändern.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="feedback-board">
	<c:forEach var="status" items="${feedbackStatusOrder}">
		<c:if test="${status ne 'NEW' or not empty groupedSubmissions['NEW']}">
			<div class="feedback-column">
				<h2>${status}</h2>
				<div class="feedback-list" data-status-id="${status}">
					<c:forEach var="submission" items="${groupedSubmissions[status]}">
						<div class="feedback-card-item" data-id="${submission.id}">
							<strong class="subject">${fn:escapeXml(not empty submission.displayTitle ? submission.displayTitle : submission.subject)}</strong>
							<p class="content-preview">${fn:escapeXml(submission.content)}</p>
							<p class="meta">
								Von: <strong>${fn:escapeXml(submission.username)}</strong> am
								${submission.formattedSubmittedAt}
							</p>
						</div>
					</c:forEach>
				</div>
			</div>
		</c:if>
	</c:forEach>
</div>

<!-- Modal for Feedback Details -->
<div class="modal-overlay" id="feedback-details-modal">
	<div class="modal-content" style="max-width: 700px;">
		<button class="modal-close-btn" type="button" aria-label="Schließen">×</button>
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
					<c:forEach var="status" items="${feedbackStatusOrder}">
						<option value="${status}">${status}</option>
					</c:forEach>
				</select>
			</div>
			<button type="submit" class="btn btn-success">
				<i class="fas fa-save"></i> Änderungen speichern
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/webjars/sortablejs/1.15.2/Sortable.min.js"></script>
<script
	src="${pageContext.request.contextPath}/js/admin/admin_feedback.js"></script>