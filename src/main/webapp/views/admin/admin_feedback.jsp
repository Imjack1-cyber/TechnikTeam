<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Feedback-Übersicht" />
</c:import>

<h1>
	<i class="fas fa-inbox"></i> Feedback-Übersicht
</h1>
<p>Hier sind alle von Benutzern eingereichten Feedbacks, Wünsche und
	Fehlermeldungen aufgelistet.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="feedback-list">
	<c:if test="${empty submissions}">
		<div class="card">
			<p>Bisher wurde kein Feedback eingereicht.</p>
		</div>
	</c:if>

	<c:forEach var="sub" items="${submissions}">
		<c:set var="statusClass" value="status-info" />
		<c:if test="${sub.status == 'PLANNED'}">
			<c:set var="statusClass" value="status-warn" />
		</c:if>
		<c:if test="${sub.status == 'COMPLETED'}">
			<c:set var="statusClass" value="status-ok" />
		</c:if>
		<c:if test="${sub.status == 'REJECTED'}">
			<c:set var="statusClass" value="status-danger" />
		</c:if>

		<div class="card" data-submission-id="${sub.id}">
			<div
				style="display: flex; justify-content: space-between; align-items: start;">
				<div>
					<h3 class="card-title" style="border: none; padding: 0;">
						<c:out value="${sub.subject}" />
					</h3>
					<p class="details-subtitle" style="margin-top: -0.75rem;">
						Eingereicht von <strong><c:out value="${sub.username}" /></strong>
						am
						<c:out value="${sub.formattedSubmittedAt}" />
						Uhr
					</p>
				</div>
				<div>
					<span class="status-badge ${statusClass}"><c:out
							value="${sub.status}" /></span>
				</div>
			</div>

			<div class="markdown-content"
				style="white-space: pre-wrap; background-color: var(--bg-color); padding: 1rem; border-radius: var(--border-radius);">${fn:escapeXml(sub.content)}
			</div>

			<div class="card-actions">
				<form class="js-feedback-status-form"
					style="display: flex; gap: 0.5rem; align-items: center;"
					action="${pageContext.request.contextPath}/admin/action/feedback"
					method="POST">
					<input type="hidden" name="action" value="updateStatus"> <input
						type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
					<input type="hidden" name="submissionId" value="${sub.id}">
					<select name="status" class="form-group"
						style="margin: 0; min-width: 150px;">
						<option value="NEW" ${sub.status == 'NEW' ? 'selected' : ''}>Neu</option>
						<option value="VIEWED" ${sub.status == 'VIEWED' ? 'selected' : ''}>Gesehen</option>
						<option value="PLANNED"
							${sub.status == 'PLANNED' ? 'selected' : ''}>Geplant</option>
						<option value="COMPLETED"
							${sub.status == 'COMPLETED' ? 'selected' : ''}>Abgeschlossen</option>
						<option value="REJECTED"
							${sub.status == 'REJECTED' ? 'selected' : ''}>Abgelehnt</option>
					</select>
					<button type="submit" class="btn btn-small">Status ändern</button>
				</form>

				<c:if
					test="${sub.status == 'COMPLETED' or sub.status == 'REJECTED'}">
					<form class="js-feedback-delete-form"
						action="${pageContext.request.contextPath}/admin/action/feedback"
						method="POST">
						<input type="hidden" name="action" value="delete"> <input
							type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
						<input type="hidden" name="submissionId" value="${sub.id}">
						<button type="submit" class="btn btn-small btn-danger-outline">Löschen</button>
					</form>
				</c:if>
			</div>
		</div>
	</c:forEach>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_feedback.js"></script>